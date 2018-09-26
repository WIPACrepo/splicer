/*
 * class: MockSpliceableFactory
 *
 * Version $Id: MockSpliceableFactory.java 17114 2018-09-26 09:51:56Z dglo $
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
    /**
     * Create an instance of this class.
     */
    public MockSpliceableFactory()
    {
    }

    // instance member method (alphabetic)

    public void backingBufferShift(List objects,
                                   int index,
                                   int shift)
    {
    }

    @Override
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

    @Override
    public void invalidateSpliceables(List spliceables)
    {
    }

    @Override
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
