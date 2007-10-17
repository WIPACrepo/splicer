/*
 * class: MockSpliceable
 *
 * Version $Id: MockSpliceable.java,v 1.1 2005/08/01 22:26:00 patton Exp $
 *
 * Date: September 15 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer.test;

import icecube.daq.splicer.Spliceable;

/**
 * This class implements the Spliceable interface such that the functionality
 * of the components of the Splicer pattern can be tested.
 *
 * @author patton
 * @version $Id: MockSpliceable.java,v 1.1 2005/08/01 22:26:00 patton Exp $
 */
public class MockSpliceable
        implements Spliceable
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    // private static member data

    // private instance member data

    /**
     * The ordering represented by this object.
     */
    private final long order;

    /**
     * The length of this object in numebr of bytes.
     */
    private final byte length;

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param order the order, represented by a number, of this object.
     */
    public MockSpliceable(long order)
    {
        this(order,
             0);
    }

    /**
     * Create an instance of this class.
     *
     * @param order the order, represented by a number, of this object.
     * @param length the length of this object in bytes.
     */
    public MockSpliceable(long order,
                   int length)
    {
        this.order = order;
        this.length = (byte) length;
    }

    // instance member method (alphabetic)

    public int compareTo(Object object)
    {
        final MockSpliceable rhs = (MockSpliceable) object;
        if (order < rhs.order) {
            return -1;
        } else if (order > rhs.order) {
            return 1;
        }
        return 0;
    }

    /**
     * Returns the length of this object in numebr of bytes.
     *
     * @return the length of this object in numebr of bytes.
     */
    public byte getLength()
    {
        return length;
    }

    /**
     * Returns the order of this object.
     *
     * @return the order of this object.
     */
    public long getOrder()
    {
        return order;
    }

    // static member methods (alphabetic)

    // Description of this object.

    public String toString()
    {
        return "MockSpliceable[order " + order + ", len " + length + "]";
    }

    // public static void main(String args[]) {}
}
