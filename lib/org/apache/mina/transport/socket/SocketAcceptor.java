// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.transport.socket;

import java.net.InetSocketAddress;
import org.apache.mina.core.service.IoAcceptor;

public interface SocketAcceptor extends IoAcceptor
{
    InetSocketAddress getLocalAddress();
    
    InetSocketAddress getDefaultLocalAddress();
    
    void setDefaultLocalAddress(final InetSocketAddress p0);
    
    boolean isReuseAddress();
    
    void setReuseAddress(final boolean p0);
    
    int getBacklog();
    
    void setBacklog(final int p0);
    
    SocketSessionConfig getSessionConfig();
}
