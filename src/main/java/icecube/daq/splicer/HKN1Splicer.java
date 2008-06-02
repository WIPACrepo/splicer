package icecube.daq.splicer;

import icecube.daq.hkn1.Node;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class HKN1Splicer implements Splicer, Runnable
{
    private SplicedAnalysis             analysis;
    private ArrayList<Node<Spliceable>> exposeList;
    private HKN1Comparator              spliceableCmp = new HKN1Comparator();
    private Node<Spliceable>            terminalNode;
    private volatile int                state         = Splicer.STOPPED;
    private volatile int                counter;
    private ArrayList<Spliceable>       rope;
    private Object                      ropeLock      = new Object();
    private int                         decrement;
    private static final Logger logger = Logger.getLogger(HKN1Splicer.class);
    private ArrayList<SplicerListener>  listeners;
    private long                        waitMillis    = 1000L;

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
        if (logger.isDebugEnabled()) {
            logger.debug("Adding splicer listener.");
        }

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
        Node<Spliceable> node = createNode(spliceableCmp);
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

        synchronized (this) {
            notify();
        }
    }

    /**
     * Create a tree node.
     *
     * @param cmp comparator
     *
     * @return new node
     */
    public Node<Spliceable> createNode(Comparator<Spliceable> spliceableCmp)
    {
        return new Node<Spliceable>(spliceableCmp);
    }

    public void dispose()
    {
        if (state != Splicer.STOPPED) {
            changeState(Splicer.STOPPING);
        }
    }

    /**
     * Return internal splicer state for debugging.
     *
     * @return strings which describe internal splicer state
     */
    public String[] dumpDescription()
    {
        final StringBuilder spacesSource =
            new StringBuilder("              ");

        final int totNodes = exposeList.size();

        StringBuilder[] lines = new StringBuilder[totNodes];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = new StringBuilder(80);
        }

        ArrayList<Node<Spliceable>> peers =
            new ArrayList<Node<Spliceable>>(exposeList);
        ArrayList<Node<Spliceable>> sinks =
            new ArrayList<Node<Spliceable>>();

        int prevSeen = peers.size() + 1;
        while (true) {
            int widest = 0;
            for (Node node : peers) {
                int width = node.toString().length();
                if (width >= widest) {
                    widest = width + 1;
                }
            }

            while (spacesSource.length() < widest) {
                spacesSource.append("                ");
            }

            String columnSpaces =
                spacesSource.toString().substring(0, widest);

            Node prevNode = null;
            int nodeNum = 0;
            int numSeen = 0;
            boolean allSink = true;
            for (Node node : peers) {
                final boolean isPrevNode = node == prevNode;
                if (!isPrevNode) {
                    numSeen++;
                }

                boolean dumpNode;
                if (node.peer() != null && !peers.contains(node.peer())) {
                    sinks.add(node);
                    dumpNode = false;
                } else {
                    dumpNode = !isPrevNode;

                    if (node.sink() != null) {
                        sinks.add(node.sink());
                    }

                    allSink = false;
                }

                if (!dumpNode) {
                    lines[nodeNum].append(columnSpaces);
                } else {
                    String nodeStr = node.toString();
                    lines[nodeNum].append(nodeStr);

                    int pad = columnSpaces.length() - nodeStr.length();
                    if (pad > 0) {
                        String padding = columnSpaces.substring(0, pad);
                        lines[nodeNum].append(padding);
                    }
                }

                prevNode = node;
                nodeNum++;
            }

            if (numSeen == 1) {
                break;
            } else if (allSink && prevSeen == numSeen) {
                break;
            }
            prevSeen = numSeen;

            ArrayList<Node<Spliceable>> tmp = peers;
            peers = sinks;
            sinks = tmp;
            tmp.clear();
        }

        String[] lineStr = new String[lines.length];
        for (int i = 0; i < lines.length; i++) {
            lineStr[i] = lines[i].toString().trim();
        }

        return lineStr;
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

    /**
     * Perform any extra code during this pass in the loop.
     *
     * @param nodes list of strand tail nodes
     */
    public void loopCheck(List<Node<Spliceable>> nodes)
    {
    }

    /**
     * Perform any needed initialization before the loop starts.
     */
    public void loopInit(List<Node<Spliceable>> nodes)
    {
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

    void setWaitMillis(long val)
    {
        waitMillis = val;
    }

    public void start()
    {
        if (state != Splicer.STOPPED) {
            throw new Error("Expected splicer to be " +
                            getStateString(Splicer.STOPPED) + ", not " +
                            getStateString());
        }

        changeState(Splicer.STARTING);

        if (exposeList.size() == 0) {
            throw new Error("No strands have been added to splicer");
        }

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

        if (logger.isInfoEnabled()) {
            logger.info("HKN1Splicer was started.");
        }
    }

    public void start(Spliceable start)
    {
        start();
    }

    public void stop()
    {
        if (state != Splicer.STOPPED) {
            changeState(Splicer.STOPPING);

            if (logger.isInfoEnabled()) {
                logger.info("Stopping HKN1Splicer.");
            }
        }
    }

    public void stop(Spliceable stop) throws OrderingException
    {
        stop();
    }

    public void truncate(Spliceable spliceable)
    {
        ArrayList removeRope = new ArrayList();

        synchronized (ropeLock)
        {
            if (LAST_POSSIBLE_SPLICEABLE.equals(spliceable)) {
                // Remove all in SplicerChangedEvent, below
                ArrayList tmpRope = removeRope;
                removeRope = rope;
                rope = tmpRope;
            } else if(rope.size() > 0) {
                int splicerPos = 0;
                for(int i=0; i<rope.size(); i++) {
                    if(rope.get(i).compareSpliceable(spliceable) >= 0) break;
                    splicerPos++;
                }

                // This is the stuff we want to remove from the list
                // NOTE: upper bound is exclusive
                List subrange = rope.subList(0, splicerPos);

                removeRope.addAll(subrange);
                subrange.clear();

            }
            decrement += removeRope.size();
        }

        SplicerChangedEvent event = new SplicerChangedEvent(this, state, spliceable, removeRope);
        synchronized (listeners) {
            for (SplicerListener listener : listeners)
            {
                if (logger.isDebugEnabled()) {
                    logger.debug("Firing truncate event to listener.");
                }
                listener.truncated(event);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Rope truncated to length " + rope.size());
        }
    }

    public void run()
    {
        terminalNode = Node.makeTree(exposeList);
        changeState(Splicer.STARTED);
        boolean sawLast = false;
        Spliceable previousSpliceable = null;

        // make sure decrement starts at zero
        decrement = 0;

        loopInit(exposeList);

        while (state == Splicer.STARTED)
        {
            try
            {
                synchronized (this)
                {
                    this.wait(waitMillis);
                }
            }
            catch (InterruptedException e)
            {
                logger.error("Splicer run thread was interrupted.");
            }

            loopCheck(exposeList);

            boolean addedToRope;
            synchronized (terminalNode)
            {
                addedToRope = !terminalNode.isEmpty();
                while (!terminalNode.isEmpty())
                {
                    Spliceable obj = terminalNode.pop();
                    // Make sanity check on objects coming out of splicer
                    if (previousSpliceable != null && previousSpliceable.compareSpliceable(obj) > 0)
                    {
                        logger.warn("Ignoring out-of-order object");
                    }
                    else if (obj != Splicer.LAST_POSSIBLE_SPLICEABLE)
                    {
                        synchronized (ropeLock)
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
                synchronized (ropeLock) {
                    if (logger.isDebugEnabled())
                        logger.debug("SplicedAnalysis.execute(" +
                                     rope.size() + ", " + decrement +
                                     ") - counter = " + counter);
                    int tmpDec = decrement;
                    decrement = 0;
                    analysis.execute(rope, tmpDec);
                }
            }
        }

        synchronized (ropeLock) {
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
        if (logger.isInfoEnabled()) {
            logger.info("HKN1Splicer was stopped.");
        }

        if (counter != 0) {
            logger.error("Resetting counter from " + counter + " to 0");
            counter = 0;
        }

        for (Node<Spliceable> node : exposeList) {
            node.clear();
        }
    }

    public String toString()
    {
        return "HKN1Splicer[" + getStateString() +
            "," + exposeList.size() + " strands]";
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

        public String toString()
        {
            return "Leaf:" + expose.getName() + "*" + expose.depth();
        }
    }
}

class HKN1Comparator implements Comparator<Spliceable>
{

    public int compare(Spliceable s1, Spliceable s2)
    {
        if (s1 == Splicer.LAST_POSSIBLE_SPLICEABLE) {
            if (s2 == Splicer.LAST_POSSIBLE_SPLICEABLE) {
                return 0;
            }

            return 1;
        } else if (s2 == Splicer.LAST_POSSIBLE_SPLICEABLE) {
            return -1;
        }

        return s1.compareSpliceable(s2);
    }

}
