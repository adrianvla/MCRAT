// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.polling;

import java.nio.channels.ClosedSelectorException;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.core.session.IoSession;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import org.apache.mina.util.ExceptionMonitor;
import org.apache.mina.core.RuntimeIoException;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.Executor;
import org.apache.mina.core.service.SimpleIoProcessorPool;
import org.apache.mina.core.session.IoSessionConfig;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.mina.core.service.AbstractIoService;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Queue;
import org.apache.mina.core.service.IoProcessor;
import java.util.concurrent.Semaphore;
import org.apache.mina.core.service.AbstractIoAcceptor;
import org.apache.mina.core.session.AbstractIoSession;

public abstract class AbstractPollingIoAcceptor<S extends AbstractIoSession, H> extends AbstractIoAcceptor
{
    private final Semaphore lock;
    private final IoProcessor<S> processor;
    private final boolean createdProcessor;
    private final Queue<AcceptorOperationFuture> registerQueue;
    private final Queue<AcceptorOperationFuture> cancelQueue;
    private final Map<SocketAddress, H> boundHandles;
    private final ServiceOperationFuture disposalFuture;
    private volatile boolean selectable;
    private AtomicReference<Acceptor> acceptorRef;
    protected boolean reuseAddress;
    protected int backlog;
    
    protected AbstractPollingIoAcceptor(final IoSessionConfig sessionConfig, final Class<? extends IoProcessor<S>> processorClass) {
        this(sessionConfig, null, (IoProcessor)new SimpleIoProcessorPool(processorClass), true, null);
    }
    
    protected AbstractPollingIoAcceptor(final IoSessionConfig sessionConfig, final Class<? extends IoProcessor<S>> processorClass, final int processorCount) {
        this(sessionConfig, null, (IoProcessor)new SimpleIoProcessorPool(processorClass, processorCount), true, null);
    }
    
    protected AbstractPollingIoAcceptor(final IoSessionConfig sessionConfig, final Class<? extends IoProcessor<S>> processorClass, final int processorCount, final SelectorProvider selectorProvider) {
        this(sessionConfig, null, (IoProcessor)new SimpleIoProcessorPool(processorClass, processorCount, selectorProvider), true, selectorProvider);
    }
    
    protected AbstractPollingIoAcceptor(final IoSessionConfig sessionConfig, final IoProcessor<S> processor) {
        this(sessionConfig, null, processor, false, null);
    }
    
    protected AbstractPollingIoAcceptor(final IoSessionConfig sessionConfig, final Executor executor, final IoProcessor<S> processor) {
        this(sessionConfig, executor, processor, false, null);
    }
    
    private AbstractPollingIoAcceptor(final IoSessionConfig sessionConfig, final Executor executor, final IoProcessor<S> processor, final boolean createdProcessor, final SelectorProvider selectorProvider) {
        super(sessionConfig, executor);
        this.lock = new Semaphore(1);
        this.registerQueue = new ConcurrentLinkedQueue<AcceptorOperationFuture>();
        this.cancelQueue = new ConcurrentLinkedQueue<AcceptorOperationFuture>();
        this.boundHandles = Collections.synchronizedMap(new HashMap<SocketAddress, H>());
        this.disposalFuture = new ServiceOperationFuture();
        this.acceptorRef = new AtomicReference<Acceptor>();
        this.reuseAddress = false;
        this.backlog = 50;
        if (processor == null) {
            throw new IllegalArgumentException("processor");
        }
        this.processor = processor;
        this.createdProcessor = createdProcessor;
        try {
            this.init(selectorProvider);
            this.selectable = true;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e2) {
            throw new RuntimeIoException("Failed to initialize.", e2);
        }
        finally {
            if (!this.selectable) {
                try {
                    this.destroy();
                }
                catch (Exception e3) {
                    ExceptionMonitor.getInstance().exceptionCaught(e3);
                }
            }
        }
    }
    
    protected abstract void init() throws Exception;
    
    protected abstract void init(final SelectorProvider p0) throws Exception;
    
    protected abstract void destroy() throws Exception;
    
