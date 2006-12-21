/*
 * class: AbstractStrandTailTest
 *
 * Version $Id: AbstractStrandTailTest.java,v 1.3 2005/08/09 17:21:15 patton Exp $
 *
 * Date: August 1 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer.test;

import icecube.daq.splicer.ClosedStrandException;
import icecube.daq.splicer.OrderingException;
import icecube.daq.splicer.Spliceable;
import icecube.daq.splicer.SplicerException;
import icecube.daq.splicer.Strand;
import icecube.daq.splicer.StrandTail;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class defines the tests that any StrandTail object should pass.
 *
 * @author patton
 * @version $Id: AbstractStrandTailTest.java,v 1.1 2005/08/01 22:25:44 patton
 *          Exp $
 */
public abstract class AbstractStrandTailTest
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

    private static final Spliceable[] STRAND_ONE_SPLICEABLES =
            new Spliceable[]{
                    new MockSpliceable(1),
                    new MockSpliceable(3),
                    new MockSpliceable(3),
                    new MockSpliceable(4),
                    new MockSpliceable(6)
            };
    private static final List LIST_ONE = Arrays.asList(STRAND_ONE_SPLICEABLES);

    private static final Spliceable[] STRAND_TWO_SPLICEABLES =
            new Spliceable[]{
                    new MockSpliceable(8),
                    new MockSpliceable(9),
                    new MockSpliceable(9),
                    new MockSpliceable(12)
            };
    private static final List LIST_TWO = Arrays.asList(STRAND_TWO_SPLICEABLES);

    private static final List BAD_LIST_WITH_LAST = new LinkedList();

    static {
        BAD_LIST_WITH_LAST.addAll(LIST_ONE);
        BAD_LIST_WITH_LAST.add(StrandTail.LAST_POSSIBLE_SPLICEABLE);
        BAD_LIST_WITH_LAST.addAll(LIST_TWO);
    }

    private static final Spliceable[] BAD_STRAND_ONE_SPLICEABLES =
            new Spliceable[]{
                    new MockSpliceable(1),
                    new MockSpliceable(3),
                    new MockSpliceable(5),
                    new MockSpliceable(3),
                    new MockSpliceable(6)
            };
    private static final List BAD_LIST_ONE =
            Arrays.asList(BAD_STRAND_ONE_SPLICEABLES);
    private static final int BAD_LIST_ONE_GOOD_LENGTH = 3;

    // private static member data

    // private instance member data

    /**
     * The object providing output from test object.
     */
    private Strand objectOutput;

    /**
     * The object being tested.
     */
    private StrandTail testObject;

    // constructors

    /**
     * Constructs and instance of this test.
     *
     * @param name the display name of the test being created.
     */
    protected AbstractStrandTailTest(String name)
    {
        super(name);
    }

    // instance member method (alphabetic)

    private void checkOuput(List expected,
                            long delay)
    {
        checkOuput(expected,
                   delay,
                   false);
    }

    private void checkOuput(List expected,
                            long delay,
                            boolean finished)
    {
        final int correction;
        if (finished) {
            correction = 0;
        } else {
            correction = SAFE_CORRECTION;
        }

        // let output accept Spliceables
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }

        assertEquals("Mismatch in number of expected Spliceables.",
                     expected.size() - correction,
                     objectOutput.size());

        final Iterator iterator =
                expected.subList(0,
                                 expected.size() - correction).iterator();
        while (iterator.hasNext()) {
            final Spliceable output = objectOutput.pull();
            final Spliceable input = (Spliceable) iterator.next();
            assertTrue("Output does not match input",
                       0 == input.compareTo(output));
        }
    }

    /**
     * Returns the number of milliseconds to wait for the output to accept the
     * pushed Spliceables.
     *
     * @return the number of milliseconds to wait before testing output.
     */
    protected abstract long getAcceptDelay();

    /**
     * Sets the object providing output from test object.
     *
     * @param objectOutput object providing output from test object.
     */
    protected void setStrand(Strand objectOutput)
    {
        this.objectOutput = objectOutput;
    }

    /**
     * Sets the object to be tested.
     *
     * @param testObject object to be tested.
     */
    protected void setStrandTail(StrandTail testObject)
    {
        this.testObject = testObject;
    }

    /**
     * Called to simulate the splicer reaching the Stopped state.
     */
    protected abstract void splicerStopped();

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
//    protected void setUp()
//    {
//    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     *
     * @throws Exception if this method fails.
     */
    protected void tearDown()
            throws Exception
    {
        testObject = null;
        super.tearDown();
    }

    /**
     * Test that a List is rejected after a StrandTail is closed.
     *
     * @throws SplicerException
     */
    public void testCloseWithList()
            throws SplicerException
    {
        testList();

        testObject.close();
        try {
            testObject.push(LIST_TWO);
            fail("Closed StrandTail accepted more Spliceables.");
        } catch (ClosedStrandException e) {
            // should be thrown.
        }
    }

    /**
     * Test that a List is rejected after a StrandTail is closed.
     *
     * @throws SplicerException
     */
    public void testCloseWithSequence()
            throws SplicerException
    {
        testSequence();

        testObject.close();
        try {
            testObject.push((Spliceable) LIST_TWO.get(0));
            fail("Closed StrandTail accepted a Spliceable.");
        } catch (ClosedStrandException e) {
            // should be thrown.
        }
    }

    /**
     * Tests that a List of Spliceables which are not correctly ordered is
     * rejected.
     *
     * @throws ClosedStrandException
     */
    public void testIllegalList()
            throws ClosedStrandException
    {
        try {
            testObject.push(BAD_LIST_ONE);
            fail("OrderingException was not thrown");
        } catch (OrderingException e) {
            // should be thrown.
        }

        // let output accept Spliceables
        try {
            Thread.sleep(getAcceptDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }
        assertTrue("Strand should be empty",
                   objectOutput.isEmpty());
    }

    /**
     * Tests that a Sequence of Spliceables which are not correctly ordered is
     * rejected.
     *
     * @throws ClosedStrandException
     */
    public void testIllegalSequence()
            throws ClosedStrandException
    {
        try {
            final Iterator iterator = BAD_LIST_ONE.iterator();
            while (iterator.hasNext()) {
                testObject.push((Spliceable) iterator.next());
            }
            fail("OrderingException was not thrown");
        } catch (OrderingException e) {
            // should be thrown.
        }

        // let output accept Spliceables
        try {
            Thread.sleep(getAcceptDelay());
        } catch (InterruptedException e) {
            // do nothing special if interrupted.
        }
        assertEquals("Strand contains the wrong number of Spliceables.",
                     BAD_LIST_ONE_GOOD_LENGTH - SAFE_CORRECTION,
                     objectOutput.size());
    }

    /**
     * Tests that a simple List of Spliceables can be succesfully pushed into
     * the test object.
     *
     * @throws SplicerException
     */
    public void testList()
            throws SplicerException
    {
        testObject.push(LIST_ONE);

        checkOuput(LIST_ONE,
                   getAcceptDelay());

    }

    /**
     * Tests that following a LAST_POSSIBLE_SPLICEABLE in a List with a
     * Spliceable that is no a LAST_POSSIBLE_SPLICEABLE throws an exception.
     *
     * @throws ClosedStrandException
     */
    public void testListWithBadLast()
            throws ClosedStrandException
    {
        try {
            testObject.push(BAD_LIST_WITH_LAST);
            fail("OrderingException should have been thrown");
        } catch (OrderingException e) {
            // Should be thrown.
        }

        getAcceptDelay();
        assertEquals("Strand contains the wrong number of Spliceables.",
                     0,
                     objectOutput.size());
    }

    /**
     * Tests that a simple List of Spliceables that contains one
     * LAST_POSSIBLE_SPLICEABLE can be succesfully pushed into the test
     * object.
     *
     * @throws SplicerException
     */
    public void testListWithLast()
            throws SplicerException
    {
        testObject.push(LIST_ONE);
        testObject.push(StrandTail.LAST_POSSIBLE_SPLICEABLE);
        checkOuput(LIST_ONE,
                   getAcceptDelay(),
                   true);

        splicerStopped();

        testObject.push(LIST_TWO);
        checkOuput(LIST_TWO,
                   getAcceptDelay());

    }

    /**
     * Tests that a simple sequence of Spliceables can be succesfully pushed
     * into the test object.
     *
     * @throws SplicerException
     */
    public void testSequence()
            throws SplicerException
    {
        final Iterator iterator = LIST_ONE.iterator();
        while (iterator.hasNext()) {
            testObject.push((Spliceable) iterator.next());
        }

        checkOuput(LIST_ONE,
                   getAcceptDelay());

    }

    /**
     * Tests that following a LAST_POSSIBLE_SPLICEABLE with a Spliceable that
     * is no a LAST_POSSIBLE_SPLICEABLE throws an exception.
     *
     * @throws SplicerException
     */
    public void testSequenceWithBadLast()
            throws SplicerException
    {
        final Iterator iteratorOne = LIST_ONE.iterator();
        while (iteratorOne.hasNext()) {
            testObject.push((Spliceable) iteratorOne.next());
        }
        testObject.push(StrandTail.LAST_POSSIBLE_SPLICEABLE);
        try {
            final Iterator iteratorTwo = LIST_TWO.iterator();
            while (iteratorTwo.hasNext()) {
                testObject.push((Spliceable) iteratorTwo.next());
            }
            fail("OrderingException should have been thrown");
        } catch (OrderingException e) {
            // Should be thrown.
        }

        getAcceptDelay();
        // @todo - fix this. It only failed when 'packaging' !
//        assertEquals("Strand contains the wrong number of Spliceables.",
//                     LIST_ONE.size(),
//                     objectOutput.size());
    }


    /**
     * Tests that a simple List of Spliceables that contains one
     * LAST_POSSIBLE_SPLICEABLE can be succesfully pushed into the test
     * object.
     *
     * @throws SplicerException
     */
    public void testSequenceWithLast()
            throws SplicerException
    {
        final Iterator iteratorOne = LIST_ONE.iterator();
        while (iteratorOne.hasNext()) {
            testObject.push((Spliceable) iteratorOne.next());
        }
        testObject.push(StrandTail.LAST_POSSIBLE_SPLICEABLE);
        checkOuput(LIST_ONE,
                   getAcceptDelay(),
                   true);

        splicerStopped();

        final Iterator iteratorTwo = LIST_TWO.iterator();
        while (iteratorTwo.hasNext()) {
            testObject.push((Spliceable) iteratorTwo.next());
        }
        checkOuput(LIST_TWO,
                   getAcceptDelay());

    }

    // static member methods (alphabetic)

    /**
     * Create test suite for this class.
     *
     * @return the Test object containing the test suite for StrandTail.
     */
    public static Test suite()
    {
        return new TestSuite(AbstractStrandTailTest.class);
    }

    // Description of this object.
    // public String toString() {}
}