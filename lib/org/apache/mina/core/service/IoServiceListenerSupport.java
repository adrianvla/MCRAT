// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.service;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.filterchain.IoFilterChain;
import java.util.Iterator;
import org.apache.mina.util.ExceptionMonitor;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;
import org.apache.mina.core.session.IoSession;
import java.util.concurrent.ConcurrentMap;
import java.util.List;

public class IoServiceListenerSupport
{
    private final IoService service;
    private final List<IoServiceListener> listeners;
    private final ConcurrentMap<Long, IoSession> managedSessions;
    private final Map<Long, IoSession> readOnlyManagedSessions;
    private final AtomicBoolean activated;
    private volatile long activationTime;
    private volatile int largestManagedSessionCount;
    private AtomicLong cumulativeManagedSessionCount;
    
    public IoServiceListenerSupport(final IoService service) {
        this.listeners = new CopyOnWriteArrayList<IoServiceListener>();
        this.managedSessions = new ConcurrentHashMap<Long, IoSession>();
        this.readOnlyManagedSessions = Collections.unmodifiableMap((Map<? extends Long, ? extends IoSession>)this.managedSessions);
        this.activated = new AtomicBoolean();
        this.largestManagedSessionCount = 0;
        this.cumulativeManagedSessionCount = new AtomicLong(0L);
        if (service == null) {
            throw new IllegalArgumentException("service");
        }
        this.service = service;
    }
    
    public void add(final IoServiceListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }
    
    public void remove(final IoServiceListener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }
    
    public long getActivationTime() {
        return this.activationTime;
    }
    
    public Map<Long, IoSession> getManagedSessions() {
        return this.readOnlyManagedSessions;
    }
    
    public int getManagedSessionCount() {
        return this.managedSessions.size();
    }
    
    public int getLargestManagedSessionCount() {
        return this.largestManagedSessionCount;
    }
    
    public long getCumulativeManagedSessionCount() {
        return this.cumulativeManagedSessionCount.get();
    }
    
    public boolean isActive() {
        return this.activated.get();
    }
    
    public void fireServiceActivated() {
        if (!this.activated.compareAndSet(false, true)) {
            return;
        }
        this.activationTime = System.currentTimeMillis();
        for (final IoServiceListener listener : this.listeners) {
            try {
                listener.serviceActivated(this.service);
            }
            catch (Exception e) {
                ExceptionMonitor.getInstance().exceptionCaught(e);
            }
        }
    }
    
    public void fireServiceDeactivated() {
        if (!this.activated.compareAndSet(true, false)) {
            return;
        }
        try {
            for (final IoServiceListener listener : this.listeners) {
                try {
                    listener.serviceDeactivated(this.service);
                }
                catch (Exception e) {
                    ExceptionMonitor.getInstance().exceptionCaught(e);
                }
            }
        }
        finally {
            this.disconnectSessions();
        }
    }
    
    public void fireSessionCreated(final IoSession session) {
        boolean firstSession = false;
        if (session.getService() instanceof IoConnector) {
            synchronized (this.managedSessions) {
                firstSession = this.managedSessions.isEmpty();
            }
        }
        if (this.managedSessions.putIfAbsent(session.getId(), session) != null) {
            return;
        }
        if (firstSession) {
            this.fireServiceActivated();
        }
        final IoFilterChain filterChain = session.getFilterChain();
        filterChain.fireSessionCreated();
        filterChain.fireSessionOpened();
        final int managedSessionCount = this.managedSessions.size();
        if (managedSessionCount > this.largestManagedSessionCount) {
            this.largestManagedSessionCount = managedSessionCount;
        }
        this.cumulativeManagedSessionCount.incrementAndGet();
        for (final IoServiceListener l : this.listeners) {
            try {
                l.sessionCreated(session);
            }
            catch (Exception e) {
                ExceptionMonitor.getInstance().exceptionCaught(e);
            }
        }
    }
    
    public void fireSessionDestroyed(final IoSession session) {
        if (this.managedSessions.remove(session.getId()) == null) {
            return;
        }
        session.getFilterChain().fireSessionClosed();
        try {
            for (final IoServiceListener l : this.listeners) {
                try {
                    l.sessionDestroyed(session);
                }
                catch (Exception e) {
                    ExceptionMonitor.getInstance().exceptionCaught(e);
                }
            }
        }
        finally {
            if (session.getService() instanceof IoConnector) {
                boolean lastSession = false;
                synchronized (this.managedSessions) {
                    lastSession = this.managedSessions.isEmpty();
                }
                if (lastSession) {
                    this.fireServiceDeactivated();
                }
            }
        }
    }
    
    private void disconnectSessions() {
        if (!(this.service instanceof IoAcceptor)) {
            return;
        }
        if (!((IoAcceptor)this.service).isCloseOnDeactivation()) {
            return;
        }
        final Object lock = new Object();
        final IoFutureListener<IoFuture> listener = new LockNotifyingListener(lock);
        for (final IoSession s : this.managedSessions.values()) {
            s.closeNow().addListener((IoFutureListener<?>)listener);
        }
        try {
            synchronized (lock) {
                while (!this.managedSessions.isEmpty()) {
                    lock.wait(500L);
                }
            }
        }
        catch (InterruptedException ex) {}
    }
    
    private static class LockNotifyingListener implements IoFutureListener<IoFuture>
    {
        private final Object lock;
        
        public LockNotifyingListener(final Object lock) {
            this.lock = lock;
        }
        
        @Override
        public void operationComplete(final IoFuture future) {
            synchronized (this.lock) {
                this.lock.notifyAll();
            }
        }
    }
}
