package icecube.daq.priority;

import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

public class Sorter<T>
{
    /** Log message handler */
    private static final Logger LOG = Logger.getLogger(Sorter.class);

    public static final int DEFAULT_CHUNK_SIZE = 1500;
    public static final int DEFAULT_NUM_THREADS = 3;

    /** Sorter name to use in error messages */
    private String name;

    /** Generic end-of-stream marker */
    private T eos;

    private ReentrantLock lock = new ReentrantLock();
    private PriorityQueue<SourceDataPair<T>> prioQueue;
    private boolean initialized;

    private int expectedInputs;
    private int chunkSize;
    private DataComparator comp;
    private SubSorter<T>[] subsorters;
    private DataConsumer<T> consumer;
    private T previousData;

    private boolean running;
    private Object runLock = new Object();

    private int totalRegistered;

    private long checked;
    private long processCalls;

    /**
     * Create a sorter
     *
     * @param name name (used in error messages)
     * @param maxInputs maximum number of sources which will ever be added
     * @param tcomp comparison function used to sort data
     * @param consumer data consumer
     * @param eos end-of-stream marker
     *
     * @throws SorterException if there is a problem with the input params
     */
    public Sorter(String name, int maxInputs, Comparator<T> tcomp,
                  DataConsumer<T> consumer, T eos)
        throws SorterException
    {
        this(name, maxInputs, tcomp, consumer, eos, DEFAULT_NUM_THREADS,
             DEFAULT_CHUNK_SIZE);
    }

    /**
     * Create a sorter
     *
     * @param name name (used in error messages)
     * @param maxInputs maximum number of sources which will ever be added
     * @param tcomp comparison function used to sort data
     * @param consumer data consumer
     * @param eos end-of-stream marker
     * @param numThreads maximum number of subsorter threads allowed
     * @param chunkSize size of data queue which triggers the main Sorter
     *
     * @throws SorterException if there is a problem with the input params
     */
    public Sorter(String name, int maxInputs, Comparator<T> tcomp,
                  DataConsumer<T> consumer, T eos, int numThreads,
                  int chunkSize)
        throws SorterException
    {
        if (maxInputs == 0) {
            throw new SorterException("Maximum number of " + name +
                                      " inputs cannot be 0");
        } else if (numThreads == 0) {
            throw new SorterException("Maximum number of " + name +
                                      " threads cannot be 0");
        } else if (eos == null) {
            throw new SorterException(name + " end-of-stream marker" +
                                      " has not been set");
        }

        this.name = name;
        this.comp = new DataComparator<T>(tcomp);
        this.consumer = consumer;
        this.eos = eos;
        this.chunkSize = chunkSize;

        expectedInputs = maxInputs;

        subsorters = new SubSorter[Math.min(maxInputs, numThreads)];

        prioQueue =
            new PriorityQueue<SourceDataPair<T>>(maxInputs / subsorters.length,
                                                 comp);
    }

