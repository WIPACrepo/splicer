package icecube.daq.priority;

import java.util.Comparator;

/**
 * Compare wrapped data objects
 */
class DataComparator<T>
    implements Comparator<DataWrapper<T>>
{
    private Comparator<T> comp;

    /**
     * Create a wrapper data comparator
     *
     * @param comp data comparator
     *
     * @throws SorterException if comparator is null
     */
    DataComparator(Comparator<T> comp)
        throws SorterException
    {
        if (comp == null) {
            throw new SorterException("Comparator cannot be null");
        }

        this.comp = comp;
    }

    /**
     * Compare raw data
     *
     * @param a first object
     * @param b second object
     */
    public int compareData(T a, T b)
    {
        return comp.compare(a, b);
    }

    /**
     * Compare wrapped data
     *
     * @param a first wrapper
     * @param b second wrapper
     */
    @Override
    public int compare(DataWrapper<T> a, DataWrapper<T> b)
    {
        return comp.compare(a.data(), b.data());
    }
}
