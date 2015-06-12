/*
 * interface: Splicer
 *
 * Version $Id: Splicer.java 15570 2015-06-12 16:19:32Z dglo $
 *
 * Date: August 1 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

/**
 * This interface is used to manage the weaving of one or more {@link Strand}s
 * of objects into a single ordered List which can then be
 * analyzed.
 * <p>
 * A client uses an instance of this class by calling the {@link
 * #beginStrand()} methods for each Strand and then pushing objects into
 * the {@link StrandTail} that is returned. These objects are then put into
 * a single ordered List of objects (called the "rope") which is handed to
 * the registered {@link SplicedAnalysis} object. This object's
 * <code>analyse</code> method is automatically called whenever there is a
 * change in the rope, i.e. it either gets longer or shorter.
 * <p>
 * It is the client's responsibility to detect when Strands appear to have
 * stalled, i.e. stopped receiving Spliceables. One way this can be done is to
 * have the analysis object record the time whenever it is called and then
 * monitor this value to see how frequently it changes.
 * <p>
 * While the {@link SplicedAnalysis} object is called automatically whenever
 * the "rope" changes, there are times when a client want to invoke that
 * objects <code>analyse</code> method when there has been no change in the
 * "rope", for example a resource that had been stopping the analysis from
 * proceeding has become available. In this case the client can invoke the
 * <code>analyze()</code>} method.
 * <p>
 * <b>Warning:</b> The <code>analyze</code> method must never be called from
 * within it own analysis <code>analyse</code> method as this may cause a
 * deadlock! (All <code>analyse</code> invocation are allowed to execute in the
 * same Thread and thus calling <code>analyze</code> from within the
 * <code>analyse</code> will block the Thread, while waiting for what could be
 * the same Thread to execute!.)
 *
 * @author patton
 * @version $Id: Splicer.java 15570 2015-06-12 16:19:32Z dglo $
 */
public interface Splicer<T>
{
    /** List of all possible states */
    enum State {
        FAILED,
        STOPPED,
        STARTING,
        STARTED,
        STOPPING,
        DISPOSED
    };

    /**
     * The specified SplicerListener will receive SplicerChangedEvent objects.
     *
     * @param listener the SplicerListener to add.
     */
    void addSplicerListener(SplicerListener<T> listener);

    /**
     * Adds a new {@link Strand} to this object. The returned {@link
     * StrandTail} can be used by the client to push new objects
     * into the new Strand and to close that Strand when it is no longer
     * needed.
     *
     * @return the StrandTail used to push object into the new Strand.
     */
    StrandTail<T> beginStrand();

    /**
     * Frees up the resources used by this object. After ths method has been
     * invoke the behavor of any method in this interface, except those
     * dealting directly with state, will be undetermined.
     */
    void dispose();

    /**
     * Requests that this object stop weaving data from all of its {@link
     * Strand}s.
     * <p>
     * This method does not wait for objects already pushed into this
     * object to be woven, but rather stops weaving as soon as possible. Those
     * objects already pushed but not woven will be handled when this object
     * is re-started.
     * <p>
     * If this object has already stopped then this method will have no
     * effect.
     */
    void forceStop();

    /**
     * Returns the {@link SplicedAnalysis} that is being used by this object.
     *
     * @return the {@link SplicedAnalysis} that is being used by this object.
     */
    SplicedAnalysis<T> getAnalysis();

    /**
     * Returns the current state of this object.
     *
     * @return the current state of this object.
     */
    State getState();

    /**
     * Returns the number of open {@link Strand}s that are in this object.
     *
     * @return the number of open Strands.
     */
    int getStrandCount();

    /**
     * The specified SplicerListener will no longer receive SplicerChangedEvent
     * objects.
     *
     * @param listener the SplicerListener to remove.
     */
    void removeSplicerListener(SplicerListener<T> listener);

    /**
     * Requests that this object start weaving data from all of its {@link
     * Strand}s.
     * <p>
     * This method will produce a "frayed" start such that there is no
     * guarantee that the initial objects handed to the analysis object are
     * greater than or equal to the first object in each Strand. However it
     * is guaranteed that the analysis object will not be invoked until at
     * least one object has been seen in each Strand.
     * <p>
     * If this object has already started, or is in the process of starting
     * then this method will have no effect.
     *
     */
    void start();

    /**
     * Requests that this object stop weaving data from all of its {@link
     * Strand}s.
     * <p>
     * This method will produce a "frayed" stop such that there is no guarantee
     * that the final objects handed to the analysis object are less than
     * or equal to the last object in each Strand.
     * <p>
     * If this object has already stopped, or is in the process of stopping,
     * then this method will have no effect.
     *
     */
    void stop();
}
