package icecube.daq.splicer;

public class TimeStamp
    implements Spliceable
{
    public long timestamp;

    public TimeStamp(long val)
    {
        timestamp = val;
    }

    @Override
    public int compareSpliceable(Spliceable s)
    {
        if (!(s instanceof TimeStamp)) return -1;

        TimeStamp ts = (TimeStamp) s;
        if (timestamp < ts.timestamp) return -1;
        if (timestamp > ts.timestamp) return +1;
        return 0;
    }

    @Override
    public String toString()
    {
        return "T" + timestamp;
    }
}
