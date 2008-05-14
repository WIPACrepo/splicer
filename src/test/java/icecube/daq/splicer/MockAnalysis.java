package icecube.daq.splicer;

import icecube.daq.splicer.SplicedAnalysis;
import icecube.daq.splicer.Splicer;
import icecube.daq.splicer.SplicerChangedEvent;
import icecube.daq.splicer.SplicerListener;

import java.util.List;

public class MockAnalysis implements SplicedAnalysis, SplicerListener
{
    private boolean unsorted;
    private int outputCount;
    private int listOffset;
    private Splicer splicer;
    private TimeStamp lastObj;

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
        for (int i = listOffset; i < listLen; i++)
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

        if (listLen > 0)
        {
            if (listLen >= listOffset) listOffset = listLen;
            splicer.truncate((Spliceable) splicedObjects.get(listLen - 1));
        }
    }

    public void failed(SplicerChangedEvent event)
    {
        throw new Error("Unimplemented");
    }

    public SpliceableFactory getFactory()
    {
        return null;
    }

    public int getOutputCount() { return outputCount; }
    public boolean isOrdered() { return !unsorted; }

    public void setSplicer(Splicer splicer)
    {
        this.splicer = splicer;
        splicer.addSplicerListener(this);
    }

    public void started(SplicerChangedEvent event) { }
    public void starting(SplicerChangedEvent event) { }
    public void stopped(SplicerChangedEvent event) { }
    public void stopping(SplicerChangedEvent event) { }

    public void truncated(SplicerChangedEvent event)
    {
        if (event.getSpliceable() == Splicer.LAST_POSSIBLE_SPLICEABLE) {
            listOffset = 0;
        } else {
            listOffset -= event.getAllSpliceables().size();
        }
    }
}
