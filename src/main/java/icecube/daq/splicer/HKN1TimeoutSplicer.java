package icecube.daq.splicer;

import icecube.daq.hkn1.Node;
import icecube.daq.hkn1.Node;

import java.util.Comparator;
import java.util.List;

/**
 * This version of the HKN1Splicer can survive one or more dead strand tails.
 */
public class HKN1TimeoutSplicer
    extends HKN1Splicer
{
    /** Number of time slices */
    private static final int NUM_SLICES = 10;

    /** Duration of one time slice (in milliseconds) */
    private long timeSlice;
    /** Time of previous time slice */
    private long prevTime;

    /**
     * Create a timeout splicer.
     *
     * @param analysis splicer analysis logic
     */
    public HKN1TimeoutSplicer(SplicedAnalysis analysis)
    {
        this(analysis, 60);
    }

    /**
     * Create a timeout splicer.
     *
     * @param analysis splicer analysis logic
     * @param timeoutSecs number of seconds a strand tail can be idle
     *                    before being marked inactive
     */
    public HKN1TimeoutSplicer(SplicedAnalysis analysis, int timeoutSecs)
    {
        super(analysis);

        setTimeoutSeconds(timeoutSecs);
    }

    /**
     * Create a tree node.
     *
     * @param cmp comparator
     *
     * @return new node
     */
    public Node<Spliceable> createNode(Comparator<Spliceable> cmp)
    {
        return new TimeoutNode<Spliceable>(cmp);
    }

    /**
     * If we're in a new time slice, check for inactive nodes.
     *
     * @param nodes list of strand tail nodes
     */
    public void loopCheck(List<Node<Spliceable>> nodes)
    {
        // are we in a new time slice?
        long curTime = System.currentTimeMillis();
        if (curTime >= prevTime + timeSlice) {
            for (Node<Spliceable> node : nodes) {
                ((TimeoutNode) node).checkInactive();
            }

            prevTime = curTime;
        }
    }

    /**
     * Seed the previous time slice value before the loop starts.
     *
     * @param nodes list of strand tail nodes
     */
    public void loopInit(List<Node<Spliceable>> nodes)
    {
        prevTime = System.currentTimeMillis();
    }

    /**
     * Set the number of seconds a strand tail can be idle before being
     * marked inactive.
     *
     * @param timeoutSecs number of seconds before node is marked inactive
     */
    public void setTimeoutSeconds(int timeoutSecs)
    {
        timeSlice = (timeoutSecs  * 1000) / NUM_SLICES;
        setWaitMillis(timeSlice);
    }

    /**
     * Resilient node which can survive having an idle peer.
     */
    class TimeoutNode<T>
        extends Node<T>
    {
        /** Number of inactive time slices */
        private int sliceCount;
        /** Has data been pushed to this node during the current time slice? */
        private boolean pushedData;
        /** Is this node currently marked inactive? */
        private boolean inactive;

        /**
         * Create a timeout node.
         *
         * @param cmp node comparator
         */
        TimeoutNode(Comparator<T> cmp)
        {
            super(cmp);
        }

        /**
         * Has this node been inactive during the current time slice?
         */
        void checkInactive()
        {
            if (pushedData) {
                // this node was active, so don't do anything
                pushedData = false;
            } else if (sliceCount < HKN1TimeoutSplicer.NUM_SLICES) {
                // this node was inactive,
                // but not yet long enough to be considered idle
                sliceCount++;
            } else {
                // this node is idle!
                setInactive(true);
            }
        }

        /**
         * Compare this node and its peer.
         *
         * @return -1 if this node is inactive and the peer has data
         *         1 if peer is inactive and this node has data
         *         otherwise compare data using the comparator
         */
        public int compare()
        {
            TimeoutNode<T> peer = (TimeoutNode<T>) peer();
            if (inactive && !peer.isEmpty()) return 1;
            if (peer.inactive && !isEmpty()) return -1;
            return super.compare();
        }

        /**
         * Create a new node.
         *
         * @return new node
         */
        public Node<T> createNode()
        {
            return new TimeoutNode<T>(getComparator());
        }

        /**
         * Is there data available from either this node or its peer?
         *
         * @return <tt>true</tt> if there is data available
         */
        public boolean isDataAvailable()
        {
            TimeoutNode peer = (TimeoutNode) peer();
            if (peer == null) return false;
            return (!isEmpty() && !peer.isEmpty()) ||
                (inactive && !peer.isEmpty()) ||
                (!isEmpty() && peer.inactive);
        }

        /**
         * Along with standard push() behavior, reset inactive counters
         *
         * @param element data to be pushed
         */
        public void push(T element)
        {
            super.push(element);

            setInactive(false);
            pushedData = true;
            sliceCount = 0;
        }

        /**
         * Set this node active/inactive.  Note that this may percolate
         * up the tree if this node's peer is also active/inactive.
         *
         * @param val <tt>true</tt> if node is now inactive
         */
        private void setInactive(boolean val)
        {
            if (inactive != val) {
                inactive = val;

                TimeoutNode peer = (TimeoutNode) peer();
                if (peer != null && inactive == peer.inactive) {
                    TimeoutNode sink = (TimeoutNode) sink();

                    if (sink != null && inactive != sink.inactive) {
                        sink.setInactive(inactive);

                        if (inactive) {
                            if (sink.peer() != null) {
                                sink.peer().checkList();
                            }

                            sink.setInactive(inactive);
                        }
                    }
                }
            }
        }

        /**
         * Debugging representation of this node.
         *
         * @return debugging string
         */
        public String toString()
        {
            return super.toString() +
                (inactive ? "<I>" : "<#" + sliceCount + ">");
        }
    }
}
