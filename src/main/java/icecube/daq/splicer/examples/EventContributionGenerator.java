/*
 * class: TimeStampGenerator
 *
 * Version $Id: EventContributionGenerator.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: September 19 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer.examples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * This class generators EventContributions and places them into a Channel.
 *
 * @author patton
 * @version $Id: EventContributionGenerator.java,v 1.3 2004/08/04 20:41:56
 *          patton Exp $
 */
public class EventContributionGenerator
        implements Runnable
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    /**
     * The size of the byte buffer to build.
     */
    private static final int BUFFER_SIZE = 1024;

    /**
     * Delay time between propogation calls.
     */
    private static final long DELAY_TIME = 10L;

    /**
     * The 0.5 used to round up values.
     */
    private static final double ROUNDUP_HALF = 0.5;

    // private static member data

    // private instance member data

    /**
     * The EventNumberPropagator used to feed numbers into this object.
     */
    private final EventNumberPropagator propagator = new EventNumberPropagator();

    /**
     * Name used in this objects label creation.
     */
    private final String label;

    /**
     * Average time, in milliseconds, to 'stall' before generating.
     */
    private final int millis;

    /**
     * Channel used to output the time stamps.
     */
    private final WritableByteChannel channel;

    /**
     * ByteBuffer used to fill channel.
     */
    private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param channel ByteChannel used to output the time stamps.
     * @param label the label used in this objects default payload creation.
     * @param millis the average time, in milliseconds, to 'stall' before
     * generating.
     */
    public EventContributionGenerator(WritableByteChannel channel,
                                      String label,
                                      int millis)
    {
        this.channel = channel;
        this.label = label;
        this.millis = millis;
    }

    // instance member method (alphabetic)

    /**
     * Fills the specified payloadBuffer with any payload associated with the
     * time stamp currently being constructed.
     *
     * @param payloadBuffer the ByteBuffer into which the payload is to be
     * placed.
     * @param number the event number.
     */
    void fillPayload(ByteBuffer payloadBuffer,
                     Integer number)
    {
        final String payload = "Hits for Event " +
                               number.toString() +
                               " from " +
                               label;
        final byte[] stringAsBytes = payload.getBytes();
        payloadBuffer.put(stringAsBytes);
    }

    /**
     * Requests that this object generate its contribution to the specified
     * event.
     *
     * @param number the event whose contribution should be generated.
     */
    synchronized void requestEvent(Integer number)
    {
        propagator.propagate(number);
        notifyAll();
    }

    public synchronized void run()
    {
        while (!propagator.isFinished()) {
            Integer event = propagator.getNumber();
            while (!propagator.isFinished() &&
                   null == event) {
                try {
                    wait(DELAY_TIME);
                } catch (InterruptedException e) {
                    // do nothing special if interrupted.
                }
                event = propagator.getNumber();
            }
            propagator.next();

            if (null != event) {
                final long pause = (long) ((double) millis * (ROUNDUP_HALF +
                                                              Math.random()));
                try {
                    wait(pause);
                } catch (InterruptedException e) {
                    // do nothing special if interrupted.
                }

                buffer.clear();
                final int lengthPosition = buffer.position();
                buffer.position(lengthPosition + 4);
                buffer.putInt(event.intValue());
                fillPayload(buffer,
                            event);
                buffer.putInt(lengthPosition,
                              (int) (byte) (buffer.position() -
                                            lengthPosition));
                buffer.flip();
                try {
                    channel.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Sets the finished flag.
     *
     * @param finished the value to which the flag should be set.
     */
    public void setFinished(boolean finished)
    {
        propagator.setFinished(finished);
    }

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}
