/*
 * class: EventBuildingExample
 *
 * Version $Id: EventBuildingExample.java,v 1.8 2006/02/10 19:54:22 patton Exp $
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class run an example of how a {@link Splicer Splicer} object can be
 * used.
 *
 * @author patton
 * @version $Id: EventBuildingExample.java,v 1.5 2004/08/04 20:41:56 patton Exp
 *          $
 */
public final class EventBuildingExample
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    /**
     * The number of generators to create.
     */
    private static final int GENERATOR_COUNT = 5;

    /**
     * The average time, in milliseconds, to 'stall' before generating.
     */
    private static final int GENERATOR_TIME = 500;

    /**
     * The leader used in the label of the generators.
     */
    private static final String LABEL_LEADER = "Generator ";

    /**
     * The length of the Label's leader.
     */
    private static final int LABEL_LEADER_LENGTH = LABEL_LEADER.length();

    /**
     * Event at which the Splicer is stopped.
     */
    private static final int EVENTS_TO_BUILD = 50;

    // private static member data

    // private instance member data

    /**
     * List of EventContributionGenerators suppliing data.
     */
    private final List generators = new ArrayList();

    /**
     * List of Thread running the EventContributionGenerators objects.
     */
    private final List threads = new ArrayList();

    // constructors

    /**
     * Create an instance of this class.
     *
     * @throws IOException is there is a problem with the channels.
     */
    private EventBuildingExample()
            throws IOException
    {
        LoggingConsumer.installDefault();

        final EventGatherer analysis = new EventGatherer(GENERATOR_COUNT);
        final EventContributionFactory factory =
                analysis.getEventContributionFactory();
        final Splicer splicer = new ChannelBasedSplicerImpl(analysis);
        analysis.setSplicer(splicer);
        splicer.addSplicerListener(new EventContributionGeneratorKiller(splicer));

        final List channels = getChannels();
        Iterator iterator = channels.iterator();
        while (iterator.hasNext()) {
            splicer.addSpliceableChannel((SelectableChannel) iterator.next());
        }
        factory.setCurrentEvent(new Integer(0));
        splicer.start();

        startChannels();

        // In safe more need to generate one more event that is being build to
        // account for the contributions left in the tail.
        // @todo work out how to fluch the tail!
        final int finished = EVENTS_TO_BUILD + 1;
        for (int event = 0;
             finished != event;
             event++) {
            final Integer eventNumber = new Integer(event);
            factory.setCurrentEvent(eventNumber);
            iterator = generators.iterator();
            if (EVENTS_TO_BUILD == event) {
                analysis.setFinished(true);
            } else {
            analysis.addEvent(eventNumber); // needs to preceed generators.
            }
            while (iterator.hasNext()) {
                ((EventContributionGenerator)
                        iterator.next()).requestEvent(eventNumber);
            }
        }
    }

    // instance member method (alphabetic)

    private List getChannels()
            throws IOException
    {
        final List result = new ArrayList();
        final StringBuffer labelBuffer = new StringBuffer(LABEL_LEADER + '0');
        for (int count = 0;
             GENERATOR_COUNT != count;
             count++) {
            labelBuffer.replace(LABEL_LEADER_LENGTH,
                                labelBuffer.length(),
                                Integer.toString(count));
            final Pipe pipe = Pipe.open();
            result.add(pipe.source());
            final String label = labelBuffer.toString();
            final EventContributionGenerator generator =
                    new EventContributionGenerator(pipe.sink(),
                                                   label,
                                                   GENERATOR_TIME);
            generators.add(generator);
            threads.add(new Thread(generator,
                                   label));
        }
        return result;
    }

    private void startChannels()
    {
        final Iterator iterator = threads.iterator();
        while (iterator.hasNext()) {
            ((Thread) iterator.next()).start();
        }
    }

    // static member methods (alphabetic)

    private final class EventContributionGeneratorKiller
            extends SplicerAdapter
    {
        private final Splicer splicer;

        private EventContributionGeneratorKiller(Splicer splicer)
        {
            this.splicer = splicer;
        }

        public void disposed(SplicerChangedEvent event)
        {
            final Iterator iterator = generators.iterator();
            while (iterator.hasNext()) {
                ((EventContributionGenerator)
                        iterator.next()).setFinished(true);
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
        new EventBuildingExample();
    }
}
