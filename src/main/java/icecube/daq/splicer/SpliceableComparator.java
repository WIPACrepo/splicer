package icecube.daq.splicer;

import java.util.Comparator;

/**
 * Compare two Spliceables.
 */
public class SpliceableComparator
    implements Comparator<Spliceable>
{
    /** Spliceable which marks the end of the data */
    private Spliceable lastSpliceable;

    /**
     * Create comparator
     *
     * @param lastSpliceable object which marks the end of the data
     */
    public SpliceableComparator(Spliceable lastSpliceable)
    {
        this.lastSpliceable = lastSpliceable;
    }

    /**
     * Compare two Spliceables.
     *
     * @param s1 first Spliceable
     * @param s2 first Spliceable
     *
     * @return the usual values
     */
    @Override
    public int compare(Spliceable s1, Spliceable s2)
    {
        if (s1 == lastSpliceable) {
            if (s2 == lastSpliceable) {
                return 0;
            }

            return 1;
        } else if (s2 == lastSpliceable) {
            return -1;
        } else if (s1 == null) {
            if (s2 == null) {
                return 0;
            }

            return 1;
        } else if (s2 == null) {
            return -1;
        }

        return s1.compareSpliceable(s2);
    }
}
