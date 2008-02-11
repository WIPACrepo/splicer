/*
 * class: MockSpliceable
 *
 * Version $Id: MockStrand.java,v 1.2 2005/08/09 17:21:15 patton Exp $
 *
 * Date: September 15 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer.test;

import icecube.daq.splicer.Spliceable;
import icecube.daq.splicer.Strand;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the Spliceable interface such that the functionality
 * of the cocentrator can be tested.
 *
 * @author patton
 * @version $Id: MockStrand.java,v 1.2 2005/08/09 17:21:15 patton Exp $
 */
public class MockStrand
        implements Strand
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    // private static member data

    // private instance member data

    /**
     * The ordered list of Spliceables this object will supply.
     */
    private final List contents;

    /**
     * The index to the "head" Spliceable.
     */
    private int headIndex;

    /**
     * The index to the "tail" Spliceable.
     */
    private int tailIndex;

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param contents the ordered list of Spliceables this object will supply.
     */
    public MockStrand(List contents)
    {
        this.contents = new ArrayList(contents);
        headIndex = 0;
        tailIndex = contents.size() - 1;
    }

    // instance member method (alphabetic)

    public Spliceable head()
    {
        if (tailIndex < headIndex) {
            return null;
        }
        return (Spliceable) contents.get(headIndex);
    }

    public boolean isEmpty()
    {
        return tailIndex < headIndex;
    }

    public Spliceable pull()
    {
        if (tailIndex < headIndex) {
            return null;
        }
        return (Spliceable) contents.get(headIndex++);
    }

    /**
     * Resets this object to the state in which is was created.
     */
    public void reset()
    {
        headIndex = 0;
        tailIndex = contents.size() - 1;
    }

    public int size()
    {
        return tailIndex - headIndex + 1;
    }

    public Spliceable tail()
    {
        if (tailIndex < headIndex) {
            return null;
        }
        return (Spliceable) contents.get(tailIndex);
    }

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}
