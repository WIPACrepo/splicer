/*
 * class: ClosedStrandException
 *
 * Version $Id: ClosedStrandException.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: August 1 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

/**
 * This class is thrown when there is an attempt to push a Spliceable into a
 * closed Strand.
 *
 * @author patton
 * @version $Id: ClosedStrandException.java,v 1.1 2005/08/01 22:33:03 patton
 *          Exp $
 */
public class ClosedStrandException
        extends SplicerException
{
    // constructors

    /**
     * Create an instance of this class.
     *
     * @param message the message string for this object.
     */
    public ClosedStrandException(String message)
    {
        super(message);
    }
}
