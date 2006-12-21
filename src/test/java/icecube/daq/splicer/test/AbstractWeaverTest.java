/*
 * class: AbstractWeaverTest
 *
 * Version $Id: AbstractWeaverTest.java,v 1.5 2006/02/10 20:48:03 patton Exp $
 *
 * Date: July 29 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer.test;

import icecube.daq.splicer.Spliceable;
import icecube.daq.splicer.Weaver;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class defines the tests that any Weaver object should pass.
 *
 * @author patton
 * @version $Id: AbstractWeaverTest.java,v 1.5 2006/02/10 20:48:03 patton Exp $
 */
public class AbstractWeaverTest
        extends TestCase
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    private static final Spliceable[] STRAND_ONE_SPLICEABLES =
            new Spliceable[]{
                new MockSpliceable(1),
                new MockSpliceable(3),
                new MockSpliceable(4),
                new MockSpliceable(6)
            };
    private static final List LIST_ONE = Arrays.asList(STRAND_ONE_SPLICEABLES);

    private static final Spliceable[] STRAND_TWO_SPLICEABLES =
            new Spliceable[]{
                new MockSpliceable(2),
                new MockSpliceable(4),
                new MockSpliceable(5)
            };
    private static final List LIST_TWO = Arrays.asList(STRAND_TWO_SPLICEABLES);

    private static final MockStrand STRAND_ONE = new MockStrand(LIST_ONE);
    private static final MockStrand STRAND_TWO = new MockStrand(LIST_TWO);

    // private static member data

    // private instance member data

    /**
     * The object being tested.
     */
    private Weaver testObject;

    // constructors

    /**
     * Constructs and instance of this test.
     *
     * @param name the display name of the test being created.
     */
    public AbstractWeaverTest(String name)
    {
        super(name);
    }

    // instance member method (alphabetic)

    /**
     * Chckes that the specified rope conforms to the expected List.
     *
     * @param expected the fully ordered list of Spliceables.
     * @param rope the result of a weave.
     */
    void checkRope(List expected,
                   List rope)
    {
        Collections.sort(expected);
        final int finished = rope.size();
        if (0 == finished) {
            fail("Nothing added to the Rope.");
        }
        for (int spliceable = 0;
             finished != spliceable;
             spliceable++) {
            Spliceable expect = ((Spliceable) expected.get(spliceable));
            Spliceable woven = ((Spliceable) rope.get(spliceable));
            if (0 != expect.compareTo(woven)) {
                fail("Rope does not contain the correct Spliceable at index " +
                     spliceable +
                     ".");
            }
        }
    }

    private void executeDoubleWeave()
    {
        testObject.addStrand(STRAND_ONE);
        testObject.addStrand(STRAND_TWO);
        assertEquals("Wrong number of Strands",
                     2,
                     testObject.getStrandCount());

        List rope = new ArrayList(2);
        testObject.weave(rope);
        List expected = new ArrayList(Arrays.asList(STRAND_ONE_SPLICEABLES));
        expected.addAll(new ArrayList(Arrays.asList(STRAND_TWO_SPLICEABLES)));
        checkRope(expected,
                  rope);
    }

    private void executeSingleWeave()
    {
        testObject.addStrand(STRAND_ONE);
        List rope = new ArrayList(1);
        testObject.weave(rope);
        List expected = new ArrayList(Arrays.asList(STRAND_ONE_SPLICEABLES));
        checkRope(expected,
                  rope);
    }

    /**
     * Sets the object to be tested.
     *
     * @param testObject object to be tested.
     */
    protected void setWeaver(Weaver testObject)
    {
        this.testObject = testObject;
    }

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
     * Tests that adding a Strand twice does not cause problems.
     */
    public void testAddingDuplicateStrand()
    {
        STRAND_ONE.reset();
        STRAND_TWO.reset();

        testObject.addStrand(STRAND_TWO);

        executeDoubleWeave();
    }

    /**
     * Tests that adding a null Strand does not cause problems.
     */
    public void testAddingNullStrand()
    {
        STRAND_ONE.reset();
        STRAND_TWO.reset();

        testObject.addStrand(null);

        executeDoubleWeave();
    }

    /**
     * Test that weave works for two Strands.
     */
    public void testDoubleWeave()
    {
        STRAND_ONE.reset();
        STRAND_TWO.reset();

        executeDoubleWeave();
    }

    /**
     * Tests that adding a null Strand does not cause problems.
     */
    public void testRemovingNullStrand()
    {
        STRAND_ONE.reset();
        STRAND_TWO.reset();

        testObject.removeStrand(null);

        executeDoubleWeave();
    }

    /**
     * Test that weave works for one Strand.
     */
    public void testSingleWeave()
    {
        STRAND_ONE.reset();

        executeSingleWeave();
    }

    /**
     * Tests that removing a Strand twice does not cause problems.
     */
    public void testUnaddedStrand()
    {
        STRAND_ONE.reset();
        STRAND_TWO.reset();

        testObject.removeStrand(STRAND_TWO);

        executeSingleWeave();
    }

    // static member methods (alphabetic)

    /**
     * Create test suite for this class.
     *
     * @return the Test object containing the test suite for Weaver.
     */
    public static Test suite()
    {
        return new TestSuite(AbstractWeaverTest.class);
    }

    // Description of this object.
    // public String toString() {}
}