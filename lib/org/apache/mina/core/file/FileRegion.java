// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.file;

import java.nio.channels.FileChannel;

public interface FileRegion
{
    FileChannel getFileChannel();
    
    long getPosition();
    
    void update(final long p0);
    
    long getRemainingBytes();
    
    long getWrittenBytes();
    
    String getFilename();
}
