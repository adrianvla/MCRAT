// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.executor;

import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilterEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executor;
import org.apache.mina.core.session.IoEventType;
import java.util.EnumSet;
import org.apache.mina.core.filterchain.IoFilterAdapter;

public class ExecutorFilter extends IoFilterAdapter
{
    private EnumSet<IoEventType> eventTypes;
    private Executor executor;
    private boolean manageableExecutor;
    private static final int DEFAULT_MAX_POOL_SIZE = 16;
    private static final int BASE_THREAD_NUMBER = 0;
    private static final long DEFAULT_KEEPALIVE_TIME = 30L;
    private static final boolean MANAGEABLE_EXECUTOR = true;
    private static final boolean NOT_MANAGEABLE_EXECUTOR = false;
    private static final IoEventType[] DEFAULT_EVENT_SET;
    
    public ExecutorFilter() {
        final Executor executor = this.createDefaultExecutor(0, 16, 30L, TimeUnit.SECONDS, Executors.defaultThreadFactory(), null);
        this.init(executor, true, new IoEventType[0]);
    }
    
    public ExecutorFilter(final int maximumPoolSize) {
        final Executor executor = this.createDefaultExecutor(0, maximumPoolSize, 30L, TimeUnit.SECONDS, Executors.defaultThreadFactory(), null);
        this.init(executor, true, new IoEventType[0]);
    }
    
    public ExecutorFilter(final int corePoolSize, final int maximumPoolSize) {
        final Executor executor = this.createDefaultExecutor(corePoolSize, maximumPoolSize, 30L, TimeUnit.SECONDS, Executors.defaultThreadFactory(), null);
        this.init(executor, true, new IoEventType[0]);
    }
    