    /**
     * If all subsorters have data, sort as much as possible
     *
     * @param finalCheck <tt>true</tt> if all data should be flushed
     */
    public void check(boolean finalCheck)
    {
        if (finalCheck) {
            lock.lock();
        } else if (!lock.tryLock()) {
            // if lock is held by another thread, don't wait
            return;
        }

        checked++;

        try {
            checkInternal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sort as much data as possible
     */
    private void checkInternal()
    {
        // check to see if all subsorters have input.
        for (SubSorter<T> ss : subsorters) {
            if (ss == null) {
                // initial subsorters array was not fully populated
                continue;
            }

            // All ACTIVE data queues must have some data
            // to continue
            if (ss.isEmptyQueue() && ss.isActiveQueue()) {
                // no data for some subsorter, no work to do
                return;
            }
        }

        // if we get here then there is data from every subsorter
        processCalls++;
        process();
    }

    /**
     * Get the number of objects required for a sort to be initiated
     *
     * @return number of objects required for a sort to be initiated
     */
    public int getChunkSize()
    {
        return chunkSize;
    }

    /**
     * Get sorter name
     *
     * @return name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get number of sorter checks
     *
     * @return number of checks
     */
    public long getNumChecked()
    {
        return checked;
    }

    /**
     * Get number of objects sorted by the sorter
     *
     * @return number of outputs
     */
    public long getNumOutput()
    {
        long total = 0;
        for (SubSorter<T> ss : subsorters) {
            if (ss != null) {
                total += ss.getOutputCount();
            }
        }
        return total;
    }

    /**
     * Get number of calls to sorter.process()
     *
     * @return number of calls to process()
     */
    public long getNumProcessCalls()
    {
        return processCalls;
    }

    /**
     * Get number of objects waiting to be sorted
     *
     * @return number of objects in the queue
     */
    public int getNumQueued()
    {
        int total = 0;
        for (SubSorter<T> ss : subsorters) {
            if (ss != null) {
                total += ss.getQueueSize();
            }
        }
        return total + prioQueue.size();
    }

    /**
     * Get the number of subsorters allocated by this sorter
     *
     * @return number of subsorters
     */
    public int getNumSubsorters()
    {
        return subsorters.length;
    }

    /**
     * Is the sorter thread running?
     *
     * @return <tt>true</tt> if the thread is running
     */
    public boolean isRunning()
    {
        return running;
    }

    /**
     * Sort data from all subsorters
     */
    private void process()
    {
        if (!initialized) {
            // put a point from each input on the priority queue
            for (SubSorter<T> ss : subsorters) {
                if (ss == null) {
                    // ignore missing subsorters
                    continue;
                }

                // we are guaranteed that every subsorter has supplied
                // at least one point before we get here
                T buf = ss.pollQueue();
                prioQueue.add(new SourceDataPair<T>(ss, buf));
            }
            initialized = true;
        }

        // continue pulling points out of the the various queues until one is
        // empty
        // we are guaranteed that one will be empty as the thread running this
        // method IS the thread of a subsorter
        while (true) {
            SourceDataPair<T> data = prioQueue.poll();

            if (data == null) {
                // the priority queue is empty and we are done
                try {
                    consumer.endOfStream(0L);
                } catch (IOException ex) {
                    LOG.error(name + " could not send end-of-stream", ex);
                }
                break;
            }

            if (!data.subsorter().isEmptyQueue()) {
                if (previousData != null &&
                    comp.compareData(previousData, data.data()) > 0)
                {
                    // this should never happen
                    LOG.error("Out-of-order data!  (prev=" + previousData +
                              ", this=" + data);
                } else {
                    // hand data to consumer
                    try {
                        consumer.consume(data.data());
                    } catch (IOException ex) {
                        LOG.error(name + " could not send " +
                                  data.subsorter() + " data", ex);
                    } catch (Throwable thr) {
                        LOG.error(name + " caught unexpected exception from " +
                                  data.subsorter(), thr);
                    }

                    previousData = data.data();
                }

                // get next element
                T newPt = data.subsorter().pollQueue();
                if (newPt == null) {
                    // this should never happen
                    LOG.error(name + " newPt is null,  " + data.subsorter() +
                              " isEmpty: " + data.subsorter().isEmptyQueue() +
                              " size: " + data.subsorter().getQueueSize());
                }

                // stuff the new data into the wrapper and requeue it
                data.setData(newPt);
                prioQueue.add(data);
            } else {
                // found an empty queue

                // if element is not an end-of-stream...
                if (data.data() != eos) {
                    // ...put it's min element back on the queue
                    // and exit the loop
                    prioQueue.add(data);
                    break;
                }

                // complain if the queue is still active
                if (data.subsorter().isActiveQueue()) {
                    LOG.error(name + " got EOS from active SS " +
                              data.subsorter());
                }

                // this subsorter will be ignored from now on
                // (EOS will not be sent to consumer)
            }
        }
    }

    /**
     * Register an input source
     *
     * @param name source name
     *
     * @return new input queue
     *
     * @throws SorterException if the sorter has been started
     */
    public SortInput<T> register(String name)
        throws SorterException
    {
        SubSorter<T> subsort;
        synchronized (runLock) {
            if (running) {
                // if running you can't register any more
                // inputs
                final String errMsg = "Cannot register more inputs after" +
                    " starting the " + name + " sorter";
                throw new SorterException(errMsg);
            }

            int num = totalRegistered++ % subsorters.length;

            // need to map this mbid to a subsorter
            // if there is no num entry in this map
            // then create a new subsorter
            if (subsorters[num] == null) {
                subsort = new SubSorter<T>(this, num, expectedInputs, comp,
                                           eos);
                subsorters[num] = subsort;
            }

            subsort = subsorters[num];
        }

        SortInput<T> sin = new SortInput<T>(name, eos);
        subsort.register(sin);

        return sin;
    }

    /**
     * Set the number of objects required for a sort to be initiated
     *
     * @param val number of objects required for a sort to be initiated
     */
    public void setChunkSize(int val)
    {
        chunkSize = val;
    }

    /**
     * Start sorting
     */
    public void start()
    {
        synchronized (runLock) {
            running = true;
        }

        for (SubSorter<T> ss : subsorters) {
            if (ss != null) {
                ss.start();
            }
        }
    }

    /**
     * Debugging string
     *
     * @return name
     */
    public String toString()
    {
        return name;
    }

    /**
     * Wait for all subsorter threads to stop
     * @param millis milliseconds to wait
     */
    public void waitForStop(long millis)
        throws SorterException
    {
        for (SubSorter<T> ss : subsorters) {
            if (ss != null) {
                try {
                    ss.join(millis);
                } catch (InterruptedException ex) {
                    LOG.error("Interrupted join for " + ss, ex);
                }
            }
        }
        for (SubSorter<T> ss : subsorters) {
            if (ss != null && ss.isAlive()) {
                throw new SorterException(ss.toString() + " did not stop");
            }
        }
    }

    /**
     * Wrapper which connects a data object with the subsorter it came from
     */
    class SourceDataPair<T>
        implements DataWrapper<T>
    {
        private SubSorter<T> ss;
        private T data;

        SourceDataPair(SubSorter<T> ss, T data)
        {
            this.ss = ss;
            this.data = data;
        }

        public T data()
        {
            return data;
        }

        void setData(T newData)
        {
            this.data = newData;
        }

        SubSorter<T> subsorter()
        {
            return ss;
        }
    }
}
