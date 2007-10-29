/*
 * interface: Splicer
 *
 * Version $Id: Splicer.java 2205 2007-10-29 20:44:05Z dglo $
 *
 * Date: August 1 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.List;


/**
 * This interface is used to manage the weaving of one or more {@link Strand}s
 * of {@link Spliceable}s into a single ordered List which can then be
 * analyzed.
 * <p/>
 * A client uses an instance of this class by calling the {@link
 * #beginStrand()} methods for each Strand and then pushing Spliceables into
 * the {@link StrandTail} that is returned. These Spliceables are then put into
 * a single ordered List of Spliceables (called the "rope") which is handed to
 * the registered {@link SplicedAnalysis} object. This object's
 * <code>execute</code> method is automatically called whenever there is a
 * change in the rope, i.e. it either gets longer or shorter.
 * <p/>
 * It is the client's responsibility to truncate the "rope", i.e. removed the
 * lowest Spliceables from the rope, whenever they no longer need those
 * Spliceable. This is done using {@link #truncate(Spliceable)} method. Without
 * calls to this methods the "rope" will continue to grow indefinitely.
 * <p/>
 * It is also the client's responsibility to detect when Strands appear to have
 * stalled, i.e. stopped receiving SPliceables. One way this can be done is to
 * have the analysis object record the time whenever it is called and then
 * monitor this value to see how frequently it changes. If the client does
 * detect a stall, it can use the {@link #pendingStrands()} method to find out
 * which Strand this object is waiting on for more Spliceables.
 * <p/>
 * While the {@link SplicedAnalysis} object is called automatically whenever
 * the "rope" changes, there are times when a client want to invoke that
 * objects <code>execute</code> method when there has been no change in the
 * "rope", for example a resource that had been stopping the analysis from
 * proceeding has become available. In this case the client can invoke the
 * {@link #analyze()} method.
 * <p/>
 * <b>Warning:</b> The <code>analyze</code> method must never be called from
 * within it own analysis <code>execute</code> method as this may cause a
 * deadlock! (All <code>execute</code> invocation are allowed to execute in the
 * same Thread and thus calling <code>analyze</code> from within the
 * <code>execute</code> will block the Thread, while waiting for what could be
 * the same Thread to execute!.)
 *
 * @author patton
 * @version $Id: Splicer.java 2205 2007-10-29 20:44:05Z dglo $
 */
public interface Splicer
{

    // public static final member data

    /**
     * The return value of {@link #getState} when this object has failed.
     */
    int FAILED = 0;

    /**
     * The return value of {@link #getState} when this object is Stopped.
     */
    int STOPPED = FAILED + 1;

    /**
     * The return value of {@link #getState} when this object is Starting.
     */
    int STARTING = STOPPED + 1;

    /**
     * The return value of {@link #getState} when this object is Started.
     */
    int STARTED = STARTING + 1;

    /**
     * The return value of {@link #getState} when this object is Stopping.
     */
    int STOPPING = STARTED + 1;

    /**
     * The return value of {@link #getState} when this object has been
     * disposed.
     */
    int DISPOSED = STOPPING + 1;

    /**
     * The object representing the last possible Spliceable in the ordering.
     */
    Spliceable LAST_POSSIBLE_SPLICEABLE = new Spliceable()
    {
        public int compareSpliceable(Spliceable spl)
        {
            if (this == spl) {
                return 0;
            }
            return 1;
        }
        public String toString()
        {
            return "LAST_POSSIBLE_SPLICEABLE";
        }
    };

    // instance member method (alphabetic)

    /**
     * Adds the specified channel to this object so its data can be used to
     * construct Spliceable objects. The channel can only be added when this
     * object is in the Stopped state. If the channel has already been added
     * then this method will have no effect.
     * <p/>
     * The channel must implement the ReadableByteChannel interface.
     * <p/>
     * This method is optional, but should have a matching {@link
     * #removeSpliceableChannel(SelectableChannel)} method if it is
     * implemented. If it is not implemented then a UnsupportedOperationException
     * is thrown by this method.
     *
     * @param channel the channel to be added.
     * @throws IllegalArgumentException is channel does not implement
     * ReadableByteChannel interface.
     * @throws IOException if the channel can not be made non-blocking.
     * @throws UnsupportedOperationException if the implementation does not
     * support this method.
     */
    void addSpliceableChannel(SelectableChannel channel)
            throws IOException;

    /**
     * The specified SplicerListener will receive SplicerChangedEvent objects.
     *
     * @param listener the SplicerListener to add.
     */
    void addSplicerListener(SplicerListener listener);

