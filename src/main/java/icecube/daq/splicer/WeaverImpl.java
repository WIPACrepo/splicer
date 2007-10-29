/*
 * class: WeaverImpl
 *
 * Version $Id: WeaverImpl.java 2205 2007-10-29 20:44:05Z dglo $
 *
 * Date: July 29 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class implements the {@link Weaver} interface using a two pass
 * algorithm The first pass finds the "greatest" Spliceable common to the know
 * set of Strands, while the second pass collects all of the Spliceables less
 * of equal to that common Spliceable.
 *
 * @author patton
 * @version $Id: WeaverImpl.java 2205 2007-10-29 20:44:05Z dglo $
 */
class WeaverImpl
        implements Weaver
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    /**
     * Local definition of the maximum possible Spliceable.
     */
    private static final Spliceable LAST_POSSIBLE_SPLICEABLE =
            Splicer.LAST_POSSIBLE_SPLICEABLE;

    // private static member data

    // private instance member data

    /**
     * The set of known Strands.
     */
    private final List strands = new ArrayList(1);

    /**
     * Object used to compare Spliceables.
     */
    private final SpliceableComparator cmp = new SpliceableComparator();

    // constructors

    /**
     * Create an instance of this class.
     */
    WeaverImpl()
    {
    }

    // instance member method (alphabetic)

    public void addStrand(Strand strand)
    {
        // If the Strand is null or already in the known set do nothing.
        if (null == strand ||
            strands.contains(strand)) {
            return;
        }
        strands.add(strand);
    }

    /**
     * Finds the greatest Spliceable that is common to all Strands in the know
     * set. If the set of known Strands is empty, or any of the known Strands
     * do not contain at least one Spliceable, then <code>null</code> it
     * returned.
     *
     * @return the greatest common Spliceable, or <code>null</code> if it does
     *         not exist.
     */
    private Spliceable findGreatestCommonSpliceable()
    {
        // If this object does not manage any Strands return null.
        if (0 == strands.size()) {
            return null;
        }

        // Iterate over all known Strands to find the greatest common
        // Spliceable.
        Spliceable greatestSpliceable = LAST_POSSIBLE_SPLICEABLE;
        final Iterator iterator = strands.iterator();
        while (null != greatestSpliceable &&
               iterator.hasNext()) {
            final Strand strand = (Strand) iterator.next();

            // If the current Strand is empty then the result is null.
            if (0 == strand.size()) {
                return null;
            }

            // Otherwise is the current Strand does not contain the current
            // candidate, change candidate to be the tail of this Strand.
            final Spliceable tail = strand.tail();
            if ((0 != LAST_POSSIBLE_SPLICEABLE.compareSpliceable(tail)) &&
                (0 < greatestSpliceable.compareSpliceable(tail))) {
                greatestSpliceable = tail;
            }
        }
        return greatestSpliceable;
    }

    /**
     * Gathers into the returned List all Spliceables that a less or equal to
     * the specified Spliceable. The specified spliceable must not be
     * <code>null</code> or the <code>LAST_POSSIBLE_SPICEABLE</code> object.
     *
     * @param limit the largest Spliceable to be included in the output List.
     * @param gatheredObjects the List in which to place the gathered objects.
     * @return The ordered List of Spliceables less or equal to the limit.
     */
    private List gatherNewObjects(Spliceable limit,
                                  List gatheredObjects)
    {
        // Prepare List for new Spliceables.
        gatheredObjects.clear();

        // Iterate over all known Strands adding all Spliceables that a less or
        // equal to the greatest common Spliceable.
        final Iterator iterator = strands.iterator();
        while (iterator.hasNext()) {
            final Strand strand = (Strand) iterator.next();
            Spliceable head = strand.head();
            while ((null != head) &&
                   (0 >= head.compareSpliceable(limit))) {
                gatheredObjects.add(strand.pull());
                head = strand.head();
            }
        }

        // Sort the gathered Spliceables.
        Collections.sort(gatheredObjects, cmp);

        return gatheredObjects;
    }

    public int getStrandCount()
    {
        return strands.size();
    }

    public void removeStrand(Strand strand)
    {
        // If the Strand is null do nothing
        if (null == strand) {
            return;
        }
        strands.remove(strand);
    }

    public void weave(List wovenRope)
    {
        final Spliceable greatestCommonSpliceable =
                findGreatestCommonSpliceable();

        // If no new valid Spliceables have been found the do nothing.
        if (null == greatestCommonSpliceable ||
            LAST_POSSIBLE_SPLICEABLE.equals(greatestCommonSpliceable)) {
            return;
        }

        // Gather the new objects
        gatherNewObjects(greatestCommonSpliceable,
                         wovenRope);
    }

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}