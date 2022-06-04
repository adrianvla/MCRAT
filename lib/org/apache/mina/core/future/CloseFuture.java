// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.future;

public interface CloseFuture extends IoFuture
{
    boolean isClosed();
    
    void setClosed();
    
    CloseFuture await() throws InterruptedException;
    
    CloseFuture awaitUninterruptibly();
    
    CloseFuture addListener(final IoFutureListener<?> p0);
    
    CloseFuture removeListener(final IoFutureListener<?> p0);
}
