// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.executor;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.session.DummySession;
import org.slf4j.LoggerFactory;
import java.util.Queue;
import java.util.Iterator;
import org.apache.mina.core.session.IoEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import java.util.concurrent.ThreadPoolExecutor;

public class OrderedThreadPoolExecutor extends ThreadPoolExecutor
{
    private static final Logger LOGGER;
    private static final int DEFAULT_INITIAL_THREAD_POOL_SIZE = 0;
    private static final int DEFAULT_MAX_THREAD_POOL = 16;
    private static final int DEFAULT_KEEP_ALIVE = 30;
    private static final IoSession EXIT_SIGNAL;
    private final AttributeKey TASKS_QUEUE;
    private final BlockingQueue<IoSession> waitingSessions;
    private final Set<Worker> workers;
    private volatile int largestPoolSize;
    private final AtomicInteger idleWorkers;
    private long completedTaskCount;
    private volatile boolean shutdown;
    private final IoEventQueueHandler eventQueueHandler;
    
    public OrderedThreadPoolExecutor() {
        this(0, 16, 30L, TimeUnit.SECONDS, Executors.defaultThreadFactory(), null);
    }
    
    public OrderedThreadPoolExecutor(final int maximumPoolSize) {
        this(0, maximumPoolSize, 30L, TimeUnit.SECONDS, Executors.defaultThreadFactory(), null);
    }
    
