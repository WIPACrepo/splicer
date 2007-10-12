/*
 * class: MonitoredAnalysis
 *
 * Version $Id: MonitoredAnalysis.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: August 19 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

import icecube.icebucket.monitor.ScalarFlowMonitor;
import icecube.icebucket.monitor.simple.ScalarFlowMonitorImpl;

import java.util.List;

/**
 * This class decorates an existing SplicedAnalysis to measure the rates at
 * which data is being analyzed.
 *
 * @author patton
 * @version $Id: MonitoredAnalysis.java 2125 2007-10-12 18:27:05Z ksb $
 */
public class MonitoredAnalysis
        implements SplicedAnalysis
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    // private static member data

    // private instance member data

    /**
     * The object tracking the flow of objects.
     */
    private ScalarFlowMonitor flowMonitor;

    /**
     * The previous size of the List of Spliceable objects.
     */
    private int previousSize;

    /**
     * The SplicedAnalysis instance being decorated.
     */
    private final SplicedAnalysis splicedAnalysis;

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param splicedAnalysis the SplicerAnalysis object being monitored.
     */
    public MonitoredAnalysis(SplicedAnalysis splicedAnalysis)
    {
        this(splicedAnalysis,
             new ScalarFlowMonitorImpl());

    }

    /**
     * Create an instance of this class.
     *
     * @param splicedAnalysis the SplicerAnalysis object being monitored.
     */
    public MonitoredAnalysis(SplicedAnalysis splicedAnalysis,
                             ScalarFlowMonitor flowMonitor)
    {
        this.splicedAnalysis = splicedAnalysis;
        this.flowMonitor = flowMonitor;

    }

    // instance member method (alphabetic)

    public void execute(List splicedObjects,
                        int decrement)
    {
        flowMonitor.measure(splicedObjects.size() + decrement - previousSize);
        previousSize = splicedObjects.size();
        splicedAnalysis.execute(splicedObjects,
                                decrement);
    }

    public SpliceableFactory getFactory()
    {
        return splicedAnalysis.getFactory();
    }

    /**
     * Returns the history of objects analyzed by the associated Splicer object.
     *
     * @return the history of objects analyzed by the associated Splicer object.
     */
    public float[] getObjectHistory()
    {
        return flowMonitor.getHistory();
    }

    /**
     * Returns the rate of objects analyzed by the associated Splicer object.
     *
     * @return the rate of objects analyzed by the associated Splicer object.
     */
    public float getObjectRate()
    {
        return flowMonitor.getRate();
    }

    /**
     * Returns the total number of objects analyzed by the associated Splicer
     * object.
     *
     * @return the total number of objects analyzed by the associated Splicer
     *         object.
     */
    public long getObjectTotal()
    {
        return flowMonitor.getTotal();
    }

    /**
     * Reset this object so it behaves as if it was just created.
     */
    void reset()
    {
        flowMonitor.reset();
    }

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}