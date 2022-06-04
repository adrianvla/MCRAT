// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.future;

import java.util.concurrent.TimeUnit;
import org.apache.mina.core.session.IoSession;

public interface IoFuture
{
    IoSession getSession();
    
    IoFuture await() throws InterruptedException;
    
    boolean await(final long p0, final TimeUnit p1) throws InterruptedException;
    
    boolean await(final long p0) throws InterruptedException;
    
    IoFuture awaitUninterruptibly();
    
    boolean awaitUninterruptibly(final long p0, final TimeUnit p1);
    
    boolean awaitUninterruptibly(final long p0);
    
    @Deprecated
    void join();
    
    @Deprecated
    boolean join(final long p0);
    
    boolean isDone();
    
    IoFuture addListener(final IoFutureListener<?> p0);
    
    IoFuture removeListener(final IoFutureListener<?> p0);
}
