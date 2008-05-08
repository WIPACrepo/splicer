/* -*- mode: java; indent-tabs-mode:t; tab-width:4 -*- */

package icecube.daq.hkn1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

public class Node<T>
{
	private Node<T>			sink;
	private Node<T>			peer;
	private LinkedList<T>	list;
	private Comparator<T>	cmp;
	private String			myName;
	private T				val;

	/**
	 * Two argument constructor for node specifying comparison class and the counter class
	 * @param cmp node contents are ordered using this Comparator
	 * @param cnt external counter object which can be used to keep track of total number
	 * of objects managed by a collection of Nodes.  If null then this feature is ignored.
	 */
	public Node(Comparator<T> cmp)
	{
		list		= new LinkedList<T>();
		myName		= "";
		this.cmp	= cmp;
	}

	public void setPeer(Node<T> peer) {	this.peer = peer; }

	public void setSink(Node<T> sink) {	this.sink = sink; }

	public boolean isEmpty() { return val == null; }

	public int depth() { return (val == null ? 0 : list.size() + 1); }

	public void setName(String name) { myName = name; }

	public String getName() { return myName; }

	public T head() { return val; }

	public void push(T element)
	{
		if (val != null) list.add(element); else val = element;
		if (sink == null) return;
		while (!isEmpty() && !peer.isEmpty())
		{
			if (cmp.compare(head(), peer.head()) > 0)
				sink.push(peer.pop());
			else
				sink.push(pop());
		}
	}

	public T pop()
	{
		if (isEmpty()) return null;
		T rval = val;
		if (list.size() > 0)
			val = list.removeFirst();
		else
			val = null;
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
	 * Create a sorting tree structure.
	 * @param <T> type of element being pushed through the nodes
	 * @param inputs a list of input nodes
	 * @param cmp the comparator class.  The function cmp.compare() will be
	 * called on the elements in the nodes to determine ordering.
	 * @return
	 */
	public static<T> Node<T> makeTree(Collection<Node<T>> inputs, Comparator<T> cmp)
	{
		ArrayList<Node<T>> copy = new ArrayList<Node<T>>(inputs);

		// Name the nodes in the tree - first layer is Nx x = 0..n
		// The following layers are combinations so that trail is clear.

		int n = 0;
		for (Node<T> node : copy) node.setName("N" + n++);

		while (copy.size() > 1)
		{
			ArrayList<Node<T>> tmp = new ArrayList<Node<T>>();
			Iterator<Node<T>> iter = copy.iterator();
			while (iter.hasNext()) {
				Node<T> a = iter.next();
				Node<T> b;
				Node<T> sink = new Node<T>(cmp);
				if (iter.hasNext())
					b = iter.next();
				else
				{
					b = a;
					if (tmp.isEmpty())
						a = null;
					else
						a = tmp.remove(tmp.size() - 1);
					sink = new Node<T>(cmp);
				}

				a.setPeer(b);
				b.setPeer(a);
				a.setSink(sink);
				b.setSink(sink);
				// Keep track of the 'position' of the node
				sink.setName("(" + a.myName + "," + b.myName + ")");
				tmp.add(sink);
			}

			copy = tmp;
		}
		return copy.get(0);

	}
}
