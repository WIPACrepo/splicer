/*
 * class: SplicerAdapter
 *
 * Version $Id: SplicerAdapter.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: September 17 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer;

/**
 * This class provides empty implementation of all the methods in the {@link
 * SplicerListener} interface.
 *
 * @author patton
 * @version $Id: SplicerAdapter.java 2125 2007-10-12 18:27:05Z ksb $
 */
public class SplicerAdapter
        implements SplicerListener
{

    // constructors

    /**
     * Create an instance of this class.
     */
    public SplicerAdapter()
    {
    }

    // instance member method (alphabetic)

    public void disposed(SplicerChangedEvent event)
    {
    }

    public void failed(SplicerChangedEvent event)
    {
    }

    public void starting(SplicerChangedEvent event)
    {
    }

    public void started(SplicerChangedEvent event)
    {
    }

    public void stopped(SplicerChangedEvent event)
    {
    }

    public void stopping(SplicerChangedEvent event)
    {
    }

    public void truncated(SplicerChangedEvent event)
    {
    }
}
