package icecube.daq.splicer;

import icecube.daq.priority.AdjustmentTask;
import icecube.daq.priority.DataConsumer;
import icecube.daq.priority.SortInput;
import icecube.daq.priority.Sorter;
import icecube.daq.priority.SorterException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

public class PrioritySplicer<T>
    implements Splicer<T>, PrioritySplicerMBean
{
    /** Log message handler */
    private static final Logger LOG = Logger.getLogger(PrioritySplicer.class);

    private int nextStrand;

    private Sorter sorter;
    private ConsumerBridge<T> bridge;

    private volatile State state = State.STOPPED;
    private List<SplicerListener<T>> listeners =
        new ArrayList<SplicerListener<T>>();
    private AdjustmentTask task;

    public PrioritySplicer(String name, SplicedAnalysis<T> analysis,
                           Comparator<T> comp, T lastObject, int maxChannels)
        throws SplicerException
    {
        this(name, analysis, comp, lastObject, maxChannels, Integer.MIN_VALUE,
             Sorter.DEFAULT_CHUNK_SIZE);
    }

    public PrioritySplicer(String name, SplicedAnalysis<T> analysis,
                           Comparator<T> comp, T lastObject, int maxChannels,
                           int maxCPUs)
        throws SplicerException
    {
        this(name, analysis, comp, lastObject, maxChannels, maxCPUs,
             Sorter.DEFAULT_CHUNK_SIZE);
    }

    public PrioritySplicer(String name, SplicedAnalysis<T> analysis,
                           Comparator<T> comp, T lastObject, int maxChannels,
                           int maxCPUs, int maxSubsorterDepth)
        throws SplicerException
    {
        bridge = new ConsumerBridge(this, analysis);

        // allow sorter to use half of the cores
        if (maxCPUs == Integer.MIN_VALUE) {
            maxCPUs = Runtime.getRuntime().availableProcessors() / 2;
            if (maxCPUs < 1) {
                maxCPUs = 1;
            }
        }

        try {
            sorter = new Sorter(name, maxChannels, comp, bridge, lastObject,
                                maxCPUs, maxSubsorterDepth);
        } catch (SorterException se) {
            throw new SplicerException("Cannot create Sorter", se);
        }
    }

    @Override
    public void addSplicerListener(SplicerListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public StrandTail beginStrand()
    {
        final String name = String.format("#%d", nextStrand++);
        try {
            return new PrioTail(sorter.register(name));
        } catch (SorterException se) {
            throw new Error("Something has gone horribly wrong with " +
                            sorter.getName(), se);
        }
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
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Unknown state " + newState);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void dispose()
    {
        throw new Error("Unimplemented");
    }

    @Override
    public void forceStop()
    {
        // NO OP
    }

    @Override
    public SplicedAnalysis<T> getAnalysis()
    {
        return bridge.getAnalysis();
    }

    /**
     * Get the number of objects required for a sort to be initiated
     *
     * @return number of objects required for a sort to be initiated
     */
    public int getChunkSize()
    {
        return sorter.getChunkSize();
    }

    /**
     * Get sorter name
     *
     * @return name
     */
    public String getName()
    {
        return sorter.getName();
    }

    /**
     * Get number of sorter checks
     *
     * @return number of checks
     */
    public long getNumberOfChecks()
    {
        return sorter.getNumChecked();
    }

    /**
     * Get number of objects sorted by the sorter
     *
     * @return number of outputs
     */
    public long getNumberOfOutputs()
    {
        return sorter.getNumOutput();
    }

    /**
     * Get number of calls to sorter.process()
     *
     * @return number of calls to process()
     */
    public long getNumberOfProcessCalls()
    {
        return sorter.getNumProcessCalls();
    }

    /**
     * Get number of objects waiting to be sorted
     *
     * @return number of queued objects
     */
    public int getQueueSize()
    {
        return sorter.getNumQueued();
    }

    @Override
    public State getState()
    {
        return state;
    }

    @Override
    public int getStrandCount()
    {
        return sorter.getNumQueued();
    }

    @Override
    public void removeSplicerListener(SplicerListener listener)
    {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public void start()
    {
        if (state != State.STOPPED && state != State.STARTING &&
            state != State.STARTED)
        {
            throw new Error("Cannot start PrioritySplicer " +
                            sorter.getName() + ", state is " + state);
        }

        if (state == State.STARTED) {
            LOG.error("PrioritySplicer " + sorter.getName() +
                      " has already been started");
        } else if (state == State.STARTING) {
            LOG.error("PrioritySplicer " + sorter.getName() +
                      " is already starting");
        } else {
            bridge.reset();

            task = new AdjustmentTask();
            task.register(sorter);

            changeState(State.STARTING);
            sorter.start();
            task.start();
            changeState(State.STARTED);
        }
    }

    @Override
    public void stop()
    {
        if (state != State.STARTED && state != State.STOPPING &&
            state != State.STOPPED)
        {
            throw new Error("Cannot stop PrioritySplicer " + sorter.getName() +
                            ", state is " + state);
        }

        if (state == State.STOPPED) {
            LOG.error("PrioritySplicer " + sorter.getName() +
                      " has already been stopped");
        } else if (state == State.STOPPING) {
            LOG.error("PrioritySplicer " + sorter.getName() +
                      " is already stopping");
        } else {
            changeState(State.STOPPING);
            sorter.waitForStop();
            changeState(State.STOPPED);
        }

        // telling AdjustmentTask to stop multiple times is not a problem
        task.stop();
    }
}

class ConsumerBridge<T>
    implements DataConsumer<T>, Runnable
{
    private PrioritySplicer<T> splicer;
    private SplicedAnalysis<T> analysis;

    private List<T> one = new ArrayList<T>(1);

    private Thread stopThread;

    ConsumerBridge(PrioritySplicer<T> splicer, SplicedAnalysis<T> analysis)
    {
        this.splicer = splicer;
        this.analysis = analysis;
    }

    /**
     * Consume a piece of data.
     *
     * @param data data
     */
    public void consume(T data)
        throws IOException
    {
        synchronized (one) {
            one.add(data);
            analysis.analyze(one);
            one.clear();
        }
    }

    /**
     * The stream is closed.
     */
    public void endOfStream(long token)
        throws IOException
    {
        stopThread = new Thread(this, "StopThread");
        stopThread.start();
    }

    SplicedAnalysis<T> getAnalysis()
    {
        return analysis;
    }

    void reset()
    {
        // do nothing
    }

    public void run()
    {
        splicer.stop();
    }
}

/**
 * Push new objects into the {@link Strand} associated with this object.
 */
class PrioTail<T>
    implements StrandTail<T>
{
    /** Log message handler */
    private static final Logger LOG = Logger.getLogger(PrioTail.class);

    private SortInput<T> sin;

    PrioTail(SortInput<T> sin)
    {
        this.sin = sin;
    }

    /**
     * Closes the associated {@link Strand}. The Splicer will continue to
     * handle those objects already pushed into this object but will not
     * acccept any more. Any further attempt to push in a object into this
     * object will cause a ClosedStrandException to be thrown.
     * <p/>
     * If the associated Strand is already closed then invoking this method
     * will have no effect.
     */
    public void close()
    {
        if (!sin.isStopped()) {
            try {
                sin.stop();
            } catch (SorterException se) {
                LOG.error("Cannot close " + sin, se);
            }
        }
    }

    /**
     * Returns true if the {@link #close()} method has been called on this
     * object.
     *
     * @return true if this object is closed.
     */
    public boolean isClosed()
    {
        return sin.isStopped();
    }

    /**
     * Returns the object at the "head" of this object without
     * removing it from this object. If this object is currently empty this
     * method will return <code>null</code>.
     *
     * @return the object at the "head" of this object.
     */
    public T head()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Adds the specified List of objects onto the tail of
     * the associated {@link Strand}. The List of objects must be ordered
     * such that all object, <code>s</code>, - with the exception of the
     * end-of-stream object - that are lower in the
     * list than object <code>t</code> are also less or equal to
     * <code>t</code>,
     * <p/>
     * <pre>
     *    0 > s.compareSpliceable(t)
     * </pre>
     * <p/>
     * otherwise an IllegalArgumentException will be thrown.
     * <p/>
     * Moreover the first object in the List must be greater or equal to
     * the last object - again, with the exception of the
     * end-of-stream object - pushed into this object
     * otherwise an IllegalArgumentException will be thrown.
     *
     * @param spliceables the List of objects to be added.
     * @return this object, so that pushes can be chained.
     * @throws OrderingException if the specified List of objects is not
     * properly ordered or is mis-ordered with respect to objects already
     * pushed into this object
     * @throws ClosedStrandException is the associated Strand has been closed.
     */
    public StrandTail<T> push(List<T> spliceables)
        throws OrderingException, ClosedStrandException
    {
        for (T t : spliceables) {
            push(t);
        }
        return this;
    }

    /**
     * Adds the specified object onto the tail of the associated
     * {@link Strand}. The specified object must be greater or equal to all
     * other objects, <code>s</code>, - with the exception of the end-of-stream
     * object - that have been previously pushed into this object,
     * <p/>
     * <pre>
     *    0 > s.compareSpliceable(spliceable)
     * </pre>
     * <p/>
     * otherwise an IllegalArgumentException will be thrown.
     * <p/>
     * Any objects pushed into the Strand after an end-of-stream
     * object will not appear in the associated Strand until the Splicer has
     * "stopped".
     *
     * @param spliceable the object to be added.
     * @return this object, so that pushes can be chained.
     * @throws OrderingException if the specified object is mis-ordered
     * with respect to objects already pushed into this object
     * @throws ClosedStrandException is the associated Strand has been closed.
     */
    public StrandTail<T> push(T spliceable)
        throws OrderingException, ClosedStrandException
    {
        if (!sin.isStopped()) {
            try {
                sin.put(spliceable);
            } catch (SorterException se) {
                throw new ClosedStrandException("Input " + sin +
                                                " has been closed");
            } catch (OutOfMemoryError oom) {
                throw new ClosedStrandException("Out of memory for " + sin);
            }
        }

        return this;
    }

    /**
     * Returns the number of objects pushed into this object
     * that have yet to be woven into the resultant rope.
     *
     * @return the number of objects yet to be woven.
     */
    public int size()
    {
        return sin.getQueueSize();
    }
}
