/*
 * interface: SplicedAnalysis
 *
 * Version $Id: SplicedAnalysis.java,v 1.6 2005/08/09 17:21:15 patton Exp $
 *
 * Date: September 4 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer;

import java.util.List;

/**
 * This interface defines the the methods that must be implemented by any
 * analysis that wants to run on the results of a {@link Splicer Splicer}
 * object.
 *
 * @author patton
 * @version $Id: SplicedAnalysis.java,v 1.6 2005/08/09 17:21:15 patton Exp $
 */
public interface SplicedAnalysis
{

    // instance member method (alphabetic)

    /**
     * Called by the {@link Splicer Splicer} to analyze the
     * List of Spliceable objects provided.
     *
     * @param splicedObjects a List of Spliceable objects.
     * @param decrement the decrement of the indices in the List since the last
     * invocation.
     */
    void execute(List splicedObjects,
                 int decrement);

    /**
     * Returns the {@link SpliceableFactory} that should be used to create the
     * {@link Spliceable Spliceable} objects used by this
     * object.
     *
     * @return the SpliceableFactory that creates Spliceable objects.
     */
    SpliceableFactory getFactory();
}
