/*
 * class: SplicerImpl
 *
 * Version $Id: SplicerImpl.java,v 1.29 2006/02/12 22:26:29 patton Exp $
 *
 * Date: August 1 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

import icecube.icebucket.monitor.ScalarFlowMonitor;
import icecube.icebucket.monitor.simple.ScalarFlowMonitorImpl;
import icecube.icebucket.util.ThreadInvoker;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * This class manges the weaving of one or more {@link Strand}s of {@link
 * Spliceable}s.
 * <p/>
 * Note that only those methods of the {@link Splicer} interface that modify or
 * return member data, and those that access member data also accessed in those
 * methods, need to synchronized as all other methods should be invoked within
 * the internal Thread.
 *
 * @author patton
 * @version $Id: SplicerImpl.java,v 1.29 2006/02/12 22:26:29 patton Exp $
 */
public class SplicerImpl
        implements Splicer,
                   StrandManager
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    /**
     * The List of behaviours for all possible states.
     */
    private static final StateBehaviour[] STATE_BEHAVIOURS =
            new StateBehaviour[]{
                    new StateBehaviour()
                    {
                        private static final int STATE = FAILED;

                        public void fireEvent(SplicerListener listener,
                                              SplicerChangedEvent event)
                        {
                            listener.failed(event);
                        }

                        public int getState()
                        {
                            return STATE;
                        }

                        public String getStateString()
                        {
                            return "FAILED";
                        }

                        public boolean transitionAllowed(int newState)
                        {
                            return FAILED == newState ||
                                   STARTING == newState ||
                                   STOPPING == newState ||
                                   DISPOSED == newState;
                        }
                    },
                    new StateBehaviour()
                    {
                        private static final int STATE = STOPPED;

                        public void fireEvent(SplicerListener listener,
                                              SplicerChangedEvent event)
                        {
                            listener.stopped(event);
                        }

                        public int getState()
                        {
                            return STATE;
                        }

                        public String getStateString()
                        {
                            return "STOPPED";
                        }

                        public boolean transitionAllowed(int newState)
                        {
                            return STARTING == newState ||
                                   DISPOSED == newState;
                        }
                    },
                    new StateBehaviour()
                    {
                        private static final int STATE = STARTING;

                        public void fireEvent(SplicerListener listener,
                                              SplicerChangedEvent event)
                        {
                            listener.starting(event);
                        }

                        public int getState()
                        {
                            return STATE;
                        }

                        public String getStateString()
                        {
                            return "STARTING";
                        }

                        public boolean transitionAllowed(int newState)
                        {
                            return FAILED == newState ||
                                   STARTED == newState ||
                                   STOPPING == newState;
                        }
                    },
                    new StateBehaviour()
                    {
                        private static final int STATE = STARTED;

                        public void fireEvent(SplicerListener listener,
                                              SplicerChangedEvent event)
                        {
                            listener.started(event);
                        }

                        public int getState()
                        {
                            return STATE;
                        }

                        public String getStateString()
                        {
                            return "STARTED";
                        }

                        public boolean transitionAllowed(int newState)
                        {
                            return FAILED == newState ||
                                   STOPPING == newState;
                        }
                    },
                    new StateBehaviour()
                    {
                        private static final int STATE = STOPPING;

                        public void fireEvent(SplicerListener listener,
                                              SplicerChangedEvent event)
                        {
                            listener.stopping(event);
                        }

                        public int getState()
                        {
                            return STATE;
                        }

                        public String getStateString()
                        {
                            return "STOPPING";
                        }

                        public boolean transitionAllowed(int newState)
                        {
                            return FAILED == newState ||
                                   STOPPED == newState;
                        }
                    },
                    new StateBehaviour()
                    {
                        private static final int STATE = DISPOSED;

                        public void fireEvent(SplicerListener listener,
                                              SplicerChangedEvent event)
                        {
                            listener.disposed(event);
                        }

                        public int getState()
                        {
                            return STATE;
                        }

                        public String getStateString()
                        {
                            return "DISPOSED";
                        }

                        public boolean transitionAllowed(int newState)
                        {
                            return false;
                        }
                    },
            };

    // private static member data

    // private instance member data

    /**
     * The SplicedAnalysis to be executed.
     */
    private final SplicedAnalysis analysis;

    /**
     * The queue of Runnables used to handle splicer->analysis data flow.
     */
    private final ThreadInvoker analysisExecutionQueue = new ThreadInvoker();

    /**
     * The Spliceable to use to cut the rope for a clean start.
     * <p/>
     * Should be null before "STARTING" and after "STARTED".
     */
    private Spliceable beginningSpliceable;

    /**
     * The StateBehaviour's object for the current state.
     */
    private SplicerImpl.StateBehaviour currentBehaviour =
            STATE_BEHAVIOURS[STOPPED];

    /**
     * The List of empty Strands.
     * <p/>
     * Should be filled when "STOPPED".
     */
    private final List emptyStrands = new ArrayList(1);

    /**
     * The list of listeners receiving events from this object.
     */
    private final List listeners = new ArrayList(1);

    /**
     * The current lowest common Spliceable candidate.
     * <p/>
     * Should be null when {@link #lowestCommonStrands} is filled.
     */
    private Spliceable lowestCommonSpliceable;

    /**
     * The List of Strands still to be checked for the lowest common
     * Spliceable.
     * <p/>
     * Should be filled when "STOPPED".
     */
    private final List lowestCommonStrands = new ArrayList(1);

    /**
     * The MonitoredAnalysis that wraps the specified SplicedAnalysis.
     */
    private final MonitoredAnalysis monitoredAnalysis;

    /**
     * The object used to inspect rates in this object.
     */
    private final MonitorPoints monitorPoints;

    /**
     * List of Spliceables waiting to be added to the {@link rope}.
     */
    private final List pendingRope = new ArrayList(0);

    /**
     * List of Spliceables being analyzed by analysis.
     * <p/>
     * Should be cleared after "STOPPED" and before "STARTING".
     */
    private final List rope = new ArrayList(1);

    /**
     * List of new Spliceables to add to the rope when possible.
     */
    private final List ropeExtension = new ArrayList(0);

    /**
     * The current "head" of the "rope".
     */
    private Spliceable ropeHeadSpliceable;

    /**
     * The  queue of Runnables used to handle tail->strand data flow.
     */
    private final ThreadInvoker splicerExecutionQueue = new ThreadInvoker();

    /**
     * The Spliceable, if any, requested for a clean start.
     * <p/>
     * Should be null before "STARTING" and after "STARTED".
     */
    private Spliceable startSpliceable;

    /**
     * The number of open stands that are in this object.
     */
    private int strandCount;

    /**
     * The open Strands in this object.
     */
    private final List strands = new ArrayList(1);

    /**
     * True if a weave method has been queued but not executed.
     */
    private boolean weaveQueued;

    /**
     * The Weaver object used to weave the Strands into a Rope.
     */
    private final Weaver weaver = new WeaverImpl();

    /**
     * True if weaving is currently in progress.
     */
    private boolean weavingInProgess;

    /**
     * The object used to lock while weaving is in progress.
     */
    private final Object weavingLock = new Object();

    /**
     * The current state of the weaving activity.
     */
    private int weavingState;

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param analysis the SplicedAnalysis to be executed by this object.
     */
    public SplicerImpl(SplicedAnalysis analysis)
    {
        this.analysis = analysis;
        final ScalarFlowMonitor outputFlow = new ScalarFlowMonitorImpl();
        monitoredAnalysis = new MonitoredAnalysis(analysis,
                                                  outputFlow);
        monitorPoints = new MonitorPoints(null,
                                          null,
                                          outputFlow);
        final Thread splicerThread = new Thread(splicerExecutionQueue,
                                                "Weaver");
        splicerThread.start();
        final Thread analysisThread = new Thread(analysisExecutionQueue,
                                                 "SplicedAnalyzer");
        analysisThread.start();
    }

    // instance member method (alphabetic)

    public void addSpliceableChannel(SelectableChannel channel)
            throws IOException
    {
        throw new UnsupportedOperationException();
    }

    public void addSplicerListener(SplicerListener listener)
    {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    public void analyze()
    {
        analysisExecutionQueue.invokeAndWait(new Runnable()
        {
            public void run()
            {
                monitoredAnalysis.execute(Collections.unmodifiableList(rope),
                                          0);
            }
        });
    }

    public StrandTail beginStrand()
    {
        if (STOPPED != getState()) {
            throw new IllegalStateException("Curently only support new" +
                                            " Strands when Splicer is" +
                                            " stopped.");
        }

        final ManagedStrand managedStrand = new StrandImpl(this);
        splicerExecutionQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                strands.add(managedStrand);
                lowestCommonStrands.add(managedStrand);
                weaver.addStrand(managedStrand.getStrand());
                setStrandCount(strands.size());
            }
        });
        return managedStrand.getTail(splicerExecutionQueue);
    }

    /**
     * Cuts of the "head" of the "rope" by removing all those elements that are
     * less than the specified Spliceable.
     * <p/>
     * This method is only executed in the 'analysis' thread.
     *
     * @param cut the number of Spliceables removed from the specified rope.
     * @param ropeToCut the rope to trim.
     * @return the number of Spliceables removed.
     */
    private int cutOffHead(Spliceable cut,
                           List ropeToCut)
    {
        int index;
        if (LAST_POSSIBLE_SPLICEABLE.equals(cut)) {
            index = ropeToCut.size() - 1;
        } else {
            index = Collections.binarySearch(ropeToCut,
                                             cut);
            if (0 > index) {
                index = -1 * (index + 2);
            } else {

                // Work backwards to find the exact cut off index.
                while ((0 <= index) &&
                       (0 == cut.compareTo(ropeToCut.get(index)))) {
                    index--;
                }
            }
        }

        // If the beginning of the rope to cut matches the cut off then no cutting
        // is needed.
        if (0 > index) {
            return 0;
        }

        final int end = index + 1;
        final List deadSpliceables = ropeToCut.subList(0,
                                                      end);
        final int currentState = getState();
        final SplicerChangedEvent event =
                new SplicerChangedEvent(this,
                                        currentState,
                                        cut,
                                        deadSpliceables);
        synchronized (listeners) {
            final Iterator iterator = listeners.iterator();
            while (iterator.hasNext()) {
                final SplicerListener listener =
                        (SplicerListener) iterator.next();
                listener.truncated(event);
            }
        }

        deadSpliceables.clear();
        return end;
    }

    public void dispose()
    {
        splicerExecutionQueue.terminate();
        analysisExecutionQueue.terminate();
        setState(DISPOSED);
    }

    /**
     * Informs all listners of a internal Splicer event.
     *
     * @param event object describing the event.
     */
    private void fireChangeEvent(SplicerChangedEvent event)
    {
        synchronized (listeners) {
            final Iterator iterator = listeners.iterator();
            while (iterator.hasNext()) {
                final SplicerListener listener =
                        (SplicerListener) iterator.next();
                currentBehaviour.fireEvent(listener,
                                           event);
            }
        }
    }

    public void forceStop()
    {
        // If already stopped do nothing.
        if (STOPPED == getState()) {
            return;
        }

        // Otherwise set up forced stop of consumer.
        setState(STOPPING);
        splicerExecutionQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                final Iterator iterator = strands.iterator();
                while (iterator.hasNext()) {
                    final ManagedStrand managedStrand =
                            (ManagedStrand) iterator.next();
                    managedStrand.forceHalt();
                    if (managedStrand.isRemoveableAsEmpty()) {
                        if (emptyStrands.remove(managedStrand)) {
                            weaver.removeStrand(managedStrand.getStrand());
                        }
                    }
                }
                weavingState = STOPPING;
                queueWeave();
            }
        });
    }

    public SplicedAnalysis getAnalysis()
    {
        return analysis;
    }

    public MonitorPoints getMonitorPoints()
    {
        return monitorPoints;
    }

    public synchronized int getState()
    {
        return currentBehaviour.getState();
    }

    public synchronized String getStateString()
    {
        return currentBehaviour.getStateString();
    }

    public String getStateString(int state)
    {
        return STATE_BEHAVIOURS[state].getStateString();
    }

    public synchronized int getStrandCount()
    {
        return strandCount;
    }

    /**
     * Returns true if this object has transition from STARTING to STARTED.
     *
     * @return true if this object has transition from STARTING to STARTED.
     */
    private boolean hasStarted()
    {
        // If "frayed" start has completed, simple return true.
        if (null == startSpliceable) {
            return true;

        }

        // Otherwise if beginning of "clean" start set cut-off Spliceable.
        if (null == beginningSpliceable) {
            if (0 > startSpliceable.compareTo(lowestCommonSpliceable)) {
                beginningSpliceable = lowestCommonSpliceable;
            } else {
                beginningSpliceable = startSpliceable;
            }
        }

        final int maxCut = ropeExtension.size();
        if (maxCut == cutOffHead(beginningSpliceable,
                                 ropeExtension)) {
            return false;
        }

        startSpliceable = null;
        beginningSpliceable = null;

        return true;
    }

    /**
     * Returns true if this object's condition enables it to weave.
     *
     * @return true if this object's condition enables it to weave.
     */
    private boolean isAbleToWeaver()
    {
        return (STARTING == weavingState ||
                STARTED == weavingState ||
                STOPPING == weavingState) &&
                                          (!strands.isEmpty()) &&
                                          emptyStrands.isEmpty();
    }

    public List pendingChannels()
    {
        throw new UnsupportedOperationException();
    }

    public List pendingStrands()
    {
        final List result = new ArrayList(1);
        splicerExecutionQueue.invokeAndWait(new Runnable()
        {
            public void run()
            {
                final Iterator iterator = emptyStrands.iterator();
                while (iterator.hasNext()) {
                    final ManagedStrand managedStrand =
                            (ManagedStrand) iterator.next();
                    result.add(managedStrand.getTail(splicerExecutionQueue));
                }
            }
        });
        return result;
    }

    /**
     * Queues the weave method to be execute if not already queued.
     */
    private void queueWeave()
    {
        if (weaveQueued) {
            return;
        }
        splicerExecutionQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                weave();
            }
        });
        weaveQueued = true;
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

    /**
     * Sets all the internal attribute so that this object can be restarted.
     */
    private void reset()
    {
        analysisExecutionQueue.invokeAndWait(new Runnable()
        {
            public void run()
            {
                // Make sure all woven Spliceables are in rope to be discarded.
                final boolean extended;
                synchronized (pendingRope) {
                    extended = 0 != pendingRope.size();
                    if (extended) {
                        rope.addAll(pendingRope);
                        pendingRope.clear();
                    }
                }

                if (extended) {
                    monitoredAnalysis
                            .execute(Collections.unmodifiableList(rope),
                                     0);
                }

                final int ropeSize = cutOffHead(LAST_POSSIBLE_SPLICEABLE,
                                                rope);
                monitoredAnalysis.execute(Collections.unmodifiableList(rope),
                                          ropeSize);
            }
        });
        lowestCommonStrands.clear();
        emptyStrands.clear();
        lowestCommonSpliceable = null;
        final Iterator managedStrands = strands.iterator();
        while (managedStrands.hasNext()) {
            final ManagedStrand managedStrand =
                    (ManagedStrand) managedStrands.next();
            weaver.addStrand(managedStrand.getStrand());
            lowestCommonStrands.add(managedStrand);
            emptyStrands.add(managedStrand);
        }

        final Iterator proceedStrands = strands.iterator();
        while (proceedStrands.hasNext()) {
            final ManagedStrand managedStrand =
                    (ManagedStrand) proceedStrands.next();
            managedStrand.proceed();
        }
    }

    /**
     * Set the current state of this object.
     *
     * @param state the target state for this object
     * @throws IllegalStateException if the target state is not allowed given
     * the current state of this object,
     */
    private synchronized void setState(int state)
    {
        final int currentState = currentBehaviour.getState();

        // If there is no state change then ignore.
        if (currentState == state) {
            return;
        }

        // Otherwise see if state transition is allowed.
        if (!currentBehaviour.transitionAllowed(state)) {
            throw new IllegalStateException("Tried to transition from " +
                                            getStateString(currentState) +
                                            " to " +
                                            getStateString(state));
        }
        final SplicerChangedEvent event = new SplicerChangedEvent(this,
                                                                  currentState,
                                                                  state);
        currentBehaviour = STATE_BEHAVIOURS[state];
        fireChangeEvent(event);
    }

    private synchronized void setStrandCount(int strandCount)
    {
        this.strandCount = strandCount;
    }

    public void start()
    {
        // If already starting or started do nothing.
        final int state = getState();
        if ((STARTING == state) ||
            (STARTED == state)) {
            return;
        }

        // Otherwise set up start of weaving.
        setState(STARTING);
        splicerExecutionQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                weavingState = STARTING;
                queueWeave();
            }
        });
    }

    public void start(final Spliceable start)
    {
        // Check arguments
        if (null == start) {
            throw new NullPointerException("null not allowed as argument.");
        }
        if (0 == LAST_POSSIBLE_SPLICEABLE.compareTo(start)) {
            throw new IllegalArgumentException("LAST_POSSIBLE_SPLICEABLE is" +
                                               " not allowed as argument");
        }

        // If already starting or started do nothing.
        final int state = getState();
        if ((STARTING == state) ||
            (STARTED == state)) {
            return;
        }

        // Otherwise set up start of weaving.
        setState(STARTING);
        splicerExecutionQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                startSpliceable = start;
                weavingState = STARTING;
                queueWeave();
            }
        });
    }

    public void stop()
    {
        // If already stopping or stopped do nothing.
        final int state = getState();
        if ((STOPPING == state) ||
            (STOPPED == state)) {
            return;
        }

        // Otherwise set up stop of weaving.
        setState(STOPPING);
        splicerExecutionQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                final Iterator iterator = strands.iterator();
                while (iterator.hasNext()) {
                    final ManagedStrand managedStrand =
                            (ManagedStrand) iterator.next();
                    managedStrand.halt();
                    if (managedStrand.isRemoveableAsEmpty()) {
                        if (emptyStrands.remove(managedStrand)) {
                            weaver.removeStrand(managedStrand.getStrand());
                        }
                    }
                }
                weavingState = STOPPING;
                queueWeave();
            }
        });
    }

    public void stop(final Spliceable stop)
            throws OrderingException
    {
        // Check arguments
        if (null == stop) {
            throw new NullPointerException("null not allowed as argument.");
        }
        if (0 == LAST_POSSIBLE_SPLICEABLE.compareTo(stop)) {
            throw new IllegalArgumentException("LAST_POSSIBLE_SPLICEABLE is" +
                                               " not allowed as argument");
        }

        synchronized (weavingLock) {
            if ((null != ropeHeadSpliceable) &&
                (0 > stop.compareTo(ropeHeadSpliceable))) {
                throw new OrderingException("Requested stop has already been" +
                                            " passed.");
            }
        }

        // If already stopping or stopped do nothing.
        final int state = getState();
        if ((STOPPING == state) ||
            (STOPPED == state)) {
            return;
        }

        // Otherwise set up stop of weaving.
        setState(STOPPING);
        splicerExecutionQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                final Iterator iterator = strands.iterator();
                while (iterator.hasNext()) {
                    final ManagedStrand managedStrand =
                            (ManagedStrand) iterator.next();
                    managedStrand.halt(stop);
                    if (managedStrand.isRemoveableAsEmpty()) {
                        if (emptyStrands.remove(managedStrand)) {
                            weaver.removeStrand(managedStrand.getStrand());
                        }
                    }
                }
                weavingState = STOPPING;
                queueWeave();
            }
        });
    }

    public void strandBecameEmpty(ManagedStrand managedStrand)
    {

        if (!weavingInProgess &&
            managedStrand.isRemoveableAsEmpty()) {
            synchronized (weavingLock) {
                weaver.removeStrand(managedStrand.getStrand());
            }
            return;
        }
        emptyStrands.add(managedStrand);
    }

    public void strandBecameRemoveable(ManagedStrand managedStrand)
    {
        // Note: This does not need to check if weaving is in progress as it is
        // always invoked from within the same Invocable that handles the
        // weaving and can not be invoked within the weave method.

        if (emptyStrands.remove(managedStrand)) {
            weaver.removeStrand(managedStrand.getStrand());
        }
        queueWeave();
    }

    public void strandClosed(ManagedStrand managedStrand)
    {
        // Note: This does not need to check if weaving is in progress as it is
        // always invoked from within the same Invocable that handles the
        // weaving and can not be invoked within the weave method.

        if (managedStrand.isRemoveableAsEmpty()) {
            if (emptyStrands.remove(managedStrand)) {
                weaver.removeStrand(managedStrand.getStrand());
            }
            strands.remove(managedStrand);
            setStrandCount(strands.size());
            queueWeave();
        }
    }

    public void strandNoLongerEmpty(ManagedStrand managedStrand)
    {
        // Note: This does not need to check if weaving is in progress as it is
        // always invoked from within the same Invocable that handles the
        // weaving and is either invoked outside the weave method or after the
        // Weaver has been used.

        // Work at locating the the lowest common Spliceable.
        if ((0 != lowestCommonStrands.size()) &&
            (STOPPED == weavingState ||
             STARTING == weavingState) &&
                                       lowestCommonStrands
                                               .contains(managedStrand)) {
            lowestCommonStrands.remove(managedStrand);
            if (!managedStrand.isRemoveableAsEmpty()) {
                final Strand strand = managedStrand.getStrand();
                if ((null == lowestCommonSpliceable) ||
                    (0 > lowestCommonSpliceable.compareTo(strand.head()))) {
                    lowestCommonSpliceable = strand.head();
                }
            }
        }

        emptyStrands.remove(managedStrand);
        queueWeave();
    }

    public void truncate(final Spliceable spliceable)
    {
        analysisExecutionQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                final int decrement = cutOffHead(spliceable,
                                                 rope);
                if (0 != decrement) {
                    monitoredAnalysis
                            .execute(Collections.unmodifiableList(rope),
                                     decrement);
                }
            }
        });
    }

    /**
     * Updates the strands after a weave has been invoked.
     *
     * @return true if the weaver's set of know Strands has changed.
     */
    private boolean updateStrands()
    {
        boolean weaverModified = false;
        final Iterator iterator = strands.iterator();
        while (iterator.hasNext()) {
            final ManagedStrand managedStrand =
                    (ManagedStrand) iterator.next();
            managedStrand.transferToClientThread();
            if (managedStrand.isRemoveableAsEmpty()) {
                if (emptyStrands.remove(managedStrand)) {
                    weaver.removeStrand(managedStrand.getStrand());
                    weaverModified = true;
                }
                if (managedStrand.isClosed()) {
                    iterator.remove();
                    setStrandCount(strands.size());
                }
            }
        }
        return weaverModified;
    }

    private void weave()
    {
        weaveQueued = false;

        if (0 == weaver.getStrandCount()) {
            try {
                if ((STARTED == weavingState) ||
                    (STARTING == weavingState)) {
                    weavingState = STOPPING;
                    setState(STOPPING);
                    queueWeave();
                    return;
                }

                if (STOPPING == weavingState) {
                    weavingState = STOPPED;
                    reset();
                    setState(STOPPED);
                    return;
                }
            } catch (IllegalStateException e) {
                weavingState = FAILED;
                setState(FAILED);
                return;
            }
        }

        if (isAbleToWeaver()) {
            weavingInProgess = true;
            synchronized (weavingLock) {
                weaver.weave(ropeExtension);
                if (!ropeExtension.isEmpty()) {
                    ropeHeadSpliceable =
                            (Spliceable) ropeExtension
                                    .get(ropeExtension.size() - 1);
                }
            }
            weavingInProgess = false;

            if (updateStrands()) {
                queueWeave();
            }

            if (0 != ropeExtension.size()) {

                if (STARTING == weavingState &&
                    hasStarted()) {
                    try {
                        weavingState = STARTED;
                        setState(STARTED);
                    } catch (IllegalStateException e) {
                        weavingState = FAILED;
                        setState(FAILED);
                    }
                }
            }

            // ropeExtension can be shortened by hasStarted.
            if (0 != ropeExtension.size()) {
                final boolean queueExecution;
                synchronized (pendingRope) {
                    queueExecution = 0 == pendingRope.size();
                    pendingRope.addAll(ropeExtension);
                }
                // Drop references to the gathered objects.
                ropeExtension.clear();

                if (queueExecution) {
                    analysisExecutionQueue.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            synchronized (pendingRope) {
                                if (0 == pendingRope.size()) {
                                    return;
                                }
                                rope.addAll(pendingRope);
                                pendingRope.clear();
                            }
                            monitoredAnalysis.execute(
                                    Collections.unmodifiableList(rope),
                                    0);
                        }
                    });
                }
            }
        }
    }

    // static member methods (alphabetic)

    private static interface StateBehaviour
    {
        /**
         * Calls the appropriate method in the listener for a change into the
         * state this object implements.
         *
         * @param listener the listener to call.
         * @param event the event to use as the parameter to the call.
         */
        void fireEvent(SplicerListener listener,
                       SplicerChangedEvent event);

        /**
         * Returns the state this object implements.
         *
         * @return the state this object implements.
         */
        int getState();

        /**
         * Returns a string describing the state this object implements.
         *
         * @return a string describing the state this object implements.
         */
        String getStateString();

        /**
         * Returns true if this state can change in the the target state.
         *
         * @param newState the target state.
         * @return true if this state can change in the the target state.
         */
        boolean transitionAllowed(int newState);
    }

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}
