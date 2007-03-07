/*
 * class: AbstractStrandTest
 *
 * Version $Id: AbstractStrandTest.java,v 1.1 2005/08/01 22:25:44 patton Exp $
 *
 * Date: July 31 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer.test;

import icecube.daq.splicer.Strand;
import icecube.daq.splicer.Spliceable;
import junit.framework.*;

import java.util.List;
import java.util.Arrays;

/**
 * This class defines the tests that any Strand object should pass.
 *
 * @version $Id: AbstractStrandTest.java,v 1.1 2005/08/01 22:25:44 patton Exp $
 * @author patton
 */
public class AbstractStrandTest
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

    // private static member data

    // private instance member data

    /** The object being tested. */
    private Strand testObject;

    // constructors

    /**
     * Constructs and instance of this test.
     *
     * @param name the display name of the test being created.
     */
    public AbstractStrandTest(String name)
    {
        super(name);
    }

    // instance member method (alphabetic)

    /**
     * Returns a List of Spliceables to put into the Strand.
     *
     * @return a List of Spliceables to put into the Strand.
     */
    protected List getSpliceables()
    {
        return LIST_ONE;
    }

    /**
     * Sets the object to be tested.
     *
     * @param testObject object to be tested.
     */
    protected void setStrand(Strand testObject)
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
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
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
     * Test the structure of Spliceables held in the Strand.
     */
    public void testContents()
    {
        if (testObject.isEmpty()) {
            fail("Test Strand must not be empty.");
        }

        final int finished = testObject.size();
        Spliceable tail = testObject.tail();
        Spliceable last = null;
        Spliceable head = null;
        for (int element = 0;
                finished != element;
                element++) {
            head = testObject.head();
            if (!head.equals(testObject.pull())) {
                fail("Head Spliceable not returned when pull is invoked.");
            }
            if ((null != last) &&
                (0 < last.compareTo(head))){
                fail("Head Spliceable is less than preceeding Spliceable");
            }
        }

        if (!testObject.isEmpty()) {
            fail("The Strand is not empty after " +
                 finished +
                 " Spliceables have been pulled.");
        }

        if (0 != tail.compareTo(head)) {
            fail("Tail Spliceable not the last Spliceable pulled for the" +
                 " Strand.");
        }
    }

    // static member methods (alphabetic)

    /**
     * Create test suite for this class.
     *
     * @return the Test object containing the test suite for Strand.
     */
    public static Test suite()
    {
        return new TestSuite(AbstractStrandTest.class);
    }

    // Description of this object.
    // public String toString() {}
}