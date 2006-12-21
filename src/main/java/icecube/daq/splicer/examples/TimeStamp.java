/*
 * class: TimeStamp
 *
 * Version $Id: TimeStamp.java,v 1.6 2005/10/12 17:48:20 patton Exp $
 *
 * Date: September 19 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer.examples;

import icecube.daq.splicer.Spliceable;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * This class is a time stamped container which holds a payload.
 *
 * @author patton
 * @version $Id: TimeStamp.java,v 1.6 2005/10/12 17:48:20 patton Exp $
 */
class TimeStamp
        implements Spliceable
{
    // private instance member data

    /**
     * Time with which this object is stamped.
     */
    private final Date time;

    /**
     * ByteBuffer backing this object.
     */
    private final ByteBuffer buffer;

    /**
     * Offset into the buffer of this object's payload.
     */
    private int offset;

    /**
     * Length of the payload in the buffer.
     */
    private final int length;

    /**
     * Payload of this object.
     */
    private Object payload;

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param time the time with which this object if stamped.
     * @param buffer the ByteBuffer backing this object.
     * @param offset the Offset into the buffer of this object's payload.
     * @param length the length of the payload in the buffer.
     */
    TimeStamp(Date time,
              ByteBuffer buffer,
              int offset,
              int length)
    {
        this.time = time;
        this.buffer = buffer;
        this.offset = offset;
        this.length = length;
    }

    // instance member method (alphabetic)

    public int compareTo(Object object)
    {
        return time.compareTo(((TimeStamp) object).time);
    }

    /**
     * Called when a payload needs to be created from the backing buffer.
     *
     * @return the object created as the payload of this object.
     */
    protected Object createPayload()
    {
        final ByteBuffer payloadBuffer = getBuffer();
        final byte[] bytes = new byte[length];
        synchronized (buffer) {
            final int payloadOffset = getOffset();
            final int position = payloadBuffer.position();
            final int limit = payloadBuffer.limit();
            payloadBuffer.position(payloadOffset).limit(payloadOffset +
                                                        getLength());
            payloadBuffer.get(bytes);
            payloadBuffer.limit(limit).position(position);
        }
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
     * Returns the time with which this object if stamped.
     *
     * @return the time with which this object if stamped.
     */
    public Date getTime()
    {
        return time;
    }

    void shiftOffset(int shift)
    {
        offset -= shift;
    }
}
