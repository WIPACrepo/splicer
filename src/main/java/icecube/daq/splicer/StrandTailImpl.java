/*
 * class: StrandTailImpl
 *
 * Version $Id: StrandTailImpl.java,v 1.20 2006/02/04 21:54:29 patton Exp $
 *
 * Date: July 31 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

import icecube.icebucket.util.Invocable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is the Object through which Spliceables are pushed into a
 * StrandImpl object.
 *
 * @author patton
 * @version $Id: StrandTailImpl.java,v 1.20 2006/02/04 21:54:29 patton Exp $
 */
class StrandTailImpl
        implements StrandTail
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

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
     * The Invocable object used by this object.
     */
    private final Invocable invocable;

    /**
     * True if a LAST_POSSIBLE_SPLICER flag is in effect.
     */
    private boolean lastPossibleInEffect;
    /**
     * The last spliceable currently in the contents of this object.
     */
    private Spliceable lastSpliceable;

    /**
     * True if this object should hold on to last spliceable.
     */
    private final boolean safeMode;

    /**
     * The index to current section accepting new Spliceables.
     */
    private int section = -1;

    /**
     * The StrandImpl this object is fronting.
     */
    private final StrandImpl strand;

    /**
     * The ordered list of offsets to the succeeding "tail" of each section.
     */
    private int[] tailOffsets = new int[1];

    /**
     * True if there is a transfer request pending.
     */
    private boolean transferPending;

    /**
     * The next Splicable waiting to be woven.
     */
    private Spliceable unwovenHead;

    /**
     * Lock object used to transfer unwoven details to this object.
     */
    private final Object unwovenLock = new Object();

    /**
     * The number of unwoven Spliceables currently in this Object.
     */
    private int unwovenSize;

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param strand the StrnadImpl this object is supplying.
     * @param invocable the Invocable object into which this object will queue
     * commands.
     * @param safeMode true if this object should hold on to last spliceable.
     */
    StrandTailImpl(StrandImpl strand,
                   Invocable invocable,
                   boolean safeMode)
    {
        this.strand = strand;
        this.invocable = invocable;
        this.safeMode = safeMode;
        beginSection();
    }

    // instance member method (alphabetic)

    /**
     * Begins a new section of Spliceables.
     */
    private synchronized void beginSection()
    {
        section++;

        // If necessary extend the tailOffsets array.
        if (section >= tailOffsets.length) {
            final int[] newArray = new int[2 * tailOffsets.length];
            System.arraycopy(tailOffsets,
                             0,
                             newArray,
                             0,
                             tailOffsets.length);
            tailOffsets = newArray;
        }
        tailOffsets[section] = -1;
    }

    public synchronized void close()
    {
        // Make sure last Spliceables will be transfered.
        prepareTransfer();

        // queue up signal to StrandImpl
        invocable.invokeLater(new Runnable()
        {
            public void run()
            {
                strand.close();
            }
        });
        closed = true;
    }

    /**
     * Returns the Invocable used by this object. This method does not need to
     * be synchronized as it is only called from the StrandImpl Thread.
     *
     * @return the Invocable used by this object.
     */
    Invocable getInvocable()
    {
        return invocable;
    }

    private synchronized Spliceable getLocalHead()
    {
        if (-1 == tailOffsets[0]) {
            return null;
        }
        return (Spliceable) contents.get(0);
    }

    public Spliceable head()
    {
        final Spliceable result;
        synchronized (unwovenLock) {
            result = unwovenHead;
        }
        if (null == result) {
            return getLocalHead();
        }
        return result;
    }

    public synchronized boolean isClosed()
    {
        return closed;
    }

    public synchronized StrandTail push(List spliceables)
            throws OrderingException,
                   ClosedStrandException
    {
        if (0 == spliceables.size()) {
            return this;
        }

        if (closed) {
            throw new ClosedStrandException("The associated Strand has" +
                                            " already been closed");
        }

        // Test that the list is well ordered.
        boolean listContainsLastPossible = false;
        Spliceable lastGoodSpliceable = lastSpliceable;
        final Iterator iterator = spliceables.iterator();
        while (iterator.hasNext()) {
            final Spliceable spliceable = (Spliceable) iterator.next();
            if (0 == LAST_POSSIBLE_SPLICEABLE.compareTo(spliceable)) {
                listContainsLastPossible = true;
            } else {
                if (listContainsLastPossible) {
                    throw new OrderingException
                            ("non LAST_POSSIBLE_SPLICEABLE appears after a" +
                             " LAST_POSSIBLE_SPLICEABLE.");
                } else if ((null != lastGoodSpliceable) &&
                           (0 > spliceable.compareTo(lastGoodSpliceable))) {
                    throw new OrderingException("List is not well ordered.");
                } else {
                    lastGoodSpliceable = spliceable;
                }
            }
        }

        // If list is well ordered added each element.
        final Iterator pushIterator = spliceables.iterator();
        while (pushIterator.hasNext()) {
            push((Spliceable) pushIterator.next());
        }
        return this;
    }

    public synchronized StrandTail push(Spliceable spliceable)
            throws OrderingException,
                   ClosedStrandException
    {
        if (closed) {
            throw new ClosedStrandException("The associated Strand has" +
                                            " already been closed");
        }

        if (null == spliceable) {
            throw new NullPointerException();
        }

        if (0 == LAST_POSSIBLE_SPLICEABLE.compareTo(spliceable)) {

            // Only allow one section to exist.
            if (!lastPossibleInEffect) {
                lastSpliceable = null;
                beginSection();
                prepareTransfer();
                lastPossibleInEffect = true;
            }
            return this;
        }

        if (lastPossibleInEffect) {
            throw new OrderingException("Splicer has not stopped since" +
                                        " LAST_POSSIBLE_SPLICEABLE was" +
                                        "pushed.");
        }

        if ((null != lastSpliceable) &&
            (0 > spliceable.compareTo(lastSpliceable))) {
            throw new OrderingException("Spliceable is not well ordered.");
        }

        prepareTransfer();

        contents.add(spliceable);
        tailOffsets[section]++;
        lastSpliceable = spliceable;
        return this;
    }

    private void prepareTransfer()
    {
        if (!transferPending) {
            invocable.invokeLater(new Runnable()
            {
                public void run()
                {
                    transfer();
                }
            });
            transferPending = true;
        }
    }

    /**
     * Clears the next LAST_POSSIBLE_SPLICEABLE if it exists.
     */
    synchronized void clearLastPossible()
    {
        lastPossibleInEffect = false;

        if (0 == section) {
            return;
        }

        // Shift all offsets to the previous section
        final int currentTail = tailOffsets[0];
        for (int index = 0;
             section != index;
             index++) {
            tailOffsets[index] = tailOffsets[index + 1];
        }
        section--;

        // Add back on the number of Spliceables still in the Strand.
        tailOffsets[0] += currentTail + 1;
    }

    public int size()
    {
        synchronized (unwovenLock) {
            return tailOffsets[0] + 1 + unwovenSize;
        }
    }

    /**
     * Transfer the current Spliceables into the associated {@link
     * StrandImpl}.
     */
    private synchronized void transfer()
    {
        // If required, do not transfer the last Splicable in the tail so that
        // it does not get presented to the SpliceableAnalysis and thus can not
        // be recycled.

        // current algorithm is based on limitation of only one section.
        final Object keptLastSplicable;
        if ((safeMode) &&
            (!closed) &&
            (0 == section)) {
            final int eventCount = contents.size();
            if (0 == eventCount) {
                return;
            }
            keptLastSplicable = contents.remove(eventCount - 1);
            tailOffsets[section]--;
        } else {
            keptLastSplicable = null;
        }

        if (strand.push(contents,
                        tailOffsets,
                        section + 1)) {
            contents.clear();
            tailOffsets[0] = -1;
            section = 0;
        }

        // If last Spliceable was not transfered, restore it into the data
        // structure.
        if (null != keptLastSplicable) {
            contents.add(keptLastSplicable);
            tailOffsets[section]++;
        }

        transferPending = false;
    }

    void updateUnwoven(int size,
                       Spliceable head)
    {
        synchronized (unwovenLock) {
            unwovenSize = size;
            unwovenHead = head;
        }
    }

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}