    protected abstract int select() throws Exception;
    
    protected abstract void wakeup();
    
    protected abstract Iterator<H> selectedHandles();
    
    protected abstract H open(final SocketAddress p0) throws Exception;
    
    protected abstract SocketAddress localAddress(final H p0) throws Exception;
    
    protected abstract S accept(final IoProcessor<S> p0, final H p1) throws Exception;
    
    protected abstract void close(final H p0) throws Exception;
    
    @Override
    protected void dispose0() throws Exception {
        this.unbind();
        this.startupAcceptor();
        this.wakeup();
    }
    
    @Override
    protected final Set<SocketAddress> bindInternal(final List<? extends SocketAddress> localAddresses) throws Exception {
        final AcceptorOperationFuture request = new AcceptorOperationFuture(localAddresses);
        this.registerQueue.add(request);
        this.startupAcceptor();
        try {
            this.lock.acquire();
            this.wakeup();
        }
        finally {
            this.lock.release();
        }
        request.awaitUninterruptibly();
        if (request.getException() != null) {
            throw request.getException();
        }
        final Set<SocketAddress> newLocalAddresses = new HashSet<SocketAddress>();
        for (final H handle : this.boundHandles.values()) {
            newLocalAddresses.add(this.localAddress(handle));
        }
        return newLocalAddresses;
    }
    
    private void startupAcceptor() throws InterruptedException {
        if (!this.selectable) {
            this.registerQueue.clear();
            this.cancelQueue.clear();
        }
        Acceptor acceptor = this.acceptorRef.get();
        if (acceptor == null) {
            this.lock.acquire();
            acceptor = new Acceptor();
            if (this.acceptorRef.compareAndSet(null, acceptor)) {
                this.executeWorker(acceptor);
            }
            else {
                this.lock.release();
            }
        }
    }
    
    @Override
    protected final void unbind0(final List<? extends SocketAddress> localAddresses) throws Exception {
        final AcceptorOperationFuture future = new AcceptorOperationFuture(localAddresses);
        this.cancelQueue.add(future);
        this.startupAcceptor();
        this.wakeup();
        future.awaitUninterruptibly();
        if (future.getException() != null) {
            throw future.getException();
        }
    }
    
    private int registerHandles() {
        while (true) {
            final AcceptorOperationFuture future = this.registerQueue.poll();
            if (future == null) {
                break;
            }
            final Map<SocketAddress, H> newHandles = new ConcurrentHashMap<SocketAddress, H>();
            final List<SocketAddress> localAddresses = future.getLocalAddresses();
            try {
                for (final SocketAddress a : localAddresses) {
                    final H handle = this.open(a);
                    newHandles.put(this.localAddress(handle), handle);
                }
                this.boundHandles.putAll((Map<? extends SocketAddress, ? extends H>)newHandles);
                future.setDone();
                return newHandles.size();
            }
            catch (Exception e) {
                future.setException(e);
            }
            finally {
                if (future.getException() != null) {
                    for (final H handle2 : newHandles.values()) {
                        try {
                            this.close(handle2);
                        }
                        catch (Exception e2) {
                            ExceptionMonitor.getInstance().exceptionCaught(e2);
                        }
                    }
                    this.wakeup();
                }
            }
        }
        return 0;
    }
    
    private int unregisterHandles() {
        int cancelledHandles = 0;
        while (true) {
            final AcceptorOperationFuture future = this.cancelQueue.poll();
            if (future == null) {
                break;
            }
            for (final SocketAddress a : future.getLocalAddresses()) {
                final H handle = this.boundHandles.remove(a);
                if (handle == null) {
                    continue;
                }
                try {
                    this.close(handle);
                    this.wakeup();
                }
                catch (Exception e) {
                    ExceptionMonitor.getInstance().exceptionCaught(e);
                }
                finally {
                    ++cancelledHandles;
                }
            }
            future.setDone();
        }
        return cancelledHandles;
    }
    
    @Override
    public final IoSession newSession(final SocketAddress remoteAddress, final SocketAddress localAddress) {
        throw new UnsupportedOperationException();
    }
    
