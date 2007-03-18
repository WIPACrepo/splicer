package icecube.daq.splicer;

import icecube.daq.hkn1.Counter;
import icecube.daq.hkn1.Node;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class HKN1Splicer implements Splicer, Counter, Runnable
{
    SplicedAnalysis             analysis      = null;
    ArrayList<Node<Spliceable>> exposeList;
    SpliceableComparator        spliceableCmp = new SpliceableComparator();
    Node<Spliceable>            terminalNode  = null;
    volatile int                state         = Splicer.STOPPED;
    volatile int                counter       = 0;
    LinkedList<Spliceable>       rope;
    int                         decrement     = 0;
    private static final Logger logger = Logger.getLogger(HKN1Splicer.class);
    ArrayList<SplicerListener>  listeners     = null;
    
    public HKN1Splicer(SplicedAnalysis analysis)
    {
        this.analysis = analysis;
        exposeList = new ArrayList<Node<Spliceable>>();
        rope = new LinkedList<Spliceable>();
        listeners = new ArrayList<SplicerListener>();
    }

    public void addSpliceableChannel(SelectableChannel channel)
            throws IOException
    {
        throw new UnsupportedOperationException();

    }

    public void addSplicerListener(SplicerListener listener)
    {
        logger.debug("Adding splicer listener.");
        listeners.add(listener);
    }

    public void analyze()
    {
        throw new UnsupportedOperationException();
    }

    public StrandTail beginStrand()
    {
        Node<Spliceable> node = new Node<Spliceable>(spliceableCmp, this);
        exposeList.add(node);
        return new HKN1LeafNode(node);
    }

    public void dispose()
    {
        int oldState = state;
        state = Splicer.STOPPING;
        SplicerChangedEvent disposeEvent = new SplicerChangedEvent(this, oldState, state);
        for (SplicerListener listener : listeners)
            listener.disposed(disposeEvent);
    }

    public void forceStop()
    {
        // NO OP
    }

    public SplicedAnalysis getAnalysis()
    {
        return this.analysis;
    }

    public MonitorPoints getMonitorPoints()
    {
        throw new UnsupportedOperationException();
    }

    public int getState()
    {
        return state;
    }

    public String getStateString()
    {
        return getStateString(state);
    }

    public String getStateString(int state)
    {
        if (state == Splicer.STARTED)
            return "STARTED";
        else if (state == Splicer.STOPPED)
            return "STOPPED";
        else if (state == Splicer.STOPPING)
            return "STOPPING";
        return "UNKNOWN";
    }

    public int getStrandCount()
    {
        return counter;
    }

    public List pendingChannels()
    {
        return new ArrayList();
    }

    public List pendingStrands()
    {
        return new ArrayList();
    }

    public void removeSpliceableChannel(SelectableChannel channel)
    {
        throw new UnsupportedOperationException();
    }

    public void removeSplicerListener(SplicerListener listener)
    {
        throw new UnsupportedOperationException();
    }

    public void start()
    {
        state = Splicer.STARTING;
        new Thread(this).start();
        logger.info("HKN1Splicer was started.");
    }

    public void start(Spliceable start)
    {
        start();
    }

    public void stop()
    {
        state = Splicer.STOPPING;
        logger.info("Stopping HKN1Splicer.");
    }

    public void stop(Spliceable stop) throws OrderingException
    {
        stop();
    }

    public void truncate(Spliceable spliceable)
    {
        ArrayList truncatedList = new ArrayList();
        
        synchronized (rope) 
        {
            decrement = 0;
            
            while (rope.size() > 0)
            {
                Spliceable x = rope.peek();
                if (spliceable.compareTo(x) < 0) return;
                rope.removeFirst();
                truncatedList.add(x);
                decrement++;
            }
        }
        
        SplicerChangedEvent event = new SplicerChangedEvent(this, state, spliceable, truncatedList);
        for (SplicerListener listener : listeners)
        {
            logger.debug("Firing truncate event to listener.");
            listener.truncated(event);
        }
        
        logger.debug("Rope truncated to length " + rope.size());
    }

    public void announce(Node<?> node)
    {
        
    }

    public void dec()
    {
        counter--;
    }

    public long getCount()
    {
        return counter;
    }

    public void inc()
    {
        counter++;
    }

    public boolean overflow()
    {
        return false;
    }

    public void run()
    {
        terminalNode = Node.makeTree(exposeList, spliceableCmp, this);
        state = Splicer.STARTED;
        long nObj = 0L;
        
        while (state == Splicer.STARTED)
        {
            try
            {
                boolean addedToRope;
                synchronized (this)
                {
                    this.wait(1000L);
                }
                synchronized (terminalNode)
                {
                    addedToRope = !terminalNode.isEmpty();
                    while (!terminalNode.isEmpty())
                    {
                        Spliceable obj = terminalNode.pop();;
                        if (obj != Splicer.LAST_POSSIBLE_SPLICEABLE) 
                        {
                            synchronized (rope)
                            {
                                rope.add(obj);
                            }
                        }
                        else 
                        {
                            dispose();
                        }
                    }
                }
                if (addedToRope)
                {
                    if (logger.isDebugEnabled())
                        logger.debug("SplicedAnalysis.execute(" 
                                + rope.size() + ", " 
                                + decrement + ") - counter = " + counter);
                    this.analysis.execute(rope, decrement);
                }
            }
            catch (InterruptedException e)
            {
                logger.error("Splicer run thread was interrupted.");
            }
            
        }
        
        state = Splicer.STOPPED;
        logger.info("HKN1Splicer was stopped.");
    }
    
    // inner class
    class HKN1LeafNode implements StrandTail
    {

        private Node<Spliceable> expose;
        private long nInput;
        
        public HKN1LeafNode(Node<Spliceable> node)
        {
            expose = node;
            nInput = 0L;
        }

        public void close()
        {
            // intentional no-op
        }

        public Spliceable head()
        {
            return expose.head();
        }

        public boolean isClosed()
        {
            // TODO Auto-generated method stub
            return false;
        }

        public StrandTail push(List spliceables) throws OrderingException,
                ClosedStrandException
        {
            Iterator it = spliceables.listIterator();
            while (it.hasNext()) push((Spliceable) it.next());
            return this;
        }

        public StrandTail push(Spliceable spliceable) throws OrderingException,
                ClosedStrandException
        {
            synchronized (terminalNode)
            {
                expose.push(spliceable);
                if (nInput++ % 1000 == 0)
                    logger.debug("Pushing payload # " + nInput + " into strandTail " + this);
            }
            synchronized (HKN1Splicer.this)
            {
                HKN1Splicer.this.notify();
            }
            return this;
        }

        public int size()
        {
            if (expose.isEmpty()) return 0;
            return 1;
        }
    }

}

class SpliceableComparator implements Comparator<Spliceable>
{

    public int compare(Spliceable s1, Spliceable s2)
    {
        return s1.compareTo(s2);
    }

}

