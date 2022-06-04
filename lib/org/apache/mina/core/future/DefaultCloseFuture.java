// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.future;

import org.apache.mina.core.session.IoSession;

public class DefaultCloseFuture extends DefaultIoFuture implements CloseFuture
{
    public DefaultCloseFuture(final IoSession session) {
        super(session);
    }
    
    @Override
    public boolean isClosed() {
        return this.isDone() && (boolean)this.getValue();
    }
    
    @Override
    public void setClosed() {
        this.setValue(Boolean.TRUE);
    }
    
    @Override
    public CloseFuture await() throws InterruptedException {
        return (CloseFuture)super.await();
    }
    
    @Override
    public CloseFuture awaitUninterruptibly() {
        return (CloseFuture)super.awaitUninterruptibly();
    }
    
    @Override
    public CloseFuture addListener(final IoFutureListener<?> listener) {
        return (CloseFuture)super.addListener(listener);
    }
    
    @Override
    public CloseFuture removeListener(final IoFutureListener<?> listener) {
        return (CloseFuture)super.removeListener(listener);
    }
}
