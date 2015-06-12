/*
 * class: OrderingException
 *
 * Version $Id: OrderingException.java 15570 2015-06-12 16:19:32Z dglo $
 *
 * Date: August 1 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

/**
 * This class is thrown when there is a mis-ordering of objects.
 *
 * @author patton
 * @version $Id: OrderingException.java 15570 2015-06-12 16:19:32Z dglo $
 */
public class OrderingException
        extends SplicerException
{
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
