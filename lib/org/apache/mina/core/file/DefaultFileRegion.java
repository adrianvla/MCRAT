// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.file;

import java.io.IOException;
import java.nio.channels.FileChannel;

public class DefaultFileRegion implements FileRegion
{
    private final FileChannel channel;
    private final long originalPosition;
    private long position;
    private long remainingBytes;
    
    public DefaultFileRegion(final FileChannel channel) throws IOException {
        this(channel, 0L, channel.size());
    }
    
    public DefaultFileRegion(final FileChannel channel, final long position, final long remainingBytes) {
        if (channel == null) {
            throw new IllegalArgumentException("channel can not be null");
        }
        if (position < 0L) {
            throw new IllegalArgumentException("position may not be less than 0");
        }
        if (remainingBytes < 0L) {
            throw new IllegalArgumentException("remainingBytes may not be less than 0");
        }
        this.channel = channel;
        this.originalPosition = position;
        this.position = position;
        this.remainingBytes = remainingBytes;
    }
    
    @Override
    public long getWrittenBytes() {
        return this.position - this.originalPosition;
    }
    
    @Override
    public long getRemainingBytes() {
        return this.remainingBytes;
    }
    
    @Override
    public FileChannel getFileChannel() {
        return this.channel;
    }
    
    @Override
    public long getPosition() {
        return this.position;
    }
    
    @Override
    public void update(final long value) {
        this.position += value;
        this.remainingBytes -= value;
    }
    
    @Override
    public String getFilename() {
        return null;
    }
}
