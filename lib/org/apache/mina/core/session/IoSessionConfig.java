// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.session;

public interface IoSessionConfig
{
    int getReadBufferSize();
    
    void setReadBufferSize(final int p0);
    
    int getMinReadBufferSize();
    
    void setMinReadBufferSize(final int p0);
    
    int getMaxReadBufferSize();
    
    void setMaxReadBufferSize(final int p0);
    
    int getThroughputCalculationInterval();
    
    long getThroughputCalculationIntervalInMillis();
    
    void setThroughputCalculationInterval(final int p0);
    
    int getIdleTime(final IdleStatus p0);
    
    long getIdleTimeInMillis(final IdleStatus p0);
    
    void setIdleTime(final IdleStatus p0, final int p1);
    
    int getReaderIdleTime();
    
    long getReaderIdleTimeInMillis();
    
    void setReaderIdleTime(final int p0);
    
    int getWriterIdleTime();
    
    long getWriterIdleTimeInMillis();
    
    void setWriterIdleTime(final int p0);
    
    int getBothIdleTime();
    
    long getBothIdleTimeInMillis();
    
    void setBothIdleTime(final int p0);
    
    int getWriteTimeout();
    
    long getWriteTimeoutInMillis();
    
    void setWriteTimeout(final int p0);
    
    boolean isUseReadOperation();
    
    void setUseReadOperation(final boolean p0);
    
    void setAll(final IoSessionConfig p0);
}
