/*
 * class: TimeStampGenerator
 *
 * Version $Id: TimeStampGenerator.java,v 1.21 2005/10/18 15:27:47 patton Exp $
 *
 * Date: September 19 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer.examples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.BufferOverflowException;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

/**
 * This class generates time stamps objects and places them into a Channel.
 *
 * @author patton
 * @version $Id: TimeStampGenerator.java,v 1.13 2004/08/04 20:41:56 patton Exp
 *          $
 */
public class TimeStampGenerator
        implements Runnable
{
    // private static final member data

    /**
     * The size with which to create the buffers.
     */
    private static final int BUFFER_SIZE = 1024;

    /**
     * The default value to assign to generatorTime.
     */
    private static final int DEFAULT_GENERATOR_TIME = 1000;

    /**
     * The 0.5 used to round up values.
     */
    private static final double ROUNDUP_HALF = 0.5;

    // private instance member data

    /**
     * Number of bytes output so far.
     */
    private int bytes;

    /**
     * Channel into which the TimeStamps will be placed.
     */
    private WritableByteChannel channel;

    /**
     * ByteBuffer used to fill channel.
     */
    private final ByteBuffer channelBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    /**
     * Number of time stamps generated so far.
     */
    private int count;

    /**
     * true if this object should finish, i.e. permenantly stop.
     */
    private boolean finish;

    /**
     * true if this object has stalled due to flow control.
     */
    private boolean flowControlled;

    /**
     * true if this object should be generation objects.
     */
    private boolean generating;

    /**
     * Average time, in milliseconds, between TimeStamp creations.
     */
    private int generatorTime;

    /**
     * The integer used to identify this object.
     */
    private final int identity;

    /**
     * ByteBuffer used to build the payload.
     */
    private final ByteBuffer payloadBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param id the integer used to identify this object.
     */
    public TimeStampGenerator(int id)
    {
        this(id,
             DEFAULT_GENERATOR_TIME);
    }

    /**
     * Create an instance of this class.
     *
     * @param id the integer used to identify this object.
     * @param generatorTime the average time, in milliseconds, between
     * creations.
     */
    public TimeStampGenerator(int id,
                              int generatorTime)
    {
        identity = id;
        this.generatorTime = generatorTime;
    }

    // instance member method (alphabetic)

    /**
     * Fills the specified payloadBuffer with any payload associated with the
     * time stamp currently being constructed.
     *
     * @param payloadBuffer the ByteBuffer into which the payload is to be
     * placed.
     */
    void fillPayload(ByteBuffer payloadBuffer)
    {
        final String payload = "Element " +
                               getCount() +
                               " of Generator ID=" +
                               getId();
        final byte[] stringAsBytes = payload.getBytes();
        payloadBuffer.put(stringAsBytes);
    }

    /**
     * Returns the number of bytes output so far.
     *
     * @return the number of bytes output so far.
     */
    public synchronized int getBytes()
    {
        return bytes;
    }

    /**
     * Returns the number of time stamped objects generated so far.
     *
     * @return the number of time stamped objects generated so far.
     */
    public synchronized int getCount()
    {
        return count;
    }

    synchronized boolean getFinish()
    {
        return finish;
    }

    synchronized boolean getGenerating()
    {
        return generating;
    }

    /**
     * Returns average time, in milliseconds, between TimeStamp creations.
     *
     * @return average time, in milliseconds, between TimeStamp creations.
     */
    public synchronized int getGeneratorTime()
    {
        return generatorTime;
    }

    /**
     * Returns the integer used to identify this object.
     *
     * @return the integer used to identify this object.
     */
    public int getId()
    {
        return identity;
    }

    public synchronized void run()
    {
        long pause = (long) ((double) getGeneratorTime() *
                             (ROUNDUP_HALF + Math.random()));
        try {
            Thread.sleep(pause);
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        while (!getFinish()) {
            if (getGenerating()) {

                // If no payload is pending - create a new one.
                if (0 == payloadBuffer.position()) {
                    flowControlled = false;
                    payloadBuffer.position(4);
                    payloadBuffer.putLong(new Date().getTime());
                    fillPayload(payloadBuffer);
                    bytes += payloadBuffer.position();
                    count += 1;
                    payloadBuffer.putInt(0,
                                         payloadBuffer.position());
                } else {
                    if (!flowControlled) {
//                        System.out.println("Generator " +
//                                           getId() +
//                                           " has Flow controlled");
                    }
                    flowControlled = true;
                }

                payloadBuffer.flip();
                try {
                    channelBuffer.put(payloadBuffer);
                } catch (BufferOverflowException e) {
                    // do nothing and use this stuff next time through.
                }
                payloadBuffer.compact();

                // Try to write out pending payloads.
                channelBuffer.flip();
                try {
                    channel.write(channelBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                channelBuffer.compact();

            } else {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // do nothing special if interrupted.
                }
            }

            if (getGenerating()) {
                pause = (long) ((double) generatorTime *
                                (ROUNDUP_HALF + Math.random()));
                if (0 == pause) {
                    pause = 1;
                }
                try {
                    wait(pause);
                } catch (InterruptedException e) {
                    // do nothing special if interrupted.
                }
            }
        }


    }

    /**
     * Sets the channel into which the TimeStamps will be placed.
     *
     * @param channel the channel into which the TimeStamps will be placed.
     */
    public synchronized void setChannel(WritableByteChannel channel)
    {
        this.channel = channel;
    }

    /**
     * Set the flag to signal whether this object should finish generation or
     * not.
     *
     * @param finish the flag to signal whether to finish generation or not.
     */
    public synchronized void setFinish(boolean finish)
    {
        this.finish = finish;
        setGenerating(false);
    }

    /**
     * Set the flag to signal whether this object should be generating objects
     * or not.
     *
     * @param generating the flag to signal whether object are being
     * generated.
     */
    public synchronized void setGenerating(boolean generating)
    {
        this.generating = generating;
        notifyAll();
    }

    /**
     * Sets the average time, in milliseconds, between TimeStamp creations.
     *
     * @param generatorTime the average time, in milliseconds, between
     * creations.
     */
    public synchronized void setGeneratorTime(int generatorTime)
    {
        this.generatorTime = generatorTime;
    }
}