    public int getBacklog() {
        return this.backlog;
    }
    
    public void setBacklog(final int backlog) {
        synchronized (this.bindLock) {
            if (this.isActive()) {
                throw new IllegalStateException("backlog can't be set while the acceptor is bound.");
            }
            this.backlog = backlog;
        }
    }
    
    public boolean isReuseAddress() {
        return this.reuseAddress;
    }
    
    public void setReuseAddress(final boolean reuseAddress) {
        synchronized (this.bindLock) {
            if (this.isActive()) {
                throw new IllegalStateException("backlog can't be set while the acceptor is bound.");
            }
            this.reuseAddress = reuseAddress;
        }
    }
    
    @Override
    public SocketSessionConfig getSessionConfig() {
        return (SocketSessionConfig)this.sessionConfig;
    }
    
    private class Acceptor implements Runnable
    {
        @Override
        public void run() {
            assert AbstractPollingIoAcceptor.this.acceptorRef.get() == this;
            int nHandles = 0;
            AbstractPollingIoAcceptor.this.lock.release();
            while (AbstractPollingIoAcceptor.this.selectable) {
                try {
                    nHandles += AbstractPollingIoAcceptor.this.registerHandles();
                    final int selected = AbstractPollingIoAcceptor.this.select();
                    if (nHandles == 0) {
                        AbstractPollingIoAcceptor.this.acceptorRef.set(null);
                        if (AbstractPollingIoAcceptor.this.registerQueue.isEmpty() && AbstractPollingIoAcceptor.this.cancelQueue.isEmpty()) {
                            assert AbstractPollingIoAcceptor.this.acceptorRef.get() != this;
                            break;
                        }
                        else if (!AbstractPollingIoAcceptor.this.acceptorRef.compareAndSet(null, this)) {
                            assert AbstractPollingIoAcceptor.this.acceptorRef.get() != this;
                            break;
                        }
                        else {
                            assert AbstractPollingIoAcceptor.this.acceptorRef.get() == this;
                        }
                    }
                    if (selected > 0) {
                        this.processHandles(AbstractPollingIoAcceptor.this.selectedHandles());
                    }
                    nHandles -= AbstractPollingIoAcceptor.this.unregisterHandles();
                    continue;
                }
                catch (ClosedSelectorException cse) {
                    ExceptionMonitor.getInstance().exceptionCaught(cse);
                }
                catch (Exception e) {
                    ExceptionMonitor.getInstance().exceptionCaught(e);
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException e2) {
                        ExceptionMonitor.getInstance().exceptionCaught(e2);
                    }
                    continue;
                }
                break;
            }
            if (AbstractPollingIoAcceptor.this.selectable && AbstractPollingIoAcceptor.this.isDisposing()) {
                AbstractPollingIoAcceptor.this.selectable = false;
                try {
                    if (AbstractPollingIoAcceptor.this.createdProcessor) {
                        AbstractPollingIoAcceptor.this.processor.dispose();
                    }
                }
                finally {
                    try {
                        synchronized (AbstractPollingIoAcceptor.this.disposalLock) {
                            if (AbstractPollingIoAcceptor.this.isDisposing()) {
                                AbstractPollingIoAcceptor.this.destroy();
                            }
                        }
                    }
                    catch (Exception e3) {
                        ExceptionMonitor.getInstance().exceptionCaught(e3);
                        AbstractPollingIoAcceptor.this.disposalFuture.setDone();
                    }
                    finally {
                        AbstractPollingIoAcceptor.this.disposalFuture.setDone();
                    }
                }
            }
        }
        
        private void processHandles(final Iterator<H> handles) throws Exception {
            while (handles.hasNext()) {
                final H handle = handles.next();
                handles.remove();
                final S session = AbstractPollingIoAcceptor.this.accept(AbstractPollingIoAcceptor.this.processor, handle);
                if (session == null) {
                    continue;
                }
                AbstractIoService.this.initSession(session, null, null);
                session.getProcessor().add(session);
            }
        }
    }
}
