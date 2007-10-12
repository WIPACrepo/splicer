/*
 * class: SplicerException
 *
 * Version $Id: SplicerException.java 2125 2007-10-12 18:27:05Z ksb $
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
 * @version $Id: SplicerException.java 2125 2007-10-12 18:27:05Z ksb $
 */
public class SplicerException
        extends Exception
{
    // constructors

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
    public SplicerException(String message,
                            Throwable cause)
    {
        super(message,
              cause);
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
