/*
 * class: OrderingException
 *
 * Version $Id: OrderingException.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: August 1 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

/**
 * This class is thrown when there is a mis-ordering of Spliceables.
 *
 * @author patton
 * @version $Id: OrderingException.java 2125 2007-10-12 18:27:05Z ksb $
 */
public class OrderingException
        extends SplicerException
{
    // constructors

    /**
     * Create an instance of this class.
     *
     * @param message the message string for this object.
     */
    public OrderingException(String message)
    {
        super(message);
    }
}
