/*
 * interface: SplicerListener
 *
 * Version $Id: SplicerListener.java 13347 2011-09-09 19:08:37Z seshadrivija $
 *
 * Date: September 12 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer;

/**
 * This interface is used by the {@link Splicer} to inform clients of changes
 * that occurs during its lifecycle.
 *
 * @author patton
 * @version $Id: SplicerListener.java 13347 2011-09-09 19:08:37Z seshadrivija $
 */
public interface SplicerListener
{

    // public static final member data

    // instance member method (alphabetic)

    /**
     * Called when the {@link Splicer Splicer} enters the disposed state.
     *
     * @param event the event encapsulating this state change.
     */
    void disposed(SplicerChangedEvent event);

    /**
     * Called when the {@link Splicer Splicer} enters the failed state.
     *
     * @param event the event encapsulating this state change.
     */
    void failed(SplicerChangedEvent event);

    /**
     * Called when the {@link Splicer Splicer} enters the starting state.
     *
     * @param event the event encapsulating this state change.
     */
    void starting(SplicerChangedEvent event);

    /**
     * Called when the {@link Splicer Splicer} enters the started state.
     *
     * @param event the event encapsulating this state change.
     */
    void started(SplicerChangedEvent event);

    /**
     * Called when the {@link Splicer Splicer} enters the stopped state.
     *
     * @param event the event encapsulating this state change.
     */
    void stopped(SplicerChangedEvent event);

    /**
     * Called when the {@link Splicer Splicer} enters the stopping state.
     *
     * @param event the event encapsulating this state change.
     */
    void stopping(SplicerChangedEvent event);

    /**
     * Called when the {@link Splicer Splicer} has truncated its "rope". This
     * method is called whenever the "rope" is cut, for example to make a clean
     * start from the frayed beginning of a "rope" or cutting the rope when
     * reaching the Stopped state. This is not only invoked as the result of
     * the {@link Splicer#truncate(Spliceable)} method being invoked.
     * <p/>
     * This enables the client to be notified as to which Spliceable are never
     * going to be accessed again by the Splicer.
     * <p/>
     * When entering the Stopped state the 
     * {@link SplicerChangedEvent#getSpliceable()}
     * method will return the {@link Splicer#LAST_POSSIBLE_SPLICEABLE} object.
     *
     * @param event the event encapsulating this truncation.
     */
    void truncated(SplicerChangedEvent event);
}