    public OrderedThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize) {
        this(corePoolSize, maximumPoolSize, 30L, TimeUnit.SECONDS, Executors.defaultThreadFactory(), null);
    }
    
    public OrderedThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), null);
    }
    
    public OrderedThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final IoEventQueueHandler eventQueueHandler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), eventQueueHandler);
    }
    
    public OrderedThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, threadFactory, null);
    }
    
    public OrderedThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final ThreadFactory threadFactory, final IoEventQueueHandler eventQueueHandler) {
        super(0, 1, keepAliveTime, unit, new SynchronousQueue<Runnable>(), threadFactory, new AbortPolicy());
        this.TASKS_QUEUE = new AttributeKey(this.getClass(), "tasksQueue");
        this.waitingSessions = new LinkedBlockingQueue<IoSession>();
        this.workers = new HashSet<Worker>();
        this.idleWorkers = new AtomicInteger();
        if (corePoolSize < 0) {
            throw new IllegalArgumentException("corePoolSize: " + corePoolSize);
        }
        if (maximumPoolSize == 0 || maximumPoolSize < corePoolSize) {
            throw new IllegalArgumentException("maximumPoolSize: " + maximumPoolSize);
        }
        super.setCorePoolSize(corePoolSize);
        super.setMaximumPoolSize(maximumPoolSize);
        if (eventQueueHandler == null) {
            this.eventQueueHandler = IoEventQueueHandler.NOOP;
        }
        else {
            this.eventQueueHandler = eventQueueHandler;
        }
    }
    
    private SessionTasksQueue getSessionTasksQueue(final IoSession session) {
        SessionTasksQueue queue = (SessionTasksQueue)session.getAttribute(this.TASKS_QUEUE);
        if (queue == null) {
            queue = new SessionTasksQueue();
            final SessionTasksQueue oldQueue = (SessionTasksQueue)session.setAttributeIfAbsent(this.TASKS_QUEUE, queue);
            if (oldQueue != null) {
                queue = oldQueue;
            }
        }
        return queue;
    }
    
    public IoEventQueueHandler getQueueHandler() {
        return this.eventQueueHandler;
    }
    
    @Override
    public void setRejectedExecutionHandler(final RejectedExecutionHandler handler) {
    }
    
    private void addWorker() {
        synchronized (this.workers) {
            if (this.workers.size() >= super.getMaximumPoolSize()) {
                return;
            }
            final Worker worker = new Worker();
            final Thread thread = this.getThreadFactory().newThread(worker);
            this.idleWorkers.incrementAndGet();
            thread.start();
            this.workers.add(worker);
            if (this.workers.size() > this.largestPoolSize) {
                this.largestPoolSize = this.workers.size();
            }
        }
    }
    
    private void addWorkerIfNecessary() {
        if (this.idleWorkers.get() == 0) {
            synchronized (this.workers) {
                if (this.workers.isEmpty() || this.idleWorkers.get() == 0) {
                    this.addWorker();
                }
            }
        }
    }
    
    private void removeWorker() {
        synchronized (this.workers) {
            if (this.workers.size() <= super.getCorePoolSize()) {
                return;
            }
            this.waitingSessions.offer(OrderedThreadPoolExecutor.EXIT_SIGNAL);
        }
    }
    
    @Override
    public int getMaximumPoolSize() {
        return super.getMaximumPoolSize();
    }
    
    @Override
    public void setMaximumPoolSize(final int maximumPoolSize) {
        if (maximumPoolSize <= 0 || maximumPoolSize < super.getCorePoolSize()) {
            throw new IllegalArgumentException("maximumPoolSize: " + maximumPoolSize);
        }
        synchronized (this.workers) {
            super.setMaximumPoolSize(maximumPoolSize);
            for (int difference = this.workers.size() - maximumPoolSize; difference > 0; --difference) {
                this.removeWorker();
            }
        }
    }
    
    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        final long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
        synchronized (this.workers) {
            while (!this.isTerminated()) {
                final long waitTime = deadline - System.currentTimeMillis();
                if (waitTime <= 0L) {
                    break;
                }
                this.workers.wait(waitTime);
            }
        }
        return this.isTerminated();
    }
    
    @Override
    public boolean isShutdown() {
        return this.shutdown;
    }
    
    @Override
    public boolean isTerminated() {
        if (!this.shutdown) {
            return false;
        }
        synchronized (this.workers) {
            return this.workers.isEmpty();
        }
    }
    
    @Override
    public void shutdown() {
        if (this.shutdown) {
            return;
        }
        this.shutdown = true;
        synchronized (this.workers) {
            for (int i = this.workers.size(); i > 0; --i) {
                this.waitingSessions.offer(OrderedThreadPoolExecutor.EXIT_SIGNAL);
            }
        }
    }
    
    @Override
    public List<Runnable> shutdownNow() {
        this.shutdown();
        final List<Runnable> answer = new ArrayList<Runnable>();
        IoSession session;
        while ((session = this.waitingSessions.poll()) != null) {
            if (session == OrderedThreadPoolExecutor.EXIT_SIGNAL) {
                this.waitingSessions.offer(OrderedThreadPoolExecutor.EXIT_SIGNAL);
                Thread.yield();
            }
            else {
                final SessionTasksQueue sessionTasksQueue = (SessionTasksQueue)session.getAttribute(this.TASKS_QUEUE);
                synchronized (sessionTasksQueue.tasksQueue) {
                    for (final Runnable task : sessionTasksQueue.tasksQueue) {
                        this.getQueueHandler().polled(this, (IoEvent)task);
                        answer.add(task);
                    }
                    sessionTasksQueue.tasksQueue.clear();
                }
            }
        }
        return answer;
    }
    
    private void print(final Queue<Runnable> queue, final IoEvent event) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Adding event ").append(event.getType()).append(" to session ").append(event.getSession().getId());
        boolean first = true;
        sb.append("\nQueue : [");
        for (final Runnable elem : queue) {
            if (first) {
                first = false;
            }
            else {
                sb.append(", ");
            }
            sb.append(((IoEvent)elem).getType()).append(", ");
        }
        sb.append("]\n");
        OrderedThreadPoolExecutor.LOGGER.debug(sb.toString());
    }
    
    @Override
    public void execute(final Runnable task) {
        if (this.shutdown) {
            this.rejectTask(task);
        }
        this.checkTaskType(task);
        final IoEvent event = (IoEvent)task;
        final IoSession session = event.getSession();
        final SessionTasksQueue sessionTasksQueue = this.getSessionTasksQueue(session);
        final Queue<Runnable> tasksQueue = sessionTasksQueue.tasksQueue;
        final boolean offerEvent = this.eventQueueHandler.accept(this, event);
        boolean offerSession;
        if (offerEvent) {
            synchronized (tasksQueue) {
                tasksQueue.offer(event);
                if (sessionTasksQueue.processingCompleted) {
                    sessionTasksQueue.processingCompleted = false;
                    offerSession = true;
                }
                else {
                    offerSession = false;
                }
                if (OrderedThreadPoolExecutor.LOGGER.isDebugEnabled()) {
                    this.print(tasksQueue, event);
                }
            }
        }
        else {
            offerSession = false;
        }
        if (offerSession) {
            this.waitingSessions.offer(session);
        }
        this.addWorkerIfNecessary();
        if (offerEvent) {
            this.eventQueueHandler.offered(this, event);
        }
    }
    
    private void rejectTask(final Runnable task) {
        this.getRejectedExecutionHandler().rejectedExecution(task, this);
    }
    
    private void checkTaskType(final Runnable task) {
        if (!(task instanceof IoEvent)) {
            throw new IllegalArgumentException("task must be an IoEvent or its subclass.");
        }
    }
    
    @Override
    public int getActiveCount() {
        synchronized (this.workers) {
            return this.workers.size() - this.idleWorkers.get();
        }
    }
    
    @Override
    public long getCompletedTaskCount() {
        synchronized (this.workers) {
            long answer = this.completedTaskCount;
            for (final Worker w : this.workers) {
                answer += w.completedTaskCount.get();
            }
            return answer;
        }
    }
    
    @Override
    public int getLargestPoolSize() {
        return this.largestPoolSize;
    }
    
    @Override
    public int getPoolSize() {
        synchronized (this.workers) {
            return this.workers.size();
        }
    }
    
    @Override
    public long getTaskCount() {
        return this.getCompletedTaskCount();
    }
    
    @Override
    public boolean isTerminating() {
        synchronized (this.workers) {
            return this.isShutdown() && !this.isTerminated();
        }
    }
    
    @Override
    public int prestartAllCoreThreads() {
        int answer = 0;
        synchronized (this.workers) {
            for (int i = super.getCorePoolSize() - this.workers.size(); i > 0; --i) {
                this.addWorker();
                ++answer;
            }
        }
        return answer;
    }
    
    @Override
    public boolean prestartCoreThread() {
        synchronized (this.workers) {
            if (this.workers.size() < super.getCorePoolSize()) {
                this.addWorker();
                return true;
            }
            return false;
        }
    }
    
    @Override
    public BlockingQueue<Runnable> getQueue() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void purge() {
    }
    
    @Override
    public boolean remove(final Runnable task) {
        this.checkTaskType(task);
        final IoEvent event = (IoEvent)task;
        final IoSession session = event.getSession();
        final SessionTasksQueue sessionTasksQueue = (SessionTasksQueue)session.getAttribute(this.TASKS_QUEUE);
        if (sessionTasksQueue == null) {
            return false;
        }
        final Queue<Runnable> tasksQueue = sessionTasksQueue.tasksQueue;
        final boolean removed;
        synchronized (tasksQueue) {
            removed = tasksQueue.remove(task);
        }
        if (removed) {
            this.getQueueHandler().polled(this, event);
        }
        return removed;
    }
    
    @Override
    public int getCorePoolSize() {
        return super.getCorePoolSize();
    }
    
    @Override
    public void setCorePoolSize(final int corePoolSize) {
        if (corePoolSize < 0) {
            throw new IllegalArgumentException("corePoolSize: " + corePoolSize);
        }
        if (corePoolSize > super.getMaximumPoolSize()) {
            throw new IllegalArgumentException("corePoolSize exceeds maximumPoolSize");
        }
        synchronized (this.workers) {
            if (super.getCorePoolSize() > corePoolSize) {
                for (int i = super.getCorePoolSize() - corePoolSize; i > 0; --i) {
                    this.removeWorker();
                }
            }
            super.setCorePoolSize(corePoolSize);
        }
    }
    
    static {
        LOGGER = LoggerFactory.getLogger(OrderedThreadPoolExecutor.class);
        EXIT_SIGNAL = new DummySession();
    }
    
    private class Worker implements Runnable
    {
        private AtomicLong completedTaskCount;
        private Thread thread;
        
        private Worker() {
            this.completedTaskCount = new AtomicLong(0L);
        }
        
        @Override
        public void run() {
            this.thread = Thread.currentThread();
            try {
                while (true) {
                    final IoSession session = this.fetchSession();
                    OrderedThreadPoolExecutor.this.idleWorkers.decrementAndGet();
                    if (session == null) {
                        synchronized (OrderedThreadPoolExecutor.this.workers) {
                            if (OrderedThreadPoolExecutor.this.workers.size() > OrderedThreadPoolExecutor.this.getCorePoolSize()) {
                                OrderedThreadPoolExecutor.this.workers.remove(this);
                                break;
                            }
                        }
                    }
                    if (session == OrderedThreadPoolExecutor.EXIT_SIGNAL) {
                        break;
                    }
                    try {
                        if (session == null) {
                            continue;
                        }
                        this.runTasks(OrderedThreadPoolExecutor.this.getSessionTasksQueue(session));
                    }
                    finally {
                        OrderedThreadPoolExecutor.this.idleWorkers.incrementAndGet();
                    }
                }
            }
            finally {
                synchronized (OrderedThreadPoolExecutor.this.workers) {
                    OrderedThreadPoolExecutor.this.workers.remove(this);
                    final OrderedThreadPoolExecutor this$0 = OrderedThreadPoolExecutor.this;
                    this$0.completedTaskCount += this.completedTaskCount.get();
                    OrderedThreadPoolExecutor.this.workers.notifyAll();
                }
            }
        }
        
        private IoSession fetchSession() {
            IoSession session = null;
            long currentTime = System.currentTimeMillis();
            final long deadline = currentTime + OrderedThreadPoolExecutor.this.getKeepAliveTime(TimeUnit.MILLISECONDS);
            while (true) {
                try {
                    final long waitTime = deadline - currentTime;
                    if (waitTime > 0L) {
                        try {
                            session = OrderedThreadPoolExecutor.this.waitingSessions.poll(waitTime, TimeUnit.MILLISECONDS);
                        }
                        finally {
                            if (session == null) {
                                currentTime = System.currentTimeMillis();
                            }
                        }
                    }
                }
                catch (InterruptedException e) {
                    continue;
                }
                break;
            }
            return session;
        }
        
        private void runTasks(final SessionTasksQueue sessionTasksQueue) {
            while (true) {
                final Queue<Runnable> tasksQueue = sessionTasksQueue.tasksQueue;
                final Runnable task;
                synchronized (tasksQueue) {
                    task = tasksQueue.poll();
                    if (task == null) {
                        sessionTasksQueue.processingCompleted = true;
                        break;
                    }
                }
                OrderedThreadPoolExecutor.this.eventQueueHandler.polled(OrderedThreadPoolExecutor.this, (IoEvent)task);
                this.runTask(task);
            }
        }
        
        private void runTask(final Runnable task) {
            ThreadPoolExecutor.this.beforeExecute(this.thread, task);
            boolean ran = false;
            try {
                task.run();
                ran = true;
                ThreadPoolExecutor.this.afterExecute(task, null);
                this.completedTaskCount.incrementAndGet();
            }
            catch (RuntimeException e) {
                if (!ran) {
                    ThreadPoolExecutor.this.afterExecute(task, e);
                }
                throw e;
            }
        }
    }
    
    private class SessionTasksQueue
    {
        private final Queue<Runnable> tasksQueue;
        private boolean processingCompleted;
        
        private SessionTasksQueue() {
            this.tasksQueue = new ConcurrentLinkedQueue<Runnable>();
            this.processingCompleted = true;
        }
    }
}
