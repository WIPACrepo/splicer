/*
 * class: TimeStampAnalysis
 *
 * Version $Id: TimeStampAnalysis.java,v 1.18 2005/10/18 15:27:47 patton Exp $
 *
 * Date: September 19 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer.examples;

import icecube.daq.splicer.SpliceableFactory;
import icecube.daq.splicer.SplicedAnalysis;
import icecube.daq.splicer.Splicer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * This class is a simple analysis to be used in conjunction with a {@link
 * Splicer Splicer} object.
 *
 * @author patton
 * @version $Id: TimeStampAnalysis.java,v 1.14 2004/08/09 21:06:05 patton Exp
 *          $
 */
public class TimeStampAnalysis
        implements SplicedAnalysis
{
    // private static member data

    /**
     * Log object for this class.
     */
    private static final Log log = LogFactory.getLog(TimeStampAnalysis.class);

    /**
     * The default length of a StringBuffer in which messages are built.
     */
    private static final int DEFAULT_MESSAGE_LENGTH = 79;

    // private instance member data

    /**
     * The factory used to produce object for this object to use.
     */
    private final TimeStampFactory factory = new TimeStampFactory();

    /**
     * Size of the List which will cause it to be cut in half.
     */
    private int cutoffSize;

    /**
     * Splicer that is driving this object.
     */
    private Splicer splicer;

    /**
     * The index at which to start output.
     */
    private int start;

    /**
     * The total number of Objects analyzed.
     */
    private int count;

    /**
     * Count at which the Splicer is stopped.
     */
    private int stopCount;

    private int lastInputListSize;
    // constructors

    /**
     * Create an instance of this class.
     *
     * @param cutoffSize the size of the List which cause it to be cut.
     */
    public TimeStampAnalysis(int cutoffSize)
    {
        this(cutoffSize,
             0);
    }


    /**
     * Create an instance of this class.
     *
     * @param cutoffSize the size of the List which cause it to be cut.
     * @param stopCount the count at which the Splicer is stopped, zero if the
     * splicer is not to be stopped by this object.
     */
    public TimeStampAnalysis(int cutoffSize,
                             int stopCount)
    {
        this.cutoffSize = cutoffSize;
        this.stopCount = stopCount;
    }


    // instance member method (alphabetic)

    public SpliceableFactory getFactory()
    {
        return factory;
    }

    public synchronized void execute(List splicedObjects,
                                     int decrement)
    {
        setLastInputListSize(splicedObjects.size());

        final int finished = splicedObjects.size();
        final StringBuffer message = new StringBuffer(DEFAULT_MESSAGE_LENGTH);
        for (int index = start - decrement;
             finished != index;
             index++) {
            final TimeStamp timeStamp = (TimeStamp) splicedObjects.get(index);
            message.delete(0,
                           message.length())
                    .append(timeStamp.getTime().toString())
                    .append(" \"")
                    .append(timeStamp.getPayload())
                    .append("\" (List size is ")
                    .append(splicedObjects.size())
                    .append(')');
            log.info(message.toString());
            count++;
        }
        start = finished;

        // Example bookkeeping
        final int currentCutoff = getCutoffSize();
        if (0 != currentCutoff &&
            currentCutoff <= splicedObjects.size()) {
            splicer.truncate((TimeStamp) splicedObjects.get(splicedObjects.size() /
                                                            2));
        }

        final int currentStopCount = getStopCount();
        if (0 != currentStopCount &&
            currentStopCount <= count) {
            splicer.stop();
            count = 0;
        }

    }

    /**
     * Returns the total number of Objects analyzed.
     *
     * @return the total number of Objects analyzed.
     */
    public synchronized int getCount()
    {
        return count;
    }

    /**
     * Returns the current value of cutoffSize.
     *
     * @return the current value of cutoffSize.
     */
    public synchronized int getCutoffSize()
    {
        return cutoffSize;
    }

    /**
     * Returns length of the input List on the most recent execute() call.
     *
     * @return length of the input List on the most recent execute() call.
     */
    public synchronized int getLastInputListSize()
    {
        return lastInputListSize;
    }

    /**
     * Returns the current value of stopCount.
     *
     * @return the current value of stopCount.
     */
    public synchronized int getStopCount()
    {
        return stopCount;
    }

    /**
     * Sets the cutoffSize to the spcified value.
     *
     * @param cutoffSize the value to which cutoffSize should be set.
     */
    public synchronized void setCutoffSize(int cutoffSize)
    {
        this.cutoffSize = cutoffSize;
    }

    /**
     * Sets length of the input List on the most recent execute() call.
     *
     * @param length the length of the input List.
     */
    private synchronized void setLastInputListSize(int length)
    {
        lastInputListSize = length;
    }

    /**
     * Sets the stopCount to the spcified value.
     *
     * @param stopCount the value to which stopCount should be set.
     */
    public synchronized void setStopCount(int stopCount)
    {
        this.stopCount = stopCount;
    }

    /**
     * Sets the Splicer that is driving this object.
     *
     * @param splicer the Splicer that is driving this object.
     */
    public synchronized void setSplicer(Splicer splicer)
    {
        this.splicer = splicer;
    }

}
