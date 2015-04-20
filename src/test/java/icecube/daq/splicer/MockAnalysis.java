package icecube.daq.splicer;

import icecube.daq.splicer.SplicedAnalysis;
import icecube.daq.splicer.Splicer;
import icecube.daq.splicer.SplicerChangedEvent;
import icecube.daq.splicer.SplicerListener;

import java.util.List;

public class MockAnalysis implements SplicedAnalysis, SplicerListener
{
    private boolean truncInExec;
    private boolean unsorted;
    private int outputCount;
    private int listOffset;
    private Splicer splicer;
    private TimeStamp lastObj;

    /**
     * Count objects emitted from splicer and verify that they are ordered,
     * and implicitly truncate them.
     */
    public MockAnalysis()
    {
        this(true);
    }

    /**
     * Count objects emitted from splicer and verify that they are ordered.
     *
     * @param truncInExec if <tt>true</tt> truncate objects from within
     *                    the execute() method.  If <tt>false</tt>, truncate()
     *                    must be called explicitly.
     */
    public MockAnalysis(boolean truncInExec)
    {
        this.truncInExec = truncInExec;
    }

    /**
     * Unimplemented
     */
    public void disposed(SplicerChangedEvent event)
    {
        throw new Error("Unimplemented");
    }

    /**
     * A trivial execute method which simply truncates the splicer
     * and makes sure output is truly ordered.
     */
    public void execute(List splicedObjects, int decrement)
    {
        final int listLen = splicedObjects.size();

        int addIndex = listOffset - decrement;
        for (int i = addIndex; i < listLen; i++)
        {
            TimeStamp obj = (TimeStamp) splicedObjects.get(i);

            if (lastObj != null)
            {
                if (obj.compareSpliceable(lastObj) < 0)
                {
                    String errMsg = "ERROR: objects exiting splicer" +
                        " are not ordered: [" + lastObj.timestamp + ", " +
                        obj.timestamp + "].";
                    System.err.println(errMsg);
                    unsorted = true;
                }
            }

            lastObj = obj;
            outputCount++;
        }

        listOffset = listLen;

        if (truncInExec && listLen > 0)
        {
            splicer.truncate((Spliceable) splicedObjects.get(listLen - 1));
        }
    }

    /**
     * Unimplemented
     */
    public void failed(SplicerChangedEvent event)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the total number of objects which were emitted by the splicer.
     *
     * @return total number of objects
     */
    public int getOutputCount() { return outputCount; }

    /**
     * Were the results ordered?
     *
     * @return <tt>false</tt> if there were any out-of-order objects
     */
    public boolean isOrdered() { return !unsorted; }

    public void setSplicer(Splicer splicer)
    {
        this.splicer = splicer;
        splicer.addSplicerListener(this);
    }

    /**
     * Does nothing
     */
    public void started(SplicerChangedEvent event) { }

    /**
     * Does nothing
     */
    public void starting(SplicerChangedEvent event) { }

    /**
     * Does nothing
     */
    public void stopped(SplicerChangedEvent event) { }

    /**
     * Does nothing
     */
    public void stopping(SplicerChangedEvent event) { }

    /**
     * Does nothing
     */
    public void truncated(SplicerChangedEvent event) { }
}
