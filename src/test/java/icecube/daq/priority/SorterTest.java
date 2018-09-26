package icecube.daq.priority;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

class BBConsumer
    implements DataConsumer<ByteBuffer>
{
    private static final Logger LOG = Logger.getLogger(BBConsumer.class);

    private ByteBuffer eos;

    private boolean unordered;
    private long numBuffersSeen;
    private long lastUT;

    BBConsumer(ByteBuffer eos)
    {
        this.eos = eos;
    }

    @Override
    public void consume(ByteBuffer buf) throws IOException
    {
        if (buf == eos) {
            return;
        }

        final long utc = buf.getLong(24);
        if (!unordered && lastUT > utc) unordered = true;
        lastUT = utc;

        synchronized (this) { numBuffersSeen++; }
        if (numBuffersSeen % 1000 == 0 && LOG.isDebugEnabled()) {
            LOG.debug("# buffers: " + numBuffersSeen);
        }
    }

    @Override
    public void endOfStream(long ignored)
        throws IOException
    {
        consume(eos);
    }

    void validate(Sorter sorter, int numThrd, int numChan, long numSent)
    {
        assertFalse(sorter.getName() + " time was not sorted correctly",
                    unordered);

        assertFalse("Queue contains " + sorter.getNumQueued() + " entries",
                     sorter.getNumQueued() > 0);

        final int numStops = Math.min(sorter.getNumSubsorters(), numThrd);
        assertEquals("Bad " + sorter.getName() + " number sent",
                     numBuffersSeen + numStops, sorter.getNumOutput());

        assertEquals("Bad " + sorter.getName() + " number rcvd",
                     numSent - numThrd, numBuffersSeen);
    }
}

public class SorterTest
{
    private static final ByteBuffer EOS = createStopMsg();

    private final int nch;
    private final static Logger LOG = Logger.getLogger(SorterTest.class);

    public SorterTest()
    {
        nch =
            Integer.getInteger("icecube.daq.bindery.SorterTest.channels", 16);
    }

    private static final ByteBuffer createStopMsg()
    {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(0, 4);
        return buf;
    }

    @BeforeClass
    public static void loggingSetUp()
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
    }

    private void runTest(BBConsumer consumer, Sorter<ByteBuffer> sorter,
                         int numChan, int numThrd, int chunkSize)
        throws SorterException
    {
        BufferGenerator[] genArr = new BufferGenerator[numThrd];

        for (int ch = 0; ch < genArr.length; ch++) {
            SortInput input = sorter.register(String.format("MB#%012x", ch));
            genArr[ch] = new BufferGenerator(ch, chunkSize * 10, input);
            genArr[ch].start();
        }

        sorter.start();

        for (int ch = 0; ch < genArr.length; ch++) {
            try {
                genArr[ch].join();
            } catch (InterruptedException ie) {
                // ignore interrupts
            }
        }

        sorter.waitForStop(10000);
        assertEquals("Buffer queue is not empty, sent " +
                     sorter.getNumOutput(), 0, sorter.getNumQueued());

        long numSent = 0;
        for (int ch = 0; ch < genArr.length; ch++) {
            numSent += genArr[ch].getNumberSent();
        }

        consumer.validate(sorter, numThrd, numChan, numSent);
    }

    @Test
    public void testBadMaxInputs()
        throws SorterException
    {
        final String name = "BadMaxIn";
        try {
            new Sorter<ByteBuffer>(name, 0, new ByteBuffComparator(EOS), null,
                                   EOS);
            fail("Should not succeed");
        } catch (SorterException se) {
            if (!se.getMessage().startsWith("Maximum number of " + name +
                                            " inputs"))
            {
                throw se;
            }
        }
    }

    @Test
    public void testNoEOS()
        throws SorterException
    {
        final String name = "NoEOS";
        try {
            new Sorter<ByteBuffer>(name, nch, new ByteBuffComparator(EOS), null,
                                   null);
            fail("Should not succeed");
        } catch (SorterException se) {
            if (!se.getMessage().startsWith(name + " end-of-stream marker")) {
                throw se;
            }
        }
    }

    @Test
    public void testBadNumThreads()
        throws SorterException
    {
        final String name = "BadNumThrd";
        try {
            new Sorter<ByteBuffer>(name, nch, new ByteBuffComparator(EOS), null,
                                   EOS, 0, 1234);
            fail("Should not succeed");
        } catch (SorterException se) {
            if (!se.getMessage().startsWith("Maximum number of " + name +
                                            " threads"))
            {
                throw se;
            }
        }
    }

    @Test
    public void testRegisterAfterStart()
        throws SorterException
    {
        final int numThrd = 3;
        final int chunkSize = 500;

        BBConsumer consumer = new BBConsumer(EOS);

        Sorter<ByteBuffer> sorter =
            new Sorter<ByteBuffer>("Custom", nch, new ByteBuffComparator(EOS),
                                   consumer, EOS, numThrd, chunkSize);

        BufferGenerator[] genArr = new BufferGenerator[numThrd];

        for (int ch = 0; ch < genArr.length; ch++) {
            SortInput input = sorter.register(String.format("MB#%012x", ch));
            genArr[ch] = new BufferGenerator(ch, chunkSize * 10, input);
            genArr[ch].start();
        }

        sorter.start();

        try {
            sorter.register("TooLate");
            fail("Should not succeed");
        } catch (SorterException se) {
            if (!se.getMessage().startsWith("Cannot register more inputs")) {
                throw se;
            }
        }

        for (int ch = 0; ch < genArr.length; ch++) {
            try {
                genArr[ch].join();
            } catch (InterruptedException ie) {
                // ignore interrupts
            }
        }

        sorter.waitForStop(10000);
        assertEquals("Buffer queue is not empty, sent " +
                     sorter.getNumOutput(), 0, sorter.getNumQueued());

        try {
            sorter.register("PostStop");
            fail("Should not succeed");
        } catch (SorterException se) {
            if (!se.getMessage().startsWith("Cannot register more inputs")) {
                throw se;
            }
        }

        long numSent = 0;
        for (int ch = 0; ch < genArr.length; ch++) {
            numSent += genArr[ch].getNumberSent();
        }

        consumer.validate(sorter, numThrd, nch, numSent);
    }

    @Test
    public void testStandardConfig()
        throws SorterException
    {
        BBConsumer consumer = new BBConsumer(EOS);

        Sorter<ByteBuffer> sorter =
            new Sorter<ByteBuffer>("StdCfg", nch, new ByteBuffComparator(EOS),
                                   consumer, EOS);

        runTest(consumer, sorter, nch, Sorter.DEFAULT_NUM_THREADS,
                Sorter.DEFAULT_CHUNK_SIZE);
    }

    @Test
    public void testCustomConfig()
        throws SorterException
    {
        final int numThrd = 3;
        final int chunkSize = 500;

        BBConsumer consumer = new BBConsumer(EOS);

        Sorter<ByteBuffer> sorter =
            new Sorter<ByteBuffer>("Custom", nch, new ByteBuffComparator(EOS),
                                   consumer, EOS, numThrd, chunkSize);

        runTest(consumer, sorter, nch, numThrd, chunkSize);
    }

    @Test
    public void testFewerChannels()
        throws SorterException
    {
        final int numThrd = 12;
        final int chunkSize = 500;

        BBConsumer consumer = new BBConsumer(EOS);

        Sorter<ByteBuffer> sorter =
            new Sorter<ByteBuffer>("Fewer", nch, new ByteBuffComparator(EOS),
                                   consumer, EOS, nch, chunkSize);

        runTest(consumer, sorter, nch, numThrd / 2, chunkSize);
    }

    @Test
    public void testExtraChannels()
        throws SorterException
    {
        final int numThrd = 3;
        final int chunkSize = 500;

        BBConsumer consumer = new BBConsumer(EOS);

        Sorter<ByteBuffer> sorter =
            new Sorter<ByteBuffer>("Extra", nch, new ByteBuffComparator(EOS),
                                   consumer, EOS, numThrd, chunkSize);

        runTest(consumer, sorter, nch, numThrd * 2, chunkSize);
    }
}

