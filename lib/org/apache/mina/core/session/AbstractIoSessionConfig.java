// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.session;

public abstract class AbstractIoSessionConfig implements IoSessionConfig
{
    private int minReadBufferSize;
    private int readBufferSize;
    private int maxReadBufferSize;
    private int idleTimeForRead;
    private int idleTimeForWrite;
    private int idleTimeForBoth;
    private int writeTimeout;
    private boolean useReadOperation;
    private int throughputCalculationInterval;
    
    protected AbstractIoSessionConfig() {
        this.minReadBufferSize = 64;
        this.readBufferSize = 2048;
        this.maxReadBufferSize = 65536;
        this.writeTimeout = 60;
        this.throughputCalculationInterval = 3;
    }
    
    @Override
    public void setAll(final IoSessionConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config");
        }
        this.setReadBufferSize(config.getReadBufferSize());
        this.setMinReadBufferSize(config.getMinReadBufferSize());
        this.setMaxReadBufferSize(config.getMaxReadBufferSize());
        this.setIdleTime(IdleStatus.BOTH_IDLE, config.getIdleTime(IdleStatus.BOTH_IDLE));
        this.setIdleTime(IdleStatus.READER_IDLE, config.getIdleTime(IdleStatus.READER_IDLE));
        this.setIdleTime(IdleStatus.WRITER_IDLE, config.getIdleTime(IdleStatus.WRITER_IDLE));
        this.setWriteTimeout(config.getWriteTimeout());
        this.setUseReadOperation(config.isUseReadOperation());
        this.setThroughputCalculationInterval(config.getThroughputCalculationInterval());
    }
    
    @Override
    public int getReadBufferSize() {
        return this.readBufferSize;
    }
    
    @Override
    public void setReadBufferSize(final int readBufferSize) {
        if (readBufferSize <= 0) {
            throw new IllegalArgumentException("readBufferSize: " + readBufferSize + " (expected: 1+)");
        }
        this.readBufferSize = readBufferSize;
    }
    
    @Override
    public int getMinReadBufferSize() {
        return this.minReadBufferSize;
    }
    
    @Override
    public void setMinReadBufferSize(final int minReadBufferSize) {
        if (minReadBufferSize <= 0) {
            throw new IllegalArgumentException("minReadBufferSize: " + minReadBufferSize + " (expected: 1+)");
        }
        if (minReadBufferSize > this.maxReadBufferSize) {
            throw new IllegalArgumentException("minReadBufferSize: " + minReadBufferSize + " (expected: smaller than " + this.maxReadBufferSize + ')');
        }
        this.minReadBufferSize = minReadBufferSize;
    }
    
    @Override
    public int getMaxReadBufferSize() {
        return this.maxReadBufferSize;
    }
    
    @Override
    public void setMaxReadBufferSize(final int maxReadBufferSize) {
        if (maxReadBufferSize <= 0) {
            throw new IllegalArgumentException("maxReadBufferSize: " + maxReadBufferSize + " (expected: 1+)");
        }
        if (maxReadBufferSize < this.minReadBufferSize) {
            throw new IllegalArgumentException("maxReadBufferSize: " + maxReadBufferSize + " (expected: greater than " + this.minReadBufferSize + ')');
        }
        this.maxReadBufferSize = maxReadBufferSize;
    }
    
    @Override
    public int getIdleTime(final IdleStatus status) {
        if (status == IdleStatus.BOTH_IDLE) {
            return this.idleTimeForBoth;
        }
        if (status == IdleStatus.READER_IDLE) {
            return this.idleTimeForRead;
        }
        if (status == IdleStatus.WRITER_IDLE) {
            return this.idleTimeForWrite;
        }
        throw new IllegalArgumentException("Unknown idle status: " + status);
    }
    
    @Override
    public long getIdleTimeInMillis(final IdleStatus status) {
        return this.getIdleTime(status) * 1000L;
    }
    
    @Override
    public void setIdleTime(final IdleStatus status, final int idleTime) {
        if (idleTime < 0) {
            throw new IllegalArgumentException("Illegal idle time: " + idleTime);
        }
        if (status == IdleStatus.BOTH_IDLE) {
            this.idleTimeForBoth = idleTime;
        }
        else if (status == IdleStatus.READER_IDLE) {
            this.idleTimeForRead = idleTime;
        }
        else {
            if (status != IdleStatus.WRITER_IDLE) {
                throw new IllegalArgumentException("Unknown idle status: " + status);
            }
            this.idleTimeForWrite = idleTime;
        }
    }
    
    @Override
    public final int getBothIdleTime() {
        return this.getIdleTime(IdleStatus.BOTH_IDLE);
    }
    
    @Override
    public final long getBothIdleTimeInMillis() {
        return this.getIdleTimeInMillis(IdleStatus.BOTH_IDLE);
    }
    
    @Override
    public final int getReaderIdleTime() {
        return this.getIdleTime(IdleStatus.READER_IDLE);
    }
    
    @Override
    public final long getReaderIdleTimeInMillis() {
        return this.getIdleTimeInMillis(IdleStatus.READER_IDLE);
    }
    
    @Override
    public final int getWriterIdleTime() {
        return this.getIdleTime(IdleStatus.WRITER_IDLE);
    }
    
    @Override
    public final long getWriterIdleTimeInMillis() {
        return this.getIdleTimeInMillis(IdleStatus.WRITER_IDLE);
    }
    
    @Override
    public void setBothIdleTime(final int idleTime) {
        this.setIdleTime(IdleStatus.BOTH_IDLE, idleTime);
    }
    
    @Override
    public void setReaderIdleTime(final int idleTime) {
        this.setIdleTime(IdleStatus.READER_IDLE, idleTime);
    }
    
    @Override
    public void setWriterIdleTime(final int idleTime) {
        this.setIdleTime(IdleStatus.WRITER_IDLE, idleTime);
    }
    
    @Override
    public int getWriteTimeout() {
        return this.writeTimeout;
    }
    
    @Override
    public long getWriteTimeoutInMillis() {
        return this.writeTimeout * 1000L;
    }
    
    @Override
    public void setWriteTimeout(final int writeTimeout) {
        if (writeTimeout < 0) {
            throw new IllegalArgumentException("Illegal write timeout: " + writeTimeout);
        }
        this.writeTimeout = writeTimeout;
    }
    
    @Override
    public boolean isUseReadOperation() {
        return this.useReadOperation;
    }
    
    @Override
    public void setUseReadOperation(final boolean useReadOperation) {
        this.useReadOperation = useReadOperation;
    }
    
    @Override
    public int getThroughputCalculationInterval() {
        return this.throughputCalculationInterval;
    }
    
    @Override
    public void setThroughputCalculationInterval(final int throughputCalculationInterval) {
        if (throughputCalculationInterval < 0) {
            throw new IllegalArgumentException("throughputCalculationInterval: " + throughputCalculationInterval);
        }
        this.throughputCalculationInterval = throughputCalculationInterval;
    }
    
    @Override
    public long getThroughputCalculationIntervalInMillis() {
        return this.throughputCalculationInterval * 1000L;
    }
}
