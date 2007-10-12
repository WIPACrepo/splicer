/*
 * class: MockSpliceable
 *
 * Version $Id: StrandImpl.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: September 15 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer;

import icecube.icebucket.util.Invocable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements the {@link Strand} interface using a simple List. The
 * "head" of the list is element 0 while the rest of the list contains all
 * Spliceable that have been pushed into this objects associated {@link
 * StrandTail}. This object tracks the end of each "section", i.e. the set of
 * Spliceable that occured before a LAST_POSSIBLE_SLICEABLE object was pushed
 * into the StrandTail, or a clean cut has been requested.
 * <p/>
 * If should be noted that the methods in this object are not synchronized as
 * they should all be invoked from within the same thread. This means that the
 * StringTail associated with this object does need to worry about
 * synchronization as its {@link StrandTailImpl#transfer()}, {@link
 * StrandTailImpl#clearLastPossible()} and {@link StrandTailImpl#updateUnwoven(int,
 * Spliceable)} will be invoked by this object's thread, while its other
 * methods will be invoked by the client's thread.
 *
 * @author patton
 * @version $Id: StrandImpl.java 2125 2007-10-12 18:27:05Z ksb $
 * @since v3.0
 */
class StrandImpl
        implements Strand,
                   ManagedStrand
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    /**
     * True if the tails should operate in "safe" mode.
     */
    private static final boolean SAFE_MODE = true;

    // private static member data

    // private instance member data

    /**
     * True if this object has been closed.
     */
    private boolean closed;

    /**
     * The ordered list of Spliceables this object will supply.
     */
    private final List contents = new LinkedList();

    /**
     * True if this object is not accepting any new Spliceables.
     */
    private boolean halted;

    /**
     * The spliceable specified when a halt was requested.
     */
    private Spliceable haltSpliceable;

    /**
     * The last Spliceable pulled from this object.
     */
    private Spliceable lastPulledSpliceable;

    /**
     * The object which is managing this object.
     */
    private final StrandManager manager;

    /**
     * The index to current section accepting new Spliceables.
     */
    private int section;

    /**
     * The StandTail which feeds this object.
     */
    private StrandTailImpl tail;

    /**
     * The offsets to the current visible "tail".
     */
    private int visibleTail;

    /**
     * The ordered list of offsets to the succeeding "tail" of each section.
     */
    private int[] tailOffsets = new int[1];

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param manager the StrandManager managing this object.
     */
    StrandImpl(StrandManager manager)
    {
        this.manager = manager;
        manager.strandBecameEmpty(this);
        visibleTail = -1;
        tailOffsets[0] = -1;
    }

    // instance member method (alphabetic)

    /**
     * Closes this object.
     */
    void close()
    {
        closed = true;
        manager.strandClosed(this);
    }

    private boolean extendVisibleSection(int value)
    {
        final boolean startedEmpty;
        startedEmpty = isEmpty();

        visibleTail = value;

        return (startedEmpty && (!isEmpty()));
    }


    public void forceHalt()
    {
        halted = true;
        if (reduceVisibleSection(-1)) {
            manager.strandBecameEmpty(this);
        }
    }

    public Strand getStrand()
    {
        return this;
    }

    public StrandTail getTail(Invocable invocable)
    {
        if (null == invocable) {
            throw new IllegalArgumentException("Invocable must not be null.");
        }

        if (null == tail) {
            tail = new StrandTailImpl(this,
                                      invocable,
                                      SAFE_MODE);
        }

        // Test that the same invokable was used in the call.
        if (tail.getInvocable() != invocable) {
            throw new IllegalArgumentException("StrandTail already bound to" +
                                               " different invocable.");
        }
        return tail;
    }

    public void halt()
    {
        if (halted ||
            null != haltSpliceable) {
            return;
        }
        halted = true;
    }

    public void halt(Spliceable spliceable)
    {
        if (halted ||
            null != haltSpliceable) {
            return;
        }

        if ((null != lastPulledSpliceable) &&
            (0 < lastPulledSpliceable.compareTo(spliceable))) {
            halted = true;
            if (reduceVisibleSection(-1)) {
                manager.strandBecameEmpty(this);
            }
            return;
        }

        haltSpliceable = spliceable;
        updateForHalt();
    }

    public Spliceable head()
    {
        if (isEmpty()) {
            return null;
        }
        return (Spliceable) contents.get(0);
    }

    public boolean isClosed()
    {
        return closed;
    }

    public boolean isEmpty()
    {
        return 0 > visibleTail;
    }

    public boolean isRemoveableAsEmpty()
    {
        return isEmpty() && (isClosed() || 0 != section || halted);
    }

    public void proceed()
    {
        halted = false;
        haltSpliceable = null;

        final boolean noLongerEmpty = extendVisibleSection(tailOffsets[0]);

        if (useNextSection() || noLongerEmpty) {
            manager.strandNoLongerEmpty(this);
        }
    }

    public Spliceable pull()
    {
        if (isEmpty()) {
            return null;
        }
        final Spliceable result = (Spliceable) contents.get(0);
        lastPulledSpliceable = (Spliceable) contents.remove(0);
        tailOffsets[0]--;
        if (reduceVisibleSection(visibleTail - 1)) {
            manager.strandBecameEmpty(this);
        }
        return result;
    }

    /**
     * Added new Spliceable to this object. The push may not be successful as
     * this object may refused new Spliceables while the
     *
     * @param spliceables the list of new Spliceables.
     * @param offsets the offsets of the "tail" in succeeding sections.
     * @param length the number of valid  section in offsets.
     * @return true if the push was successful.
     */
    boolean push(List spliceables,
                 int[] offsets,
                 int length)
    {
        if (halted) {
            return false;
        }

        contents.addAll(spliceables);

        tailOffsets[section] += offsets[0] + 1;

        // Expand array of tail offsets if necessary
        final int newSection = section + length - 1;
        if (newSection >= tailOffsets.length) {
            final int[] newArray = new int[2 * tailOffsets.length];
            System.arraycopy(tailOffsets,
                             0,
                             newArray,
                             0,
                             tailOffsets.length);
            tailOffsets = newArray;
        }

        // Copy over offsets for the new sections
        final int finished = newSection + 1;
        for (int index = section + 1;
             finished != index;
             index++) {
            tailOffsets[index] = offsets[index + section];
        }
        // Update current section.
        section = newSection;

        if (extendVisibleSection(tailOffsets[0])) {
            manager.strandNoLongerEmpty(this);
        } else if (isRemoveableAsEmpty() &&
                   (1 < length)) {
            manager.strandBecameRemoveable(this);
        }

        // If a specified halt has been requested see if it in now in this
        // object.
        if (null != haltSpliceable) {
            updateForHalt();
        }

        transferToClientThread();
        return true;
    }

    private boolean reduceVisibleSection(int value)
    {
        final boolean startedEmpty;
        startedEmpty = isEmpty();

        visibleTail = value;

        return ((!startedEmpty) && isEmpty());
    }

    public int size()
    {
        return visibleTail + 1;
    }

    /**
     * Inspects the first section of Spliceables to see if the requested
     * Spliceable at which to halt exists.
     */
    private void updateForHalt()
    {
        if (0 != section) {
            halted = true;
        }

        if (isEmpty()) {
            if (halted) {
                haltSpliceable = null;
            }
            return;
        }

        final int finished = tailOffsets[0] + 1;
        int index = Collections.binarySearch(contents.subList(0,
                                                              finished),
                                             haltSpliceable);
        // If all Spliceables in the Strand are less than the halt Spliceable
        // then do nothing more.
        if (((-1 * finished) - 1) == index) {
            return;
        }

        if (0 > index) {
            index = -1 * (index + 1);
        } else {

            // Work forwards to find the exact cut off index.
            while ((finished > index) &&
                   (0 ==
                    ((Spliceable) contents.get(index))
                            .compareTo(haltSpliceable))) {
                index++;
            }
        }

        // If all Spliceables in the Strand are less than the halt Spliceable
        // then do nothing more.
        if (finished == index) {
            return;
        }
        halted = true;
        haltSpliceable = null;
        // Subtract "1" to move tail _onto_ tail element, rather than
        // one-past-end which is the value in "index".
        if (reduceVisibleSection(index - 1)) {
            manager.strandBecameEmpty(this);
        }
    }

    /**
     * Sets up this object to use the next section of Spliceables, if it
     * exists.
     *
     * @return true if this changes this object from empty to not empty.
     */
    boolean useNextSection()
    {
        if (null != tail) {
            tail.clearLastPossible();
        }

        if (0 == section) {
            return false;
        }

        // Shift all offsets to the previous section
        for (int index = 0;
             section != index;
             index++) {
            tailOffsets[index] = tailOffsets[index + 1];
        }
        section--;

        // Add back on the number of Spliceables still in the Strand.
        tailOffsets[0] += visibleTail + 1;
        return extendVisibleSection(tailOffsets[0]);
    }

    public Spliceable tail()
    {
        if (isEmpty()) {
            return null;
        }
        return (Spliceable) contents.get(visibleTail);
    }

    public void transferToClientThread()
    {
        tail.updateUnwoven(size(),
                           head());
    }

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}
