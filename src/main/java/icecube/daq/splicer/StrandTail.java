/*
 * interface: StrandTail
 *
 * Version $Id: StrandTail.java 15570 2015-06-12 16:19:32Z dglo $
 *
 * Date: July 30 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

import java.util.List;

/**
 * This interface is used by clients of the Splicer to push new objects
 * into the {@link Strand} associated with this object.
 *
 * @author patton
 * @version $Id: StrandTail.java 15570 2015-06-12 16:19:32Z dglo $
 * @since v3.0
 */
public interface StrandTail<T>
{
    /**
     * Closes the associated {@link Strand}. The Splicer will continue to
     * handle those objects already pushed into this object but will not
     * acccept any more. Any further attempt to push in a object into this
     * object will cause a ClosedStrandException to be thrown.
     * <p>
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
     * Returns the object at the "head" of this object without
     * removing it from this object. If this object is currently empty this
     * method will return <code>null</code>.
     *
     * @return the object at the "head" of this object.
     */
    T head();

    /**
     * Adds the specified List of objects onto the tail of
     * the associated {@link Strand}. The List of objects must be ordered
     * such that all object, <code>s</code>, - with the exception of the
     * end-of-stream object - that are lower in the
     * list than object <code>t</code> are also less or equal to
     * <code>t</code>,
     * <p>
     * <pre>
     *    0 &gt; s.compareSpliceable(t)
     * </pre>
     * <p>
     * otherwise an IllegalArgumentException will be thrown.
     * <p>
     * Moreover the first object in the List must be greater or equal to
     * the last object - again, with the exception of the
     * end-of-stream object - pushed into this object
     * otherwise an IllegalArgumentException will be thrown.
     *
     * @param spliceables the List of objects to be added.
     * @return this object, so that pushes can be chained.
     * @throws OrderingException if the specified List of objects is not
     * properly ordered or is mis-ordered with respect to objects already
     * pushed into this object
     * @throws ClosedStrandException is the associated Strand has been closed.
     */
    StrandTail<T> push(List<T> spliceables)
        throws OrderingException, ClosedStrandException;

    /**
     * Adds the specified object onto the tail of the associated
     * {@link Strand}. The specified object must be greater or equal to all
     * other objects, <code>s</code>, - with the exception of the end-of-stream
     * object - that have been previously pushed into this object,
     * <p>
     * <pre>
     *    0 &gt; s.compareSpliceable(spliceable)
     * </pre>
     * <p>
     * otherwise an IllegalArgumentException will be thrown.
     * <p>
     * Any objects pushed into the Strand after an end-of-stream
     * object will not appear in the associated Strand until the Splicer has
     * "stopped".
     *
     * @param spliceable the object to be added.
     * @return this object, so that pushes can be chained.
     * @throws OrderingException if the specified object is mis-ordered
     * with respect to objects already pushed into this object
     * @throws ClosedStrandException is the assoicated Strand has been closed.
     */
    StrandTail<T> push(T spliceable)
        throws OrderingException, ClosedStrandException;

    /**
     * Returns the number of objects pushed into this object
     * that have yet to be woven into the resultant rope.
     *
     * @return the number of objects yet to be woven.
     */
    int size();
}
