/*
 * class: EventNumberPropagator
 *
 * Version $Id: EventNumberPropagator.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: October 21 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer.examples;

import icecube.daq.splicer.Splicer;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to propagate an event number between different components
 * of the Event example of {@link Splicer Slicer} usage.
 *
 * @author patton
 * @version $Id: EventNumberPropagator.java,v 1.4 2004/08/04 20:41:56 patton
 *          Exp $
 */
class EventNumberPropagator
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    // private static member data

    // private instance member data

    /**
     * The list of event numbers that have been propagated to this object.
     */
    private final List numbers = new ArrayList();

    /**
     * True if all the event numbers have been propagated.
     */
    private boolean finished;

    // constructors

    /**
     * Create an instance of this class.
     */
    EventNumberPropagator()
    {
    }

    // instance member method (alphabetic)

    /**
     * Gets the current event number.
     *
     * @return the current event number, null if there none is available.
     */
    public synchronized Integer getNumber()
    {
        if (numbers.isEmpty()) {
            return null;
        }
        return (Integer) numbers.get(0);
    }

    /**
     * Returns true if all the event numbers have been propagated.
     *
     * @return true if all the event numbers have been propagated.
     */
    public synchronized boolean isFinished()
    {
        if (!numbers.isEmpty()) {
            return false;
        }
        return finished;
    }

    /**
     * States that the last event number is no longer needed and that its
     * propogration is complete.
     */
    public synchronized void next()
    {
        if (!numbers.isEmpty()) {
            numbers.remove(0);
        }
    }

    /**
     * Adds an event number to be propagated.
     *
     * @param eventNumber the event number to be propagated.
     */
    public synchronized void propagate(Integer eventNumber)
    {
        if (finished) {
            throw new IllegalStateException("Propagator has already" +
                                            " finished.");
        }
        numbers.add(eventNumber);
    }

    /**
     * Sets the finished flag.
     *
     * @param finished the value to which the flag should be set.
     */
    public synchronized void setFinished(boolean finished)
    {
        this.finished = finished;
    }



    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}
