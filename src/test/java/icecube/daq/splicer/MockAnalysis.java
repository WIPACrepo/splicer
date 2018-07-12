package icecube.daq.splicer;

import java.util.List;

/**
 * Count objects emitted from splicer and verify that they are ordered.
 */
public class MockAnalysis
    implements SplicedAnalysis<Spliceable>, SplicerListener<Spliceable>
{
    private boolean truncInExec;
    private boolean unsorted;
    private int outputCount;
    private Splicer splicer;
    private Spliceable lastObj;

    /**
     * A trivial execute method which simply truncates the splicer
     * and makes sure output is truly ordered.
     */
    public void analyze(List<Spliceable> splicedObjects)
    {
        for (Spliceable obj : splicedObjects) {
            if (lastObj != null)
            {
                if (obj.compareSpliceable(lastObj) < 0)
                {
                    unsorted = true;

                    String errMsg = "ERROR: objects exiting splicer" +
                        " are not ordered: [" + lastObj + ", " +
                        obj + "].";
                    throw new Error(errMsg);
                }
            }

            lastObj = obj;
            outputCount++;
        }
    }

    /**
     * Unimplemented
     */
    public void disposed(SplicerChangedEvent<Spliceable> event)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     */
    public void failed(SplicerChangedEvent<Spliceable> event)
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
    public void started(SplicerChangedEvent<Spliceable> event) { }

    /**
     * Does nothing
     */
    public void starting(SplicerChangedEvent<Spliceable> event) { }

    /**
     * Does nothing
     */
    public void stopped(SplicerChangedEvent<Spliceable> event) { }

    /**
     * Does nothing
     */
    public void stopping(SplicerChangedEvent<Spliceable> event) { }
}
