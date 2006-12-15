/*
 * interface: StrandTail
 *
 * Version $Id: StrandTail.java,v 1.8 2006/02/04 21:00:52 patton Exp $
 *
 * Date: July 30 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

import java.util.List;

/**
 * This interface is used by clients of the Splicer to push new Spliceables
 * into the {@link Strand} associated with this object.
 *
 * @author patton
 * @version $Id: StrandTail.java,v 1.8 2006/02/04 21:00:52 patton Exp $
 * @since v3.0
 */
public interface StrandTail
{
    // public static final member data

    /**
     * A {@link Spliceable} that, when pushed into this object, indicates that
     * no more Spliceables will be pushed into this object until the {@link
     * Splicer} has stopped. Unlike closing this object, when the Splicer
     * re-starts this object is expected to provide new Spliceables to be
     * woven.
     * <p/>
     * Pushing any Spliceable other than another LAST_POSSIBLE_SPLICEABLE will
     * cause a {@link ClosedStrandException} to be thrown. Pushing another
     * LAST_POSSIBLE_SPLICEABLE into this object will have no effect until
     * after the Splicer has stopped.
     */
    Spliceable LAST_POSSIBLE_SPLICEABLE = Splicer.LAST_POSSIBLE_SPLICEABLE;

    // instance member method (alphabetic)

    /**
     * Closes the associated {@link Strand}. The Splicer will continue to
     * handle those Spliceables already pushed into this object but will not
     * acccept any more. Any further attempt to push in a Spliceable into this
     * object will cause a ClosedStrandException to be thrown.
     * <p/>
     * If the associated Strand is already closed then invoking this method
     * will have no effect.
     */
    void close();

    /**
     * Returns true if the {@link #close()} method has been called on this
     * object.
     *
     * @return true if this object is closed.
     */
    boolean isClosed();

    /**
     * Returns the {@link Spliceable} at the "head" of this object without
     * removing it from this object. If this object is currently empty this
     * method will return <code>null</code>.
     *
     * @return the Spliceable at the "head" of this object.
     */
    Spliceable head();

    /**
     * Adds the specified List of {@link Spliceable} objects onto the tail of
     * the associated {@link Strand}. The List of Spliceables must be ordered
     * such that all Spliceable, <code>s</code>, - with the exception of the
     * {@link Splicer#LAST_POSSIBLE_SPLICEABLE} object - that are lower in the
     * list than Spliceable <code>t</code> are also less or equal to
     * <code>t</code>,
     * <p/>
     * <pre>
     *    0 > s.compareTo(t)
     * </pre>
     * <p/>
     * otherwise an IllegalArgumentException will be thrown.
     * <p/>
     * Moreover the first Spliceable in the List must be greater or equal to
     * the last Spliceable - again, with the exception of the
     * <code>LAST_POSSIBLE_SPLICEABLE</code> object - pushed into this object
     * otherwise an IllegalArgumentException will be thrown.
     *
     * @param spliceables the List of Spliceable objects to be added.
     * @return this object, so that pushes can be chained.
     * @throws OrderingException if the specified List of Spliceables is not
     * properly ordered or is mis-ordered with respect to Spliceables already
     * pushed into this object
     * @throws ClosedStrandException is the associated Strand has been closed.
     */
    StrandTail push(List spliceables)
            throws OrderingException,
                   ClosedStrandException;

    /**
     * Adds the specified {@link Spliceable} onto the tail of the associated
     * {@link Strand}. The specified Spliceable must be greater or equal to all
     * other Spliceables, <code>s</code>, - with the exception of the {@link
     * Splicer#LAST_POSSIBLE_SPLICEABLE} object - that have been previously
     * pushed into this object,
     * <p/>
     * <pre>
     *    0 > s.compareTo(spliceable)
     * </pre>
     * <p/>
     * otherwise an IllegalArgumentException will be thrown.
     * <p/>
     * Any Spliceables pushed into the Strand after a <code>LAST_POSSIBLE_SPLICEABLE</code>
     * object will not appear in the associated Strand until the Splicer has
     * "stopped".
     *
     * @param spliceable the Spliceable to be added.
     * @return this object, so that pushes can be chained.
     * @throws OrderingException if the specified Spliceable is mis-ordered
     * with respect to Spliceables already pushed into this object
     * @throws ClosedStrandException is the assoicated Strand has been closed.
     */
    StrandTail push(Spliceable spliceable)
            throws OrderingException,
                   ClosedStrandException;

    /**
     * Returns the number of {@link Spliceable} objects pushed into this object
     * that have yet to be woven into the resultant rope.
     *
     * @return the number of {@link Spliceable} objects yet to be woven.
     */
    int size();
}
