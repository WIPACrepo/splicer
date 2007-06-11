package icecube.daq.hkn1;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import icecube.daq.splicer.HKN1Splicer;
import icecube.daq.splicer.Spliceable;
import icecube.daq.splicer.SpliceableFactory;
import icecube.daq.splicer.SplicedAnalysis;
import icecube.daq.splicer.SplicerListener;
import icecube.daq.splicer.Splicer;
import icecube.daq.splicer.SplicerChangedEvent;
import icecube.daq.splicer.StrandTail;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

public class HKN1SplicerTest
{

    public HKN1SplicerTest()
    {
        BasicConfigurator.configure();
    }

    @Test
    public void basicUnloadedTest() throws Exception
    {
        Analysis analysis = new Analysis();
        HKN1Splicer splicer = new HKN1Splicer(analysis);
        analysis.setSplicer(splicer);
        StrandTail tail0 = splicer.beginStrand();
        StrandTail tail1 = splicer.beginStrand();
        splicer.start();
        Random r = new Random();
        final int numObjs = 100;
        for (int i = 0; i < numObjs; i++)
        {
            TimeStamp obj = new TimeStamp(i + 1);
            if (r.nextBoolean()) {
                tail1.push(obj);
            } else {
                tail0.push(obj);
            }
            //if (i % 30 == 0) Thread.sleep(100);
        }
        tail0.push(Splicer.LAST_POSSIBLE_SPLICEABLE);
        tail1.push(Splicer.LAST_POSSIBLE_SPLICEABLE);
        Thread.sleep(100);
        splicer.stop();
        for (int i = 0; i < 10 && analysis.getOutputCount() < numObjs; i++) {
            Thread.sleep(100);
        }
        //System.out.println("splicer contents = " + splicer.getCount());
        assertTrue(analysis.isOrdered());
        assertEquals(numObjs, analysis.getOutputCount());
    }

}

class Analysis implements SplicedAnalysis, SplicerListener
{
    private boolean unsorted;
    private int outputCount;
    private int listOffset;
    private Splicer splicer;
    private TimeStamp lastObj;

    Analysis()
    {
    }

    public void disposed(SplicerChangedEvent event)
    {
        throw new Error("Unimplemented");
    }

    /**
     * A trivial execute method which simply truncates the splicer and makes
     * sure output is truly ordered.
     */
    public void execute(List splicedObjects, int decrement)
    {
        final int listLen = splicedObjects.size();
        for (int i = listOffset; i < listLen; i++)
        {
            TimeStamp obj = (TimeStamp) splicedObjects.get(i);

            if (lastObj != null)
            {
                if (obj.compareTo(lastObj) < 0)
                {
                    System.err.println(
                            "ERROR: objects exiting splicer are not ordered: ["
                            + lastObj.timestamp + ", " 
                            + obj.timestamp + "].");
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

        //System.out.println("execute called with # items = " + listLen + " decrement = " + decrement);
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

class TimeStamp implements Spliceable
{
    public long timestamp;

    public TimeStamp(long val)
    {
        timestamp = val;
    }

    public int compareTo(Object o)
    {
        
        TimeStamp ts = (TimeStamp) o;
        if (timestamp < ts.timestamp) return -1;
        if (timestamp > ts.timestamp) return +1;
        return 0;
    }

    public String toString()
    {
        return Long.toString(timestamp);
    }
}
