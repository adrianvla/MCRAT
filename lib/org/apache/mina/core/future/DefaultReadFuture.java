// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.future;

import org.apache.mina.core.RuntimeIoException;
import java.io.IOException;
import org.apache.mina.core.session.IoSession;

public class DefaultReadFuture extends DefaultIoFuture implements ReadFuture
{
    private static final Object CLOSED;
    
    public DefaultReadFuture(final IoSession session) {
        super(session);
    }
    
    @Override
    public Object getMessage() {
        if (!this.isDone()) {
            return null;
        }
        final Object v = this.getValue();
        if (v == DefaultReadFuture.CLOSED) {
            return null;
        }
        if (v instanceof RuntimeException) {
            throw (RuntimeException)v;
        }
        if (v instanceof Error) {
            throw (Error)v;
        }
        if (v instanceof IOException || v instanceof Exception) {
            throw new RuntimeIoException((Throwable)v);
        }
        return v;
    }
    
    @Override
    public boolean isRead() {
        if (this.isDone()) {
            final Object v = this.getValue();
            return v != DefaultReadFuture.CLOSED && !(v instanceof Throwable);
        }
        return false;
    }
    
    @Override
    public boolean isClosed() {
        return this.isDone() && this.getValue() == DefaultReadFuture.CLOSED;
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
    public void setClosed() {
        this.setValue(DefaultReadFuture.CLOSED);
    }
    
    @Override
    public void setRead(final Object message) {
        if (message == null) {
            throw new IllegalArgumentException("message");
        }
        this.setValue(message);
    }
    
    @Override
    public void setException(final Throwable exception) {
        if (exception == null) {
            throw new IllegalArgumentException("exception");
        }
        this.setValue(exception);
    }
    
    @Override
    public ReadFuture await() throws InterruptedException {
        return (ReadFuture)super.await();
    }
    
    @Override
    public ReadFuture awaitUninterruptibly() {
        return (ReadFuture)super.awaitUninterruptibly();
    }
    
    @Override
    public ReadFuture addListener(final IoFutureListener<?> listener) {
        return (ReadFuture)super.addListener(listener);
    }
    
    @Override
    public ReadFuture removeListener(final IoFutureListener<?> listener) {
        return (ReadFuture)super.removeListener(listener);
    }
    
    static {
        CLOSED = new Object();
    }
}
