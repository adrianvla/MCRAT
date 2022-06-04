// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.layout;

import java.nio.ByteBuffer;

public interface ByteBufferDestination
{
    ByteBuffer getByteBuffer();
    
    ByteBuffer drain(final ByteBuffer p0);
    
    void writeBytes(final ByteBuffer p0);
    
    void writeBytes(final byte[] p0, final int p1, final int p2);
}
