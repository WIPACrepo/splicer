package icecube.daq.splicer;

import icecube.daq.hkn1.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

public class HKN1Splicer<T>
    implements Splicer<T>, Runnable, HKN1SplicerMBean
{
    private static final Logger logger = Logger.getLogger(HKN1Splicer.class);

    private SplicedAnalysis<T>        analysis;
    private Comparator<T>             cmp;
    private T                         lastObject;

    private List<Node<T>>             exposeList    = new ArrayList<Node<T>>();
    private Node<T>                   terminalNode;
    private volatile State            state         = State.STOPPED;
    private volatile int              counter;
    private List<SplicerListener<T>>  listeners     =
        new ArrayList<SplicerListener<T>>();
    private long                      waitMillis    = 1000L;
    private long                      totalSent;

    public HKN1Splicer(SplicedAnalysis<T> analysis, Comparator<T> cmp,
                       T lastObject)
    {
        this.analysis = analysis;
        this.cmp = cmp;
        this.lastObject = lastObject;
    }

    @Override
    public void addSplicerListener(SplicerListener<T> listener)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding splicer listener.");
        }

        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public StrandTail<T> beginStrand()
    {
        Node<T> node = new Node<T>(cmp);
        synchronized (exposeList) {
            exposeList.add(node);
        }
        counter++;
        return new HKN1LeafNode(node);
    }

    private void changeState(State newState)
    {
        SplicerChangedEvent<T> event =
            new SplicerChangedEvent<T>(this, state, newState);
        state = newState;
        synchronized (listeners) {
            for (SplicerListener<T> listener : listeners) {
                switch (newState) {
                case DISPOSED:
                    listener.disposed(event);
                    break;
                case FAILED:
                    listener.failed(event);
                    break;
                case STARTED:
                    listener.started(event);
                    break;
                case STARTING:
                    listener.starting(event);
                    break;
                case STOPPED:
                    listener.stopped(event);
                    break;
                case STOPPING:
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
     * @param spliceableCmp comparator
     *
     * @return new node
     */
    public Node<Spliceable> createNode(Comparator<Spliceable> spliceableCmp)
    {
        return new Node<Spliceable>(spliceableCmp);
    }

    @Override
    public void dispose()
    {
        if (state != State.STOPPING && state != State.STOPPED) {
            changeState(State.STOPPING);
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

        List<Node<T>> peers =
            new ArrayList<Node<T>>(exposeList);
        List<Node<T>> sinks =
            new ArrayList<Node<T>>();

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

            List<Node<T>> tmp = peers;
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

    @Override
    public void forceStop()
    {
        // NO OP
    }

    @Override
    public SplicedAnalysis<T> getAnalysis()
    {
        return analysis;
    }

    @Override
    public State getState()
    {
        return state;
    }

    @Override
    public int getStrandCount()
    {
        return exposeList.size();
    }

    @Override
    public long getTotalSent()
    {
        return totalSent;
    }

    /**
     * Perform any extra code during this pass in the loop.
     *
     * @param nodes list of strand tail nodes
     */
    public void loopCheck(List<Node<T>> nodes)
    {
    }

    /**
     * Perform any needed initialization before the loop starts.
     */
    public void loopInit(List<Node<T>> nodes)
    {
    }

    @Override
    public void removeSplicerListener(SplicerListener<T> listener)
    {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    void setWaitMillis(long val)
    {
        waitMillis = val;
    }

    @Override
    public void start()
    {
        if (state != State.STOPPED) {
            throw new Error("Expected splicer to be " +
                            State.STOPPED.name() + ", not " +
                            state.name());
        }

        changeState(State.STARTING);

        if (exposeList.size() == 0) {
            throw new Error("No strands have been added to splicer");
        }

        Thread thread = new Thread(this);
        thread.setName("HKN1Splicer+" + analysis);
        thread.start();

        while (state != State.STARTED) {
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

    @Override
    public void stop()
    {
        if (state != State.STOPPED) {
            changeState(State.STOPPING);

            if (logger.isInfoEnabled()) {
                logger.info("Stopping HKN1Splicer.");
            }
        }
    }

    @Override
    public void run()
    {
        terminalNode = Node.makeTree(exposeList);
        changeState(State.STARTED);
        T previousSpliceable = null;

        loopInit(exposeList);

        while (state == State.STARTED) {
            try {
                synchronized (this) {
                    this.wait(waitMillis);
                }
            } catch (InterruptedException e) {
                logger.error("Splicer run thread was interrupted.");
            }

            loopCheck(exposeList);

            List<T> added = new ArrayList<T>();
            synchronized (terminalNode) {
                while (!terminalNode.isEmpty()) {
                    T obj = terminalNode.pop();
                    // Make sanity check on objects coming out of splicer
                    if (previousSpliceable != null &&
                        cmp.compare(previousSpliceable, obj) > 0)
                    {
                        logger.warn("Ignoring out-of-order object");
                    } else if (obj != lastObject) {
                        added.add(obj);
                    } else  {
                        dispose();
                    }
                }
            }
            if (added.size() > 0) {
                totalSent += added.size();
                try {
                    analysis.analyze(added);
                } catch (Throwable thr) {
                    logger.error("Analysis failed for " + added.size() +
                                 " objects", thr);
                    break;
                }
            }
        }

        changeState(State.STOPPED);
        if (logger.isInfoEnabled()) {
            logger.info("HKN1Splicer was stopped.");
        }

        if (counter != 0) {
            logger.error("Resetting counter from " + counter + " to 0");
            counter = 0;
        }

        synchronized (exposeList) {
            for (Node<T> node : exposeList) {
                node.clear();
            }
        }
    }

    @Override
    public String toString()
    {
        return "HKN1Splicer[" + state.name() + "," + exposeList.size() +
            " strands" + "]";
    }

    // inner class
    class HKN1LeafNode
        implements StrandTail<T>
    {

        private Node<T> expose;
        private long nInput;

        public HKN1LeafNode(Node<T> node)
        {
            expose = node;
            nInput = 0L;
        }

        @Override
        public void close()
        {
            synchronized (exposeList) {
                exposeList.remove(expose);
            }
            counter--;
        }

        @Override
        public T head()
        {
            return expose.head();
        }

        @Override
        public boolean isClosed()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public StrandTail<T> push(List<T> spliceables)
            throws OrderingException, ClosedStrandException
        {
            for (T spl : spliceables) {
                push(spl);
            }
            return this;
        }

        @Override
        public StrandTail<T> push(T spliceable)
            throws OrderingException, ClosedStrandException
        {
            synchronized (terminalNode) {
                expose.push(spliceable);
                if (logger.isDebugEnabled() && nInput++ % 1000 == 0) {
                    logger.debug("Pushing payload # " + nInput +
                        " into strandTail " + this);
                }
            }
            synchronized (HKN1Splicer.this) {
                HKN1Splicer.this.notify();
            }
            return this;
        }

        @Override
        public int size()
        {
            return expose.depth();
        }

        @Override
        public String toString()
        {
            return "Leaf:" + expose.getName() + "*" + expose.depth();
        }
    }
}
