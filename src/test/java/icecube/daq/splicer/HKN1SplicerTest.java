package icecube.daq.splicer;

import static org.junit.Assert.*;

import icecube.daq.splicer.HKN1Splicer;
import icecube.daq.splicer.Splicer;
import icecube.daq.splicer.StrandTail;

import java.util.Random;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.TTCCLayout;
import org.junit.Before;
import org.junit.Test;

public class HKN1SplicerTest
{

    public HKN1SplicerTest()
    {
        BasicConfigurator.resetConfiguration();

        ConsoleAppender appender = new ConsoleAppender(new TTCCLayout());
        appender.setName("TestAppender");
        appender.setThreshold(Level.FATAL);
        appender.activateOptions();

        BasicConfigurator.configure(appender);
    }

    @Test
    public void basicUnloadedTest() throws Exception
    {
        MockAnalysis analysis = new MockAnalysis();
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
        }
        tail0.push(Splicer.LAST_POSSIBLE_SPLICEABLE);
        tail1.push(Splicer.LAST_POSSIBLE_SPLICEABLE);
        Thread.sleep(100);
        splicer.stop();
        for (int i = 0; i < 10 && analysis.getOutputCount() < numObjs; i++) {
            Thread.sleep(100);
        }
        assertTrue(analysis.isOrdered());
        assertEquals(numObjs, analysis.getOutputCount());
    }


    @Test
    public void subthresholdTest() throws Exception
    {
        MockAnalysis analysis = new MockAnalysis(false);
        HKN1Splicer splicer = new HKN1Splicer(analysis);
        analysis.setSplicer(splicer);

        StrandTail tail0 = splicer.beginStrand();
        StrandTail tail1 = splicer.beginStrand();
        splicer.start();

        int numObjs = 40;

        for (int i = 1; i < 30; i++)
        {
            tail0.push(new TimeStamp(i));
        }

        tail1.push(new TimeStamp(30));

        Thread.sleep(100);

        tail1.push(new TimeStamp(31));
        tail1.push(new TimeStamp(32));
        tail1.push(new TimeStamp(33));

        splicer.truncate(new TimeStamp(4));

        tail0.push(new TimeStamp(34));

        Thread.sleep(100);

        splicer.truncate(new TimeStamp(6));
        splicer.truncate(new TimeStamp(8));
        splicer.truncate(new TimeStamp(11));
        splicer.truncate(new TimeStamp(13));
        splicer.truncate(new TimeStamp(15));

        tail0.push(new TimeStamp(35));
        tail0.push(new TimeStamp(36));
        tail0.push(new TimeStamp(37));
        tail0.push(new TimeStamp(38));
        tail0.push(new TimeStamp(39));

        tail1.push(new TimeStamp(40));

        Thread.sleep(100);

        tail0.push(Splicer.LAST_POSSIBLE_SPLICEABLE);
        tail1.push(Splicer.LAST_POSSIBLE_SPLICEABLE);

        Thread.sleep(100);

        splicer.stop();
        for (int i = 0; i < 10 && analysis.getOutputCount() < numObjs; i++) {
            Thread.sleep(100);
        }
        assertTrue(analysis.isOrdered());
        assertEquals(numObjs, analysis.getOutputCount());
    }
}