    public ExecutorFilter(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit) {
        final Executor executor = this.createDefaultExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), null);
        this.init(executor, true, new IoEventType[0]);
    }
    
    public ExecutorFilter(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final IoEventQueueHandler queueHandler) {
        final Executor executor = this.createDefaultExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), queueHandler);
        this.init(executor, true, new IoEventType[0]);
    }
    
    public ExecutorFilter(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final ThreadFactory threadFactory) {
        final Executor executor = this.createDefaultExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, threadFactory, null);
        this.init(executor, true, new IoEventType[0]);
    }
    
    public ExecutorFilter(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final ThreadFactory threadFactory, final IoEventQueueHandler queueHandler) {
        final Executor executor = new OrderedThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, threadFactory, queueHandler);
        this.init(executor, true, new IoEventType[0]);
    }
    
    public ExecutorFilter(final IoEventType... eventTypes) {
        final Executor executor = this.createDefaultExecutor(0, 16, 30L, TimeUnit.SECONDS, Executors.defaultThreadFactory(), null);
        this.init(executor, true, eventTypes);
    }
    
    public ExecutorFilter(final int maximumPoolSize, final IoEventType... eventTypes) {
        final Executor executor = this.createDefaultExecutor(0, maximumPoolSize, 30L, TimeUnit.SECONDS, Executors.defaultThreadFactory(), null);
        this.init(executor, true, eventTypes);
    }
    
    public ExecutorFilter(final int corePoolSize, final int maximumPoolSize, final IoEventType... eventTypes) {
        final Executor executor = this.createDefaultExecutor(corePoolSize, maximumPoolSize, 30L, TimeUnit.SECONDS, Executors.defaultThreadFactory(), null);
        this.init(executor, true, eventTypes);
    }
    
    public ExecutorFilter(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final IoEventType... eventTypes) {
        final Executor executor = this.createDefaultExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), null);
        this.init(executor, true, eventTypes);
    }
    
    public ExecutorFilter(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final IoEventQueueHandler queueHandler, final IoEventType... eventTypes) {
        final Executor executor = this.createDefaultExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), queueHandler);
        this.init(executor, true, eventTypes);
    }
    
    public ExecutorFilter(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final ThreadFactory threadFactory, final IoEventType... eventTypes) {
        final Executor executor = this.createDefaultExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, threadFactory, null);
        this.init(executor, true, eventTypes);
    }
    
    public ExecutorFilter(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final ThreadFactory threadFactory, final IoEventQueueHandler queueHandler, final IoEventType... eventTypes) {
        final Executor executor = new OrderedThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, threadFactory, queueHandler);
        this.init(executor, true, eventTypes);
    }
    
    public ExecutorFilter(final Executor executor) {
        this.init(executor, false, new IoEventType[0]);
    }
    
    public ExecutorFilter(final Executor executor, final IoEventType... eventTypes) {
        this.init(executor, false, eventTypes);
    }
    
    private Executor createDefaultExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final ThreadFactory threadFactory, final IoEventQueueHandler queueHandler) {
        final Executor executor = new OrderedThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, threadFactory, queueHandler);
        return executor;
    }
    
    private void initEventTypes(IoEventType... eventTypes) {
        if (eventTypes == null || eventTypes.length == 0) {
            eventTypes = ExecutorFilter.DEFAULT_EVENT_SET;
        }
        this.eventTypes = EnumSet.of(eventTypes[0], eventTypes);
        if (this.eventTypes.contains(IoEventType.SESSION_CREATED)) {
            this.eventTypes = null;
            throw new IllegalArgumentException(IoEventType.SESSION_CREATED + " is not allowed.");
        }
    }
    
    private void init(final Executor executor, final boolean manageableExecutor, final IoEventType... eventTypes) {
        if (executor == null) {
            throw new IllegalArgumentException("executor");
        }
        this.initEventTypes(eventTypes);
        this.executor = executor;
        this.manageableExecutor = manageableExecutor;
    }
    
    @Override
    public void destroy() {
        if (this.manageableExecutor) {
            ((ExecutorService)this.executor).shutdown();
        }
    }
    
    public final Executor getExecutor() {
        return this.executor;
    }
    
    protected void fireEvent(final IoFilterEvent event) {
        this.executor.execute(event);
    }
    
    @Override
    public void onPreAdd(final IoFilterChain parent, final String name, final IoFilter.NextFilter nextFilter) throws Exception {
        if (parent.contains(this)) {
            throw new IllegalArgumentException("You can't add the same filter instance more than once.  Create another instance and add it.");
        }
    }
    
    @Override
    public final void sessionOpened(final IoFilter.NextFilter nextFilter, final IoSession session) {
        if (this.eventTypes.contains(IoEventType.SESSION_OPENED)) {
            final IoFilterEvent event = new IoFilterEvent(nextFilter, IoEventType.SESSION_OPENED, session, null);
            this.fireEvent(event);
        }
        else {
            nextFilter.sessionOpened(session);
        }
    }
    
    @Override
    public final void sessionClosed(final IoFilter.NextFilter nextFilter, final IoSession session) {
        if (this.eventTypes.contains(IoEventType.SESSION_CLOSED)) {
            final IoFilterEvent event = new IoFilterEvent(nextFilter, IoEventType.SESSION_CLOSED, session, null);
            this.fireEvent(event);
        }
        else {
            nextFilter.sessionClosed(session);
        }
    }
    
    @Override
    public final void sessionIdle(final IoFilter.NextFilter nextFilter, final IoSession session, final IdleStatus status) {
        if (this.eventTypes.contains(IoEventType.SESSION_IDLE)) {
            final IoFilterEvent event = new IoFilterEvent(nextFilter, IoEventType.SESSION_IDLE, session, status);
            this.fireEvent(event);
        }
        else {
            nextFilter.sessionIdle(session, status);
        }
    }
    
    @Override
    public final void exceptionCaught(final IoFilter.NextFilter nextFilter, final IoSession session, final Throwable cause) {
        if (this.eventTypes.contains(IoEventType.EXCEPTION_CAUGHT)) {
            final IoFilterEvent event = new IoFilterEvent(nextFilter, IoEventType.EXCEPTION_CAUGHT, session, cause);
            this.fireEvent(event);
        }
        else {
            nextFilter.exceptionCaught(session, cause);
        }
    }
    
    @Override
    public final void messageReceived(final IoFilter.NextFilter nextFilter, final IoSession session, final Object message) {
        if (this.eventTypes.contains(IoEventType.MESSAGE_RECEIVED)) {
            final IoFilterEvent event = new IoFilterEvent(nextFilter, IoEventType.MESSAGE_RECEIVED, session, message);
            this.fireEvent(event);
        }
        else {
            nextFilter.messageReceived(session, message);
        }
    }
    
    @Override
    public final void messageSent(final IoFilter.NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) {
        if (this.eventTypes.contains(IoEventType.MESSAGE_SENT)) {
            final IoFilterEvent event = new IoFilterEvent(nextFilter, IoEventType.MESSAGE_SENT, session, writeRequest);
            this.fireEvent(event);
        }
        else {
            nextFilter.messageSent(session, writeRequest);
        }
    }
    
    @Override
    public final void filterWrite(final IoFilter.NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) {
        if (this.eventTypes.contains(IoEventType.WRITE)) {
            final IoFilterEvent event = new IoFilterEvent(nextFilter, IoEventType.WRITE, session, writeRequest);
            this.fireEvent(event);
        }
        else {
            nextFilter.filterWrite(session, writeRequest);
        }
    }
    
    @Override
    public final void filterClose(final IoFilter.NextFilter nextFilter, final IoSession session) throws Exception {
        if (this.eventTypes.contains(IoEventType.CLOSE)) {
            final IoFilterEvent event = new IoFilterEvent(nextFilter, IoEventType.CLOSE, session, null);
            this.fireEvent(event);
        }
        else {
            nextFilter.filterClose(session);
        }
    }
    
    static {
        DEFAULT_EVENT_SET = new IoEventType[] { IoEventType.EXCEPTION_CAUGHT, IoEventType.MESSAGE_RECEIVED, IoEventType.MESSAGE_SENT, IoEventType.SESSION_CLOSED, IoEventType.SESSION_IDLE, IoEventType.SESSION_OPENED };
    }
}
