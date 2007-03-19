/*
 * class: MockSplicedAnalysis
 *
 * Version $Id: MockSplicedAnalysis.java,v 1.5 2005/08/05 17:29:55 patton Exp $
 *
 * Date: September 15 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer.test;

import icecube.daq.splicer.Spliceable;
import icecube.daq.splicer.SpliceableFactory;
import icecube.daq.splicer.SplicedAnalysis;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the SplicedAnalysis interface such that it can assert
 * that the MockSpliceable objects created by a Splicer appear and that they
 * are in the correct order.
 *
 * @author patton
 * @version $Id: MockSplicedAnalysis.java,v 1.4 2004/08/04 20:41:56 patton Exp
 *          $
 */
public class MockSplicedAnalysis
        implements SplicedAnalysis
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    // private static member data

    // private instance member data

    /**
     * true if there has been a failure in the comparison section.
     */
    private boolean compareFailure;

    /**
     * The current position in the expectedObjects List.
     */
    private int cursor;

    /**
     * A List of MockSpliceable objects in the order they are expected.
     */
    private List expectedObjects;

    /**
     * The factory used to produce object for this object to use.
     */
    private final SpliceableFactory factory;

    /**
     * Explanation of failure, if failure exists.
     */
    private String failureMessage;

    /**
     * The objects expected to be first in the splicedObjeccts list.
     */
    private Spliceable firstSplicable;

    /**
     * True is the execute method has been run since a change in this object.
     */
    private boolean ran;

    /**
     * The index at which to start conparing objects.
     */
    private int start;

    /**
     * true if the analysis is successful.
     */
    private boolean successful;

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param factory the MockSpliceableFactory to be used with this object.
     */
    public MockSplicedAnalysis(MockSpliceableFactory factory)
    {
        this.factory = factory;
        failureMessage = "Have not seen enough spliceables.";
    }

    // instance member method (alphabetic)

    public void execute(List splicedObjects,
                        int decrement)
    {
        ran = true;
        if (compareFailure) {
            return;
        }

        if (null != firstSplicable &&
            0 != firstSplicable.compareTo(splicedObjects.get(0))) {
            failureMessage = "First Spliceable did not match expected.";
            compareFailure = true;
        }

        final int finished = splicedObjects.size();
        for (int index = start - decrement;
             finished != index;
             index++) {
            if (cursor == expectedObjects.size()) {
                failureMessage = "Too many Spliceable; expected <" +
                                 expectedObjects.size() +
                                 "> but seen at least <" +
                                 (cursor + finished - index) +
                                 ">.";
                setSuccessful(false);
            } else {
                final MockSpliceable expected =
                        (MockSpliceable) expectedObjects.get(cursor);
                final MockSpliceable actual =
                        (MockSpliceable) splicedObjects.get(index);
                if (0 != expected.compareTo(actual)) {
                    failureMessage = "Expected #" + cursor + ": " + expected +
                        " did not match actual #" + index + ": " + actual + ".";
                    compareFailure = true;
                }
                cursor++;
                if (!compareFailure &&
                    cursor == expectedObjects.size()) {
                    setSuccessful(true);
                }
            }
        }
        start = finished;
    }

    public SpliceableFactory getFactory()
    {
        return factory;
    }

    /**
     * Returned the explanation why this object was not successful.
     *
     * @return the explanation why this object was not successful.
     */
    public String getFailureMessage()
    {
        if (!ran) {
            return "The execute method was not run after changes in this" +
                   " object.";
        }
        return failureMessage;
    }

    /**
     * Returns true if the analysis has proceded as expected.
     *
     * @return true if the analysis has proceded as expected.
     */
    public synchronized boolean isSuccessful()
    {
        if (ran) {
            return successful;
        }
        return false;
    }

    /**
     * Sets the successful flag to the specified value.
     *
     * @param successful the value to be set in the successful flag.
     */
    private synchronized void setSuccessful(boolean successful)
    {
        this.successful = successful;
    }

    /**
     * Sets the List of objects that are expected during the test.
     *
     * @param expectedObjects the List of objects expected.
     */
    public void setExpectedObjects(List expectedObjects)
    {
        this.expectedObjects = new ArrayList(expectedObjects);
        ran = false;
    }

    /**
     * Sets the Spliceable that is expected to be the first in the list during
     * the next invocation of the {@link #execute} method.
     *
     * @param spliceable the object exoected to be a the beginning of the
     * List.
     */
    public void setFirstSplicable(Spliceable spliceable)
    {
        firstSplicable = spliceable;
        ran = false;
    }

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}
