/*
 * class: TimeStamp
 *
 * Version $Id: EventContribution.java,v 1.4 2004/08/10 04:42:04 patton Exp $
 *
 * Date: September 19 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer.examples;

import icecube.daq.splicer.Spliceable;

import java.nio.ByteBuffer;

/**
 * This class is a contribution to an Event in the Event example of {@link
 * Splicer} usage..
 *
 * @author patton
 * @version $Id: EventContribution.java,v 1.4 2004/08/10 04:42:04 patton Exp $
 */
class EventContribution
        implements Spliceable
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    // private static member data

    // private instance member data

    /**
     * The event to which this object is contributing.
     */
    private final Integer number;

    /**
     * ByteBuffer backing this object.
     */
    private final ByteBuffer buffer;

    /**
     * Offset into the buffer of this objects payload.
     */
    private int offset;

    /**
     * Length of the payload in the buffer.
     */
    private final int length;

    /**
     * payload of this object.
     */
    private Object payload;

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param number the event to which this object is contributing.
     * @param buffer the ByteBuffer backing this object.
     * @param offset the Offset into the buffer of this objects payload.
     * @param length the length of the payload in the buffer.
     */
    EventContribution(Integer number,
                      ByteBuffer buffer,
                      int offset,
                      int length)
    {
        this.number = number;
        this.buffer = buffer;
        this.offset = offset;
        this.length = length;
    }

    // instance member method (alphabetic)

    public int compareTo(Object object)
    {
        final int result =
                number.compareTo(((EventContribution) object).number);
        return result;
    }

    /**
     * Called when a payload needs to be created from the backing buffer.
     *
     * @return the object create as the payload of this object.
     */
    protected Object createPayload()
    {
        final ByteBuffer payloadBuffer = getBuffer();
        final int payloadOffset = getOffset();
        final byte[] bytes = new byte[length];
        final int position = payloadBuffer.position();
        final int limit = payloadBuffer.limit();
        payloadBuffer.position(payloadOffset).limit(payloadOffset +
                                                    getLength());
        payloadBuffer.get(bytes);
        payloadBuffer.limit(limit).position(position);
        return new String(bytes);
    }

    protected ByteBuffer getBuffer()
    {
        return buffer;
    }

    protected int getLength()
    {
        return length;
    }

    protected int getOffset()
    {
        return offset;
    }

    /**
     * Returns this objects payload as an object.
     *
     * @return this objects payload as an object.
     */
    public Object getPayload()
    {
        if (null == payload) {
            payload = createPayload();
        }
        return payload;
    }

    /**
     * Returns the event to which this object is contributing.
     *
     * @return the event to which this object is contributing.
     */
    public Integer getNumber()
    {
        return number;
    }

    void shiftOffset(int shift)
    {
        offset -= shift;
    }

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}
