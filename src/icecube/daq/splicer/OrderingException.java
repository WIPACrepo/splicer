/*
 * class: OrderingException
 *
 * Version $Id: OrderingException.java,v 1.2 2005/08/09 01:32:27 patton Exp $
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
 * @version $Id: OrderingException.java,v 1.2 2005/08/09 01:32:27 patton Exp $
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
