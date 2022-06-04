// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.future;

public interface WriteFuture extends IoFuture
{
    boolean isWritten();
    
    Throwable getException();
    
    void setWritten();
    
    void setException(final Throwable p0);
    
    WriteFuture await() throws InterruptedException;
    
    WriteFuture awaitUninterruptibly();
    
    WriteFuture addListener(final IoFutureListener<?> p0);
    
    WriteFuture removeListener(final IoFutureListener<?> p0);
}
