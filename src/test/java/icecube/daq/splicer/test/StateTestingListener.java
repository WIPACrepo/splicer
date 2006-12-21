/*
 * class: StateTestingListener
 *
 * Version $Id: StateTestingListener.java,v 1.3 2005/08/23 19:48:22 patton Exp $
 *
 * Date: October 20 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer.test;

import icecube.daq.splicer.Splicer;
import icecube.daq.splicer.SplicerChangedEvent;
import icecube.daq.splicer.SplicerListener;
import junit.framework.Assert;

/**
 * This class ...does what?
 *
 * @author patton
 * @version $Id: StateTestingListener.java,v 1.2 2004/08/04 20:41:56 patton Exp
 *          $
 */
public class StateTestingListener
        extends Assert
        implements SplicerListener
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    // private static member data

    // private instance member data

    /**
     * The last sate seen by this object.
     */
    private int currentState;

    // constructors

    /**
     * Create an instance of this class.
     */
    StateTestingListener()
    {
    }

    // instance member method (alphabetic)

    public void disposed(SplicerChangedEvent event)
    {
        currentState = event.getNewState();
        assertEquals(Splicer.DISPOSED,
                     currentState);
        final int oldState = event.getOldState();
        assertTrue(Splicer.FAILED == oldState ||
                   Splicer.STOPPED == oldState);
    }

    public void failed(SplicerChangedEvent event)
    {
        currentState = event.getNewState();
        assertEquals(Splicer.FAILED,
                     currentState);
        final int oldState = event.getOldState();
        assertTrue(Splicer.FAILED == oldState ||
                   Splicer.STARTING == oldState ||
                   Splicer.STARTED == oldState ||
                   Splicer.STOPPING == oldState);
    }

    public void testState(int state)
    {
        assertEquals("Mismatch in states.",
                     currentState,
                     state);
    }

    public void starting(SplicerChangedEvent event)
    {
        currentState = event.getNewState();
        assertEquals(Splicer.STARTING,
                     currentState);
        final int oldState = event.getOldState();
        assertTrue(Splicer.FAILED == oldState ||
                   Splicer.STOPPED == oldState);
    }

    public void started(SplicerChangedEvent event)
    {
        currentState = event.getNewState();
        assertEquals(Splicer.STARTED,
                     currentState);
        final int oldState = event.getOldState();
        assertTrue(Splicer.STARTING == oldState);
    }

    public void stopping(SplicerChangedEvent event)
    {
        currentState = event.getNewState();
        assertEquals(Splicer.STOPPING,
                     currentState);
        final int oldState = event.getOldState();
        assertTrue(Splicer.FAILED == oldState ||
                   Splicer.STARTING == oldState ||
                   Splicer.STARTED == oldState);
    }

    public void stopped(SplicerChangedEvent event)
    {
        currentState = event.getNewState();
        assertEquals(Splicer.STOPPED,
                     currentState);
        final int oldState = event.getOldState();
        assertTrue(Splicer.STOPPING == oldState);
    }


    public void truncated(SplicerChangedEvent event)
    {
    }

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}
