// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.future;

import org.apache.mina.core.session.IoSession;

public interface ConnectFuture extends IoFuture
{
    IoSession getSession();
    
    Throwable getException();
    
    boolean isConnected();
    
    boolean isCanceled();
    
    void setSession(final IoSession p0);
    
    void setException(final Throwable p0);
    
    boolean cancel();
    
    ConnectFuture await() throws InterruptedException;
    
    ConnectFuture awaitUninterruptibly();
    
    ConnectFuture addListener(final IoFutureListener<?> p0);
    
    ConnectFuture removeListener(final IoFutureListener<?> p0);
}
