/*
 * interface: Spliceable
 *
 * Version $Id: Spliceable.java 2205 2007-10-29 20:44:05Z dglo $
 *
 * Date: September 12 2003
 *
 * (c) 2003 IceCube Collaboration
 */

package icecube.daq.splicer;

/**
 * This interface marks an object as being able to be used by a {@link
 * Splicer}.
 *
 * @author patton
 * @version $Id: Spliceable.java 2205 2007-10-29 20:44:05Z dglo $
 */
public interface Spliceable
{
    int compareSpliceable(Spliceable spl);
}
