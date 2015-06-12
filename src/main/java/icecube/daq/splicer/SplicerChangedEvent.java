/*
 * class: SplicerChangedEvent
 *
 * Version $Id: SplicerChangedEvent.java 15570 2015-06-12 16:19:32Z dglo $
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
public final class SplicerChangedEvent<T>
        extends EventObject
{
    /** The state before the event. */
    private final Splicer.State oldState;

    /** The state after the event. */
    private final Splicer.State newState;

    /** The object, if any, related to the event. */
    private T spliceable;

    /** The Collection of objects, if any, related to the event. */
    private Collection<T> allSpliceables;

    /**
     * Create an instance of this class when the state of the Splicer has
     * changed.
     *
     * @param source the object generating this event.
     * @param oldState the original state of the source.
     * @param newState the new state of the sourcce.
     */
    public SplicerChangedEvent(Splicer source,
                               Splicer.State oldState,
                               Splicer.State newState)
    {
        super(source);
        this.oldState = oldState;
        this.newState = newState;
    }

    /**
     * Create an instance of this class related to a particular object.
     *
     * @param source the object generating this event.
     * @param state the state of the source when the evetn occured.
     * @param spliceable the object related to the event.
     * @param allSpliceables the Collection of objects related to the event.
     * <em>Note:</em> This Collection is not copied, but is made immutable!
     */
    public SplicerChangedEvent(Splicer source,
                               Splicer.State state,
                               T spliceable,
                               Collection<T> allSpliceables)
    {
        super(source);
        oldState = state;
        newState = state;
        this.spliceable = spliceable;
        this.allSpliceables =
                Collections.unmodifiableCollection(allSpliceables);
    }

    /**
     * Returns the Collection of objects, if any, related to the event. If
     * no Collection is related to the event then <code>null</code> is
     * returned. The Collection may be empty.
     * <p>
     * <em>Note:</em> This Collection is immutable and should not be used once
     * the event has been handled. If the clioent need to keep this information
     * it should be copied into a new list. (This keep unnecessary copying to a
     * minimum and thus keeps the Splicer efficient.)
     *
     * @return the Collection of objects, if any, related to the event.
     */
    public Collection<T> getAllSpliceables()
    {
        return allSpliceables;
    }

    /**
     * Returns the state after the change.
     *
     * @return the state after the change.
     */
    public Splicer.State getNewState()
    {
        return newState;
    }

    /**
     * Returns the state before the change.
     *
     * @return the state before the change.
     */
    public Splicer.State getOldState()
    {
        return oldState;
    }

    /**
     * Returns the object, if any, related to the event. If no object
     * is related to the event then <code>null</code> is returned.
     *
     * @return the object, if any, related to the event.
     */
    public T getSpliceable()
    {
        return spliceable;
    }
}
