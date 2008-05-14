package icecube.daq.splicer;

import static org.junit.Assert.*;

import icecube.daq.splicer.HKN1TimeoutSplicer;
import icecube.daq.splicer.Splicer;
import icecube.daq.splicer.StrandTail;

import java.util.Random;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.TTCCLayout;
import org.junit.Before;
import org.junit.Test;

public class HKN1TimeoutSplicerTest
{

    public HKN1TimeoutSplicerTest()
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
        HKN1TimeoutSplicer splicer = new HKN1TimeoutSplicer(analysis);
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
    public void starvedTailTest() throws Exception
    {
        final int timeoutSecs = 1;

        final int reps = 6;
        for (int nTails = 4; nTails < 19; nTails++) {

            MockAnalysis analysis = new MockAnalysis();
            HKN1TimeoutSplicer splicer =
                new HKN1TimeoutSplicer(analysis, timeoutSecs);
            analysis.setSplicer(splicer);

            StrandTail[] tails = new StrandTail[nTails];
            for (int i = 0; i < tails.length; i++) {
                tails[i] = splicer.beginStrand();
            }

            splicer.start();

            int numTails = tails.length;

            final int halfway = reps / 2;

            int numSent = 0;
            for (int i = 0; i < reps; i++)
            {
                if (i == halfway) {
                    // stop sending data to last strand
                    numTails--;

                    // wait for nodes to *almost* go inactive
                    try {
                        Thread.sleep((timeoutSecs * 1000) - 10);
                    } catch (Exception ex) {
                        // ignore interrupts
                    }
                } else if (i > halfway) {
                    // slow down this thread
                    Thread.yield();
                }

                int num = numTails - 1;
                for (int n = 0; n <= num; n++) {
                    tails[n].push(new TimeStamp(++numSent));
                }
            }

            for (int i = 0; i < numTails; i++) {
                tails[i].push(Splicer.LAST_POSSIBLE_SPLICEABLE);
            }

            waitForSplicerStop(splicer, timeoutSecs);

            if (splicer.getState() != Splicer.STOPPED) {
                splicer.stop();
            }

            waitForSplicerStop(splicer, timeoutSecs);

            int numDropped;
            if (nTails == 4) {
                numDropped = 3;
            } else if (nTails == 5) {
                numDropped = 0;
            } else {
                numDropped = (((nTails - 2) % 4) + 1) * 3;
            }

            boolean expOrdered = (nTails != 5);

            assertEquals("Splicer is not stopped",
                         splicer.getState(), Splicer.STOPPED);
            if (expOrdered) {
                assertTrue("Output is not ordered", analysis.isOrdered());
            } else {
                assertFalse("Output is ordered", analysis.isOrdered());
            }

            assertEquals("Some data was lost",
                         numSent - numDropped, analysis.getOutputCount());
        }
    }

    private static void waitForSplicerStop(Splicer splicer, int timeoutSecs)
    {
        for (int i = 0;
             i < (timeoutSecs * 150) && splicer.getState() != Splicer.STOPPED;
             i++)
        {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                // ignore interrupts
            }
        }
    }
}
