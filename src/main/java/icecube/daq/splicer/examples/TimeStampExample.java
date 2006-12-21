/*
 * class: TimeStampExample
 *
 * Version $Id: TimeStampExample.java,v 1.12 2005/10/18 15:27:47 patton Exp $
 *
 * Date: September 19 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer.examples;

import icecube.daq.splicer.ChannelBasedSplicerImpl;
import icecube.daq.splicer.Splicer;
import icecube.daq.splicer.SplicerAdapter;
import icecube.daq.splicer.SplicerChangedEvent;
import icecube.icebucket.logging.LoggingConsumer;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class run an example of how a {@link Splicer Splicer} object can be
 * used.
 *
 * @author patton
 * @version $Id: TimeStampExample.java,v 1.12 2005/10/18 15:27:47 patton Exp $
 */
public class TimeStampExample
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    /**
     * The anticipated avarge payload size - used to set buffer sizes.
     */
    private static final int AVG_PAYLOAD_SIZE = 40;

    /**
     * The number of generators to create.
     */
    private static final int GENERATOR_COUNT = 5;

    /**
     * The average time, in milliseconds, between generator creations.
     */
    private static final int GENERATOR_PAUSE = 500;

    /**
     * Size of the List which will cause it to be cut in half.
     */
    private static final int CUTOFF_SIZE = 10;

    /**
     * Count at which the Splicer is stopped.
     */
    private static final int STOP_COUNT = 50;

    // private static member data

    // private instance member data

    /**
     * List of TimeStampGenerators suppliing data.
     */
    private final List generators = new ArrayList();

    // constructors

    /**
     * Create an instance of this class.
     *
     * @throws IOException is there is a problem with the channels.
     */
    protected TimeStampExample(int cutoffSize,
                               int stopCount,
                               int generatorCount,
                               int generatorPause)
            throws IOException
    {
        LoggingConsumer.installDefault();

        final TimeStampAnalysis analysis = new TimeStampAnalysis(cutoffSize,
                                                                 stopCount);
        final Splicer splicer =
                new ChannelBasedSplicerImpl(analysis,
                                            cutoffSize * AVG_PAYLOAD_SIZE);
        analysis.setSplicer(splicer);
        splicer.addSplicerListener(new TimeStampGeneratorKiller(splicer));

        final List channels = getChannels(generatorCount,
                                          generatorPause);
        final Iterator iterator = channels.iterator();
        while (iterator.hasNext()) {
            splicer.addSpliceableChannel((SelectableChannel) iterator.next());
        }
        splicer.start();

        startChannels();
    }

    // instance member method (alphabetic)

    private List getChannels(int generatorCount,
                             int generatorPause)
            throws IOException
    {
        final List result = new ArrayList();
        for (int count = 0;
             generatorCount != count;
             count++) {
            final Pipe pipe = Pipe.open();
            final AbstractSelectableChannel source = pipe.source();
            source.configureBlocking(false);
            result.add(source);
            final TimeStampGenerator generator =
                    new TimeStampGenerator(count,
                                           generatorPause);
            final AbstractSelectableChannel sink = pipe.sink();
            sink.configureBlocking(false);
            generator.setChannel((WritableByteChannel) sink);
            generators.add(generator);
            final Thread thread = new Thread(generator,
                                             "Generator ID=" +
                                             generator.getId());
            thread.start();
        }
        return result;
    }

    private void startChannels()
    {
        final Iterator iterator = generators.iterator();
        while (iterator.hasNext()) {
            ((TimeStampGenerator) iterator.next()).setGenerating(true);
        }
    }

    // static member methods (alphabetic)

    private final class TimeStampGeneratorKiller
            extends SplicerAdapter
    {
        private final Splicer splicer;

        private TimeStampGeneratorKiller(Splicer splicer)
        {
            this.splicer = splicer;
        }

        public void disposed(SplicerChangedEvent event)
        {
            final Iterator iterator = generators.iterator();
            while (iterator.hasNext()) {
                ((TimeStampGenerator) iterator.next()).setFinish(true);
            }
        }

        public void stopped(SplicerChangedEvent event)
        {
            splicer.dispose();
        }
    }

    // Description of this object.
    // public String toString() {}

    /**
     * Runs the TimeStamped example.
     *
     * @param args not used.
     * @throws IOException if there is a problem with IO during execution.
     */
    public static void main(String[] args)
            throws IOException
    {
        new TimeStampExample(CUTOFF_SIZE,
                             STOP_COUNT,
                             GENERATOR_COUNT,
                             GENERATOR_PAUSE);
    }
}
