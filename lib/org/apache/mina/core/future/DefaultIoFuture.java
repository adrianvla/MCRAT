// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.future;

import org.apache.mina.util.ExceptionMonitor;
import java.util.Iterator;
import java.util.ArrayList;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.polling.AbstractPollingIoProcessor;
import java.util.concurrent.TimeUnit;
import java.util.List;
import org.apache.mina.core.session.IoSession;

public class DefaultIoFuture implements IoFuture
{
    private static final long DEAD_LOCK_CHECK_INTERVAL = 5000L;
    private final IoSession session;
    private final Object lock;
    private IoFutureListener<?> firstListener;
    private List<IoFutureListener<?>> otherListeners;
    private Object result;
    private boolean ready;
    private int waiters;
    
    public DefaultIoFuture(final IoSession session) {
        this.session = session;
        this.lock = this;
    }
    
    @Override
    public IoSession getSession() {
        return this.session;
    }
    
    @Deprecated
    @Override
    public void join() {
        this.awaitUninterruptibly();
    }
    
    @Deprecated
    @Override
    public boolean join(final long timeoutMillis) {
        return this.awaitUninterruptibly(timeoutMillis);
    }
    
    @Override
    public IoFuture await() throws InterruptedException {
        synchronized (this.lock) {
            while (!this.ready) {
                ++this.waiters;
                try {
                    this.lock.wait(5000L);
                }
                finally {
                    --this.waiters;
                    if (!this.ready) {
                        this.checkDeadLock();
                    }
                }
            }
        }
        return this;
    }
    
    @Override
    public boolean await(final long timeout, final TimeUnit unit) throws InterruptedException {
        return this.await0(unit.toMillis(timeout), true);
    }
    
    @Override
    public boolean await(final long timeoutMillis) throws InterruptedException {
        return this.await0(timeoutMillis, true);
    }
    
    @Override
    public IoFuture awaitUninterruptibly() {
        try {
            this.await0(Long.MAX_VALUE, false);
        }
        catch (InterruptedException ex) {}
        return this;
    }
    
    @Override
    public boolean awaitUninterruptibly(final long timeout, final TimeUnit unit) {
        try {
            return this.await0(unit.toMillis(timeout), false);
        }
        catch (InterruptedException e) {
            throw new InternalError();
        }
    }
    
    @Override
    public boolean awaitUninterruptibly(final long timeoutMillis) {
        try {
            return this.await0(timeoutMillis, false);
        }
        catch (InterruptedException e) {
            throw new InternalError();
        }
    }
    
    private boolean await0(final long timeoutMillis, final boolean interruptable) throws InterruptedException {
        long endTime = System.currentTimeMillis() + timeoutMillis;
        if (endTime < 0L) {
            endTime = Long.MAX_VALUE;
        }
        synchronized (this.lock) {
            if (this.ready || timeoutMillis <= 0L) {
                return this.ready;
            }
            ++this.waiters;
            try {
                while (true) {
                    try {
                        final long timeOut = Math.min(timeoutMillis, 5000L);
                        this.lock.wait(timeOut);
                    }
                    catch (InterruptedException e) {
                        if (interruptable) {
                            throw e;
                        }
                    }
                    if (this.ready || endTime < System.currentTimeMillis()) {
                        break;
                    }
                    this.checkDeadLock();
                }
                return this.ready;
            }
            finally {
                --this.waiters;
                if (!this.ready) {
                    this.checkDeadLock();
                }
            }
        }
    }
    
    private void checkDeadLock() {
        if (!(this instanceof CloseFuture) && !(this instanceof WriteFuture) && !(this instanceof ReadFuture) && !(this instanceof ConnectFuture)) {
            return;
        }
        final StackTraceElement[] stackTrace2;
        final StackTraceElement[] stackTrace = stackTrace2 = Thread.currentThread().getStackTrace();
        for (final StackTraceElement stackElement : stackTrace2) {
            if (AbstractPollingIoProcessor.class.getName().equals(stackElement.getClassName())) {
                final IllegalStateException e = new IllegalStateException("t");
                e.getStackTrace();
                throw new IllegalStateException("DEAD LOCK: " + IoFuture.class.getSimpleName() + ".await() was invoked from an I/O processor thread.  " + "Please use " + IoFutureListener.class.getSimpleName() + " or configure a proper thread model alternatively.");
            }
        }
        for (final StackTraceElement s : stackTrace) {
            try {
                final Class<?> cls = DefaultIoFuture.class.getClassLoader().loadClass(s.getClassName());
                if (IoProcessor.class.isAssignableFrom(cls)) {
                    throw new IllegalStateException("DEAD LOCK: " + IoFuture.class.getSimpleName() + ".await() was invoked from an I/O processor thread.  " + "Please use " + IoFutureListener.class.getSimpleName() + " or configure a proper thread model alternatively.");
                }
            }
            catch (ClassNotFoundException ex) {}
        }
    }
    
    @Override
    public boolean isDone() {
        synchronized (this.lock) {
            return this.ready;
        }
    }
    
    public boolean setValue(final Object newValue) {
        synchronized (this.lock) {
            if (this.ready) {
                return false;
            }
            this.result = newValue;
            this.ready = true;
            if (this.waiters > 0) {
                this.lock.notifyAll();
            }
        }
        this.notifyListeners();
        return true;
    }
    
    protected Object getValue() {
        synchronized (this.lock) {
            return this.result;
        }
    }
    
    @Override
    public IoFuture addListener(final IoFutureListener<?> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener");
        }
        synchronized (this.lock) {
            if (this.ready) {
                this.notifyListener(listener);
            }
            else if (this.firstListener == null) {
                this.firstListener = listener;
            }
            else {
                if (this.otherListeners == null) {
                    this.otherListeners = new ArrayList<IoFutureListener<?>>(1);
                }
                this.otherListeners.add(listener);
            }
        }
        return this;
    }
    
    @Override
    public IoFuture removeListener(final IoFutureListener<?> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener");
        }
        synchronized (this.lock) {
            if (!this.ready) {
                if (listener == this.firstListener) {
                    if (this.otherListeners != null && !this.otherListeners.isEmpty()) {
                        this.firstListener = this.otherListeners.remove(0);
                    }
                    else {
                        this.firstListener = null;
                    }
                }
                else if (this.otherListeners != null) {
                    this.otherListeners.remove(listener);
                }
            }
        }
        return this;
    }
    
    private void notifyListeners() {
        if (this.firstListener != null) {
            this.notifyListener(this.firstListener);
            this.firstListener = null;
            if (this.otherListeners != null) {
                for (final IoFutureListener<?> listener : this.otherListeners) {
                    this.notifyListener(listener);
                }
                this.otherListeners = null;
            }
        }
    }
    
    private void notifyListener(final IoFutureListener listener) {
        try {
            listener.operationComplete(this);
        }
        catch (Exception e) {
            ExceptionMonitor.getInstance().exceptionCaught(e);
        }
    }
}
