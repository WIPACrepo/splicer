/*
 * interface: Weaver
 *
 * Version $Id: Weaver.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: July 29 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

import java.util.List;


/**
 * This interface is used by the {@link Splicer} to weave the {@link
 * Spliceable}s contained in one or more {@link Strand}s into a single ordered
 * collection of Spliceables.
 *
 * @author patton
 * @version $Id: Weaver.java 2125 2007-10-12 18:27:05Z ksb $
 * @since v3.0
 */
public interface Weaver
{
    // instance member method (alphabetic)

    /**
     * Adds a {@link Strand} to this object's set of known Strands. If the
     * Strand is <code>null</code> or is already in the known set this method
     * will have no effect.
     *
     * @param strand the Strand to be added.
     */
    void addStrand(Strand strand);

    /**
     * Returns the number of Strands in the set of known Strands.
     *
     * @return the number of Strands in the set of known Strands.
     */
    int getStrandCount();

    /**
     * Removes a {@link Strand} from this object's set of known Strands. If the
     * Strand is <code>null</code> or is not in the known set this method will
     * have no effect.
     *
     * @param strand the Strand to be removed.
     */
    void removeStrand(Strand strand);

    /**
     * Fills the provided List with an ordered collection of {@link
     * Spliceable}s, taken from the set of known {@link Strand}s. The last
     * Spliceable, <code>l</code> in the List must be less than or equal to all
     * future Spliceables, <code>s</code>.
     * <p/>
     * <code> 0 >= l.compareTo(s); </code>
     * <p/>
     * When this method has finished at least one of the Strands must be
     * "empty".
     *
     * @param rope an empty list to be filled by this method.
     */
    void weave(List rope);
}
