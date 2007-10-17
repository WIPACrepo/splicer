/*
 * interface: SpliceableFactory
 *
 * Version $Id: SpliceableFactory.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: September 4 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * This interface defines the methods used by the {@link Splicer} class to
 * manage the creation of {@link Spliceable} objects based on the contents of a
 * ByteBuffer.
 *
 * @author patton
 * @version $Id: SpliceableFactory.java 2125 2007-10-12 18:27:05Z ksb $
 */
public interface SpliceableFactory
{

    // instance member method (alphabetic)

    /**
     * Modifies the specified objects when their backing ByteBuffer is being
     * shifted. This also can be used to release any resources that are held by
     * any objects that will be invalid after the shift.
     *
     * @param objects the List of Spliceable objects before the buffer is
     * shifted.
     * @param index the index to the first valid object after the shift has
     * taken place.
     * @param shift the number of bytes that the buffer is going to be moved.
     */
    void backingBufferShift(List objects,
                            int index,
                            int shift);

    /**
     * Returns an empty Spliceable object representing the current place in the
     * order of Spliceable objects.
     *
     * @return A new object representing the current place.
     */
    Spliceable createCurrentPlaceSpliceable();

    /**
     * Returns a Spliceable object based on the data in the buffer.
     *
     * @param buffer the ByteBuffer holding the raw objects.
     * @return A new object based on the data in the buffer, null if there is
     *         not an object to return. This could mean that the next object is
     *         not fully contained in the buffer, or the object is not ready
     *         for comparison with other Spliceables.
     */
    Spliceable createSpliceable(ByteBuffer buffer);

    /**
     * Tells the factory that the specified Spliceables are no long considered
     * valid by teh Splicer. I.e. they are before the earlisest place of
     * interest. It is important not to modify the List that is the parameter
     * of this method as, for efficiency, it is an internal Splicer list!
     *
     * @param spliceables The List of Spliceables not longer in use.
     */
    void invalidateSpliceables(List spliceables);

    /**
     * Skips the next spliceable in the buffer if it exist. The resulting buffer
     * points to the following spliceable that might exist.
     *
     * @param buffer the ByteBuffer holding the raw objects.
     * @return true if the Spliceable was successfully skipped, false otherwise
     *         (in which case the buffer is untouched).
     */
    boolean skipSpliceable(ByteBuffer buffer);
}
