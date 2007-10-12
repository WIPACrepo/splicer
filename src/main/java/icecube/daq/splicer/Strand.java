/*
 * interface: Strand
 *
 * Version $Id: Strand.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: August 4 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.splicer;


/**
 * This interface is used by the {@link Weaver} to access the {@link
 * Spliceable} objects it needs to weave into an ordered List of Spliceables.
 * <p/>
 * The Spliceables returned by this object are ordered, such that the
 * Spliceable, <code>h</code>, returned by the {@link #head()} or {@link
 * #pull()} method will always be less than or equal to any other Spliceable,
 * <code>s</code>, returned by either of these two calls.
 * <p/>
 * <pre>
 *    0 >= h.compareTo(s);
 * </pre>
 * <p/>
 * The Spliceable returned by the {@link #tail()} method will be greater than
 * or equal to all other Spliceables contained in this object at the time the
 * <code>tail</code> is invoked.
 *
 * @author patton
 * @version $Id: Strand.java 2125 2007-10-12 18:27:05Z ksb $
 * @since v2.0
 */
public interface Strand
{
    // instance member method (alphabetic)

    /**
     * Returns the {@link Spliceable} at the "head" of this object without
     * removing it from this object. If this object is currently empty this
     * method will return <code>null</code>.
     *
     * @return the Spliceable at the "head" of this object.
     */
    Spliceable head();

    /**
     * Returns true if the size of this object is zero.
     *
     * @return true if the size of this object is zero.
     */
    boolean isEmpty();

    /**
     * Returns the {@link Spliceable} at the "head" of this object and removes
     * it from this object. If this object is currently empty this method will
     * return <code>null</code>.
     *
     * @return the Spliceable at the "head" of this object.
     */
    Spliceable pull();

    /**
     * Returns the number of {@link Spliceable} currently in this object.
     *
     * @return the number of Spliceables currently in this object.
     */
    int size();

    /**
     * Returns the {@link Spliceable} at the "tail" of this object without
     * removing it from this object. If this object is currently empty this
     * method will return <code>null</code>.
     *
     * @return the Spliceable at the "tail" of this object.
     */
    Spliceable tail();
}
