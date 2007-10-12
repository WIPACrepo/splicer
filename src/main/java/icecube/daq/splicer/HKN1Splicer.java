package icecube.daq.splicer;

import icecube.daq.hkn1.Counter;
import icecube.daq.hkn1.Node;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.Collections;
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
    ArrayList<Spliceable>       rope;
    int                         decrement     = 0;
    private static final Logger logger = Logger.getLogger(HKN1Splicer.class);
    ArrayList<SplicerListener>  listeners     = null;
    int                         outputCount;
    
    public HKN1Splicer(SplicedAnalysis analysis)
    {
        this.analysis = analysis;
        exposeList = new ArrayList<Node<Spliceable>>();
        rope = new ArrayList<Spliceable>();
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
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void analyze()
    {
        throw new UnsupportedOperationException();
    }

    public StrandTail beginStrand()
    {
        Node<Spliceable> node = new Node<Spliceable>(spliceableCmp, this);
        exposeList.add(node);
        counter++;
        return new HKN1LeafNode(node);
    }

    private void changeState(int newState)
    {
        SplicerChangedEvent event =
            new SplicerChangedEvent(this, state, newState);
        state = newState;
        synchronized (listeners) {
            for (SplicerListener listener : listeners) {
                switch (newState) {
                case Splicer.DISPOSED:
                    listener.disposed(event);
                    break;
                case Splicer.FAILED:
                    listener.failed(event);
                    break;
                case Splicer.STARTED:
                    listener.started(event);
                    break;
                case Splicer.STARTING:
                    listener.starting(event);
                    break;
                case Splicer.STOPPED:
                    listener.stopped(event);
                    break;
                case Splicer.STOPPING:
                    listener.stopping(event);
                    break;
                default:
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unknown state " + newState);
                    }
                    break;
                }
            }
        }
    }

    public void dispose()
    {
        changeState(Splicer.STOPPING);
    }

    public void forceStop()
    {
        // NO OP
    }

    public SplicedAnalysis getAnalysis()
    {
        return analysis;
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
        String str;
        switch (state) {
        case Splicer.DISPOSED:
            str = "DISPOSED";
            break;
        case Splicer.FAILED:
            str = "FAILED";
            break;
        case Splicer.STARTED:
            str = "STARTED";
            break;
        case Splicer.STARTING:
            str = "STARTING";
            break;
        case Splicer.STOPPED:
            str = "STOPPED";
            break;
        case Splicer.STOPPING:
            str = "STOPPING";
            break;
        default:
            str = "UNKNOWN";
            break;
        }
        return str;
    }

    public int getStrandCount()
    {
        return exposeList.size();
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
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void start()
    {
        changeState(Splicer.STARTING);

        outputCount = 0;

        Thread thread = new Thread(this);
        thread.setName("HKN1Splicer");
        thread.start();

        while (state != Splicer.STARTED) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {
                // ignore interrupts
            }
        }

        logger.info("HKN1Splicer was started.");
    }

    public void start(Spliceable start)
    {
        start();
    }

    public void stop()
    {
        changeState(Splicer.STOPPING);
        logger.info("Stopping HKN1Splicer.");
    }

    public void stop(Spliceable stop) throws OrderingException
    {
        stop();
    }

    public void truncate(Spliceable spliceable)
    {
        ArrayList oldRope;
        ArrayList newRope = new ArrayList();
        
        synchronized (rope) 
        {
            decrement = rope.size();
            
            if (!LAST_POSSIBLE_SPLICEABLE.equals(spliceable)) {
                while (rope.size() > 0)
                {
                    Spliceable x = rope.get(rope.size()-1);
                    if (x.compareTo(spliceable) < 0) break;
                    newRope.add(x);
                    rope.remove(rope.size()-1);
                    decrement--;
                }
            }
            oldRope = rope;
            rope = newRope;
        }

        Collections.reverse(rope);
        SplicerChangedEvent event = new SplicerChangedEvent(this, state, spliceable, oldRope);
        synchronized (listeners) {
            for (SplicerListener listener : listeners)
            {
                logger.debug("Firing truncate event to listener.");
                listener.truncated(event);
            }
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Rope truncated to length " + rope.size());
        }
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
        changeState(Splicer.STARTED);
        long nObj = 0L;
        boolean sawLast = false;
        Spliceable previousSpliceable = null;
        
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
                        Spliceable obj = terminalNode.pop();
                        // Make sanity check on objects coming out of splicer
                        if (previousSpliceable != null && previousSpliceable.compareTo(obj) > 0)
                        {
                            logger.warn("Ignoring out-of-order object");
                        }
                        else if (obj != Splicer.LAST_POSSIBLE_SPLICEABLE) 
                        {
                            synchronized (rope)
                            {
                                rope.add(obj);
                            }
                        }
                        else 
                        {
                            sawLast = true;
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
                    analysis.execute(rope, decrement);
                }
            }
            catch (InterruptedException e)
            {
                logger.error("Splicer run thread was interrupted.");
            }
            
        }
        
        synchronized (rope) {
            if (rope.size() == 0) {
                analysis.execute(rope, 0);
            } else {
                analysis.execute(new ArrayList(), 0);
            }

            Spliceable finalTrunc;
            if (sawLast || rope.size() == 0) {
                finalTrunc = Splicer.LAST_POSSIBLE_SPLICEABLE;
            } else {
                finalTrunc = rope.get(rope.size() - 1);
            }

            truncate(finalTrunc);

            if (rope.size() > 0) {
                logger.error("Clearing " + rope.size() + " rope entries");
                rope.clear();
            }
        }
        
        changeState(Splicer.STOPPED);
        logger.info("HKN1Splicer was stopped.");

        if (counter != 0) {
            logger.error("Resetting counter from " + counter + " to 0");
            counter = 0;
        }
        if (decrement != 0) {
            logger.error("Resetting decrement from " + decrement + " to 0");
            decrement = 0;
        }
        for (Node<Spliceable> node : exposeList) {
            node.clear();
        }
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
            exposeList.remove(expose);
            counter--;
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
                if (logger.isDebugEnabled() && nInput++ % 1000 == 0)
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
            return expose.depth();
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

