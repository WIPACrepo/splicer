/*
 * interface: SplicedAnalysis
 *
 * Version $Id: SplicedAnalysis.java 15570 2015-06-12 16:19:32Z dglo $
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
 * @version $Id: SplicedAnalysis.java 15570 2015-06-12 16:19:32Z dglo $
 */
public interface SplicedAnalysis<T>
{
    /**
     * Called by the {@link Splicer Splicer} to analyze the
     * List of objects provided.
     *
     * @param splicedObjects a List of objects.
     */
    void analyze(List<T> splicedObjects);
}
