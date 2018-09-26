package icecube.daq.priority;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

/**
 * Input source for Sorter.
 *
 * Input data should be "sorted" already
 */
public class SortInput<T>
{
    /** Log message handler */
    private static final Logger LOG = Logger.getLogger(Sorter.class);

    private static final int CAPACITY = 500000;

    private String name;
    private T eos;

    // keep track of the number of points coming into this sorter input object
    private long inputCount;

    private BlockingQueue<T> inputQueue;

    private AtomicBoolean addedLast = new AtomicBoolean(false);
    private AtomicBoolean stopped = new AtomicBoolean(false);

    /**
     * Create an input source
     *
     * @param name source name (used in error messages)
     * @param marker end-of-stream marker
     */
    public SortInput(String name, T marker)
    {
        this.name = name;
        this.eos = marker;

        if (marker == null) {
            throw new Error("End-of-stream marker cannot be null");
        }

        inputQueue = new LinkedBlockingQueue<T>(CAPACITY);
    }

    /**
     * Get the next data object
     *
     * @return next data object
     */
    public T get()
    {
        T data = null;
        try {
            data = inputQueue.take();
        } catch (InterruptedException ex) {
            LOG.error(name + " could not get next value", ex);
        }

        if (!stopped.get() && data == eos) {
            stopped.set(true);
        }

        return data;
    }

    /**
     * Return the number of objects 'put' into this sorter input queue
     *
     * @return number of objects 'put' into the queue
     */
    public long getInputCount()
    {
        return inputCount;
    }

    /**
     * Get the internal queue size
     *
     * @return queue size
     */
    public int getQueueSize()
    {
        return inputQueue.size();
    }

    /**
     * Has this source reached the end of its input?
     *
     * @return <tt>true</tt> if this source has reached the end of stream
     */
    public boolean isStopped()
    {
        return stopped.get();
    }

    /**
     * Push the next object onto the input queue
     *
     * @param data data object
     *
     * @throws SorterException if stopped or if data cannot be enqueued
     */
    public void put(T data)
        throws SorterException
    {
        // ignore null inputs (signify timeout errors)
        if (data == null) {
            return;
        }

        if (stopped.get()) {
            final String errMsg =
                String.format("Input source %s is stopped", name);
            throw new SorterException(errMsg);
        }

        try {
            inputQueue.put(data);
            inputCount++;
        } catch (InterruptedException ex) {
            throw new SorterException("Could not put next value", ex);
        }
    }

    /**
     * Push the end-of-stream marker onto the queue
     *
     * @throws SorterException if data cannot be enqueued
     */
    public void putLast()
        throws SorterException
    {
        if (!stopped.get()) {
            if (addedLast.get()) {
                LOG.error("Ignoring multiple attempts to add last marker to " +
                          name);
            } else {
                addedLast.set(true);
                put(eos);
            }
        }
    }

    /**
     * Stop the input source
     *
     * @throws SorterException if data cannot be enqueued
     */
    public void stop()
        throws SorterException
    {
        if (stopped.get()) {
            LOG.error("SortInput " + name + " is already stopped");
        } else {
            putLast();
        }
    }

    /**
     * Debugging string
     *
     * @return debugging string
     */
    @Override
    public String toString()
    {
        return String.format("%s[%d->%d]", name, inputQueue.size(),
                             inputCount);
    }
}
