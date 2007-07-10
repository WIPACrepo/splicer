/*
 * class: TimeStampFactory
 *
 * Version $Id: EventContributionFactory.java,v 1.9 2005/08/09 01:28:48 patton Exp $
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
import java.util.Iterator;
import java.util.List;

/**
 * This class is used by a {@link Splicer Splicer} object to create {@link
 * EventContribution} objects from a ReadableByteBuffer.
 *
 * @author patton
 * @version $Id: EventContributionFactory.java,v 1.5 2004/08/09 20:30:03 patton
 *          Exp $
 */
class EventContributionFactory
        implements SpliceableFactory
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    // private static member data

    /**
     * Byte count of the length data.
     */
    private static final int LENGTH_BYTE_COUNT = 4;

    /**
     * Byte count of the time data.
     */
    private static final int NUMBER_BYTE_COUNT = 4;

    // private instance member data

    /**
     * The 'current' Event number.
     */
    private Integer currentEvent;

    // constructors

    /**
     * Create an instance of this class.
     */
    EventContributionFactory()
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
            final EventContribution eventContribution = (EventContribution) iterator.next();
            cursor++;
            if (cursor > index) {
                eventContribution.shiftOffset(shift);
            }
        }
    }

    public Spliceable createCurrentPlaceSpliceable()
    {
        if (null == currentEvent) {
            throw new NullPointerException("No Current Event!");
        }
        return new EventContribution(currentEvent,
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

        // Create a new EventContribution.
        final int length = buffer.getInt(begin);
        final int numberOffset = begin + LENGTH_BYTE_COUNT;
        final int payloadOffset = numberOffset + NUMBER_BYTE_COUNT;
        return new EventContribution(new Integer(buffer.getInt(numberOffset)),
                                     buffer,
                                     payloadOffset,
                                     length - LENGTH_BYTE_COUNT -
                                     NUMBER_BYTE_COUNT);
    }

    public void invalidateSpliceables(List spliceables)
    {
    }

    public boolean skipSpliceable(ByteBuffer buffer)
    {
        // Check that this object can read the Spliceables length.
        final int begin = buffer.position();
        if (begin + LENGTH_BYTE_COUNT > buffer.limit()) {
            return false;
        }

        // Check that the Spliceable is fully contained.
        final int length = buffer.getInt(begin);
        final int nextSpliceableBegin = begin + length;
        if (nextSpliceableBegin > buffer.limit()) {
            return false;
        }
        buffer.position(nextSpliceableBegin);
        return true;
    }

    public void setCurrentEvent(Integer number)
    {
        currentEvent = number;

    }

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}
