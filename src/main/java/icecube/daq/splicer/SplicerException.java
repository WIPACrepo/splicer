/*
 * class: SplicerException
 *
 * Version $Id: SplicerException.java 15570 2015-06-12 16:19:32Z dglo $
 *
 * Date: August 1 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

/**
 * This class is the base class for any exceptions thrown by this package.
 *
 * @author patton
 * @version $Id: SplicerException.java 15570 2015-06-12 16:19:32Z dglo $
 */
public class SplicerException
        extends Exception
{
    /**
     * Create an instance of this class.
     *
     * @param message the message string for this object.
     */
    public SplicerException(String message)
    {
        super(message);
    }

    /**
     * Create an instance of this class.
     *
     * @param message the message string for this object.
     * @param cause the exception which caused this object to be created.
     */
    public SplicerException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Create an instance of this class.
     *
     * @param cause the exception which caused this object to be created.
     */
    public SplicerException(Throwable cause)
    {
        super(cause);
    }
}
