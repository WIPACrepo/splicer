/*
 * class: SplicerChangedEvent
 *
 * Version $Id: SplicerChangedEvent.java 15513 2015-04-20 19:02:50Z dglo $
 *
 * Date: September 5 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer;

import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;

/**
 * This class represents a change in the state of a {@link Splicer Splicer}
 * object.
 *
 * @author patton
 * @version $Id: SplicerChangedEvent.java,v 1.4 2004/03/12 17:19:40 patton Exp
 *          $
 */
public final class SplicerChangedEvent
        extends EventObject
{

    // private instance member data

    /**
     * The state before the evnt.
     */
    private final int oldState;

    /**
     * The state after the event.
     */
    private final int newState;

    /**
     * The Spliceable, if any, related to the event.
     */
    private Spliceable spliceable;

    /**
     * The Collection of Spliceables, if any, related to the event.
     */
    private Collection allSpliceables;

    // constructors

    /**
     * Create an instance of this class when the state of the Splicer has
     * changed.
     *
     * @param source the object generating this event.
     * @param oldState the original state of the source.
     * @param newState the new state of the sourcce.
     */
    SplicerChangedEvent(Object source,
                        int oldState,
                        int newState)
    {
        super(source);
        this.oldState = oldState;
        this.newState = newState;
    }

    /**
     * Create an instance of this class related to a particular Spliceable.
     *
     * @param source the object generating this event.
     * @param state the state of the source when the evetn occured.
     * @param spliceable the Spliceable related to the event.
     * @param allSpliceables the Collection Spliceable related to the event.
     * <em>Note:</em> This Collection is not copied, but is made immutable!
     */
    public SplicerChangedEvent(Object source,
                        int state,
                        Spliceable spliceable,
                        Collection allSpliceables)
    {
        super(source);
        oldState = state;
        newState = state;
        this.spliceable = spliceable;
        this.allSpliceables =
                Collections.unmodifiableCollection(allSpliceables);
    }

    // instance member method (alphabetic)

    /**
     * Returns the Collection of Spliceables, if any, related to the event. If
     * no Collection is related to the event then <code>null</code> is
     * returned. The Collection may be empty.
     * <p>
     * <em>Note:</em> This Collection is immutable and should not be used once
     * the event has been handled. If the clioent need to keep this information
     * it should be copied into a new list. (This keep unnecessary copying to a
     * minimum and thus keeps the Splicer efficient.)
     *
     * @return the Collection of Spliceables, if any, related to the event.
     */
    public Collection getAllSpliceables()
    {
        return allSpliceables;
    }

    /**
     * Returns the state after the change.
     *
     * @return the state after the change.
     */
    public int getNewState()
    {
        return newState;
    }

    /**
     * Returns the state before the change.
     *
     * @return the state before the change.
     */
    public int getOldState()
    {
        return oldState;
    }

    /**
     * Returns the Spliceable, if any, related to the event. If no Spliceable
     * is related to the event then <code>null</code> is returned.
     *
     * @return the Spliceable, if any, related to the event.
     */
    public Spliceable getSpliceable()
    {
        return spliceable;
    }
}
