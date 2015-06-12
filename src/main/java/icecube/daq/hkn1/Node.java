package icecube.daq.hkn1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

public class Node<T>
{
    private Comparator<T>    cmp;

    private Node<T>          sink;
    private Node<T>          peer;
    private LinkedList<T>    list;
    private String           myName;
    private T                val;

    /**
     * Two argument constructor for node specifying comparison class and
     *  the counter class
     * @param cmp node contents are ordered using this Comparator
     */
    public Node(Comparator<T> cmp)
    {
        this.cmp    = cmp;

        list        = new LinkedList<T>();
        myName      = "";
    }

    public Node<T> peer()
    {
        return peer;
    }

    public void setPeer(Node<T> peer)
    {
        this.peer = peer;
    }

    public Node<T> sink()
    {
        return sink;
    }

    public void setSink(Node<T> sink)
    {
        this.sink = sink;
    }

    public boolean isEmpty()
    {
        return val == null;
    }

    public int depth()
    {
        return (val == null ? 0 : list.size() + 1);
    }

    public void setName(String name)
    {
         myName = name;
    }

    public String getName()
    {
         return myName;
    }

    public T head()
    {
         return val;
    }

    public int compare()
    {
        return cmp.compare(head(), peer.head());
    }

    public Comparator<T> getComparator()
    {
        return cmp;
    }

    /**
     * Push data into this node.
     *
     * @param element data to be pushed
     */
    public void push(T element)
    {
        if (element == null) {
            throw new Error("Cannot push null value");
        }
        if (val != null) {
            list.add(element);
        } else {
            val = element;
        }
        checkList();
    }

    /**
     * Is there data available from either this node or its peer?
     *
     * @return <tt>true</tt> if there is data available
     */
    public boolean isDataAvailable()
    {
        return !isEmpty() && !peer.isEmpty();
    }

    public void checkList()
    {
        if (sink == null) {
            return;
        }
        while (isDataAvailable()) {
            if (compare() > 0) {
                sink.push(peer.pop());
            } else {
                sink.push(pop());
            }
        }
    }

    public T pop()
    {
        T rval;
        if (isEmpty()) {
            rval = null;
        } else {
            rval = val;
            if (list.size() > 0) {
                val = list.removeFirst();
            } else {
                val = null;
            }
        }
        return rval;
    }

    /**
     * Zoink out all stored elements.
     */
    public void clear()
    {
        list.clear();
        val = null;
    }

    public String toString()
    {
        return myName + "*" + depth();
    }

    /**
     * Create a new node.
     *
     * @return new node
     */
    public Node<T> createNode()
    {
        return new Node<T>(cmp);
    }

    /**
     * Create a sorting tree structure.
     * @param <T> type of element being pushed through the nodes
     * @param inputs a list of input nodes
     * @param cmp vestigial parameter
     * @return
     * @deprecated
     */
    public static<T> Node<T> makeTree(Collection<Node<T>> inputs,
                                      Comparator<T> cmp)
    {
        return makeTree(inputs);
    }

    /**
     * Create a sorting tree structure.
     * @param <T> type of element being pushed through the nodes
     * @param inputs a list of input nodes
     * @return
     */
    public static<T> Node<T> makeTree(Collection<Node<T>> inputs)
    {
        ArrayList<Node<T>> copy = new ArrayList<Node<T>>(inputs);

        // Name the nodes in the tree - first layer is Nx x = 0..n
        // The following layers are combinations so that trail is clear.

        int n = 0;
        for (Node<T> node : copy) {
            node.setName("N" + n++);
        }

        while (copy.size() > 1) {
            ArrayList<Node<T>> tmp = new ArrayList<Node<T>>();
            Iterator<Node<T>> iter = copy.iterator();
            while (iter.hasNext()) {
                Node<T> a = iter.next();
                Node<T> b;
                Node<T> sink = a.createNode();
                if (iter.hasNext()) {
                    b = iter.next();
                } else {
                    b = a;
                    if (tmp.isEmpty()) {
                        a = null;
						//TODO: Is this correct for error handling
						throw new Error("makeTree tmp array list is empty");
                    } else {
                        a = tmp.remove(tmp.size() - 1);
                    }
                }

                // Keep track of the 'position' of the node
                sink.setName("(" + a.myName + "," + b.myName + ")");

                a.setPeer(b);
                b.setPeer(a);
                a.setSink(sink);
                b.setSink(sink);
                tmp.add(sink);
            }

            copy = tmp;
        }
        return copy.get(0);

    }
}
