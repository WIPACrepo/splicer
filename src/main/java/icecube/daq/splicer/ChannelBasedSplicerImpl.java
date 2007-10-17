/*
 * class: ChannelBasedSplicerImpl
 *
 * Version $Id: ChannelBasedSplicerImpl.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: August 8 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.splicer;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.List;

/**
 * This class added an implementation of SpliceableChannel management to the
 * basic {@link StrandImpl} class.
 *
 * @author patton
 * @version $Id: ChannelBasedSplicerImpl.java,v 1.1 2005/08/08 22:40:14 patton
 *          Exp $
 */
public class ChannelBasedSplicerImpl
        extends SplicerImpl
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    // private static member data

    // private instance member data

    private final SpliceableChannelsCarder carder;

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param analysis the SplicedAnalysis to be executed by this object.
     */
    public ChannelBasedSplicerImpl(SplicedAnalysis analysis)
    {
        super(analysis);
        carder = new SpliceableChannelsCarder(this,
                                              analysis.getFactory());
    }

    /**
     * Create an instance of this class.
     *
     * @param analysis the SplicedAnalysis to be executed by this object.
     * @param bufferSize the size of ByteBuffers this object should use.
     */
    public ChannelBasedSplicerImpl(SplicedAnalysis analysis,
                                   int bufferSize)
    {
        super(analysis);
        carder = new SpliceableChannelsCarder(this,
                                              analysis.getFactory(),
                                              bufferSize);
    }

    // instance member method (alphabetic)

    public void addSpliceableChannel(SelectableChannel channel)
            throws IOException
    {
        carder.addSpliceableChannel(channel);
    }

    public void dispose()
    {
        carder.dispose();
        super.dispose();
    }

    public List pendingChannels()
    {
        return carder.getChannels(pendingStrands());
    }

    public void removeSpliceableChannel(SelectableChannel channel)
    {
        carder.removeSpliceableChannel(channel);
    }

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}