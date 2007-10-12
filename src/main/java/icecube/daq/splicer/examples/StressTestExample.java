/*
 * class: TimeStampExample
 *
 * Version $Id: StressTestExample.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: September 19 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer.examples;

import icecube.daq.splicer.Splicer;

import java.io.IOException;

/**
 * This class run an example of how a {@link Splicer Splicer} object can be
 * used.
 *
 * @author patton
 * @version $Id: StressTestExample.java 2125 2007-10-12 18:27:05Z ksb $
 */
public final class StressTestExample
        extends TimeStampExample
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    /**
     * The number of generators to create.
     */
    private static final int GENERATOR_COUNT = 80;

    /**
     * The average time, in milliseconds, between generator creations.
     */
    private static final int GENERATOR_PAUSE = 20;

    /**
     * Size of the List which will cause it to be cut in half.
     */
    private static final int CUTOFF_SIZE = 6400;

    /**
     * Count at which the Splicer is stopped.
     */
    private static final int STOP_COUNT = 1000000;

    // private static member data

    // constructors

    /**
     * Create an instance of this class.
     *
     * @throws IOException is there is a problem with the channels.
     */
    protected StressTestExample(int cutoffSize,
                                int stopCount,
                                int generatorCount,
                                int generatorPause)
            throws IOException
    {
        super(cutoffSize,
              stopCount,
              generatorCount,
              generatorPause);
    }

    // private instance member data

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
        new StressTestExample(CUTOFF_SIZE,
                              STOP_COUNT,
                              GENERATOR_COUNT,
                              GENERATOR_PAUSE);
    }
}
