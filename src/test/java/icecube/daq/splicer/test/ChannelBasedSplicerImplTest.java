/*
 * class: ChannelBasedSplicerImplTest
 *
 * Version $Id: ChannelBasedSplicerImplTest.java,v 1.5 2005/08/09 17:24:40 patton Exp $
 *
 * Date: August 8 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer.test;

import icecube.daq.splicer.ChannelBasedSplicerImpl;
import icecube.daq.splicer.OrderingException;
import icecube.daq.splicer.Splicer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.Arrays;
import java.util.List;

/**
 * This class defines the tests that any ChannelBasedSplicerImpl object should
 * pass.
 *
 * @author patton
 * @version $Id: ChannelBasedSplicerImplTest.java,v 1.5 2005/08/09 17:24:40
 *          patton Exp $
 */
public class ChannelBasedSplicerImplTest
        extends TestCase
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    /**
     * Correction to lengths if running in safe mode.
     */
    private int SAFE_CORRECTION = 1;

    /**
     * The time, in millis, to wait for a command to complete execution.
     */
    private static final long COMMAND_EXECUTION_PAUSE = 10L;

    /**
     * The maxumin number of channels used in these tests.
     */
    private static final int MAX_NUMBER_CHANNELS = 2;

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
                                       33),
                    new MockSpliceable(8L,
                                       37),
                    new MockSpliceable(9L,
                                       41)
            };

    /**
     * The List wrapper for the SPLICEABLES_ARRAY.
     */
    private static final List TEST_SPLICABLE_OBJECTS =
            Arrays.asList(SPLICEABLES_ARRAY);

    // private static member data

    // private instance member data

    /**
     * The MockSplicedAnalysis object used in these tests.
     */
    private MockSplicedAnalysis analysis;

    /**
     * The MockSpliceableFactory object used in these tests.
     */
    private MockSpliceableFactory factory;

    /**
     * Byte buffer used to load the sink channel of a pipe.
     */
    private final ByteBuffer pipeInput = ByteBuffer.allocate(1024);

    /**
     * The List of Pipe objects used as SpliceableChannels for these tests.
     */
    private final Pipe[] pipes = new Pipe[MAX_NUMBER_CHANNELS];

    /**
     * The object being tested.
     */
    private ChannelBasedSplicerImpl testObject;

    // constructors

    /**
     * Constructs and instance of this test.
     *
     * @param name the name of the test.
     */
    public ChannelBasedSplicerImplTest(String name)
    {
        super(name);
    }

    // instance member method (alphabetic)

    /**
     * Creates and adds a pipe to the testObject.
     *
     * @param number the number of the pipe to create and add.
     */
    private void addPipe(int number)
    {
        try {
            pipes[number] = Pipe.open();
            testObject.addSpliceableChannel(pipes[number].source());
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    /**
     * Loads a know Spliceable into the {@link #pipeInput} buffer.
     *
     * @param index the index into {@link #SPLICEABLES_ARRAY}.
     */
    private void loadKnownSpliceable(int index)
    {
        final MockSpliceable spliceable = SPLICEABLES_ARRAY[index];
        pipeInput.put(spliceable.getLength()).putLong(spliceable.getOrder());
        final int finished = (int) spliceable.getLength() - 9;
        for (int offset = 0;
             finished != offset;
             offset += 4) {
            pipeInput.putInt(offset);
        }
    }

    /**
     * Routes an element of the {@link #SPLICEABLES_ARRAY} into the test object
     * using the specified pipe.
     *
     * @param index the index of the Spliceable to use.
     * @param pipe the pipe through with it should be directed.
     * @param state the expected resultant state (after a suitable pause).
     * @throws IOException if pipe fails
     * @throws InterruptedException if pause is interupted.
     */
    private void routeTestSplicable(int index,
                                    int pipe,
                                    int state)
            throws IOException,
                   InterruptedException
    {
        pipeInput.clear();
        loadKnownSpliceable(index);
        pipeInput.flip();
        pipes[pipe].sink().write(pipeInput);
        Thread.sleep(COMMAND_EXECUTION_PAUSE);
        if (0 <= state) {
            assertEquals(state,
                         testObject.getState());
        }
    }

    /**
     * Routes a set of elements of the {@link #SPLICEABLES_ARRAY} into the test
     * object using the specified pipe.
     *
     * @param indices the indices of the Spliceables to use.
     * @param pipe the pipe through with it should be directed.
     * @param state the expected resultant state (after a suitable pause).
     * @throws IOException if pipe fails
     * @throws InterruptedException if pause is interupted.
     */
    private void routeTestSplicable(int[] indices,
                                    int pipe,
                                    int state)
            throws IOException,
                   InterruptedException
    {
        pipeInput.clear();
        final int finished = indices.length;
        for (int index = 0;
             finished != index;
             index++) {
            loadKnownSpliceable(indices[index]);
        }
        pipeInput.flip();
        pipes[pipe].sink().write(pipeInput);
        Thread.sleep(COMMAND_EXECUTION_PAUSE);
        if (0 <= state) {
            assertEquals(state,
                         testObject.getState());
        }
    }

    /**
     * Starts pipes[0] using the 1st element of the {@link
     * #SPLICEABLES_ARRAY}.
     *
     * @param state the expected state after the initial start.
     * @throws IOException
     * @throws InterruptedException
     */
    private void standardStart(int state)
            throws IOException,
                   InterruptedException
    {
        // Input the 1st element and check that the testObject has started.
        pipeInput.clear();
        for (int element = 0;
             (SAFE_CORRECTION + 1) != element;
             element++) {
            loadKnownSpliceable(element);
        }
        pipeInput.flip();
        pipes[0].sink().write(pipeInput);
        Thread.sleep(COMMAND_EXECUTION_PAUSE);
        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(state),
                     testObject.getStateString());
    }

    /**
     * Starts the testObject after adding a pipe.
     */
    private void startUsingOnePipe()
    {
        // This adds pipes[0] as an input channel.
        testAddSpliceableChannel();

        testObject.start();
    }

    /**
     * Starts the testObject after adding two pipes.
     */
    private void startUsingTwoPipes()
    {
        testAddSpliceableChannel();
        addPipe(1);
        testObject.start();
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
        factory = new MockSpliceableFactory(0L);
        analysis = new MockSplicedAnalysis(factory);
        testObject = new ChannelBasedSplicerImpl(analysis);
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
        try {
            testObject.dispose();
        } catch (IllegalStateException e) {
            // allow termination with extreme prejudice.
        }
        testObject = null;
        analysis = null;
        factory = null;
        super.tearDown();
    }

    /**
     * Test that a SpliceableChannel object can be added (and thus read).
     */
    public final void testAddSpliceableChannel()
    {
        testCreatedState();
        addPipe(0);
    }

    /**
     * Test that Spliceable objects that have identical places in the order but
     * come from different SpliceableChannel can be read. The last object will
     * not appear in the list as it is 'after' the common latest place of
     * interest.
     */
    public final void testCommonFromTwo()
    {
        startUsingTwoPipes();

        // Create custom array of expected Spliceables.
        final MockSpliceable[] expected =
                new MockSpliceable[]{
                        SPLICEABLES_ARRAY[0],
                        SPLICEABLES_ARRAY[0],
                        SPLICEABLES_ARRAY[1],
                        SPLICEABLES_ARRAY[2],
                        SPLICEABLES_ARRAY[2]
                };
        analysis.setExpectedObjects(Arrays.asList(expected));
        try {
            standardStart(Splicer.STARTING);

            final int[][] elements = new int[2][];
            if (0 == SAFE_CORRECTION) {
                elements[0] = new int[]{0, 2};
                elements[1] = new int[]{1, 2};
            } else {
                elements[0] = new int[]{0, 2, 4};
                elements[1] = new int[]{2, 3};
            }

            // Input the 1st & 3rd elements into pipes[1]. This should start
            // the test object.
            routeTestSplicable(elements[0],
                               1,
                               Splicer.STARTED);

            // Input the 2st & 3rd elements into pipes[0]. As both pipes have
            // now seen the 3rd element these instances should be analyzed.
            routeTestSplicable(elements[1],
                               0,
                               Splicer.STARTED);

            assertTrue(analysis.getFailureMessage(),
                       analysis.isSuccessful());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Test that this splicer is created in the correct state.
     */
    public void testCreatedState()
    {
        assertEquals(Splicer.STOPPED,
                     testObject.getState());
        testObject.addSplicerListener(new StateTestingListener());
    }

    /**
     * Test that transition Starting->Stopping works correctly.
     */
    public void testForcedStop()
    {
        testStartOne();
        testObject.forceStop();

        // Give Splicer a millisecond to complete STOPPING->STOPPED transition.
        try {
            Thread.sleep(COMMAND_EXECUTION_PAUSE);
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STOPPED),
                     testObject.getStateString());
    }

    /**
     * Test that many Spliceable objects can be read from a SpliceableChannel.
     */
    public final void testManyFromOne()
    {
        startUsingOnePipe();

        // Expect the 1st to 5th element to be analyzed.
        analysis.setExpectedObjects(TEST_SPLICABLE_OBJECTS.subList(0,
                                                                   5));
        try {
            standardStart(Splicer.STARTED);

            final int[][] elements = new int[2][];
            if (0 == SAFE_CORRECTION) {
                elements[0] = new int[]{1, 2};
                elements[1] = new int[]{3, 4};
            } else {
                elements[0] = new int[]{2, 3};
                elements[1] = new int[]{4, 5};
            }

            // Input the 2nd & 3rd elements.
            routeTestSplicable(elements[0],
                               0,
                               Splicer.STARTED);

            // Input the 4th & 5th elements.
            routeTestSplicable(elements[1],
                               0,
                               Splicer.STARTED);

            assertTrue(analysis.getFailureMessage(),
                       analysis.isSuccessful());
        } catch (IOException e) {
            fail(e.toString());
        } catch (InterruptedException e) {
            fail(e.toString());
        }
    }

    /**
     * Test that a Spliceable object can be read from each SpliceableChannel.
     * The last object will not appear in the list as it is 'after' the common
     * latest place of interest.
     */
    public final void testManyFromTwo()
    {
        startUsingTwoPipes();

        // Expect the 1st to 5th element to be analyzed.
        analysis.setExpectedObjects(
                TEST_SPLICABLE_OBJECTS.subList(0,
                                               5 + SAFE_CORRECTION));
        try {
            standardStart(Splicer.STARTING);

            final int[][] elements = new int[3][];
            if (0 == SAFE_CORRECTION) {
                elements[0] = new int[]{1, 2};
                elements[1] = new int[]{3, 4};
                elements[2] = new int[]{5};
            } else {
                elements[0] = new int[]{2, 3, 6};
                elements[1] = new int[]{4, 5, 7};
                elements[2] = new int[]{8};
            }

            // Input the 2nd & 3rd elements into pipes[1]
            routeTestSplicable(elements[0],
                               1,
                               Splicer.STARTED);

            // Input the 4th & 5th elements inot pipes[0].
            routeTestSplicable(elements[1],
                               0,
                               Splicer.STARTED);

            // Input the 6th elements into pipes[1], so the first five are
            // analyized.
            routeTestSplicable(elements[2],
                               1,
                               Splicer.STARTED);

            assertTrue(analysis.getFailureMessage(),
                       analysis.isSuccessful());
        } catch (IOException e) {
            fail(e.toString());
        } catch (InterruptedException e) {
            fail(e.toString());
        }
    }

    /**
     * Test that a Spliceable object can be read from a SpliceableChannel.
     */
    public final void testOneFromOne()
    {
        startUsingOnePipe();

        // Expect the 1st to be correctly analyzed.
        analysis.setExpectedObjects(TEST_SPLICABLE_OBJECTS.subList(0,
                                                                   1));
        try {
            standardStart(Splicer.STARTED);

            assertTrue(analysis.getFailureMessage(),
                       analysis.isSuccessful());
        } catch (IOException e) {
            fail(e.toString());
        } catch (InterruptedException e) {
            fail(e.toString());
        }
    }

    /**
     * That that a partial object is not place into the array.
     */
    public final void testPartialFromOne()
    {
        startUsingOnePipe();

        // Expect only the 1st and second to be analyzed.
        analysis.setExpectedObjects(TEST_SPLICABLE_OBJECTS.subList(0,
                                                                   2));
        try {
            standardStart(Splicer.STARTED);

            // Input the 2nd element, but only a partial part of the 3rd.
            pipeInput.clear();
            loadKnownSpliceable(1);
            final MockSpliceable spliceable = SPLICEABLES_ARRAY[2];
            pipeInput.put(spliceable.getLength())
                    .putLong(spliceable.getOrder());
            pipeInput.putInt(0);
            pipeInput.flip();
            pipes[0].sink().write(pipeInput);
            Thread.sleep(COMMAND_EXECUTION_PAUSE);

            assertTrue(analysis.getFailureMessage(),
                       analysis.isSuccessful());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Test that a SpliceableChannel object can be removed.
     */
    public final void testRemoveSpliceableChannel()
    {
        testAddSpliceableChannel();
        testObject.removeSpliceableChannel(pipes[0].source());
    }

    /**
     * Test that the Splicer can be restarted.
     *
     * @throws OrderingException
     */
    public final void testRestartWithOne()
            throws OrderingException
    {
        startUsingOnePipe();

        // Create custom array of expected Spliceables.
        final MockSpliceable[] expectedObjects = new MockSpliceable[]{
                SPLICEABLES_ARRAY[0],
                SPLICEABLES_ARRAY[1],
                SPLICEABLES_ARRAY[4]
        };
        analysis.setExpectedObjects(Arrays.asList(expectedObjects));
        try {
            standardStart(Splicer.STARTED);

            // Stop after the 2nd element.
            testObject.stop(SPLICEABLES_ARRAY[1]);
            Thread.sleep(COMMAND_EXECUTION_PAUSE);
            assertEquals("Splicer is not is the correct state",
                         testObject.getStateString(Splicer.STOPPING),
                         testObject.getStateString());
            final int[][] elements = new int[2][];
            if (0 == SAFE_CORRECTION) {
                elements[0] = new int[]{1, 2};
                elements[1] = new int[]{3, 4};
            } else {
                elements[0] = new int[]{2, 3};
                elements[1] = new int[]{4, 5};
            }

            // Input 1st and 2nd element and check that the test object has
            // stopped.
            routeTestSplicable(elements[0],
                               0,
                               Splicer.STOPPED);

            // Restart at the 4th element.
            testObject.start(SPLICEABLES_ARRAY[4]);
            Thread.sleep(COMMAND_EXECUTION_PAUSE);
            assertEquals("Splicer is not is the correct state",
                         testObject.getStateString(Splicer.STARTING),
                         testObject.getStateString());

            // Input 3rd and 4th element to check restart has occured.
            routeTestSplicable(elements[1],
                               0,
                               Splicer.STARTED);

            assertTrue(analysis.getFailureMessage(),
                       analysis.isSuccessful());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Test that an object split into two parts is correctly handled.
     */
    public final void testSplitFromOne()
    {
        startUsingOnePipe();

        // Expect the 1st to 3rd elements to be analyzed.
        analysis.setExpectedObjects(TEST_SPLICABLE_OBJECTS.subList(0,
                                                                   4));
        try {
            standardStart(Splicer.STARTED);

            // Input part of 3nd element
            pipeInput.clear();
            final MockSpliceable spliceable = SPLICEABLES_ARRAY[2];
            pipeInput.put(spliceable.getLength())
                    .putLong(spliceable.getOrder());
            pipeInput.flip();
            pipes[0].sink().write(pipeInput);
            Thread.sleep(COMMAND_EXECUTION_PAUSE);

            // Input rest of 3nd element follwed by the 3rd to make sure the
            // 4rd get analyzed.
            pipeInput.clear();
            final int finished = (int) spliceable.getLength() - 9;
            for (int offset = 0;
                 finished != offset;
                 offset += 4) {
                pipeInput.putInt(offset);
            }
            for (int element = 3;
                 (SAFE_CORRECTION + 4) != element;
                 element++) {
                loadKnownSpliceable(element);
            }
            pipeInput.flip();
            pipes[0].sink().write(pipeInput);
            Thread.sleep(COMMAND_EXECUTION_PAUSE);

            assertTrue("Analaysis was no successful",
                       analysis.isSuccessful());
        } catch (IOException e) {
            fail(e.toString());
        } catch (InterruptedException e) {
            fail(e.toString());
        }
    }

    /**
     * Test that the start method works for a single SpliceableChannel.
     */
    public final void testStartOne()
    {
        testAddSpliceableChannel();
        testObject.start(new MockSpliceable(3L));

        // Expect the 3rd element to be analyzed.
        analysis.setExpectedObjects(TEST_SPLICABLE_OBJECTS.subList(2,
                                                                   3));
        try {

            // Input the 1st element that should be ignored.
            standardStart(Splicer.STARTING);

            final int[][] elements = new int[1][];
            if (0 == SAFE_CORRECTION) {
                elements[0] = new int[]{1, 2};
            } else {
                elements[0] = new int[]{2, 3};
            }

            // Input the 2nd & 3rd elements. The first of these should be
            // ignored while the second should trigger the starting->started
            // transition. that should be ignored.
            routeTestSplicable(elements[0],
                               0,
                               Splicer.STARTED);

            assertTrue(analysis.isSuccessful());
        } catch (IOException e) {
            fail(e.toString());
        } catch (InterruptedException e) {
            fail(e.toString());
        }
    }

    /**
     * Test that the start method works for more than one SpliceableChannel.
     */
    public final void testStartTwo()
    {
        testAddSpliceableChannel();
        addPipe(1);
        testObject.start(new MockSpliceable(3L));

        // Expect the 3rd and 4th elements to be analyzed.
        analysis.setExpectedObjects(TEST_SPLICABLE_OBJECTS.subList(2,
                                                                   4));
        try {

            // Input the 1st element to pipes[0] that should be ignored.
            standardStart(Splicer.STARTING);

            final int[][] elements = new int[4][];
            if (0 == SAFE_CORRECTION) {
                elements[0] = new int[]{2};
                elements[1] = new int[]{1};
                elements[2] = new int[]{3};
                elements[3] = new int[]{4};
            } else {
                elements[0] = new int[]{2,4};
                elements[1] = new int[]{1,3};
                elements[2] = new int[]{6};
                elements[3] = new int[]{5};
            }

            // Input the 3rd element to pipes[0]. The testObject should now be
            // waiting for pipes[1] to have a valid Spliceable.
            routeTestSplicable(elements[0],
                               0,
                               Splicer.STARTING);

            // Input the 2nd element to pipes[1] that should also be ignored.
            routeTestSplicable(elements[1],
                               1,
                               Splicer.STARTING);

            // Input the 4th element to pipes[1]. That should start the
            // testObject.
            routeTestSplicable(elements[2],
                               1,
                               Splicer.STARTED);

            // Input the 5th element to pipes[0] to check that elements in
            routeTestSplicable(elements[3],
                               0,
                               Splicer.STARTED);

            assertTrue(analysis.isSuccessful());
        } catch (IOException e) {
            fail(e.toString());
        } catch (InterruptedException e) {
            fail(e.toString());
        }
    }

    /**
     * Test that the stop method works for a single SpliceableChannel.
     *
     * @throws OrderingException
     */
    public final void testStopOne()
            throws OrderingException
    {
        startUsingOnePipe();

        // Expect the 1st and 2nd element to be analyzed.
        analysis.setExpectedObjects(TEST_SPLICABLE_OBJECTS.subList(0,
                                                                   3));
        try {
            standardStart(Splicer.STARTED);

            // Request a stop.
            testObject.stop(SPLICEABLES_ARRAY[2]);
            Thread.sleep(COMMAND_EXECUTION_PAUSE);
            assertEquals(Splicer.STOPPING,
                         testObject.getState());

            final int[][] elements = new int[1][];
            if (0 == SAFE_CORRECTION) {
                elements[0] = new int[]{1, 2, 3};
            } else {
                elements[0] = new int[]{2, 3, 4};
            }

            // Input the 2nd & 3rd elements. The second of these should trigger
            // the Splicer to transition into the Stopped state.
            routeTestSplicable(elements[0],
                               0,
                               Splicer.STOPPED);

            assertTrue("Analaysis was no successful",
                       analysis.isSuccessful());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Test that transition Stopped->Starting works correctly.
     */
    public void testStoppedToStarting()
    {
        testCreatedState();
        testObject.start();
        try {
            Thread.sleep(COMMAND_EXECUTION_PAUSE);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(e.toString());
        }
        // @todo - FIx this so the test passes in safte mode(!).
//        assertEquals(Splicer.STARTING,
//                     testObject.getState());
    }

    /**
     * Test that the stop method works for a multiple SpliceableChannel.
     *
     * @throws OrderingException
     */
    public final void testStopTwo()
            throws OrderingException
    {
        startUsingTwoPipes();

        // Expect the 1st to 4th element to be analyzed.
        analysis.setExpectedObjects(TEST_SPLICABLE_OBJECTS.subList(0,
                                                                   4));
        try {
            standardStart(Splicer.STARTING);

            final int[][] elements = new int[3][];
            if (0 == SAFE_CORRECTION) {
                elements[0] = new int[]{1, 2};
                elements[1] = new int[]{3, 4};
                elements[2] = new int[]{5};
            } else {
                elements[0] = new int[]{2, 5};
                elements[1] = new int[]{3, 4, 5};
                elements[2] = new int[]{6};
            }

            // Input the 2nd & 3rd elements to pipes[1], this should start the
            // two pipe configuration.
            routeTestSplicable(elements[0],
                               1,
                               Splicer.STARTED);

            // Request a stop.
            testObject.stop(SPLICEABLES_ARRAY[3]);
            Thread.sleep(COMMAND_EXECUTION_PAUSE);

            assertEquals(Splicer.STOPPING,
                         testObject.getState());

            // Input the 4th and 5th elements put pipes[0] passed the stop
            // place, but not pipes[1] so the test object is still stopping.
            routeTestSplicable(elements[1],
                               0,
                               Splicer.STOPPING);

            // Input the 6th element to pipes[1] taking it beyond the stop
            // place and thus moving the test object into a stopped state.
            routeTestSplicable(elements[2],
                               1,
                               Splicer.STOPPED);

            assertTrue(analysis.isSuccessful());
        } catch (IOException e) {
            fail(e.toString());
        } catch (InterruptedException e) {
            fail(e.toString());
        }
    }

    /**
     * Test the truncation of the "rope" with one channel.
     */
    public final void testTruncateWithOne()
    {
        startUsingOnePipe();

        // Expect the 1st to 5th elements to ultimately be analyzed.
        analysis.setExpectedObjects(TEST_SPLICABLE_OBJECTS.subList(0,
                                                                   5));
        try {
            standardStart(Splicer.STARTED);

            final int[][] elements = new int[2][];
            if (0 == SAFE_CORRECTION) {
                elements[0] = new int[]{1, 2};
                elements[1] = new int[]{3, 4};
            } else {
                elements[0] = new int[]{2, 3};
                elements[1] = new int[]{4, 5};
            }

            // Input the 2nd & 3rd elements.
            routeTestSplicable(elements[0],
                               0,
                               Splicer.STARTED);

            // Set Epoi to match the 3rd element, and prepare the analysis
            // object for the truncated List of Splicables.
            testObject.truncate(SPLICEABLES_ARRAY[2]);
            Thread.sleep(COMMAND_EXECUTION_PAUSE);
            analysis.setFirstSplicable(SPLICEABLES_ARRAY[2]);

            // Input the 4th & 5th elements.
            routeTestSplicable(elements[1],
                               0,
                               Splicer.STARTED);

            assertTrue(analysis.isSuccessful());
        } catch (IOException e) {
            fail(e.toString());
        } catch (InterruptedException e) {
            fail(e.toString());
        }
    }

    /**
     * Test the truncation of the "rope" with two channels.
     */
    public final void testTruncateWithTwo()
    {
        startUsingTwoPipes();

        // Expect the 1st to 5th elements to ultimately be analyzed.
        analysis.setExpectedObjects(TEST_SPLICABLE_OBJECTS.subList(0,
                                                                   6));
        try {
            standardStart(Splicer.STARTING);

            final int[][] elements = new int[3][];
            if (0 == SAFE_CORRECTION) {
                elements[0] = new int[]{2, 3};
                elements[1] = new int[]{1, 4, 5};
                elements[2] = new int[]{6};
            } else {
                elements[0] = new int[]{2, 3, 6};
                elements[1] = new int[]{4, 5, 7};
                elements[2] = new int[]{8};
            }

            // Input the 2nd & 3rd elements into pipes[1]
            routeTestSplicable(elements[0],
                               1,
                               Splicer.STARTED);

            // Input the 4th & 5th elements inot pipes[0].
            routeTestSplicable(elements[1],
                               0,
                               Splicer.STARTED);

            testObject.truncate(SPLICEABLES_ARRAY[2]);
            Thread.sleep(COMMAND_EXECUTION_PAUSE);

            // Input the 6th elements into pipes[1], so the first five are
            // analyized.
            routeTestSplicable(elements[2],
                               1,
                               Splicer.STARTED);

            assertTrue("Analaysis was no successful",
                       analysis.isSuccessful());
        } catch (IOException e) {
            fail(e.toString());
        } catch (InterruptedException e) {
            fail(e.toString());
        }
    }

    /**
     * Test that a Spliceable object can be read from each SpliceableChannel.
     * The 3rd element will not appear in the list as it is 'after' the common
     * latest place of interest.
     */
    public final void testTwoFromTwo()
    {
        startUsingTwoPipes();

        // Expect 1st and 2nd elements to be analyzed,
        analysis.setExpectedObjects(TEST_SPLICABLE_OBJECTS.subList(0,
                                                                   3));
        try {
            standardStart(Splicer.STARTING);

            final int[][] elements = new int[3][];
            if (0 == SAFE_CORRECTION) {
                elements[0] = new int[]{3};
                elements[1] = new int[]{2};
            } else {
                elements[0] = new int[]{3,4};
                elements[1] = new int[]{2, 5};
            }


            // Input 3rd element into pipes[0] so that 2nd element will be
            // analyzed when input to pipes[1]
            routeTestSplicable(elements[0],
                               0,
                               Splicer.STARTING);

            // Input 2nd element into pipes[1] so test object starts and
            // spliceables from both pipes are analyzed.
            routeTestSplicable(elements[1],
                               1,
                               Splicer.STARTED);

            assertTrue(analysis.isSuccessful());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    // static member methods (alphabetic)

    /**
     * Create test suite for this class.
     *
     * @return the suite of tests declared in this class.
     */
    public static Test suite()
    {
        return new TestSuite(ChannelBasedSplicerImplTest.class);
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
