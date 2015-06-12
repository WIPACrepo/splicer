package icecube.daq.priority;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * Monitor a single Sorter, adjusting the chunk size as needed to keep
 * it from running too frequently or not frequently enough
 */
class SorterMonitor
{
    // if we're running more than 20 times per interval
    private static final int MAX_CHECK_FREQUENCY = 20;

    private Sorter sorter;

    private int maxChunkSize = Integer.MIN_VALUE;

    private long prevChecked;

    SorterMonitor(Sorter sorter)
    {
        this.sorter = sorter;
    }

    /**
     * Are we already monitoring this sorter?
     *
     * @return <tt>true</tt> if this sorter is being monitored
     */
    boolean contains(Sorter s)
    {
        return sorter.equals(s);
    }

    /**
     * Is this sorter still running?
     *
     * @return <tt>false</tt> if the sorter has stopped
     */
    boolean isRunning()
    {
        return sorter.isRunning();
    }

    /**
     * Monitor this sorter
     */
    void monitor()
    {
        // chunk size cannot exceed 30 times original size
        if (maxChunkSize == Integer.MIN_VALUE) {
            maxChunkSize = sorter.getChunkSize() * 30;
        }

        final long checked = setChecked(sorter.getNumChecked());
        if (checked == 0) {
            // we haven't checked the queue...

            // if there's stuff queued...
            final int numQueued = sorter.getNumQueued();
            if (numQueued > 0) {
                // scale down the sorter's chunk size
                final int chunkSize = sorter.getChunkSize();
                final int newChunk =
                    Math.min(chunkSize - ((chunkSize - numQueued) / 10),
                             maxChunkSize);
                if (newChunk != chunkSize) {
                    sorter.setChunkSize(newChunk);
                }

                // force a check
                sorter.check(false);
                setChecked(sorter.getNumChecked());
            }
        } else if (checked > MAX_CHECK_FREQUENCY) {
            // we've checked the queue too frequently...

            final int chunkSize = sorter.getChunkSize();
            final int newChunk = Math.min(chunkSize + (chunkSize / 10),
                                          maxChunkSize);
            if (newChunk != chunkSize) {
                sorter.setChunkSize(newChunk);
            }
        }
    }

    /**
     * Return the difference between the previous number of checks and
     * the current number
     *
     * @param val current number of checks
     *
     * @return difference
     */
    long setChecked(long val)
    {
        final long diff = val - prevChecked;
        prevChecked = val;
        return diff;
    }
}

/**
 * Adjust Sorter chunk sizes to keep them from running too frequently
 * or not frequently enough
 */
public class AdjustmentTask
    extends TimerTask
{
    private static final Logger LOG = Logger.getLogger(AdjustmentTask.class);

    private static final int FREQUENCY = 5000;

    private List<SorterMonitor> sorters = new ArrayList<SorterMonitor>();

    private Timer timer;

    /**
     * Add a sorter.
     *
     * @param s sorter
     */
    public void register(Sorter s)
    {
        for (SorterMonitor sm : sorters) {
            if (sm.contains(s)) {
                LOG.error("Ignoring reregistered Sorter " + s);
                return;
            }
        }

        sorters.add(new SorterMonitor(s));
    }

    /**
     * Run the thread
     */
    public void run()
    {
        Iterator<SorterMonitor> iter = sorters.iterator();
        while (iter.hasNext()) {
            SorterMonitor sm = iter.next();

            if (!sm.isRunning()) {
                iter.remove();
            } else {
                sm.monitor();
            }
        }

        if (sorters.size() == 0) {
            stop();
        }
    }

    /**
     * Start the thread
     */
    public void start()
    {
        timer = new Timer("AdjustmentTask");
        timer.scheduleAtFixedRate(this, FREQUENCY, FREQUENCY);
    }

    /**
     * Stop the thread
     */
    public void stop()
    {
        timer.cancel();
    }
}
