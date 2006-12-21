/*
 * class: EventGatherer
 *
 * Version $Id: EventGatherer.java,v 1.9 2005/08/09 01:28:48 patton Exp $
 *
 * Date: October 21 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer.examples;

import icecube.daq.splicer.Spliceable;
import icecube.daq.splicer.SpliceableFactory;
import icecube.daq.splicer.SplicedAnalysis;
import icecube.daq.splicer.Splicer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * This class is an implementation of the {@link SplicedAnalysis} interface
 * that gatherers together all the contributions that make up an event. This
 * version is based upon the event having a fixed number of contributions.
 *
 * @author patton
 * @version $Id: EventGatherer.java,v 1.9 2005/08/09 01:28:48 patton Exp $
 */
public class EventGatherer
        implements SplicedAnalysis
{
    // private static member data

    /**
     * Log object for this class.
     */
    private static final Log log = LogFactory.getLog(TimeStampAnalysis.class);

    /**
     * The leader used in the label of the generators.
     */
    private static final String CONTRIBUTIONS_LEADER = "Contributions : ";

    /**
     * The length of the Label's leader.
     */
    private static final int CONTRIBUTIONS_LEADER_LENGTH =
            CONTRIBUTIONS_LEADER.length();

    /**
     * The leader used in the label of the generators.
     */
    private static final String BUILT_LEADER = "Built Event number ";

    /**
     * The length of the Label's leader.
     */
    private static final int BUILT_LEADER_LENGTH = BUILT_LEADER.length();

    // private instance member data

    /**
     * The factory used to produce object for this object to use.
     */
    private final EventContributionFactory factory =
            new EventContributionFactory();

    /**
     * The EventNumberPropagator used to feed numbers into this object.
     */
    private final EventNumberPropagator propagator =
            new EventNumberPropagator();

    /**
     * The number of contributions that constitute a complete event.
     */
    private final int contributionCount;

    /**
     * Splicer that is driving this object.
     */
    private Splicer splicer;

    /**
     * The current event which is being gathered.
     */
    private Integer currentNumber;

    /**
     * The current position in the the splicedObject array.
     */
    private int cursor;

    /**
     * The event that is currently under construction.
     */
    private EventEnsemble eventUnderConstruction;

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param contributionCount the number of contributions that constitute a
     * complete event.
     */
    public EventGatherer(int contributionCount)
    {
        this.contributionCount = contributionCount;
    }

    // instance member method (alphabetic)

    /**
     * Specifies the a new event to be collected.
     *
     * @param number the a new event to be collected.
     */
    void addEvent(Integer number)
    {
        propagator.propagate(number);
    }

    public void execute(List splicedObjects,
                        int decrement)
    {
        cursor -= decrement;

        Integer number = getCurrentNumber();
        final int finished = splicedObjects.size();
        final StringBuffer contributionMessage =
                new StringBuffer(CONTRIBUTIONS_LEADER);
        final StringBuffer builtMessage =
                new StringBuffer(BUILT_LEADER);
        while (null != number &&
               finished != cursor) {
            final EventContribution contribution =
                    (EventContribution) splicedObjects.get(cursor);
            contributionMessage.delete(CONTRIBUTIONS_LEADER_LENGTH,
                                       contributionMessage.length())
                    .append(contribution.getPayload())
                    .append("\" (List size is ")
                    .append(splicedObjects.size())
                    .append(')');
            log.info(contributionMessage.toString());
            eventUnderConstruction.gather(contribution);

            if (eventUnderConstruction.isComplete()) {
                builtMessage.delete(BUILT_LEADER_LENGTH,
                                    builtMessage.length())
                        .append(number.toString());
                log.info(builtMessage.toString());
                splicer.truncate(contribution);
                currentNumber = null;
                number = getCurrentNumber();
            }

            cursor++;
        }

    }

    public SpliceableFactory getFactory()
    {
        return factory;
    }

    /**
     * Returns the {@link EventContributionFactory} that should be used to
     * create the {@link Spliceable} objects used by this object.
     *
     * @return the EventContributionFactory that creates Spliceable objects.
     */
    EventContributionFactory getEventContributionFactory()
    {
        return factory;
    }

    Integer getCurrentNumber()
    {
        if (null == currentNumber) {
            currentNumber = propagator.getNumber();
            propagator.next();
            if (null != currentNumber) {
                eventUnderConstruction = new EventEnsemble(currentNumber,
                                                           contributionCount);
            } else {
                if (propagator.isFinished()) {
                    splicer.stop();
                }
            }
        }
        return currentNumber;
    }

    /**
     * Sets the finished flag.
     *
     * @param finished the value to which the flag should be set.
     */
    public void setFinished(boolean finished)
    {
        propagator.setFinished(finished);
    }

    /**
     * Sets the Splicer that is driving this object.
     *
     * @param splicer the Splicer that is driving this object.
     */
    void setSplicer(Splicer splicer)
    {
        this.splicer = splicer;
    }

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}
