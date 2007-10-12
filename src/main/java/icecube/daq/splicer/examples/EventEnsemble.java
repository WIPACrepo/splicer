/*
 * class: EventEnsemble
 *
 * Version $Id: EventEnsemble.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: October 21 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer.examples;

/**
 * This class represents a ensemble of EventContributions.
 *
 * @author patton
 * @version $Id: EventEnsemble.java 2125 2007-10-12 18:27:05Z ksb $
 */
public class EventEnsemble
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    // private static member data

    // private instance member data

    /**
     * The number of the event that is being built in this object.
     */
    private final Integer number;

    /**
     * The number of contributions that constitute a complete event.
     */
    private final int contributionCount;

    /**
     * The number of contributions seen for this event.
     */
    private int contributionsSeen;

    // constructors

    /**
     * Create an instance of this class.
     *
     * @param number Number of the event that is being built in this object.
     * @param contributionCount The number of contributions that constitute a
     * complete event.
     */
    EventEnsemble(Integer number,
                  int contributionCount)
    {
        this.number = number;
        this.contributionCount = contributionCount;
    }

    // instance member method (alphabetic)

    /**
     * Gathers the specified conribution into the event.
     *
     * @param contribution the EventContribution to be gathered.
     */
    void gather(EventContribution contribution)
    {
        if (!contribution.getNumber().equals(number)) {
            throw new IllegalArgumentException("Mismatch in event numbers!" +
                                               "Gathering Event " +
                                               number.toString() +
                                               ", got contribution to Event " +
                                               contribution.getNumber()
                                               .toString());
        }
        contributionsSeen++;
        if (contributionCount < contributionsSeen) {
            throw new IllegalStateException("Too many contributions seen.");
        }
    }

    Integer getNumber()
    {
        return number;
    }

    boolean isComplete()
    {
        return contributionCount == contributionsSeen;
    }

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}
