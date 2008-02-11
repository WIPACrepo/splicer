/*
 * class: MonitorPoints
 *
 * Version $Id: MonitorPoints.java 2631 2008-02-11 06:27:31Z dglo $
 *
 * Date: August 4 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.splicer;

import icecube.icebucket.monitor.ScalarFlowMonitor;

/**
 * This class allows clients to monitor performance statistics of an associated
 * {@link Splicer Splicer} object.
 *
 * @author patton
 * @version $Id: MonitorPoints.java 2631 2008-02-11 06:27:31Z dglo $
 */
public class MonitorPoints
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    private static final float[] NULL_HISTORY = new float[]{0};

    private static final ScalarFlowMonitor NULL_FLOW = new ScalarFlowMonitor()
    {
        public void dispose()
        {
        }

        public long getTotal()
        {
            return 0;
        }

        public float[] getHistory()
        {
            return NULL_HISTORY;
        }

        public void measure(int count)
        {
        }

        public void reset()
        {
        }

        public float getRate()
        {
            return 0;
        }
    };

    // private static member data

    // private instance member data

    /**
     * The ScalarFlowMonitor object measuring input byte flow.
     */
    private final ScalarFlowMonitor inputByteFlow;

    /**
     * The ScalarFlowMonitor object measuring input object flow.
     */
    private final ScalarFlowMonitor inputObjectFlow;

    /**
     * The ScalarMonitor object measuring objects analyzed.
     */
    private final ScalarFlowMonitor outputObjectFlow;

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param inputByteFlow the ScalarFlowMonitor object measuring input byte
     * flow.
     * @param inputObjectFlow the ScalarFlowMonitor object measuring input
     * object flow.
     * @param outputObjectFlow the ScalarFlowMonitor object measuring analyzed
     * object flow.
     */
    MonitorPoints(ScalarFlowMonitor inputByteFlow,
                  ScalarFlowMonitor inputObjectFlow,
                  ScalarFlowMonitor outputObjectFlow)
    {
        if (null == inputByteFlow) {
            this.inputByteFlow = NULL_FLOW;
        } else {
            this.inputByteFlow = inputByteFlow;
        }
        if (null == inputObjectFlow) {
            this.inputObjectFlow = NULL_FLOW;
        } else {
            this.inputObjectFlow = inputObjectFlow;
        }
        if (null == outputObjectFlow) {
            this.outputObjectFlow = NULL_FLOW;
        } else {
            this.outputObjectFlow = outputObjectFlow;
        }
    }

    // instance member method (alphabetic)

    /**
     * Returns the history of bytes read by the associated Splicer object.
     *
     * @return the hostory of bytes read by the associated Splicer object.
     */
    public float[] getInputByteHistory()
    {
        return inputByteFlow.getHistory();
    }


    /**
     * Returns the rate of bytes read by the associated Splicer object.
     *
     * @return the rate of bytes read by the associated Splicer object.
     */
    public float getInputByteRate()
    {
        return inputByteFlow.getRate();
    }

    /**
     * Returns the total number of bytes read by the associated Splicer
     * object.
     *
     * @return the total number of bytes read by the associated Splicer
     *         object.
     */
    public long getInputByteTotal()
    {
        return inputByteFlow.getTotal();
    }

    /**
     * Returns the history of objects pushed into the associated Splicer object.
     *
     * @return the history of objects pushed into the associated Splicer object.
     */
    public float[] getInputObjectHistory()
    {
        return inputObjectFlow.getHistory();
    }


    /**
     * Returns the rate of objects pushed into the associated Splicer object.
     *
     * @return the rate of objects pushed into the associated Splicer object.
     */
    public float getInputObjectRate()
    {
        return inputObjectFlow.getRate();
    }

    /**
     * Returns the total number of objects pushed into the associated Splicer
     * object.
     *
     * @return the total number of objects pushed into the associated Splicer
     *         object.
     */
    public long getInputObjectTotal()
    {
        return inputObjectFlow.getTotal();
    }

    /**
     * Returns the history of objects analyzed by the associated Splicer object.
     *
     * @return the history of objects analyzed by the associated Splicer object.
     */
    public float[] getOutputObjectHistory()
    {
        return outputObjectFlow.getHistory();
    }


    /**
     * Returns the rate of objects analyzed by the associated Splicer object.
     *
     * @return the rate of objects analyzed by the associated Splicer object.
     */
    public float getOutputObjectRate()
    {
        return outputObjectFlow.getRate();
    }

    /**
     * Returns the total number of objects analyzed by the associated Splicer
     * object.
     *
     * @return the total number of objects analyzed by the associated Splicer
     *         object.
     */
    public long getOutputObjectTotal()
    {
        return outputObjectFlow.getTotal();
    }

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}
