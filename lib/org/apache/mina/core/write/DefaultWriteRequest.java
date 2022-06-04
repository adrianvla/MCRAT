// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.write;

import org.apache.mina.core.future.IoFuture;
import java.util.concurrent.TimeUnit;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;
import java.net.SocketAddress;
import org.apache.mina.core.future.WriteFuture;

public class DefaultWriteRequest implements WriteRequest
{
    public static final byte[] EMPTY_MESSAGE;
    private static final WriteFuture UNUSED_FUTURE;
    private final Object message;
    private final WriteFuture future;
    private final SocketAddress destination;
    
    public DefaultWriteRequest(final Object message) {
        this(message, null, null);
    }
    
    public DefaultWriteRequest(final Object message, final WriteFuture future) {
        this(message, future, null);
    }
    
    public DefaultWriteRequest(final Object message, WriteFuture future, final SocketAddress destination) {
        if (message == null) {
            throw new IllegalArgumentException("message");
        }
        if (future == null) {
            future = DefaultWriteRequest.UNUSED_FUTURE;
        }
        this.message = message;
        this.future = future;
        this.destination = destination;
    }
    
    @Override
    public WriteFuture getFuture() {
        return this.future;
    }
    
    @Override
    public Object getMessage() {
        return this.message;
    }
    
    @Override
    public WriteRequest getOriginalRequest() {
        return this;
    }
    
    @Override
    public SocketAddress getDestination() {
        return this.destination;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("WriteRequest: ");
        if (this.message.getClass().getName().equals(Object.class.getName())) {
            sb.append("CLOSE_REQUEST");
        }
        else if (this.getDestination() == null) {
            sb.append(this.message);
        }
        else {
            sb.append(this.message);
            sb.append(" => ");
            sb.append(this.getDestination());
        }
        return sb.toString();
    }
    
    @Override
    public boolean isEncoded() {
        return false;
    }
    
    static {
        EMPTY_MESSAGE = new byte[0];
        UNUSED_FUTURE = new WriteFuture() {
            @Override
            public boolean isWritten() {
                return false;
            }
            
            @Override
            public void setWritten() {
            }
            
            @Override
            public IoSession getSession() {
                return null;
            }
            
            @Override
            public void join() {
            }
            
            @Override
            public boolean join(final long timeoutInMillis) {
                return true;
            }
            
            @Override
            public boolean isDone() {
                return true;
            }
            
            @Override
            public WriteFuture addListener(final IoFutureListener<?> listener) {
                throw new IllegalStateException("You can't add a listener to a dummy future.");
            }
            
            @Override
            public WriteFuture removeListener(final IoFutureListener<?> listener) {
                throw new IllegalStateException("You can't add a listener to a dummy future.");
            }
            
            @Override
            public WriteFuture await() throws InterruptedException {
                return this;
            }
            
            @Override
            public boolean await(final long timeout, final TimeUnit unit) throws InterruptedException {
                return true;
            }
            
            @Override
            public boolean await(final long timeoutMillis) throws InterruptedException {
                return true;
            }
            
            @Override
            public WriteFuture awaitUninterruptibly() {
                return this;
            }
            
            @Override
            public boolean awaitUninterruptibly(final long timeout, final TimeUnit unit) {
                return true;
            }
            
            @Override
            public boolean awaitUninterruptibly(final long timeoutMillis) {
                return true;
            }
            
            @Override
            public Throwable getException() {
                return null;
            }
            
            @Override
            public void setException(final Throwable cause) {
            }
        };
    }
}
