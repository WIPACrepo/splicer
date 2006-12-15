/*
 * class: StrandTailImplTest
 *
 * Version $Id: StrandTailImplTest.java,v 1.5 2005/08/24 16:50:01 patton Exp $
 *
 * Date: July 31 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

import icecube.daq.splicer.test.StrandTailNoRunTest;
import icecube.icebucket.util.ThreadInvoker;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * This class defines the tests that any StrandTailImpl object should pass.
 *
 * @author patton
 * @version $Id: StrandTailImplTest.java,v 1.5 2005/08/24 16:50:01 patton Exp $
 */
public class StrandTailImplTest
        extends StrandTailNoRunTest
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
     * The invocable used to handle tail->strand data flow.
     */
    private ThreadInvoker invocable;

    /**
     * The object providing output from test object.
     */
    private StrandImpl objectOutput;

    /**
     * The object being tested.
     */
    private StrandTailImpl testObject;

    // constructors

    /**
     * Constructs and instance of this test.
     *
     * @param name the name of the test.
     */
    public StrandTailImplTest(String name)
    {
        super(name);
    }

    // instance member method (alphabetic)

    protected long getAcceptDelay()
    {
        return 1;
    }

    protected void splicerStopped()
    {
        invocable.invokeAndWait(new Runnable()
        {
            public void run()
            {
                objectOutput.proceed();
            }
        });
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

        invocable = new ThreadInvoker();
        Thread thread = new Thread(invocable);
        thread.start();

        objectOutput = new StrandImpl(NULL_MANAGER);
        testObject = (StrandTailImpl) objectOutput.getTail(invocable);
        setStrandTail(testObject);
        setStrand(objectOutput);
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
        invocable.terminate();
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
        return new TestSuite(StrandTailImplTest.class);
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
