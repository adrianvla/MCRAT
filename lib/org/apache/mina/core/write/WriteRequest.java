// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.write;

import java.net.SocketAddress;
import org.apache.mina.core.future.WriteFuture;

public interface WriteRequest
{
    WriteRequest getOriginalRequest();
    
    WriteFuture getFuture();
    
    Object getMessage();
    
    SocketAddress getDestination();
    
    boolean isEncoded();
}