    /**
     * Request that <code>execute</code> method of this object's {@link
     * SplicedAnalysis} is invoked with the current "rope". This method will
     * block until that method returns.
     * <p/>
     * It should be noted that the <code>execute</code> method may be executed
     * automatically between the time this method is invoked and requested
     * invocation of <code>execute</code> takes place. The automatic execution
     * will not affect this method, which <em>will continue to block</em> until
     * the requested execution has completed.
     * <p/>
     * <b>Warning:</b> This method must never be called from within it own
     * analysis <code>execute</code> method as this may cause a deadlock! (All
     * <code>execute</code> invocation are allowed to execute in the same
     * Thread and thus calling <code>analyze</code> from within the
     * <code>execute</code> will block the Thread, while waiting for what could
     * be the same Thread to execute!.)
     */
    void analyze();

    /**
     * Adds a new {@link Strand} to this object. The returned {@link
     * StrandTail} can be used by the client to push new {@link Spliceable}s
     * into the new Strand and to close that Strand when it is no longer
     * needed.
     *
     * @return the StrandTail used to push Spliceable into the new Strand.
     */
    StrandTail beginStrand();

    /**
     * Frees up the resources used by this object. After ths method has been
     * invoke the behavor of any method in this interface, except those
     * dealting directly with state, will be undetermined.
     */
    void dispose();

    /**
     * Requests that this object stop weaving data from all of its {@link
     * Strand}s.
     * <p/>
     * This method does not wait for Spliceables already pushed into this
     * object to be woven, but rather stops weaving as soon as possible. Those
     * Spliceable already pushed but not woven will be handled when this object
     * is re-started.
     * <p/>
     * If this object has already stopped then this method will have no
     * effect.
     */
    void forceStop();

    /**
     * Returns the {@link SplicedAnalysis} that is being used by this object.
     *
     * @return the {@link SplicedAnalysis} that is being used by this object.
     */
    SplicedAnalysis getAnalysis();

    /**
     * Returns the MonitorPoints object, if any, associated with this Splicer.
     * <p/>
     * This method is optional.
     * <p/>
     * Those implementations that do not support the Channel operations of a
     * Splicer should return "0" for the rate and total of bytes in the
     * returned object.
     *
     * @return the MonitorPoints object associated with this Splicer.
     * @throws UnsupportedOperationException if the implementation does not
     * support this method.
     */
    MonitorPoints getMonitorPoints();

    /**
     * Returns the current state of this object.
     *
     * @return the current state of this object.
     */
    int getState();

    /**
     * Returns a string describing the current state of this object.
     *
     * @return a string describing the current state of this object.
     */
    String getStateString();

    /**
     * Returns a string describing the specified state.
     *
     * @param state the state whose string is being requested.
     * @return a string describing the specified state.
     */
    String getStateString(int state);

    /**
     * Returns the number of open {@link Strand}s that are in this object.
     *
     * @return the number of open Strands.
     */
    int getStrandCount();

    /**
     * Returns the List of {@link SelectableChannel} objects on which this
     * object is waiting before it can weave any more rope.
     * <p/>
     * <b>Warning:</b> This method must never be called from within it own
     * analysis <code>execute</code> method as this may cause a deadlock. As
     * the results of this method are internal data from the Splicer, it may
     * need to finished executing any analysis before copy out this data and
     * thus could cause a deadlock.
     * <p/>
     * This method is optional, but should have a matching {@link
     * #addSpliceableChannel(SelectableChannel)} and {@link
     * #removeSpliceableChannel(SelectableChannel)} methods if it is
     * implemented. If it is not implemented then a UnsupportedOperationException
     * is thrown by this method.
     *
     * @return a List of StrandTail objects
     * @throws UnsupportedOperationException if the implementation does not
     * support this method.
     */
    List pendingChannels();

    /**
     * Returns the List of {@link StrandTail} objects on which this object is
     * waiting before it can weave any more rope.
     * <p/>
     * <b>Warning:</b> This method must never be called from within it own
     * analysis <code>execute</code> method as this may cause a deadlock. As
     * the results of this method are internal data from the Splicer, it may
     * need to finished executing any analysis before copy out this data and
     * thus could cause a deadlock.
     *
     * @return a List of StrandTail objects
     */
    List pendingStrands();

