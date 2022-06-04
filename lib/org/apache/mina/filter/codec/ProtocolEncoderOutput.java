// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.codec;

import org.apache.mina.core.future.WriteFuture;

public interface ProtocolEncoderOutput
{
    void write(final Object p0);
    
    void mergeAll();
    
    WriteFuture flush();
}
