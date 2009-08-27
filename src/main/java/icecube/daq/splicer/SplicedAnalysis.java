/*
 * interface: SplicedAnalysis
 *
 * Version $Id: SplicedAnalysis.java 4267 2009-06-05 19:11:27Z dglo $
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
 * @version $Id: SplicedAnalysis.java 4267 2009-06-05 19:11:27Z dglo $
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
}
