/*
 * class: SpliceableChannelsCarder
 *
 * Version $Id: SpliceableChannelsCarder.java 2631 2008-02-11 06:27:31Z dglo $
 *
 * Date: August 8 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

import icecube.icebucket.net.SelectorInvoker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class ...does what?
 *
 * @author patton
 * @version $Id: SpliceableChannelsCarder.java,v 1.1 2005/08/08 22:40:14 patton
 *          Exp $
 */
public final class SpliceableChannelsCarder
        extends SelectorInvoker
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    /**
     * The default size for buffers.
     */
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    /**
     * The default size for map.
     */
    private static final int DEFAULT_MAP_SIZE = 4;

    // private static member data

    // private instance member data

    /**
     * The size of ByteBuffers this object should use.
     */
    private final int bufferSize;

    /**
     * The Map of channels in use by this object, indexed by their
     * StrandTails.
     */
    private final Map channelsInUse = new HashMap(DEFAULT_MAP_SIZE);

    /**
     * The factory to be used to create Spliceables.
     */
    private final SpliceableFactory factory;

    /**
     * The object used to handler successful selects.
     */
    private Runnable selectHandler;

    /**
     * The Splicer consuming the Spiceables this object is preparing.
     */
    private final Splicer splicer;

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param splicer the Splicer consuming the Spiceables this object is
     * preparing.
     * @param factory the SpliceableFactory this object will use.
     */
    SpliceableChannelsCarder(Splicer splicer,
                             SpliceableFactory factory)
    {
        this(splicer,
             factory,
             DEFAULT_BUFFER_SIZE);
    }

    /**
     * Create an instance of this class.
     *
     * @param splicer the Splicer consuming the Spiceables this object is
     * preparing.
     * @param factory the SpliceableFactory this object will use.
     * @param bufferSize the size of ByteBuffers this object should use.
     */
    SpliceableChannelsCarder(Splicer splicer,
                             SpliceableFactory factory,
                             int bufferSize)
    {
        this.splicer = splicer;
        this.factory = factory;
        this.bufferSize = bufferSize;
        final Thread thread = new Thread(this);
        thread.start();
    }

    // instance member method (alphabetic)

    /**
     * Adds the specified channel to this object so its data can be used to
     * construct Spliceable objects. The channel can only be added when the
     * associated Splicer is in the Stopped state. If the channel has already
     * been added then this method will have no effect.
     * <p/>
     * The channel must implement the ReadableByteChannel interface.
     *
     * @param channel the channel to be added.
     * @throws IllegalArgumentException is channel does not implement
     * ReadableByteChannel interface.
     * @throws IOException if the channel can not be made non-blocking.
     */
    void addSpliceableChannel(final SelectableChannel channel)
            throws IOException
    {
        if (channelsInUse.containsValue(channel)) {
            return;
        }

        if (!(channel instanceof ReadableByteChannel)) {
            throw new IllegalArgumentException("SpliceableChannel needs to" +
                                               " be an instance of" +
                                               " ReadableByteChannel.");
        }

        channel.configureBlocking(false);

        invokeAndWait(new Runnable()
        {
            public void run()
            {
                final SelectionKey key;
                try {
                    key = channel.register(getSelector(),
                                           SelectionKey.OP_READ);

                    // Add strands belonging to the new controller.
                    final ByteBuffer buffer =
                            ByteBuffer.allocate(bufferSize);
//                    ByteBuffer.allocateDirect(bufferSize);
                    final ExternalByteBufferTail tail =
                            new ExternalByteBufferTail(splicer,
                                                       factory,
                                                       buffer);
                    key.attach(tail);
                    channelsInUse.put(tail.getStrandTail(),
                                      channel);

                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void afterLoop()
    {
    }

    protected void beforeLoop()
    {
        invokeWhenReady(getSelectHandler());
    }

    /**
     * Frees up the resources used by this object.
     */
    void dispose()
    {
        terminate();
    }

    /**
     * Returns a List of SelectableChannels that map onto the specified List of
     * StrandTails.
     *
     * @param strandTails the list of StrandTails mapping onto the channels.
     * @return List of SelectableChannels.
     */
    List getChannels(List strandTails)
    {
        final List result = new ArrayList(strandTails.size());
        final Iterator iterator = strandTails.iterator();
        while (iterator.hasNext()) {
            result.add(channelsInUse.get(iterator.next()));
        }
        return result;
    }

    private Runnable getSelectHandler()
    {
        if (null == selectHandler) {

            selectHandler = new Runnable()
            {
                public void run()
                {
                    // Set up for the next select.
                    invokeWhenReady(getSelectHandler());

                    // Process all waiting selections.
                    final Iterator iterator =
                            getSelector().selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        final SelectionKey key =
                                (SelectionKey) iterator.next();

                        try {
                            if (key.isReadable()) {
                                if (!readKey(key)) {
                                    key.cancel();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (OrderingException e) {
                            e.printStackTrace();
                        } catch (ClosedStrandException e) {
                            e.printStackTrace();
                        }

                        iterator.remove();
                    }
                }
            };
        }
        return selectHandler;
    }

    /**
     * Removes the specified channel from this object so its data can no longer
     * be used in the preparation of Spliceable objects for the associated
     * Splcier.
     * <p/>
     * If the channel has not been added then this method will have no effect.
     *
     * @param channel the channel to be removed.
     */
    void removeSpliceableChannel(final SelectableChannel channel)
    {
        invokeLater(new Runnable()
        {
            public void run()
            {
                if (!channelsInUse.containsValue(channel)) {
                    return;
                }

                final SelectionKey key = channel.keyFor(getSelector());
                final ExternalByteBufferTail tail =
                        (ExternalByteBufferTail) key.attachment();
                tail.close();
                channelsInUse.remove(tail.getStrandTail());
                key.cancel();
            }
        });
    }

    // static member methods (alphabetic)

    /**
     * This method reads and process the read data for the specified
     * connection.
     *
     * @param key the key representing the connection to read and process.
     * @return true is the key can continue to be read.
     * @throws IOException is there is an IO problem.
     */
    private static boolean readKey(SelectionKey key)
            throws IOException,
                   OrderingException,
                   ClosedStrandException
    {
        final ExternalByteBufferTail tail =
                (ExternalByteBufferTail) key.attachment();
        final ByteBuffer buffer = tail.getBuffer();
        synchronized (buffer) {

            // Since this buffer is being filled, hasRemaining() refers to free
            // space, thus if it is full do nothing.
            if (!buffer.hasRemaining()) {
                return true;
            }

            // Read waiting data into local buffer until there is no more data
            // or the buffer is full.
            final ReadableByteChannel channel =
                    (ReadableByteChannel) key.channel();
            int count = channel.read(buffer);
            while (buffer.hasRemaining() &&
                   0 < count) {
                count = channel.read(buffer);
            }
            tail.push();
        }
        return true;
    }

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}
