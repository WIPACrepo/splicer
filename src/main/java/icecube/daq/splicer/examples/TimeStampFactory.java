/*
 * class: TimeStampFactory
 *
 * Version $Id: TimeStampFactory.java,v 1.11 2004/09/11 17:59:38 patton Exp $
 *
 * Date: September 19 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer.examples;

import icecube.daq.splicer.Spliceable;
import icecube.daq.splicer.SpliceableFactory;
import icecube.daq.splicer.Splicer;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * This class is used by a {@link Splicer Splicer} object to create {@link
 * TimeStamp} objects from a ReadableByteBuffer.
 *
 * @author patton
 * @version $Id: TimeStampFactory.java,v 1.11 2004/09/11 17:59:38 patton Exp $
 */
class TimeStampFactory
        implements SpliceableFactory
{
    // private static member data

    /**
     * Byte count of the length data.
     */
    private static final int LENGTH_BYTE_COUNT = 4;

    /**
     * Byte count of the time data.
     */
    private static final int TIME_BYTE_COUNT = 8;


    // constructors

    /**
     * Create an instance of this class.
     */
    TimeStampFactory()
    {
    }

    // instance member method (alphabetic)

    public void backingBufferShift(List objects,
                                   int index,
                                   int shift)
    {
        int cursor = 0;
        final Iterator iterator = objects.iterator();
        while (iterator.hasNext()) {
            final TimeStamp timeStamp = (TimeStamp) iterator.next();
            cursor++;
            if (cursor > index) {
                timeStamp.shiftOffset(shift);
            }
        }
    }

    public Spliceable createCurrentPlaceSplicaeable()
    {
        return new TimeStamp(new Date(),
                             null,
                             0,
                             0);
    }

    public Spliceable createSpliceable(ByteBuffer buffer)
    {
        // If can not skip to next Spliceable then this one is not fully
        // contained.
        final int begin = buffer.position();
        if (!skipSpliceable(buffer)) {
            return null;
        }

        // Create a new TimeStamp.
        final int length = buffer.getInt(begin);
        final int timeOffset = begin + LENGTH_BYTE_COUNT;
        final int payloadOffset = timeOffset + TIME_BYTE_COUNT;
        return new TimeStamp(new Date(buffer.getLong(timeOffset)),
                             buffer,
                             payloadOffset,
                             length - LENGTH_BYTE_COUNT - TIME_BYTE_COUNT);
    }

    public void invalidateSplicables(List splicables)
    {
    }

    public boolean skipSpliceable(ByteBuffer buffer)
    {
        // Check that this object can read the Spliceables length.
        final int begin = buffer.position();
        if (begin + LENGTH_BYTE_COUNT > buffer.limit()) {
            return false;
        }

        // Check that the Splicable is fully contained.
        final int length = buffer.getInt(begin);
        final int nextSpliceableBegin = begin + length;
        if (nextSpliceableBegin > buffer.limit()) {
            return false;
        }
        buffer.position(nextSpliceableBegin);
        return true;
    }
}
