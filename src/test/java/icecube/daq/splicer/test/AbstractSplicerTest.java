/*
 * class: AbstractSplicerTest
 *
 * Version $Id: AbstractSplicerTest.java,v 1.19 2005/12/08 22:00:40 patton Exp $
 *
 * Date: August 1 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer.test;

import icecube.daq.splicer.ClosedStrandException;
import icecube.daq.splicer.OrderingException;
import icecube.daq.splicer.Spliceable;
import icecube.daq.splicer.SpliceableComparator;
import icecube.daq.splicer.SpliceableFactory;
import icecube.daq.splicer.SplicedAnalysis;
import icecube.daq.splicer.Splicer;
import icecube.daq.splicer.SplicerAdapter;
import icecube.daq.splicer.SplicerChangedEvent;
import icecube.daq.splicer.StrandTail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This class defines the tests that any Splicer object should pass.
 *
 * @author patton
 * @version $Id: AbstractSplicerTest.java,v 1.19 2005/12/08 22:00:40 patton Exp $
 */
public abstract class AbstractSplicerTest
        extends TestCase
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    /*
    The following are the test inputs:

    Strand 1: 1 2 3   5       9*2 | 10    12      14*2    16 17
    Strand 2:       4 5   7 8          11 12 |            16 17 18

  Starts:
    Delay   :   *
    Clean  1:         *
    Clean  2:           *
    Clean  3:                         *
  Stops:
    Clean  1:                                    *


    Strand 1: 17       20           22          26 | 26 27 29 30
    Strand 2:       19    21*3 | 21    23 24 25

  Stops:
    Clean  1:        *
    */

    private static final Spliceable[] LIST_ONE_SPLICEABLES =
            new Spliceable[]{
                    new MockSpliceable(1),
                    new MockSpliceable(2),
                    new MockSpliceable(3),
                    new MockSpliceable(5),
                    new MockSpliceable(9),
                    new MockSpliceable(9)
            };
    private static final List LIST_ONE = Arrays.asList(LIST_ONE_SPLICEABLES);

    private static final Spliceable[] LIST_TWO_SPLICEABLES =
            new Spliceable[]{
                    new MockSpliceable(4),
                    new MockSpliceable(5),
                    new MockSpliceable(7),
                    new MockSpliceable(8),
                    new MockSpliceable(11),
                    new MockSpliceable(12)
            };
    private static final List LIST_TWO = Arrays.asList(LIST_TWO_SPLICEABLES);

    private static final Spliceable[] LIST_THREE_SPLICEABLES =
            new Spliceable[]{
                    new MockSpliceable(10),
                    new MockSpliceable(12),
                    new MockSpliceable(14),
                    new MockSpliceable(14),
                    new MockSpliceable(16),
                    new MockSpliceable(17)
            };
    private static final List LIST_THREE =
            Arrays.asList(LIST_THREE_SPLICEABLES);

    private static final Spliceable[] LIST_FOUR_SPLICEABLES =
            new Spliceable[]{
                    new MockSpliceable(16),
                    new MockSpliceable(17),
                    new MockSpliceable(18)
            };
    private static final List LIST_FOUR =
            Arrays.asList(LIST_FOUR_SPLICEABLES);

    private static final Spliceable[] LIST_FIVE_SPLICEABLES =
            new Spliceable[]{
                    new MockSpliceable(17),
                    new MockSpliceable(20),
                    new MockSpliceable(22),
                    new MockSpliceable(26)
            };
    private static final List LIST_FIVE = Arrays.asList(LIST_FIVE_SPLICEABLES);

    private static final Spliceable[] LIST_SIX_SPLICEABLES =
            new Spliceable[]{
                    new MockSpliceable(19),
                    new MockSpliceable(21),
                    new MockSpliceable(21),
                    new MockSpliceable(21)
            };
    private static final List LIST_SIX = Arrays.asList(LIST_SIX_SPLICEABLES);

    private static final Spliceable[] LIST_SEVEN_SPLICEABLES =
            new Spliceable[]{
                    new MockSpliceable(26),
                    new MockSpliceable(27),
                    new MockSpliceable(29),
                    new MockSpliceable(30)
            };
    private static final List LIST_SEVEN = Arrays
            .asList(LIST_SEVEN_SPLICEABLES);

    private static final Spliceable[] LIST_EIGHT_SPLICEABLES =
            new Spliceable[]{
                    new MockSpliceable(21),
                    new MockSpliceable(23),
                    new MockSpliceable(24),
                    new MockSpliceable(25)
            };
    private static final List LIST_EIGHT = Arrays
            .asList(LIST_EIGHT_SPLICEABLES);


    private static final List CLEAN_START_THREE_RESULT = new ArrayList();

    static {
        CLEAN_START_THREE_RESULT.add(LIST_TWO_SPLICEABLES[4]);
        CLEAN_START_THREE_RESULT.add(LIST_TWO_SPLICEABLES[5]);
        CLEAN_START_THREE_RESULT.add(LIST_THREE_SPLICEABLES[1]);
        CLEAN_START_THREE_RESULT.add(LIST_THREE_SPLICEABLES[2]);
        CLEAN_START_THREE_RESULT.add(LIST_THREE_SPLICEABLES[3]);
        CLEAN_START_THREE_RESULT.add(LIST_FOUR_SPLICEABLES[0]);
        CLEAN_START_THREE_RESULT.add(LIST_THREE_SPLICEABLES[4]);
    }

    private static final List CLEAN_START_TWO_RESULT = new ArrayList();

    static {
        CLEAN_START_TWO_RESULT.add(LIST_TWO_SPLICEABLES[2]);
        CLEAN_START_TWO_RESULT.add(LIST_TWO_SPLICEABLES[3]);
        CLEAN_START_TWO_RESULT.add(LIST_ONE_SPLICEABLES[4]);
        CLEAN_START_TWO_RESULT.add(LIST_ONE_SPLICEABLES[5]);
        CLEAN_START_TWO_RESULT.add(LIST_THREE_SPLICEABLES[0]);
        CLEAN_START_TWO_RESULT.addAll(CLEAN_START_THREE_RESULT);
    }

    private static final List CLEAN_START_ONE_RESULT = new ArrayList();

    static {
        CLEAN_START_ONE_RESULT.add(LIST_ONE_SPLICEABLES[3]);
        CLEAN_START_ONE_RESULT.add(LIST_TWO_SPLICEABLES[1]);
        CLEAN_START_ONE_RESULT.addAll(CLEAN_START_TWO_RESULT);
    }

    private static final List CLEAN_STOP_ONE_RESULT = new ArrayList();

    static {
        CLEAN_STOP_ONE_RESULT.add(LIST_TWO_SPLICEABLES[2]);
        CLEAN_STOP_ONE_RESULT.add(LIST_TWO_SPLICEABLES[3]);
        CLEAN_STOP_ONE_RESULT.add(LIST_ONE_SPLICEABLES[4]);
        CLEAN_STOP_ONE_RESULT.add(LIST_ONE_SPLICEABLES[5]);
        CLEAN_STOP_ONE_RESULT.add(LIST_THREE_SPLICEABLES[0]);
        CLEAN_STOP_ONE_RESULT.add(LIST_TWO_SPLICEABLES[4]);
        CLEAN_STOP_ONE_RESULT.add(LIST_TWO_SPLICEABLES[5]);
        CLEAN_STOP_ONE_RESULT.add(LIST_THREE_SPLICEABLES[1]);
        CLEAN_STOP_ONE_RESULT.add(LIST_THREE_SPLICEABLES[2]);
        CLEAN_STOP_ONE_RESULT.add(LIST_THREE_SPLICEABLES[3]);
    }

    private static final List CLEAN_RESTOP_RESULT = new ArrayList();

    static {
        CLEAN_RESTOP_RESULT.addAll(CLEAN_STOP_ONE_RESULT);
        CLEAN_RESTOP_RESULT.add(LIST_FOUR_SPLICEABLES[0]);
        CLEAN_RESTOP_RESULT.add(LIST_THREE_SPLICEABLES[4]);
        CLEAN_RESTOP_RESULT.add(LIST_FIVE_SPLICEABLES[0]);
        CLEAN_RESTOP_RESULT.add(LIST_FOUR_SPLICEABLES[1]);
        CLEAN_RESTOP_RESULT.add(LIST_FOUR_SPLICEABLES[2]);
        CLEAN_RESTOP_RESULT.add(LIST_SIX_SPLICEABLES[0]);
        CLEAN_RESTOP_RESULT.add(LIST_FIVE_SPLICEABLES[1]);
        CLEAN_RESTOP_RESULT.add(LIST_SIX_SPLICEABLES[1]);
        CLEAN_RESTOP_RESULT.add(LIST_SIX_SPLICEABLES[2]);
        CLEAN_RESTOP_RESULT.add(LIST_SIX_SPLICEABLES[3]);
        CLEAN_RESTOP_RESULT.add(LIST_SEVEN_SPLICEABLES[0]);
    }

    private static final List DELAY_START_ONE_RESULT = new ArrayList();

    static {
        DELAY_START_ONE_RESULT.add(LIST_TWO_SPLICEABLES[0]);
        DELAY_START_ONE_RESULT.addAll(CLEAN_START_ONE_RESULT);
    }

    private static final List FRAYED_START_RESULT = new ArrayList();

    static {
        FRAYED_START_RESULT.add(LIST_ONE_SPLICEABLES[0]);
        FRAYED_START_RESULT.add(LIST_ONE_SPLICEABLES[1]);
        FRAYED_START_RESULT.add(LIST_ONE_SPLICEABLES[2]);
        FRAYED_START_RESULT.add(LIST_TWO_SPLICEABLES[0]);
        FRAYED_START_RESULT.addAll(CLEAN_START_ONE_RESULT);
    }

    private static final List FRAYED_STOP_RESULT = new ArrayList();

    static {
        FRAYED_STOP_RESULT.addAll(FRAYED_START_RESULT);
//        FRAYED_STOP_RESULT.add(LIST_THREE_SPLICEABLES[5]);
        FRAYED_STOP_RESULT.add(LIST_FOUR_SPLICEABLES[1]);
//        FRAYED_STOP_RESULT.add(LIST_FOUR_SPLICEABLES[2]);
    }

    private static final List FRAYED_RESTOP_RESULT = new ArrayList();

    static {
        final List restopped = new ArrayList(LIST_FIVE);
        restopped.addAll(FRAYED_STOP_RESULT);
        restopped.add(LIST_THREE_SPLICEABLES[5]);
        restopped.add(LIST_FOUR_SPLICEABLES[2]);
        restopped.addAll(LIST_SIX);
        restopped.addAll(LIST_SEVEN);
        restopped.remove(restopped.size() - 1);
        restopped.addAll(LIST_EIGHT);
        restopped.remove(restopped.size() - 1);
        Collections.sort(restopped, new SpliceableComparator());

        FRAYED_RESTOP_RESULT.addAll(restopped);
    }

    private static final List IMPLICIT_STOP_RESULT = new ArrayList();

    static {
        IMPLICIT_STOP_RESULT.addAll(FRAYED_START_RESULT);
        IMPLICIT_STOP_RESULT.add(LIST_THREE_SPLICEABLES[5]);
        IMPLICIT_STOP_RESULT.add(LIST_FOUR_SPLICEABLES[1]);
        IMPLICIT_STOP_RESULT.add(LIST_FOUR_SPLICEABLES[2]);
    }

    // private static member data

    // private instance member data

    /**
     * Semaphone used to signal analysis thas run.
     */
    private boolean ran;

    /**
     * SplicerListener used to check state machine.
     */
    private StateTestingListener stateListener;

    /**
     * TruncationListener used to check state machine.
     */
    private TruncationListener truncationListener;

    /**
     * The object being tested.
     */
    private Splicer testObject;

    // constructors

    /**
     * Constructs and instance of this test.
     *
     * @param name the display name of the test being created.
     */
    protected AbstractSplicerTest(String name)
    {
        super(name);
    }

    // instance member method (alphabetic)

    /**
     * Returns an instance of the NewSPlicer configured to use the specified
     * SplicedAnalysis.
     *
     * @param analysis the SplicerFactory with which to create the Splicer.
     * @return the created Splicer.
     */
    protected abstract Splicer createNewSplicer(SplicedAnalysis analysis);

    private void completeImplicitStop(StrandTail two,
                                      StrandTail one,
                                      MockSplicedAnalysis analysis)
            throws OrderingException, ClosedStrandException
    {
        two.push(Splicer.LAST_POSSIBLE_SPLICEABLE);
        one.push(Splicer.LAST_POSSIBLE_SPLICEABLE);

        // let Strand weave.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STOPPED),
                     testObject.getStateString());
        stateListener.testState(Splicer.STOPPED);

        assertTrue(analysis.getFailureMessage(),
                   analysis.isSuccessful());

        checkTruncation(Splicer.LAST_POSSIBLE_SPLICEABLE,
                        FRAYED_STOP_RESULT);
    }

    private void executeCleanStart(Spliceable start,
                                   int stateChangeIndex,
                                   StrandTail one,
                                   StrandTail two)
            throws OrderingException,
                   ClosedStrandException
    {
        cleanStart(start,
                   0 == stateChangeIndex,
                   one,
                   two);

        if (0 == stateChangeIndex) {
            assertEquals("Splicer is not is the correct state",
                         testObject.getStateString(Splicer.STARTED),
                         testObject.getStateString());
            stateListener.testState(Splicer.STARTED);
        }

        // Check status bookkeeping
        final List pendingStrands = testObject.pendingStrands();
        assertEquals(1,
                     pendingStrands.size());
        assertTrue(one == pendingStrands.get(0));
        assertEquals(1,
                     one.size());
        assertEquals(2,
                     two.size());

        one.push(LIST_THREE);
        two.push(LIST_FOUR);

        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        if (1 == stateChangeIndex) {
            assertEquals("Splicer is not is the correct state",
                         testObject.getStateString(Splicer.STARTED),
                         testObject.getStateString());
            stateListener.testState(Splicer.STARTED);
        }
    }

    private void cleanStart(Spliceable start,
                            StrandTail one,
                            StrandTail two)
            throws OrderingException,
                   ClosedStrandException
    {
        cleanStart(start,
                   true,
                   one,
                   two);
    }

    private void cleanStart(Spliceable start,
                            boolean checkStarted,
                            StrandTail one,
                            StrandTail two)
            throws OrderingException,
                   ClosedStrandException
    {
        testObject.addSplicerListener(stateListener);
        testObject.start(start);

        try {
            Thread.sleep(getBeginDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STARTING),
                     testObject.getStateString());
        stateListener.testState(Splicer.STARTING);

        one.push(LIST_ONE);
        two.push(LIST_TWO);

        // let Strand weaver.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        if (checkStarted) {
            assertEquals("Splicer is not is the correct state",
                         testObject.getStateString(Splicer.STARTED),
                         testObject.getStateString());
            stateListener.testState(Splicer.STARTED);
        }
    }

    private void frayedStartWithTest(StrandTail one,
                                     StrandTail two,
                                     List listOne,
                                     List listTwo,
                                     List listThree,
                                     List listFour)
            throws OrderingException,
                   ClosedStrandException
    {
        frayedStart(one,
                    two,
                    listOne,
                    listTwo,
                    listThree,
                    listFour,
                    true);
    }

    private void frayedStartWithoutTest(StrandTail one,
                                        StrandTail two,
                                        List listOne,
                                        List listTwo,
                                        List listThree,
                                        List listFour)
            throws OrderingException,
                   ClosedStrandException
    {
        frayedStart(one,
                    two,
                    listOne,
                    listTwo,
                    listThree,
                    listFour,
                    false);
    }

    private void frayedStart(StrandTail one,
                             StrandTail two,
                             List listOne,
                             List listTwo,
                             List listThree,
                             List listFour,
                             boolean executeTest)
            throws OrderingException,
                   ClosedStrandException
    {
        testObject.addSplicerListener(stateListener);
        testObject.start();

        one.push(listOne);
        // let Strand weaver.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        if (executeTest) {
            assertEquals("Splicer is not is the correct state",
                         testObject.getStateString(Splicer.STARTING),
                         testObject.getStateString());
            stateListener.testState(Splicer.STARTING);
        }

        two.push(listTwo);

        // let Strand weaver.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        if (executeTest) {
            assertEquals("Splicer is not is the correct state",
                         testObject.getStateString(Splicer.STARTED),
                         testObject.getStateString());
            stateListener.testState(Splicer.STARTED);
        }

        one.push(listThree);
        two.push(listFour);

        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }
    }

    /**
     * Returns the delay allowed for Strand to begin.
     *
     * @return the delay allowed for Strand to begin.
     */
    protected abstract long getBeginDelay();

    /**
     * Returns the delay allowed for Strand to close.
     *
     * @return the delay allowed for Strand to close.
     */
    protected abstract long getClosureDelay();

    /**
     * Returns the delay allowed for Strand to weave.
     *
     * @return the delay allowed for Strand to weave.
     */
    protected abstract long getWeaveDelay();

    /**
     * Sets the object to be tested.
     *
     * @param testObject object to be tested.
     */
    protected void setNewSplicer(Splicer testObject)
    {
        this.testObject = testObject;
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp()
    {
        stateListener = new StateTestingListener();
        truncationListener = new TruncationListener();
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     *
     * @throws Exception if this method fails.
     */
    protected void tearDown()
            throws Exception
    {
        if (null != testObject) {
            testObject.removeSplicerListener(stateListener);
            try {
                testObject.dispose();
            } catch (IllegalStateException e) {
                // allow termination with extreme prejudice.
            }
            testObject = null;
        }
        super.tearDown();
    }

    /**
     * Test that a simple clean start works correctly.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testCleanStartOne()
            throws OrderingException,
                   ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        analysis.setExpectedObjects(CLEAN_START_ONE_RESULT);
        testObject = createNewSplicer(analysis);

        final StrandTail one = testObject.beginStrand();
        final StrandTail two = testObject.beginStrand();

        executeCleanStart(LIST_TWO_SPLICEABLES[1],
                          0,
                          one,
                          two);

        assertTrue(analysis.getFailureMessage(),
                   analysis.isSuccessful());
    }

    /**
     * Test that a clean start with identical Spliceable works correctly.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testCleanStartTwo()
            throws OrderingException,
                   ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        analysis.setExpectedObjects(CLEAN_START_TWO_RESULT);
        testObject = createNewSplicer(analysis);

        final StrandTail one = testObject.beginStrand();
        final StrandTail two = testObject.beginStrand();

        executeCleanStart(new MockSpliceable(6),
                          0,
                          one,
                          two);

        assertTrue(analysis.getFailureMessage(),
                   analysis.isSuccessful());
    }

    /**
     * Test that a clean start occuring after the initial Lists works
     * correctly.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testCleanStartThree()
            throws OrderingException,
                   ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        analysis.setExpectedObjects(CLEAN_START_THREE_RESULT);
        testObject = createNewSplicer(analysis);

        final StrandTail one = testObject.beginStrand();
        final StrandTail two = testObject.beginStrand();

        executeCleanStart(LIST_TWO_SPLICEABLES[4],
                          1,
                          one,
                          two);

        assertTrue(analysis.getFailureMessage(),
                   analysis.isSuccessful());
    }

    /**
     * Test that a clean start with identical Spliceable works correctly.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testCleanStopOne()
            throws OrderingException,
                   ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        analysis.setExpectedObjects(CLEAN_STOP_ONE_RESULT);
        testObject = createNewSplicer(analysis);

        final StrandTail one = testObject.beginStrand();
        final StrandTail two = testObject.beginStrand();

        cleanStart(new MockSpliceable(6),
                   one,
                   two);

        testObject.stop(new MockSpliceable(14));

        // let Strand weaver.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STOPPING),
                     testObject.getStateString());
        stateListener.testState(Splicer.STOPPING);

        one.push(LIST_THREE);
        two.push(LIST_FOUR);

        // let Strand weaver.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STOPPED),
                     testObject.getStateString());
        stateListener.testState(Splicer.STOPPED);

        assertTrue(analysis.getFailureMessage(),
                   analysis.isSuccessful());
    }

    /**
     * Tests that the analyze function execute the analysis object and blocks
     * correctly.
     */
    public void testAnalyze()
    {
        testObject = createNewSplicer(new SplicedAnalysis()
        {
            public void execute(List splicedObjects,
                                int decrement)
            {
                // intentionally pause to check analyze blocks.
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // do nothing special if interrupted.
                }

                ran = true;
            }

            public SpliceableFactory getFactory()
            {
                return null;
            }
        });
        testObject.analyze();

        assertTrue("Analysis did not complete!",
                   ran);
    }

    /**
     * Test that a clean start with identical Spliceable works correctly.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testCleanRestartAndStop()
            throws OrderingException,
                   ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        analysis.setExpectedObjects(CLEAN_RESTOP_RESULT);
        testObject = createNewSplicer(analysis);

        final StrandTail one = testObject.beginStrand();
        final StrandTail two = testObject.beginStrand();

        cleanStart(new MockSpliceable(6),
                   one,
                   two);

        final Spliceable cutPoint = new MockSpliceable(17);
        testObject.stop(cutPoint);

        // let Strand weaver.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STOPPING),
                     testObject.getStateString());
        stateListener.testState(Splicer.STOPPING);

        one.push(LIST_THREE);
        one.push(LIST_FIVE);
        two.push(LIST_FOUR);

        // let Strand weaver.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STOPPED),
                     testObject.getStateString());
        stateListener.testState(Splicer.STOPPED);

        testObject.start(cutPoint);

        // let Strand weaver.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STARTING),
                     testObject.getStateString());
        stateListener.testState(Splicer.STARTING);

        two.push(LIST_SIX);

        // let Strand weaver.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STARTED),
                     testObject.getStateString());
        stateListener.testState(Splicer.STARTED);

        testObject.stop(new MockSpliceable(21));

        // let Strand weaver.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STOPPING),
                     testObject.getStateString());
        stateListener.testState(Splicer.STOPPING);

        two.push(LIST_EIGHT);
        one.push(LIST_SEVEN);

        // let Strand weaver.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STOPPED),
                     testObject.getStateString());
        stateListener.testState(Splicer.STOPPED);

        assertTrue(analysis.getFailureMessage(),
                   analysis.isSuccessful());
    }

    /**
     * Test that a request for a clean start is rejected is the stop SPliceable
     * has already passed.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testCleanStopReject()
            throws OrderingException,
                   ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        analysis.setExpectedObjects(CLEAN_STOP_ONE_RESULT);
        testObject = createNewSplicer(analysis);

        final StrandTail one = testObject.beginStrand();
        final StrandTail two = testObject.beginStrand();

        cleanStart(new MockSpliceable(6),
                   one,
                   two);

        try {
            testObject.stop(new MockSpliceable(8));
            fail("OrderingException should have been thrown");
        } catch (OrderingException e) {
            // Should be thrown
        }

    }

    /**
     * Test that a clean start get deplayed correctly when one or more Strands
     * start after the specified start.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testDelayStartOne()
            throws OrderingException,
                   ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        analysis.setExpectedObjects(DELAY_START_ONE_RESULT);
        testObject = createNewSplicer(analysis);

        final StrandTail one = testObject.beginStrand();
        final StrandTail two = testObject.beginStrand();

        executeCleanStart(LIST_ONE_SPLICEABLES[1],
                          0,
                          one,
                          two);

        assertTrue(analysis.getFailureMessage(),
                   analysis.isSuccessful());
    }

    /**
     * Test that a clean start get deplayed correctly when one or more Strands
     * start after the specified start when individual Spliceables are used
     * rather than Lists.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testDelayStartSequence()
            throws OrderingException,
                   ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        analysis.setExpectedObjects(DELAY_START_ONE_RESULT);
        testObject = createNewSplicer(analysis);
        testObject.addSplicerListener(stateListener);

        final StrandTail one = testObject.beginStrand();
        final StrandTail two = testObject.beginStrand();
        testObject.start(LIST_ONE_SPLICEABLES[1]);

        Iterator iterator = LIST_TWO.iterator();
        while (iterator.hasNext()) {
            two.push((Spliceable) iterator.next());
        }
        iterator = LIST_FOUR.iterator();
        while (iterator.hasNext()) {
            two.push((Spliceable) iterator.next());
        }
        iterator = LIST_ONE.iterator();
        while (iterator.hasNext()) {
            one.push((Spliceable) iterator.next());
        }
        iterator = LIST_THREE.iterator();
        while (iterator.hasNext()) {
            one.push((Spliceable) iterator.next());
        }

        // let Strand weaver.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }


        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STARTED),
                     testObject.getStateString());
        stateListener.testState(Splicer.STARTED);

        assertTrue(analysis.getFailureMessage(),
                   analysis.isSuccessful());

    }

    /**
     * Test that a clean start get deplayed correctly when one or more Strands
     * start after the specified start.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testDelayStartTwo()
            throws OrderingException,
                   ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        analysis.setExpectedObjects(DELAY_START_ONE_RESULT);
        testObject = createNewSplicer(analysis);
        testObject.addSplicerListener(stateListener);

        final StrandTail one = testObject.beginStrand();
        final StrandTail two = testObject.beginStrand();
        testObject.start(LIST_ONE_SPLICEABLES[1]);

        one.push(LIST_ONE);
        one.push(LIST_THREE);

        // let Strand weaver.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STARTING),
                     testObject.getStateString());
        stateListener.testState(Splicer.STARTING);

        two.push(LIST_TWO);
        two.push(LIST_FOUR);

        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STARTED),
                     testObject.getStateString());
        stateListener.testState(Splicer.STARTED);

        assertTrue(analysis.getFailureMessage(),
                   analysis.isSuccessful());
    }

    /**
     * Test that a frayed stop followed by a frayed start works correctly.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testFrayedRestartAndStop()
            throws OrderingException,
                   ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        analysis.setExpectedObjects(FRAYED_RESTOP_RESULT);
        testObject = createNewSplicer(analysis);

        final StrandTail one = testObject.beginStrand();
        final StrandTail two = testObject.beginStrand();

        frayedStartWithTest(one,
                            two,
                            LIST_ONE,
                            LIST_TWO,
                            LIST_THREE,
                            LIST_FOUR);

        testObject.stop();

        // let Strand weaver.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STOPPED),
                     testObject.getStateString());
        stateListener.testState(Splicer.STOPPED);

        frayedStartWithTest(one,
                            two,
                            LIST_FIVE,
                            LIST_SIX,
                            LIST_SEVEN,
                            LIST_EIGHT);

        testObject.stop();

        // let Strand weaver.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STOPPED),
                     testObject.getStateString());
        stateListener.testState(Splicer.STOPPED);

        assertTrue(analysis.getFailureMessage(),
                   analysis.isSuccessful());
    }

    /**
     * Test that the frayed start works correctly.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testFrayedStart()
            throws OrderingException,
                   ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        analysis.setExpectedObjects(FRAYED_START_RESULT);
        testObject = createNewSplicer(analysis);

        final StrandTail one = testObject.beginStrand();
        final StrandTail two = testObject.beginStrand();

        frayedStartWithTest(one,
                            two,
                            LIST_ONE,
                            LIST_TWO,
                            LIST_THREE,
                            LIST_FOUR);

        assertTrue(analysis.getFailureMessage(),
                   analysis.isSuccessful());
    }

    /**
     * Test that the "frayed" stop works correctly.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testFrayedStop()
            throws OrderingException,
                   ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        analysis.setExpectedObjects(FRAYED_STOP_RESULT);
        testObject = createNewSplicer(analysis);

        final StrandTail one = testObject.beginStrand();
        final StrandTail two = testObject.beginStrand();

        frayedStartWithTest(one,
                            two,
                            LIST_ONE,
                            LIST_TWO,
                            LIST_THREE,
                            LIST_FOUR);

        testObject.stop();

        // let Strand weave.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STOPPED),
                     testObject.getStateString());
        stateListener.testState(Splicer.STOPPED);

        assertTrue(analysis.getFailureMessage(),
                   analysis.isSuccessful());
    }

    /**
     * Test that the splicer stops when it is in the STARTED state and all
     * Strand have had received a LAST_POSSIBLE_SPLICER.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testImplicitStopWhenStarted()
            throws OrderingException,
                   ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        analysis.setExpectedObjects(IMPLICIT_STOP_RESULT);
        testObject = createNewSplicer(analysis);
        testObject.addSplicerListener(truncationListener);

        final StrandTail one = testObject.beginStrand();
        final StrandTail two = testObject.beginStrand();

        frayedStartWithTest(one,
                            two,
                            LIST_ONE,
                            LIST_TWO,
                            LIST_THREE,
                            LIST_FOUR);

        completeImplicitStop(two, one, analysis);
    }

    /**
     * Test that the splicer stops when it is in the STARTING state and all
     * Strand have had received a LAST_POSSIBLE_SPLICER.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testImplicitRemovalOfEmptyStrand()
            throws OrderingException,
                   ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        analysis.setExpectedObjects(IMPLICIT_STOP_RESULT);
        testObject = createNewSplicer(analysis);
        testObject.addSplicerListener(truncationListener);

        final StrandTail four = testObject.beginStrand();
        final StrandTail one = testObject.beginStrand();
        final StrandTail two = testObject.beginStrand();
        final StrandTail three = testObject.beginStrand();

        frayedStartWithoutTest(one,
                               two,
                               LIST_ONE,
                               LIST_TWO,
                               LIST_THREE,
                               LIST_FOUR);

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STARTING),
                     testObject.getStateString());
        stateListener.testState(Splicer.STARTING);

        three.push(Splicer.LAST_POSSIBLE_SPLICEABLE);
        four.push(Splicer.LAST_POSSIBLE_SPLICEABLE);

        // let Strand weave.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STARTED),
                     testObject.getStateString());
        stateListener.testState(Splicer.STARTED);

        completeImplicitStop(two, one, analysis);
    }

    /**
     * Test that a single empty stand can correctly cause an implicit stop.
     */
    public void testSingleEmptyStrand()
            throws OrderingException, ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        testObject = createNewSplicer(analysis);

        final StrandTail one = testObject.beginStrand();

        testObject.addSplicerListener(stateListener);
        testObject.start();

        // let Strand weave.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STARTING),
                     testObject.getStateString());
        stateListener.testState(Splicer.STARTING);

        one.push(Splicer.LAST_POSSIBLE_SPLICEABLE);

        // let Strand weave.
        try {
            Thread.sleep(getWeaveDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Splicer is not is the correct state",
                     testObject.getStateString(Splicer.STOPPED),
                     testObject.getStateString());
        stateListener.testState(Splicer.STOPPED);
    }

    /**
     * Test that a stalled Splicer presents the correct information to the
     * client.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testStall()
            throws OrderingException,
                   ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        analysis.setExpectedObjects(FRAYED_START_RESULT);
        testObject = createNewSplicer(analysis);

        final StrandTail one = testObject.beginStrand();
        final StrandTail two = testObject.beginStrand();

        frayedStartWithTest(one,
                            two,
                            LIST_ONE,
                            LIST_TWO,
                            LIST_THREE,
                            LIST_FOUR);

        assertTrue(analysis.getFailureMessage(),
                   analysis.isSuccessful());

        final List stalledStrands = testObject.pendingStrands();

        assertEquals("Wrong number of Strands has stalled.",
                     1,
                     stalledStrands.size());
        assertEquals("Pending Strand not correct",
                     one,
                     stalledStrands.get(0));
    }

    /**
     * Test simple management of a Strand.
     */
    public void testStrandManagement()
    {
        testObject = createNewSplicer(null);
        final StrandTail strand = testObject.beginStrand();

        // let Strand begin.
        try {
            Thread.sleep(getBeginDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }
        assertEquals("Wrong number of Strands in Splicer after adding Strand.",
                     1,
                     testObject.getStrandCount());

        strand.close();

        // let Strand close.
        try {
            Thread.sleep(getClosureDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }
        assertEquals("Wrong number of Strands in Splicer after removing" +
                     " Strand.",
                     0,
                     testObject.getStrandCount());
    }

    /**
     * Test that the ropw gets truncated correctly.
     *
     * @throws OrderingException
     * @throws ClosedStrandException
     */
    public void testTruncate()
            throws OrderingException,
                   ClosedStrandException
    {
        final MockSplicedAnalysis analysis = new MockSplicedAnalysis(null);
        analysis.setExpectedObjects(FRAYED_START_RESULT);
        testObject = createNewSplicer(analysis);

        final StrandTail one = testObject.beginStrand();
        final StrandTail two = testObject.beginStrand();

        frayedStartWithTest(one,
                            two,
                            LIST_ONE,
                            LIST_TWO,
                            LIST_THREE,
                            LIST_FOUR);

        final Spliceable cutOff = new MockSpliceable(12);

        testObject.addSplicerListener(truncationListener);

        analysis.setFirstSpliceable(cutOff);
        testObject.truncate(cutOff);

        // let Strand close.
        try {
            Thread.sleep(getClosureDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }
        assertTrue(analysis.getFailureMessage(),
                   analysis.isSuccessful());
        checkTruncation(cutOff,
                        FRAYED_START_RESULT);
    }

    private void checkTruncation(Spliceable cutOff,
                                 List truncatedSpliceables)
    {
        assertTrue("Truncation Point is not correct.",
                   0 ==
                   cutOff.compareSpliceable(truncationListener.getTruncationPoint()));
        final int element = 0;
        final Iterator iterator =
                truncationListener.getDeadSpliceables().iterator();
        while (iterator.hasNext()) {
            final Spliceable expected =
                    (Spliceable) truncatedSpliceables.get(element);
            final Spliceable actual = (Spliceable) iterator.next();
            assertTrue("Dead Spliceable List is not correct",
                       0 == expected.compareSpliceable(actual));
        }
    }

    // static member methods (alphabetic)

    private static class TruncationListener
            extends SplicerAdapter
    {
        Spliceable truncationPoint;

        Collection deadSpliceables;

        public Spliceable getTruncationPoint()
        {
            return truncationPoint;
        }

        private Collection getDeadSpliceables()
        {
            return deadSpliceables;
        }

        public void truncated(SplicerChangedEvent
                event)
        {
            truncationPoint = event.getSpliceable();
            deadSpliceables = event.getAllSpliceables();
        }
    }

    /**
     * Create test suite for this class.
     *
     * @return the Test object containing the test suite for Splicer.
     */
    public static Test suite()
    {
        return new TestSuite(AbstractSplicerTest.class);
    }

    // Description of this object.
    // public String toString() {}
}
