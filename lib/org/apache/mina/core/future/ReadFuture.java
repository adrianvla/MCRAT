// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.future;

public interface ReadFuture extends IoFuture
{
    Object getMessage();
    
    boolean isRead();
    
    boolean isClosed();
    
    Throwable getException();
    
    void setRead(final Object p0);
    
    void setClosed();
    
    void setException(final Throwable p0);
    
    ReadFuture await() throws InterruptedException;
    
    ReadFuture awaitUninterruptibly();
    
    ReadFuture addListener(final IoFutureListener<?> p0);
    
    ReadFuture removeListener(final IoFutureListener<?> p0);
}
