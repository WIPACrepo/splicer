/*
 * class: StrandImplTest
 *
 * Version $Id: MockStrandTest.java,v 1.1 2005/08/01 22:25:44 patton Exp $
 *
 * Date: July 31 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * This class check the behaviour of the MockStrand and the generic Strand
 * test.
 *
 * @author patton
 * @version $Id: MockStrandTest.java,v 1.1 2005/08/01 22:25:44 patton Exp $
 */
public class MockStrandTest
        extends AbstractStrandTest
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    // private static member data

    // private instance member data

    /**
     * The object being tested.
     */
    //private StrandImpl testObject;

    // constructors

    /**
     * Constructs and instance of this test.
     *
     * @param name the name of the test.
     */
    public MockStrandTest(String name)
    {
        super(name);
    }

    // instance member method (alphabetic)

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
        setStrand(new MockStrand(getSpliceables()));
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
        //testObject = null;
        super.tearDown();
    }

    // static member methods (alphabetic)

    /**
     * Create test suite for this class.
     *
     * @return the suite of tests declared in this class.
     */
    public static Test suite()
    {
        return new TestSuite(MockStrandTest.class);
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
