/*
 * interface: ManagedStrand
 *
 * Version $Id: ManagedStrand.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: August 1 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

import icecube.icebucket.util.Invocable;

/**
 * This interface is used by the SplicerImpl to manage a Strand
 * implementation.
 *
 * @author patton
 * @version $Id: ManagedStrand.java 2125 2007-10-12 18:27:05Z ksb $
 */
interface ManagedStrand
{

    // public static final member data

    // instance member method (alphabetic)

    /**
     * Halts Spliceables any more Spliceables appearing in the Strand.
     * Spliceables already appearing in the Strand are temporarily removed. The
     * {@link #proceed()} method reverse the effect of this method and returns
     * any Spliceable that where originally appearing in the Strand by where
     * removed.
     * <p/>
     * If the Strand has already halted this method has no effect.
     *
     * @see #proceed()
     */
    void forceHalt();

    /**
     * Returns the Strand object being managed.
     *
     * @return the Strand object being managed.
     */
    Strand getStrand();

    /**
     * Returns the StrandTail that supplies the associated Strand object.
     *
     * @param invocable the Invocable which the StrandTail should use.
     * @return the StrandTail that supplies this object.
     * @throws IllegalArgumentException if the StrandTail has already been
     * bound to a different invocable.
     */
    StrandTail getTail(Invocable invocable);

    /**
     * Halts any new Spliceables from appearing in the Strand. The {@link
     * #proceed()} method reverse the effect of this method.
     * <p/>
     * If the Strand has already halted this method has no effect.
     *
     * @see #proceed()
     */
    void halt();

    /**
     * Halts Spliceables that are greater than the specified spliceable from
     * appearing in the Strand. Spliceables that less than or equal to the
     * specified Spliceable and already appearing in the Strand are temporarily
     * removed. The {@link #proceed()} method reverse the effect of this method
     * and returns any Spliceable that where originally appearing in the Strand
     * by where removed.
     * <p/>
     * If the Strand has already halted this method has no effect.
     * <p/>
     * <em>note:</em> If a Spliceable greater than the specified Spliceable has
     * already been pulled from the Strand then this method will stop any more
     * Spliceables appearing, but will not affect those Spliceables already
     * pulled.
     *
     * @param spliceable The limit above which Spliceables will not appear in
     * the Strand.
     * @see #proceed()
     */
    void halt(Spliceable spliceable);

    /**
     * Returns true if the managed Strand object is closed.
     *
     * @return true  if the managed Strand object is closed. is closed.
     */
    boolean isClosed();

    /**
     * Returns true if the managed Strand object can be removed from weaving
     * because it is empty.
     *
     * @return true if the Strand object can be removed from weaving.
     */
    boolean isRemoveableAsEmpty();

    /**
     * Allow the appearance of pushed Spliceables to proceed after they have
     * been halted.
     * <p/>
     * If the Strand is not halted this method has no effect.
     *
     * @see #halt()
     */
    void proceed();

    /**
     * Provides an opportunity to transfer any information from the weaving
     * thread to the client threads. (This allows information in the 'weaving'
     * Thread to be moved into the 'StrandTail' Thread.)
     */
    void transferToClientThread();

    // static member methods (alphabetic)

}
