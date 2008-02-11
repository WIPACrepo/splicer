/*
 * class: ExternalByteBufferTail
 *
 * Version $Id: ExternalByteBufferTail.java 2629 2008-02-11 05:48:36Z dglo $
 *
 * Date: August 6 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class takes an external ByteBuffer and uses its contents to create
 * Spliceables that are then pushes it into a Splicer.
 *
 * @author patton
 * @version $Id: ExternalByteBufferTail.java,v 1.2 2005/08/08 22:36:00 patton
 *          Exp $
 */
class ExternalByteBufferTail
        extends SplicerAdapter
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    /**
     * The initial capacity of arrays holding Spliceable information in this
     * object.
     */
    private static final int INITIAL_SPICEABLE_CAPACITY = 8;

    // private static member data

    // private instance member data

    /**
     * The byte buffer to hold data from the channel.
     */
    private final ByteBuffer buffer;

    /**
     * The Factory that this object used to create Spliceables.
     */
    private final SpliceableFactory factory;

    /**
     * The array of offsets from one Spliceable to the next Spliceable in the
     * buffer.
     * <p/>
     * (Use an array rather than an List because the contents are a primative
     * and we want to avoid high overhead of rapid object creation and
     * destruction.)
     */
    private int[] lengthsOfSpliceables = new int[INITIAL_SPICEABLE_CAPACITY];

    /**
     * The position in the buffer, upto which, it has bee successfully parsed.
     */
    private int parsedPosition;

    /**
     * The List of currently valid Spliceables managed by this object.
     */
    private final List spliceables = new LinkedList();

    /**
     * The StrandTail into which this object pushed its Spliceables.
     */
    private final StrandTail strandTail;

    private boolean full;

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param splicer the Splicer into which this object will push the
     * Spliceables it creates.
     * @param factory the SpliceableFactory this object will use.
     * @param buffer the external buffer this object is managing.
     */
    ExternalByteBufferTail(Splicer splicer,
                           SpliceableFactory factory,
                           ByteBuffer buffer)
    {
        strandTail = splicer.beginStrand();
        splicer.addSplicerListener(this);
        this.factory = factory;
        this.buffer = buffer;
        parsedPosition = 0;
    }

    // instance member method (alphabetic)

    /**
     * Closes the associated {@link Strand}. The Splicer will continue to
     * handle those Spliceables already pushed into this object but will not
     * acccept any more. Any further attempt to push data into this object will
     * cause a ClosedStrandException to be thrown.
     * <p/>
     * If the associated Strand is already closed then invoking this method
     * will have no effect.
     */
    void close()
    {
        strandTail.close();
    }

    /**
     * Returns the buffer this object is managing.
     *
     * @return the buffer this object is managing.
     */
    ByteBuffer getBuffer()
    {
        if (full) {
            if (buffer.hasRemaining()) {
                full = false;
//                System.out.println(this + ":buffer available.");
            }
        } else {
            if (!buffer.hasRemaining()) {
                full = true;
//                System.out.println(this + ":buffer full (" +
//                                   buffer.limit() +
//                                   " bytes).");
            }
        }
        return buffer;
    }

    /**
     * Returns the StrandTail object used by this object.
     *
     * @return the StrandTail object used by this object.
     */
    StrandTail getStrandTail()
    {
        return strandTail;
    }

    /**
     * Causes new Spliceables to be created based on the current contents of
     * the buffer. These new Spliceables are then pushed into the associated
     * Splicer.
     * <p/>
     * It is the clients responsibiltity to manage synchronization with respect
     * to the external ByteBuffer by using the object returned by the {@link
     * #getBuffer()}}.
     *
     * @throws OrderingException if the Spliceables in the buffer are not
     * properly ordered or is mis-ordered with respect to Spliceables already
     * pushed into this object
     * @throws ClosedStrandException is the associated Strand has been closed.
     */
    void push()
            throws OrderingException,
                   ClosedStrandException
    {
        synchronized (buffer) {
            final int originalParsedPosition = parsedPosition;
            final int originalSize = spliceables.size();
            buffer.flip();
            buffer.position(parsedPosition);
            Spliceable candidate = factory.createSpliceable(buffer);
            while (null != candidate) {

                // Extend lengthsOfSpliceables array if necessary.
                if (lengthsOfSpliceables.length == spliceables.size()) {
                    final int[] oldArray = lengthsOfSpliceables;
                    lengthsOfSpliceables = new int[oldArray.length * 2];
                    System.arraycopy(oldArray,
                                     0,
                                     lengthsOfSpliceables,
                                     0,
                                     spliceables.size());
                }

                // Store length of new spliceable and its object.
                lengthsOfSpliceables[spliceables.size()] =
                        buffer.position() - parsedPosition;
                parsedPosition = buffer.position();
                spliceables.add(candidate);
                candidate = factory.createSpliceable(buffer);
            }
            try {
                strandTail.push(spliceables.subList(originalSize,
                                                    spliceables.size()));
            } catch (OrderingException e) {
                resetAfterFailedPush(originalParsedPosition,
                                     originalSize);
                throw e;
            } catch (ClosedStrandException e) {
                resetAfterFailedPush(originalParsedPosition,
                                     originalSize);
                throw e;
            }
            buffer.position(0);
            buffer.compact();
        }
    }

    private void resetAfterFailedPush(int origParsedPosition,
                                      int origSize)
    {
        synchronized (buffer) {
            parsedPosition = origParsedPosition;
            spliceables.subList(origSize,
                                spliceables.size()).clear();
        }
    }

    public void truncated(SplicerChangedEvent event)
    {
        final Spliceable spliceable = event.getSpliceable();

        synchronized (buffer) {
            int index;
            if (Splicer.LAST_POSSIBLE_SPLICEABLE.equals(spliceable)) {
                index = spliceables.size() - 1;
            } else {
                index = Collections.binarySearch((List) spliceables,
                                                 spliceable,
                                                 new SpliceableComparator());
                if (0 > index) {
                    index = -1 * (index + 2);
                } else {

                    // Work backwards to find the exact cut off index.
                    for (; 0 <= index; index--) {
                        Spliceable next = (Spliceable) spliceables.get(index);
                        if (0 != spliceable.compareSpliceable(next)) {
                            break;
                        }
                    }
                }
            }

            // If no Spliceable are invalidated, then no nothing
            if (0 > index) {
                return;
            }

            final List invalidated =
                    Collections.unmodifiableList(spliceables.subList(0,
                                                                     index +
                                                                     1));
            factory.invalidateSpliceables(invalidated);

            int shift = 0;
            final int finished = index + 1;
            for (int element = 0;
                 finished != element;
                 element++) {
                shift += lengthsOfSpliceables[element];
            }
            factory.backingBufferShift(spliceables,
                                       index,
                                       shift);
            spliceables.subList(0,
                                finished).clear();
            System.arraycopy(lengthsOfSpliceables,
                             finished,
                             lengthsOfSpliceables,
                             0,
                             spliceables.size());

            // Remove leading data from the buffer.
            parsedPosition -= shift;
            buffer.flip();
            buffer.position(shift);
            buffer.compact();
        }
    }

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}
