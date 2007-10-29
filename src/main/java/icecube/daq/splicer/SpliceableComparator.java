package icecube.daq.splicer;

import java.util.Comparator;

/**
 * Compare two spliceables using their compareSpliceable() method.
 */
public class SpliceableComparator
    implements Comparator
{
    public int compare(Object o1, Object o2)
    {
        return ((Spliceable) o1).compareSpliceable((Spliceable) o2);
    }
}