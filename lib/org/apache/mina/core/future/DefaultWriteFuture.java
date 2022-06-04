// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.future;

import org.apache.mina.core.session.IoSession;

public class DefaultWriteFuture extends DefaultIoFuture implements WriteFuture
{
    public static WriteFuture newWrittenFuture(final IoSession session) {
        final DefaultWriteFuture writtenFuture = new DefaultWriteFuture(session);
        writtenFuture.setWritten();
        return writtenFuture;
    }
    
    public static WriteFuture newNotWrittenFuture(final IoSession session, final Throwable cause) {
        final DefaultWriteFuture unwrittenFuture = new DefaultWriteFuture(session);
        unwrittenFuture.setException(cause);
        return unwrittenFuture;
    }
    
    public DefaultWriteFuture(final IoSession session) {
        super(session);
    }
    
    @Override
    public boolean isWritten() {
        if (this.isDone()) {
            final Object v = this.getValue();
            if (v instanceof Boolean) {
                return (boolean)v;
            }
        }
        return false;
    }
    
    @Override
    public Throwable getException() {
        if (this.isDone()) {
            final Object v = this.getValue();
            if (v instanceof Throwable) {
                return (Throwable)v;
            }
        }
        return null;
    }
    
    @Override
    public void setWritten() {
        this.setValue(Boolean.TRUE);
    }
    
    @Override
    public void setException(final Throwable exception) {
        if (exception == null) {
            throw new IllegalArgumentException("exception");
        }
        this.setValue(exception);
    }
    
    @Override
    public WriteFuture await() throws InterruptedException {
        return (WriteFuture)super.await();
    }
    
    @Override
    public WriteFuture awaitUninterruptibly() {
        return (WriteFuture)super.awaitUninterruptibly();
    }
    
    @Override
    public WriteFuture addListener(final IoFutureListener<?> listener) {
        return (WriteFuture)super.addListener(listener);
    }
    
    @Override
    public WriteFuture removeListener(final IoFutureListener<?> listener) {
        return (WriteFuture)super.removeListener(listener);
    }
}