    /**
     * Removes the specified channel from this object so its data can no longer
     * be used in the construction of the List of Spliceable objects. The
     * channel can only be removed when this object is in the Stopped state. If
     * the channel has not been added then this method will have no effect.
     * <p/>
     * This method is optional, but should have a matching {@link
     * #addSpliceableChannel(SelectableChannel)} method if it is implemented.
     * If it is not implemented then a UnsupportedOperationException is thrown
     * by this method.
     *
     * @param channel the channel to be removed.
     * @throws UnsupportedOperationException if the implementation does not
     * support this method.
     */
    void removeSpliceableChannel(SelectableChannel channel);

    /**
     * The specified SplicerListener will no longer receive SplicerChangedEvent
     * objects.
     *
     * @param listener the SplicerListener to remove.
     */
    void removeSplicerListener(SplicerListener listener);

    /**
     * Requests that this object start weaving data from all of its {@link
     * Strand}s.
     * <p/>
     * This method will produce a "frayed" start such that there is no
     * guarantee that the initial Spliceables handed to the analysis object are
     * greater than or equal to the first Spliceable in each Strand. However it
     * is guaranteed that the analysis object will not be invoked until at
     * least one Spliceable has been seen in each Strand.
     * <p/>
     * If this object has already started, or is in the process of starting
     * then this method will have no effect.
     *
     * @throws IllegalStateException if this object is not in a state from
     * which it can be started.
     */
    void start();

    /**
     * Requests that this object start weaving data from all of its {@link
     * Strand}s.
     * <p/>
     * This method will produce a "clean cut" start such that all Strands have
     * at least one Spliceable that is less than or equal to the "beginning"
     * Spliceable. The "beginning" Spliceable is defined as the greater of
     * either the specified Spliceable or the first Spliceable in one Strand
     * which is greater than or equal to the first Spliceable in all other
     * Strands (excluding those Strand whose first Spliceable is a
     * LAST_POSSIBLE_SPLICEABLE). Neither <code>null</code> nor the
     * <code>LAST_POSSIBLE_SPLICEABLE</code> object are valid arguments and
     * will cause an exception to be thrown.
     * <p/>
     * If this object has already started, or is in the process of starting
     * then this method will have no effect.
     * <p/>
     * <em>note:</em> This method will discard and Spliceables that are less
     * than the "beginning" Spliceable.
     *
     * @param start all Spliceables handled to the analysis routine are
     * guaranteed to be greater than or euqal to this object.
     * @throws IllegalStateException if this object is not in a state from
     * which it can be started.
     * @throws IllegalArgumentException if the specified Spliceable is the
     * <code>LAST_POSSIBLE_SPLICEABLE</code> object.
     * @throws NullPointerException if <code>start</code> in null.
     */
    void start(Spliceable start);

    /**
     * Requests that this object stop weaving data from all of its {@link
     * Strand}s.
     * <p/>
     * This method will produce a "frayed" stop such that there is no guarantee
     * that the final Spliceables handed to the analysis object are less than
     * or equal to the last Spliceable in each Strand.
     * <p/>
     * If this object has already stopped, or is in the process of stopping,
     * then this method will have no effect.
     *
     * @throws IllegalStateException if this object is not in a state from
     * which it can be stopped.
     */
    void stop();

    /**
     * Requests that this object stop weaving data from all of its {@link
     * Strand}s.
     * <p/>
     * This method will produce a "clean cut" stop such that all Strands have
     * at least one Spliceable that is greater than the specified Spliceable.
     * For (hopefully) obvious reasons the means that neither <code>null</code>
     * nor the <code>LAST_POSSIBLE_SPLICEABLE</code> object are valid arguments
     * and will cause an exception to be thrown.
     * <p/>
     * If this object has already stopped, or is in the process of stopping,
     * then this method will have no effect.
     *
     * @param stop all Spliceables handled to the analysis routine are
     * guaranteed to be less than or euqal to this object.
     * @throws OrderingException if the specified stop is less than the last
     * Spliceable that was weaved (making the requested clean stop
     * impossible.)
     * @throws IllegalStateException if this object is not in a state from
     * which it can be stopped.
     * @throws IllegalArgumentException if the specified Spliceable is the
     * <code>LAST_POSSIBLE_SPLICEABLE</code> object.
     * @throws NullPointerException if <code>stop</code> in null.
     */
    void stop(Spliceable stop)
            throws OrderingException;

    /**
     * Truncates the "rope" such that only those Spliceable greater than or
     * equal to the specified Spliceable remain in the "rope". It is perfectly
     * safe to a call this methods from the <code>execute</code> of a {@link
     * SplicedAnalysis} object as it will not change the "rope" passed to that
     * method until tha method returns.
     *
     * @param spliceable the cut-off Spliceable.
     */
    void truncate(Spliceable spliceable);
}
