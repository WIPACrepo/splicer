package icecube.daq.splicer;

public interface PrioritySplicerMBean
{
    int getChunkSize();
    long getNumberOfChecks();
    long getNumberOfOutputs();
    long getNumberOfProcessCalls();
    int getQueueSize();
}
