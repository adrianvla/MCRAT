// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.buffer;

import java.nio.ByteBuffer;

public interface IoBufferAllocator
{
    IoBuffer allocate(final int p0, final boolean p1);
    
    ByteBuffer allocateNioBuffer(final int p0, final boolean p1);
    
    IoBuffer wrap(final ByteBuffer p0);
    
    void dispose();
}
