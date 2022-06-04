// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.service;

import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.core.future.ConnectFuture;
import java.net.SocketAddress;

public interface IoConnector extends IoService
{
    @Deprecated
    int getConnectTimeout();
    
    long getConnectTimeoutMillis();
    
    @Deprecated
    void setConnectTimeout(final int p0);
    
    void setConnectTimeoutMillis(final long p0);
    
    SocketAddress getDefaultRemoteAddress();
    
    void setDefaultRemoteAddress(final SocketAddress p0);
    
    SocketAddress getDefaultLocalAddress();
    
    void setDefaultLocalAddress(final SocketAddress p0);
    
    ConnectFuture connect();
    
    ConnectFuture connect(final IoSessionInitializer<? extends ConnectFuture> p0);
    
    ConnectFuture connect(final SocketAddress p0);
    
    ConnectFuture connect(final SocketAddress p0, final IoSessionInitializer<? extends ConnectFuture> p1);
    
    ConnectFuture connect(final SocketAddress p0, final SocketAddress p1);
    
    ConnectFuture connect(final SocketAddress p0, final SocketAddress p1, final IoSessionInitializer<? extends ConnectFuture> p2);
}
