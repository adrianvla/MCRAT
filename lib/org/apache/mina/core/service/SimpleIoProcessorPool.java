// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.service;

import org.slf4j.LoggerFactory;
import org.apache.mina.core.session.IoSession;
import java.util.Arrays;
import org.apache.mina.core.write.WriteRequest;
import java.lang.reflect.Constructor;
import org.apache.mina.core.RuntimeIoException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.Executor;
import org.apache.mina.core.session.AttributeKey;
import org.slf4j.Logger;
import org.apache.mina.core.session.AbstractIoSession;

public class SimpleIoProcessorPool<S extends AbstractIoSession> implements IoProcessor<S>
{
    private static final Logger LOGGER;
    private static final int DEFAULT_SIZE;
    private static final AttributeKey PROCESSOR;
    private final IoProcessor<S>[] pool;
    private final Executor executor;
    private final boolean createdExecutor;
    private final Object disposalLock;
    private volatile boolean disposing;
    private volatile boolean disposed;
    
    public SimpleIoProcessorPool(final Class<? extends IoProcessor<S>> processorType) {
        this(processorType, null, SimpleIoProcessorPool.DEFAULT_SIZE, null);
    }
    
    public SimpleIoProcessorPool(final Class<? extends IoProcessor<S>> processorType, final int size) {
        this(processorType, null, size, null);
    }
    
    public SimpleIoProcessorPool(final Class<? extends IoProcessor<S>> processorType, final int size, final SelectorProvider selectorProvider) {
        this(processorType, null, size, selectorProvider);
    }
    
    public SimpleIoProcessorPool(final Class<? extends IoProcessor<S>> processorType, final Executor executor) {
        this(processorType, executor, SimpleIoProcessorPool.DEFAULT_SIZE, null);
    }
    
    public SimpleIoProcessorPool(final Class<? extends IoProcessor<S>> processorType, final Executor executor, final int size, final SelectorProvider selectorProvider) {
        this.disposalLock = new Object();
        if (processorType == null) {
            throw new IllegalArgumentException("processorType");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size: " + size + " (expected: positive integer)");
        }
        this.createdExecutor = (executor == null);
        if (this.createdExecutor) {
            this.executor = Executors.newCachedThreadPool();
            ((ThreadPoolExecutor)this.executor).setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        }
        else {
            this.executor = executor;
        }
        this.pool = (IoProcessor<S>[])new IoProcessor[size];
        boolean success = false;
        Constructor<? extends IoProcessor<S>> processorConstructor = null;
        boolean usesExecutorArg = true;
        try {
            try {
                try {
                    processorConstructor = processorType.getConstructor(ExecutorService.class);
                    this.pool[0] = (IoProcessor<S>)processorConstructor.newInstance(this.executor);
                }
                catch (NoSuchMethodException e2) {
                    try {
                        if (selectorProvider == null) {
                            processorConstructor = processorType.getConstructor(Executor.class);
                            this.pool[0] = (IoProcessor<S>)processorConstructor.newInstance(this.executor);
                        }
                        else {
                            processorConstructor = processorType.getConstructor(Executor.class, SelectorProvider.class);
                            this.pool[0] = (IoProcessor<S>)processorConstructor.newInstance(this.executor, selectorProvider);
                        }
                    }
                    catch (NoSuchMethodException e3) {
                        try {
                            processorConstructor = processorType.getConstructor((Class<?>[])new Class[0]);
                            usesExecutorArg = false;
                            this.pool[0] = (IoProcessor<S>)processorConstructor.newInstance(new Object[0]);
                        }
                        catch (NoSuchMethodException ex) {}
                    }
                }
            }
            catch (RuntimeException re) {
                SimpleIoProcessorPool.LOGGER.error("Cannot create an IoProcessor :{}", re.getMessage());
                throw re;
            }
            catch (Exception e) {
                final String msg = "Failed to create a new instance of " + processorType.getName() + ":" + e.getMessage();
                SimpleIoProcessorPool.LOGGER.error(msg, e);
                throw new RuntimeIoException(msg, e);
            }
            if (processorConstructor == null) {
                final String msg2 = String.valueOf(processorType) + " must have a public constructor with one " + ExecutorService.class.getSimpleName() + " parameter, a public constructor with one " + Executor.class.getSimpleName() + " parameter or a public default constructor.";
                SimpleIoProcessorPool.LOGGER.error(msg2);
                throw new IllegalArgumentException(msg2);
            }
            for (int i = 1; i < this.pool.length; ++i) {
                try {
                    if (usesExecutorArg) {
                        if (selectorProvider == null) {
                            this.pool[i] = (IoProcessor<S>)processorConstructor.newInstance(this.executor);
                        }
                        else {
                            this.pool[i] = (IoProcessor<S>)processorConstructor.newInstance(this.executor, selectorProvider);
                        }
                    }
                    else {
                        this.pool[i] = (IoProcessor<S>)processorConstructor.newInstance(new Object[0]);
                    }
                }
                catch (Exception ex2) {}
            }
            success = true;
        }
        finally {
            if (!success) {
                this.dispose();
            }
        }
    }
    
    @Override
    public final void add(final S session) {
        this.getProcessor(session).add(session);
    }
    
    @Override
    public final void flush(final S session) {
        this.getProcessor(session).flush(session);
    }
    
    @Override
    public final void write(final S session, final WriteRequest writeRequest) {
        this.getProcessor(session).write(session, writeRequest);
    }
    
    @Override
    public final void remove(final S session) {
        this.getProcessor(session).remove(session);
    }
    
    @Override
    public final void updateTrafficControl(final S session) {
        this.getProcessor(session).updateTrafficControl(session);
    }
    
    @Override
    public boolean isDisposed() {
        return this.disposed;
    }
    
    @Override
    public boolean isDisposing() {
        return this.disposing;
    }
    
    @Override
    public final void dispose() {
        if (this.disposed) {
            return;
        }
        synchronized (this.disposalLock) {
            if (!this.disposing) {
                this.disposing = true;
                for (final IoProcessor<S> ioProcessor : this.pool) {
                    if (ioProcessor != null) {
                        if (!ioProcessor.isDisposing()) {
                            try {
                                ioProcessor.dispose();
                            }
                            catch (Exception e) {
                                SimpleIoProcessorPool.LOGGER.warn("Failed to dispose the {} IoProcessor.", ioProcessor.getClass().getSimpleName(), e);
                            }
                        }
                    }
                }
                if (this.createdExecutor) {
                    ((ExecutorService)this.executor).shutdown();
                }
            }
            Arrays.fill(this.pool, null);
            this.disposed = true;
        }
    }
    
    private IoProcessor<S> getProcessor(final S session) {
        IoProcessor<S> processor = (IoProcessor<S>)session.getAttribute(SimpleIoProcessorPool.PROCESSOR);
        if (processor == null) {
            if (this.disposed || this.disposing) {
                throw new IllegalStateException("A disposed processor cannot be accessed.");
            }
            processor = this.pool[Math.abs((int)session.getId()) % this.pool.length];
            if (processor == null) {
                throw new IllegalStateException("A disposed processor cannot be accessed.");
            }
            session.setAttributeIfAbsent(SimpleIoProcessorPool.PROCESSOR, processor);
        }
        return processor;
    }
    
    static {
        LOGGER = LoggerFactory.getLogger(SimpleIoProcessorPool.class);
        DEFAULT_SIZE = Runtime.getRuntime().availableProcessors() + 1;
        PROCESSOR = new AttributeKey(SimpleIoProcessorPool.class, "processor");
    }
}
