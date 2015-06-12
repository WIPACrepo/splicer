package icecube.daq.priority;

/**
 * A piece of data is usually wrapped to group it with its source
 */
public interface DataWrapper<T>
{
    T data();
}
