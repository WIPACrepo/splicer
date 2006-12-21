/*
 * interface: StrandManager
 *
 * Version $Id: StrandManager.java,v 1.3 2005/08/24 16:46:34 patton Exp $
 *
 * Date: August 1 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

/**
 * This interface is used by a ManagedStrand as the mean to signal when its
 * state has changed.
 *
 * @author patton
 * @version $Id: StrandManager.java,v 1.3 2005/08/24 16:46:34 patton Exp $
 */
interface StrandManager
{

    // public static final member data

    // instance member method (alphabetic)

    /**
     * Tells this object that the specified ManagedStrand has become empty.
     *
     * @param strand the ManagedStrand that has become empty.
     */
    void strandBecameEmpty(ManagedStrand strand);

    /**
     * Tells this object that the specified ManagedStrand has become
     * removable.
     * <p/>
     * The {@link ManagedStrand#isRemoveableAsEmpty()} method for the specified
     * strand must be true when this method is invoked.
     *
     * @param strand the ManagedStrand that is removable.
     */
    void strandBecameRemoveable(ManagedStrand strand);

    /**
     * Tells this object that the specified ManagedStrand has closed.
     *
     * @param strand the ManagedStrand that has closed.
     */
    void strandClosed(ManagedStrand strand);

    /**
     * Tells this object that the specified ManagedStrand is no longer empty.
     *
     * @param strand the ManagedStrand that is no longer empty.
     */
    void strandNoLongerEmpty(ManagedStrand strand);

    // static member methods (alphabetic)

}