/**
 * Generates a sequence of ordered byte buffers
 * @author kael
 *
 */
class BufferGenerator
    extends Thread
{
    private static final Logger LOG =
        Logger.getLogger(BufferGenerator.class);

    private long mbid;
    private int numToSend;
    private SortInput input;

    private Random rand = new Random();

    private volatile boolean run;
    private volatile int numSent;

    BufferGenerator(long mbid, int numToSend, SortInput input)
    {
        this.mbid = mbid;
        this.numToSend = numToSend;
        this.input = input;
    }

    public long getNumberSent()
    {
        return numSent;
    }

    public synchronized boolean isRunning()
    {
        return run;
    }

    @Override
    public void run()
    {
        run = true;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting run thread of buffer generator " + mbid);
        }

        long lastTime = System.nanoTime();
        try {
            while (isRunning() && !interrupted()) {
                int thisChunk = numToSend / 100;
                if (numSent + thisChunk > numToSend) {
                    thisChunk = (int) numToSend - numSent;
                    if (thisChunk == 0) {
                        break;
                    }
                }

                final long subSecond = 1000000000L;
                final double subDouble = (double) subSecond;

                final long interval = (long) numSent * subSecond;

                double deltaT = (double) interval / subDouble;

                ArrayList<Long> times = new ArrayList<Long>(thisChunk);
                for (int i = 0; i < thisChunk; i++) {
                    double t = rand.nextDouble() * deltaT;
                    long  it = lastTime + (long) (t * subDouble);
                    times.add(it);
                }

                Collections.sort(times);
                for (Long time : times) {
                    ByteBuffer buf = ByteBuffer.allocate(40);
                    buf.putInt(40);
                    buf.putInt(0x1734);
                    buf.putLong(mbid);
                    buf.putLong(0L);
                    buf.putLong(time);
                    buf.putInt(1);
                    buf.putInt(2);
                    input.put((ByteBuffer) buf.flip());
                    numSent++;
                }

                lastTime += interval;

                Thread.yield();
            }

            input.putLast();
            numSent++;
            LOG.debug(String.format("Wrote eos for %012x", mbid));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Buffer generator thread " + mbid + " exiting.");
        }
    }

    public synchronized void signalStop()
    {
        run = false;
    }
}

class ByteBuffComparator
    implements Comparator<ByteBuffer>
{
    private ByteBuffer eos;

    ByteBuffComparator(ByteBuffer eos)
    {
        this.eos = eos;
    }

    @Override
    public int compare(ByteBuffer o1, ByteBuffer o2)
    {
        // handle nulls
        if (o1 == null) {
            if (o2 == null) {
                return 0;
            }

            return -1;
        } else if (o2 == null) {
            return 1;
        }

        // handle special end-of-stream marker
        if (o1 == eos) {
            if (o2 == eos) {
                return 0;
            }

            return 1;
        } else if (o2 == eos) {
            return -1;
        }

        final long t1 = o1.getLong(24);
        final long t2 = o2.getLong(24);

        if (t1 > t2) {
            return 1;
        } else if (t1 < t2) {
            return -1;
        } else {
            return 0;
        }
    }
}
