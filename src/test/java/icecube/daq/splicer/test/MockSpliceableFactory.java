/*
 * class: MockSpliceableFactory
 *
 * Version $Id: MockSpliceableFactory.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: September 15 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer.test;

import icecube.daq.splicer.Spliceable;
import icecube.daq.splicer.SpliceableFactory;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * This class implements the SpliceableFactory interface such that
 * MockSpliceable objects can be built.
 *
 * @author patton
 * @version $Id: MockSpliceableFactory.java,v 1.8 2004/08/09 20:30:02 patton
 *          Exp $
 */
public class MockSpliceableFactory
        implements SpliceableFactory
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    // private static member data

    // private instance member data

    /**
     * The value to used as the current place.
     */
    private long currentPlace;

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param currentPlace the initial current place for this object to used.
     */
    public MockSpliceableFactory(long currentPlace)
    {
        this.currentPlace = currentPlace;
    }

    // instance member method (alphabetic)

    public void backingBufferShift(List objects,
                                   int index,
                                   int shift)
    {
    }

    public Spliceable createCurrentPlaceSpliceable()
    {
        return new MockSpliceable(currentPlace);
    }

    public Spliceable createSpliceable(ByteBuffer buffer)
    {
        // If can not skip to next Spliceable then this one is not fully
        // contained.
        final int begin = buffer.position();
        if (!skipSpliceable(buffer)) {
            return null;
        }

        // Create a new MockSpliceable.
        final int length = (int) buffer.get(begin);
        final int nextSpliceableBegin = begin + length;
        if (nextSpliceableBegin > buffer.limit()) {
            return null;
        }
        buffer.position(nextSpliceableBegin);
        return new MockSpliceable(buffer.getLong(begin + 1),
                                  length);
    }

    public void invalidateSpliceables(List spliceables)
    {
    }

    /**
     * Sets the current place to be used by the {@link #createCurrentPlaceSpliceable}
     * method.
     *
     * @param currentPlace the place to be set.
     */
    void setCurrentPlace(long currentPlace)
    {
        this.currentPlace = currentPlace;
    }

    public boolean skipSpliceable(ByteBuffer buffer)
    {
        if (!buffer.hasRemaining()) {
            return false;
        }

        final int begin = buffer.position();
        final int nextSpliceableBegin = begin + (int) buffer.get(begin);
        if (nextSpliceableBegin > buffer.limit()) {
            return false;
        }
        buffer.position(nextSpliceableBegin);
        return true;
    }

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}
