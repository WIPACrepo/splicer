/*
 * class: ExternalByteBufferTailTest
 *
 * Version $Id: ExternalByteBufferTailTest.java,v 1.13 2006/02/04 21:00:52 patton Exp $
 *
 * Date: August 6 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

import icecube.daq.splicer.test.MockSpliceable;
import icecube.daq.splicer.test.MockSpliceableFactory;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class defines the tests that any ExternalByteBufferTail object should
 * pass.
 * <p/>
 * This is "White box" testing so that the MockSPliucer can create a
 * SplicerChangedEvent.
 *
 * @author patton
 * @version $Id: ExternalByteBufferTailTest.java,v 1.13 2006/02/04 21:00:52
 *          patton Exp $
 */
public class ExternalByteBufferTailTest
        extends TestCase
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    /**
     * An array of MockSpliceable objects to use in tests.
     */
    private static final MockSpliceable[] SPLICEABLES_ARRAY =
            new MockSpliceable[]{
                    new MockSpliceable(1L,
                                       9),
                    new MockSpliceable(2L,
                                       13),
                    new MockSpliceable(3L,
                                       17),
                    new MockSpliceable(4L,
                                       21),
                    new MockSpliceable(5L,
                                       25),
                    new MockSpliceable(6L,
                                       29),
                    new MockSpliceable(7L,
                                       25),
                    new MockSpliceable(8L,
                                       21)
            };

    // private static member data

    // private instance member data

    /**
     * Byte buffer used to load the test object.
     */
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);

    /**
     * Mock Splicer which receives pushed spliceables.
     */
    private final MockSplicer splicer = new MockSplicer();

    /**
     * The object being tested.
     */
    private ExternalByteBufferTail testObject;

    // constructors

    /**
     * Constructs and instance of this test.
     *
     * @param name the name of the test.
     */
    public ExternalByteBufferTailTest(String name)
    {
        super(name);
    }

    // instance member method (alphabetic)

    private void checkSplicedSpliceables(List expected)
    {
        final Iterator expect = expected.iterator();
        final Iterator actual = splicer.getSpliceables().iterator();
        while (expect.hasNext() &&
               actual.hasNext()) {
            final Spliceable expectedSpliceable = (Spliceable) expect.next();
            final Spliceable actualSpliceable = (Spliceable) actual.next();
            assertTrue("Pushed Spliceables does not matched expected.",
                       0 == expectedSpliceable.compareTo(actualSpliceable));
        }

        if (expect.hasNext()) {
            fail("Not all expected Spliceables where seen.");
        } else if (actual.hasNext()) {
            fail("More than the expected number of Spliceables were seen.");
        }
    }

    /**
     * Loads a know Spliceable into the buffer.
     *
     * @param index the index into {@link #SPLICEABLES_ARRAY}.
     */
    private void loadKnownSpliceable(int index)
    {
        final MockSpliceable spliceable = SPLICEABLES_ARRAY[index];
        buffer.put(spliceable.getLength()).putLong(spliceable.getOrder());
        final int finished = (int) spliceable.getLength() - 9;
        for (int offset = 0;
             finished != offset;
             offset += 4) {
            buffer.putInt(offset);
        }
    }


    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     *
     * @throws Exception if super class setUp fails.
     */
    protected void setUp()
            throws Exception
    {
        super.setUp();
        testObject = new ExternalByteBufferTail(splicer,
                                                new MockSpliceableFactory(0),
                                                buffer);
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     *
     * @throws Exception if super class tearDown fails.
     */
    protected void tearDown()
            throws Exception
    {
        testObject = null;
        super.tearDown();
    }

    /**
     * Test that pushing data produces the expected Spliceables.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testPush()
            throws OrderingException,
                   ClosedStrandException
    {
        synchronized (testObject.getBuffer()) {
            loadKnownSpliceable(0);
            loadKnownSpliceable(1);
            loadKnownSpliceable(2);
        }
        testObject.push();

        final List expected = new LinkedList();
        expected.add(SPLICEABLES_ARRAY[0]);
        expected.add(SPLICEABLES_ARRAY[1]);
        expected.add(SPLICEABLES_ARRAY[2]);
        checkSplicedSpliceables(expected);

        synchronized (testObject.getBuffer()) {
            loadKnownSpliceable(3);
            loadKnownSpliceable(5);
        }
        testObject.push();

        expected.add(SPLICEABLES_ARRAY[3]);
        expected.add(SPLICEABLES_ARRAY[5]);
        checkSplicedSpliceables(expected);
    }

    /**
     * Test that the truncation mechanism works correctly.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testTruncation()
            throws OrderingException,
                   ClosedStrandException
    {
        splicer.addSplicerListener(testObject);
        testPush();

        final int oldPosition = buffer.position();
        splicer.truncate(SPLICEABLES_ARRAY[2]);

        splicer.removeSplicerListener(testObject);
        final int expectedPosition = oldPosition -
                                     SPLICEABLES_ARRAY[0].getLength() -
                                     SPLICEABLES_ARRAY[1].getLength();
        assertEquals("Buffer not correctly shortened",
                     expectedPosition,
                     buffer.position());
        final List expected = new LinkedList();

        expected.add(SPLICEABLES_ARRAY[2]);
        expected.add(SPLICEABLES_ARRAY[3]);
        expected.add(SPLICEABLES_ARRAY[5]);
        checkSplicedSpliceables(expected);

        synchronized (testObject.getBuffer()) {
            loadKnownSpliceable(7);
        }
        testObject.push();

        expected.add(SPLICEABLES_ARRAY[7]);
        checkSplicedSpliceables(expected);
    }

    // static member methods (alphabetic)

    private class MockSplicer
            implements Splicer,
                       StrandTail
    {
        /**
         * The first Spliceable in splicables that is valid.
         */
        private int firstSpliceable = 0;

        /**
         * List of Spliceable pushed into this object.
         */
        private final List spliceables = new LinkedList();

        /**
         * The EcternalByteBufferTail listening to this object.
         */
        private SplicerListener listener;

        private MockSplicer()
        {
        }

        public void addSpliceableChannel(SelectableChannel channel)
                throws IOException
        {
        }

        public void addSplicerListener(SplicerListener listener)
        {
            this.listener = listener;
        }

        public void analyze()
        {
        }

        public StrandTail beginStrand()
        {
            return this;
        }

        public void close()
        {
        }

        public void dispose()
        {
        }

        public void forceStop()
        {
        }

        public SplicedAnalysis getAnalysis()
        {
            return null;
        }

        public MonitorPoints getMonitorPoints()
        {
            return null;
        }

        public int getState()
        {
            return 0;
        }

        public String getStateString()
        {
            return null;
        }

        public String getStateString(int state)
        {
            return null;
        }

        public int getStrandCount()
        {
            return 0;
        }

        public Spliceable head()
        {
            throw new UnsupportedOperationException();
        }

        public boolean isClosed()
        {
            return false;
        }

        public List pendingChannels()
        {
            return null;
        }

        public List pendingStrands()
        {
            return null;
        }

        public StrandTail push(List spliceables)
                throws OrderingException,
                       ClosedStrandException
        {
            this.spliceables.addAll(spliceables);
            return MockSplicer.this;
        }

        public StrandTail push(Spliceable spliceable)
                throws OrderingException,
                       ClosedStrandException
        {
            spliceables.add(spliceable);
            return MockSplicer.this;
        }

        public void removeSpliceableChannel(SelectableChannel channel)
        {
        }

        public void removeSplicerListener(SplicerListener listener)
        {
            if (this.listener == listener) {
                listener = null;
            }
        }

        public int size()
        {
            return 0;
        }

        public void start()
        {
        }

        public void start(Spliceable start)
        {
        }

        public void stop()
        {
        }

        public void stop(Spliceable stop)
                throws OrderingException
        {
        }

        public void truncate(Spliceable spliceable)
        {
            Collections.sort(spliceables);

            int cutOff = 0;
            boolean done = false;
            final Iterator iterator = getSpliceables().iterator();
            while (iterator.hasNext() &&
                    !done) {
                final Spliceable element = (Spliceable) iterator.next();
                if (0 > element.compareTo(spliceable)) {
                    cutOff++;
                } else {
                    done = true;
                }
            }
            final List deadSpliceables = spliceables.subList(firstSpliceable,
                                                             cutOff);
            final SplicerChangedEvent event =
                    new SplicerChangedEvent(this,
                                            Splicer.STARTED,
                                            spliceable,
                                            deadSpliceables);
            listener.truncated(event);

            firstSpliceable += cutOff;
        }

        List getSpliceables()
        {
            Collections.sort(spliceables);

            return spliceables.subList(firstSpliceable,
                                       spliceables.size());

        }
    }

    /**
     * Create test suite for this class.
     *
     * @return the suite of tests declared in this class.
     */
    public static Test suite()
    {
        return new TestSuite(ExternalByteBufferTailTest.class);
    }

    // Description of this object.
    // public String toString() {}

    /**
     * Main routine which runs text test in standalone mode.
     *
     * @param args the arguments with which to execute this method.
     */
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
