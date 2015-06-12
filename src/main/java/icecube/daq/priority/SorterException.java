package icecube.daq.priority;

/**
 * Priority sort exception
 */
public class SorterException
    extends Exception
{
    private static final long serialVersionUID = 1010L;

    public SorterException(String msg)
    {
        super(msg);
    }

    public SorterException(Throwable thr)
    {
        super(thr);
    }

    public SorterException(String msg, Throwable thr)
    {
        super(msg, thr);
    }
}
