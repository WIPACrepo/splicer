/*
 * interface: SplicerListener
 *
 * Version $Id: SplicerListener.java 15570 2015-06-12 16:19:32Z dglo $
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
 * @version $Id: SplicerListener.java 15570 2015-06-12 16:19:32Z dglo $
 */
public interface SplicerListener<T>
{
    /**
     * Called when the {@link Splicer Splicer} enters the disposed state.
     *
     * @param event the event encapsulating this state change.
     */
    void disposed(SplicerChangedEvent<T> event);

    /**
     * Called when the {@link Splicer Splicer} enters the failed state.
     *
     * @param event the event encapsulating this state change.
     */
    void failed(SplicerChangedEvent<T> event);

    /**
     * Called when the {@link Splicer Splicer} enters the starting state.
     *
     * @param event the event encapsulating this state change.
     */
    void starting(SplicerChangedEvent<T> event);

    /**
     * Called when the {@link Splicer Splicer} enters the started state.
     *
     * @param event the event encapsulating this state change.
     */
    void started(SplicerChangedEvent<T> event);

    /**
     * Called when the {@link Splicer Splicer} enters the stopped state.
     *
     * @param event the event encapsulating this state change.
     */
    void stopped(SplicerChangedEvent<T> event);

    /**
     * Called when the {@link Splicer Splicer} enters the stopping state.
     *
     * @param event the event encapsulating this state change.
     */
    void stopping(SplicerChangedEvent<T> event);
}
