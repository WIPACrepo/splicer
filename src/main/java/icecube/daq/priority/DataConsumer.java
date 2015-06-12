package icecube.daq.priority;

import java.io.IOException;

/**
 * Consume data.
 */
public interface DataConsumer<T>
{
    /**
     * Consume a piece of data.
     *
     * @param data data
     *
     * @throws IOException if the data could not be consumed
     */
    void consume(T data)
        throws IOException;

    /**
     * There will be no more data.
     *
     *
     * @param token ignored(?)
     *
     * @throws IOException if the end-of-stream could not be consumed
     */
    void endOfStream(long token)
        throws IOException;
}
