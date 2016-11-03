package icecube.daq.priority;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

public class SubSorter<T>
    implements Runnable
{
    /** Log message handler */
    private static final Logger LOG = Logger.getLogger(SubSorter.class);

    private Sorter parent;
    private int id;
    private T eos;

    private ConcurrentLinkedQueue<T> outputQueue;
    private volatile boolean active;

    private PriorityQueue<InputDataPair<T>> prioQueue;
    private AtomicBoolean running = new AtomicBoolean(false);

    private ArrayList<SortInput<T>> inputList;
    private int numStoppedInputs;

    private long outputCount;

    private Thread thread;

    /**
     * Create a subsorter
     *
     * @param parent main Sorter
     * @param id subsorter ID (used for error messages)
     * @param expectedInputs expected number of sources
     * @param comp comparison function used to sort data
     * @param marker end-of-stream marker
     */
    public SubSorter(Sorter parent, int id, int expectedInputs,
                     Comparator<DataWrapper<T>> comp, T marker)
    {
        this.parent = parent;
        this.id = id;
        this.eos = marker;

        outputQueue = new ConcurrentLinkedQueue<T>();
        active = true;

        prioQueue =
            new PriorityQueue<InputDataPair<T>>(expectedInputs, comp);

        inputList = new ArrayList<SortInput<T>>(expectedInputs);
    }

    /**
     * Get number of objects sorted by the subsorter
     *
     * @return number of outputs
     */
    public long getOutputCount()
    {
        return outputCount;
    }

    /**
     * Get number of objects ready to be sorted
     *
     * @return number of queued objects
     */
    public int getQueueSize()
    {
        return outputQueue.size();
    }

    /**
     * Is this subsorter still reading data?
     *
     * @return <tt>false</tt> if this subsorter has consumed all input data
     */
    public boolean isActiveQueue()
    {
        return active;
    }

    /**
     * Is there data waiting to be sorted?
     *
     * @return <tt>true</tt> if this subsorter has produced some data
     */
    public boolean isEmptyQueue()
    {
        return outputQueue.isEmpty();
    }

    /**
     * Is the sorter thread active?
     * @return <tt>true</tt> if the thread is alive
     */
    boolean isAlive()
    {
        return thread != null && thread.isAlive();
    }

    /**
     * Wait for this subsorter's thread to finish running
     * @param millis milliseconds to wait
     *
     * @throws InterruptedException if we were awoken before the thread ended
     */
    public void join(long millis)
        throws InterruptedException
    {
        if (thread == null) {
            throw new Error("SubSorter #" + id +
                            " thread has not been started");
        }

        thread.join(millis);
    }

    /**
     * Get the next available data object
     *
     * @return data
     */
    public T pollQueue()
    {
        return outputQueue.poll();
    }

    /**
     * Add an input source
     *
     * @param input new source
     *
     * @throws SorterException if subsorter has been started
     */
    public void register(SortInput<T> input)
        throws SorterException
    {
        if (running.get()) {
            // if running you can't register any more
            // inputs
            throw new SorterException("Sorter is running;" +
                                      " cannot register new inputs");
        }

        inputList.add(input);
    }

    /**
     * Start the subsorter thread
     */
    public void start()
    {
        thread = new Thread(this, String.format("SubSorter-%d", id));
        thread.start();
    }

    /**
     * Has this subsorter been stopped?
     *
     * @return <tt>true</tt> if the thread has been stopped
     */
    public boolean isStopped()
    {
        return !running.get();
    }

    /**
     * Stop the thread
     */
    public void stop()
    {
        running.set(false);
    }

    /**
     * Thread which sorts the incoming data
     */
    public void run()
    {
        // we want to put a cap on the time it takes to push
        // the data out of the sorter, but we don't want
        // all subsorters to try to push data out at the same time

        for (SortInput<T> sin : inputList) {
            T e = sin.get();

            // every input will get AT LEAST an eos marker
            prioQueue.add(new InputDataPair<T>(sin, e));
        }

        running.set(true);

        while (running.get()) {
            int chunkCount = 0;
            while (running.get()) {
                InputDataPair<T> minElement = prioQueue.poll();
                if (minElement == null) {
                    // the priority queue is empty
                    // we've completed this queue, all inputs should
                    // be stopped at this point
                    running.set(false);

                    LOG.info(String.format("Sub sorter#%d finished sending %d",
                                           id, outputCount));
                    break;
                }

                try {
                    outputQueue.add(minElement.data());
                } catch (OutOfMemoryError oom) {
                    // out of memory, give up
                    running.set(false);
                    try {
                        LOG.error("Sub sorter #" + id +
                                  " ran out of memory", oom);
                    } catch (Throwable thr) {
                        // don't care if we didn't log
                    }
                    break;
                }

                // increase counters
                outputCount++;
                chunkCount++;

                // get next piece of data
                SortInput<T> srcInput = minElement.input();
                T newPt = srcInput.get();

                // if input source saw end of stream
                if (srcInput.isStopped()) {
                    // remember that another source has stopped
                    numStoppedInputs++;
                    if (numStoppedInputs == inputList.size()) {
                        running.set(false);
                    }
                    break;
                }

                prioQueue.add(new InputDataPair<T>(srcInput, newPt));

                // if we've got enough data queued up,
                // break so the main sorter does its thing
                if (chunkCount >= parent.getChunkSize()) {
                    break;
                }
            }

            // push the temp output buffer onwards
            parent.check(false);
        }

        // push an end-of-subsorter marker up to the sorter
        // all SortInput objects should generate the same end-of-stream marker
        //  so just use the first one
        outputCount++;
        outputQueue.add(eos);

        // this sorter is no longer active
        active = false;

        // one last check
        parent.check(true);
    }

    /**
     * Debugging string
     *
     * @return string
     */
    public String toString()
    {
        String activeStr;
        if (!active) {
            activeStr = "";
        } else {
            activeStr = " ACTIVE";
        }

        String stopStr;
        if (running.get()) {
            stopStr = String.format(" (%d of %d stopped)", numStoppedInputs,
                                    inputList.size());
        } else {
            stopStr = " STOPPED";
        }

        return String.format("SS#%d: %d queued, %d sent", id,
                             outputQueue.size(), outputCount) +
            activeStr + stopStr;
    }

    class InputDataPair<T>
        implements DataWrapper<T>
    {
        private SortInput<T> sortInput;
        private T data;

        InputDataPair(SortInput<T> sortInput, T data)
        {
            this.sortInput = sortInput;
            this.data = data;
        }

        public T data()
        {
            return data;
        }

        SortInput<T> input()
        {
            return sortInput;
        }
    }
}
