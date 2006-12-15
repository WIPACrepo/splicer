/*
 * class: StrandImplTest
 *
 * Version $Id: StrandImplTest.java,v 1.3 2005/08/24 16:50:02 patton Exp $
 *
 * Date: July 31 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

import icecube.daq.splicer.test.StrandNoRunTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.util.List;

/**
 * This class defines the tests that any StrandImpl object should pass.
 *
 * @author patton
 * @version $Id: StrandImplTest.java,v 1.3 2005/08/24 16:50:02 patton Exp $
 */
public class StrandImplTest
        extends StrandNoRunTest
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    private static final StrandManager NULL_MANAGER = new StrandManager()
    {
        public void strandBecameEmpty(ManagedStrand strand)
        {
        }

        public void strandBecameRemoveable(ManagedStrand strand)
        {
        }

        public void strandClosed(ManagedStrand strand)
        {
        }

        public void strandNoLongerEmpty(ManagedStrand strand)
        {
        }
    };

    // private static member data

    // private instance member data

    /**
     * The object being tested.
     */
    private StrandImpl testObject;

    // constructors

    /**
     * Constructs and instance of this test.
     *
     * @param name the name of the test.
     */
    public StrandImplTest(String name)
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
        testObject = new StrandImpl(NULL_MANAGER);
        List spliceables = getSpliceables();
        int[] offsets = new int[1];
        offsets[0] = spliceables.size() - 1;
        testObject.push(spliceables,
                        offsets,
                        1);
        setStrand(testObject);
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

    // static member methods (alphabetic)

    /**
     * Create test suite for this class.
     *
     * @return the suite of tests declared in this class.
     */
    public static Test suite()
    {
        return new TestSuite(StrandImplTest.class);
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
