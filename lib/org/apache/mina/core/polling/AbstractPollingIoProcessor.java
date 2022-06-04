// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.polling;

import java.nio.channels.ClosedSelectorException;
import org.slf4j.LoggerFactory;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.transport.socket.AbstractDatagramSessionConfig;
import java.net.PortUnreachableException;
import java.util.List;
import java.util.Collection;
import org.apache.mina.core.write.WriteToClosedSessionException;
import java.util.ArrayList;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.service.IoServiceListenerSupport;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.util.ExceptionMonitor;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.util.NamePreservingRunnable;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.file.FileRegion;
import java.io.IOException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.SessionState;
import java.util.Iterator;
import org.apache.mina.core.session.IoSession;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.mina.core.future.DefaultIoFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.session.AbstractIoSession;

public abstract class AbstractPollingIoProcessor<S extends AbstractIoSession> implements IoProcessor<S>
{
    private static final Logger LOG;
    private static final long SELECT_TIMEOUT = 1000L;
    private static final ConcurrentHashMap<Class<?>, AtomicInteger> threadIds;
    private final String threadName;
    private final Executor executor;
    private final Queue<S> newSessions;
    private final Queue<S> removingSessions;
    private final Queue<S> flushingSessions;
    private final Queue<S> trafficControllingSessions;
    private final AtomicReference<Processor> processorRef;
    private long lastIdleCheckTime;
    private final Object disposalLock;
    private volatile boolean disposing;
    private volatile boolean disposed;
    private final DefaultIoFuture disposalFuture;
    protected AtomicBoolean wakeupCalled;
    
    protected AbstractPollingIoProcessor(final Executor executor) {
        this.newSessions = new ConcurrentLinkedQueue<S>();
        this.removingSessions = new ConcurrentLinkedQueue<S>();
        this.flushingSessions = new ConcurrentLinkedQueue<S>();
        this.trafficControllingSessions = new ConcurrentLinkedQueue<S>();
        this.processorRef = new AtomicReference<Processor>();
        this.disposalLock = new Object();
        this.disposalFuture = new DefaultIoFuture(null);
        this.wakeupCalled = new AtomicBoolean(false);
        if (executor == null) {
            throw new IllegalArgumentException("executor");
        }
        this.threadName = this.nextThreadName();
        this.executor = executor;
    }
    
    private String nextThreadName() {
        final Class<?> cls = this.getClass();
        final AtomicInteger threadId = AbstractPollingIoProcessor.threadIds.putIfAbsent(cls, new AtomicInteger(1));
        int newThreadId;
        if (threadId == null) {
            newThreadId = 1;
        }
        else {
            newThreadId = threadId.incrementAndGet();
        }
        return cls.getSimpleName() + '-' + newThreadId;
    }
    
    @Override
    public final boolean isDisposing() {
        return this.disposing;
    }
    
    @Override
    public final boolean isDisposed() {
        return this.disposed;
    }
    
    @Override
    public final void dispose() {
        if (this.disposed || this.disposing) {
            return;
        }
        synchronized (this.disposalLock) {
            this.disposing = true;
            this.startupProcessor();
        }
        this.disposalFuture.awaitUninterruptibly();
        this.disposed = true;
    }
    
    protected abstract void doDispose() throws Exception;
    
    protected abstract int select(final long p0) throws Exception;
    
    protected abstract int select() throws Exception;
    
    protected abstract boolean isSelectorEmpty();
    
    protected abstract void wakeup();
    
    protected abstract Iterator<S> allSessions();
    
    protected abstract Iterator<S> selectedSessions();
    
    protected abstract SessionState getState(final S p0);
    
    protected abstract boolean isWritable(final S p0);
    
    protected abstract boolean isReadable(final S p0);
    
    protected abstract void setInterestedInWrite(final S p0, final boolean p1) throws Exception;
    
    protected abstract void setInterestedInRead(final S p0, final boolean p1) throws Exception;
    
    protected abstract boolean isInterestedInRead(final S p0);
    
    protected abstract boolean isInterestedInWrite(final S p0);
    
    protected abstract void init(final S p0) throws Exception;
    
    protected abstract void destroy(final S p0) throws Exception;
    
    protected abstract int read(final S p0, final IoBuffer p1) throws Exception;
    
    protected abstract int write(final S p0, final IoBuffer p1, final int p2) throws IOException;
    
    protected abstract int transferFile(final S p0, final FileRegion p1, final int p2) throws Exception;
    
    @Override
    public final void add(final S session) {
        if (this.disposed || this.disposing) {
            throw new IllegalStateException("Already disposed.");
        }
        this.newSessions.add(session);
        this.startupProcessor();
    }
    
    @Override
    public final void remove(final S session) {
        this.scheduleRemove(session);
        this.startupProcessor();
    }
    
    private void scheduleRemove(final S session) {
        if (!this.removingSessions.contains(session)) {
            this.removingSessions.add(session);
        }
    }
    
    @Override
    public void write(final S session, final WriteRequest writeRequest) {
        final WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();
        writeRequestQueue.offer(session, writeRequest);
        if (!session.isWriteSuspended()) {
            this.flush(session);
        }
    }
    
    @Override
    public final void flush(final S session) {
        if (session.setScheduledForFlush(true)) {
            this.flushingSessions.add(session);
            this.wakeup();
        }
    }
    
    private void scheduleFlush(final S session) {
        if (session.setScheduledForFlush(true)) {
            this.flushingSessions.add(session);
        }
    }
    
    public final void updateTrafficMask(final S session) {
        this.trafficControllingSessions.add(session);
        this.wakeup();
    }
    
    private void startupProcessor() {
        Processor processor = this.processorRef.get();
        if (processor == null) {
            processor = new Processor();
            if (this.processorRef.compareAndSet(null, processor)) {
                this.executor.execute(new NamePreservingRunnable(processor, this.threadName));
            }
        }
        this.wakeup();
    }
    
    protected abstract void registerNewSelector() throws IOException;
    
    protected abstract boolean isBrokenConnection() throws IOException;
    
    private int handleNewSessions() {
        int addedSessions = 0;
        for (S session = this.newSessions.poll(); session != null; session = this.newSessions.poll()) {
            if (this.addNow(session)) {
                ++addedSessions;
            }
        }
        return addedSessions;
    }
    
    private boolean addNow(final S session) {
        boolean registered = false;
        try {
            this.init(session);
            registered = true;
            final IoFilterChainBuilder chainBuilder = session.getService().getFilterChainBuilder();
            chainBuilder.buildFilterChain(session.getFilterChain());
            final IoServiceListenerSupport listeners = ((AbstractIoService)session.getService()).getListeners();
            listeners.fireSessionCreated(session);
        }
        catch (Exception e) {
            ExceptionMonitor.getInstance().exceptionCaught(e);
            try {
                this.destroy(session);
            }
            catch (Exception e2) {
                ExceptionMonitor.getInstance().exceptionCaught(e2);
            }
            finally {
                registered = false;
            }
        }
        return registered;
    }
    
    private int removeSessions() {
        int removedSessions = 0;
        for (S session = this.removingSessions.poll(); session != null; session = this.removingSessions.poll()) {
            final SessionState state = this.getState(session);
            switch (state) {
                case OPENED: {
                    if (this.removeNow(session)) {
                        ++removedSessions;
                        break;
                    }
                    break;
                }
                case CLOSING: {
                    ++removedSessions;
                    break;
                }
                case OPENING: {
                    this.newSessions.remove(session);
                    if (this.removeNow(session)) {
                        ++removedSessions;
                        break;
                    }
                    break;
                }
                default: {
                    throw new IllegalStateException(String.valueOf(state));
                }
            }
        }
        return removedSessions;
    }
    
    private boolean removeNow(final S session) {
        this.clearWriteRequestQueue(session);
        try {
            this.destroy(session);
            return true;
        }
        catch (Exception e) {
            IoFilterChain filterChain = session.getFilterChain();
            filterChain.fireExceptionCaught(e);
            try {
                this.clearWriteRequestQueue(session);
                ((AbstractIoService)session.getService()).getListeners().fireSessionDestroyed(session);
            }
            catch (Exception e) {
                filterChain = session.getFilterChain();
                filterChain.fireExceptionCaught(e);
            }
        }
        finally {
            try {
                this.clearWriteRequestQueue(session);
                ((AbstractIoService)session.getService()).getListeners().fireSessionDestroyed(session);
            }
            catch (Exception e2) {
                final IoFilterChain filterChain2 = session.getFilterChain();
                filterChain2.fireExceptionCaught(e2);
            }
        }
        return false;
    }
    
    private void clearWriteRequestQueue(final S session) {
        final WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();
        final List<WriteRequest> failedRequests = new ArrayList<WriteRequest>();
        WriteRequest req;
        if ((req = writeRequestQueue.poll(session)) != null) {
            final Object message = req.getMessage();
            if (message instanceof IoBuffer) {
                final IoBuffer buf = (IoBuffer)message;
                if (buf.hasRemaining()) {
                    buf.reset();
                    failedRequests.add(req);
                }
                else {
                    final IoFilterChain filterChain = session.getFilterChain();
                    filterChain.fireMessageSent(req);
                }
            }
            else {
                failedRequests.add(req);
            }
            while ((req = writeRequestQueue.poll(session)) != null) {
                failedRequests.add(req);
            }
        }
        if (!failedRequests.isEmpty()) {
            final WriteToClosedSessionException cause = new WriteToClosedSessionException(failedRequests);
            for (final WriteRequest r : failedRequests) {
                session.decreaseScheduledBytesAndMessages(r);
                r.getFuture().setException(cause);
            }
            final IoFilterChain filterChain2 = session.getFilterChain();
            filterChain2.fireExceptionCaught(cause);
        }
    }
    
    private void process() throws Exception {
        final Iterator<S> i = this.selectedSessions();
        while (i.hasNext()) {
            final S session = i.next();
            this.process(session);
            i.remove();
        }
    }
    
    private void process(final S session) {
        if (this.isReadable(session) && !session.isReadSuspended()) {
            this.read(session);
        }
        if (this.isWritable(session) && !session.isWriteSuspended() && session.setScheduledForFlush(true)) {
            this.flushingSessions.add(session);
        }
    }
    
    private void read(final S session) {
        final IoSessionConfig config = session.getConfig();
        final int bufferSize = config.getReadBufferSize();
        IoBuffer buf = IoBuffer.allocate(bufferSize);
        final boolean hasFragmentation = session.getTransportMetadata().hasFragmentation();
        try {
            int readBytes = 0;
            int ret;
            try {
                if (hasFragmentation) {
                    while ((ret = this.read(session, buf)) > 0) {
                        readBytes += ret;
                        if (!buf.hasRemaining()) {
                            break;
                        }
                    }
                }
                else {
                    ret = this.read(session, buf);
                    if (ret > 0) {
                        readBytes = ret;
                    }
                }
            }
            finally {
                buf.flip();
            }
            if (readBytes > 0) {
                final IoFilterChain filterChain = session.getFilterChain();
                filterChain.fireMessageReceived(buf);
                buf = null;
                if (hasFragmentation) {
                    if (readBytes << 1 < config.getReadBufferSize()) {
                        session.decreaseReadBufferSize();
                    }
                    else if (readBytes == config.getReadBufferSize()) {
                        session.increaseReadBufferSize();
                    }
                }
            }
            if (ret < 0) {
                final IoFilterChain filterChain = session.getFilterChain();
                filterChain.fireInputClosed();
            }
        }
        catch (Exception e) {
            if (e instanceof IOException && (!(e instanceof PortUnreachableException) || !AbstractDatagramSessionConfig.class.isAssignableFrom(config.getClass()) || ((AbstractDatagramSessionConfig)config).isCloseOnPortUnreachable())) {
                this.scheduleRemove(session);
            }
            final IoFilterChain filterChain2 = session.getFilterChain();
            filterChain2.fireExceptionCaught(e);
        }
    }
    
    private void notifyIdleSessions(final long currentTime) throws Exception {
        if (currentTime - this.lastIdleCheckTime >= 1000L) {
            this.lastIdleCheckTime = currentTime;
            AbstractIoSession.notifyIdleness(this.allSessions(), currentTime);
        }
    }
    
    private void flush(final long currentTime) {
        if (this.flushingSessions.isEmpty()) {
            return;
        }
        do {
            final S session = this.flushingSessions.poll();
            if (session == null) {
                break;
            }
            session.unscheduledForFlush();
            final SessionState state = this.getState(session);
            switch (state) {
                case OPENED: {
                    try {
                        final boolean flushedAll = this.flushNow(session, currentTime);
                        if (!flushedAll || session.getWriteRequestQueue().isEmpty(session) || session.isScheduledForFlush()) {
                            continue;
                        }
                        this.scheduleFlush(session);
                    }
                    catch (Exception e) {
                        this.scheduleRemove(session);
                        session.closeNow();
                        final IoFilterChain filterChain = session.getFilterChain();
                        filterChain.fireExceptionCaught(e);
                    }
                    continue;
                }
                case CLOSING: {
                    continue;
                }
                case OPENING: {
                    this.scheduleFlush(session);
                }
                default: {
                    throw new IllegalStateException(String.valueOf(state));
                }
            }
        } while (!this.flushingSessions.isEmpty());
    }
    
    private boolean flushNow(final S session, final long currentTime) {
        if (!session.isConnected()) {
            this.scheduleRemove(session);
            return false;
        }
        final boolean hasFragmentation = session.getTransportMetadata().hasFragmentation();
        final WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();
        final int maxWrittenBytes = session.getConfig().getMaxReadBufferSize() + (session.getConfig().getMaxReadBufferSize() >>> 1);
        int writtenBytes = 0;
        WriteRequest req = null;
        try {
            this.setInterestedInWrite(session, false);
            do {
                req = session.getCurrentWriteRequest();
                if (req == null) {
                    req = writeRequestQueue.poll(session);
                    if (req == null) {
                        break;
                    }
                    session.setCurrentWriteRequest(req);
                }
                final Object message = req.getMessage();
                int localWrittenBytes;
                if (message instanceof IoBuffer) {
                    localWrittenBytes = this.writeBuffer(session, req, hasFragmentation, maxWrittenBytes - writtenBytes, currentTime);
                    if (localWrittenBytes > 0 && ((IoBuffer)message).hasRemaining()) {
                        writtenBytes += localWrittenBytes;
                        this.setInterestedInWrite(session, true);
                        return false;
                    }
                }
                else {
                    if (!(message instanceof FileRegion)) {
                        throw new IllegalStateException("Don't know how to handle message of type '" + message.getClass().getName() + "'.  Are you missing a protocol encoder?");
                    }
                    localWrittenBytes = this.writeFile(session, req, hasFragmentation, maxWrittenBytes - writtenBytes, currentTime);
                    if (localWrittenBytes > 0 && ((FileRegion)message).getRemainingBytes() > 0L) {
                        writtenBytes += localWrittenBytes;
                        this.setInterestedInWrite(session, true);
                        return false;
                    }
                }
                if (localWrittenBytes == 0) {
                    if (!req.equals(AbstractIoSession.MESSAGE_SENT_REQUEST)) {
                        this.setInterestedInWrite(session, true);
                        return false;
                    }
                }
                else {
                    writtenBytes += localWrittenBytes;
                    if (writtenBytes >= maxWrittenBytes) {
                        this.scheduleFlush(session);
                        return false;
                    }
                }
                if (message instanceof IoBuffer) {
                    ((IoBuffer)message).free();
                }
            } while (writtenBytes < maxWrittenBytes);
        }
        catch (Exception e) {
            if (req != null) {
                req.getFuture().setException(e);
            }
            final IoFilterChain filterChain = session.getFilterChain();
            filterChain.fireExceptionCaught(e);
            return false;
        }
        return true;
    }
    
    private int writeBuffer(final S session, final WriteRequest req, final boolean hasFragmentation, final int maxLength, final long currentTime) throws Exception {
        IoBuffer buf = (IoBuffer)req.getMessage();
        int localWrittenBytes = 0;
        if (buf.hasRemaining()) {
            int length;
            if (hasFragmentation) {
                length = Math.min(buf.remaining(), maxLength);
            }
            else {
                length = buf.remaining();
            }
            try {
                localWrittenBytes = this.write(session, buf, length);
            }
            catch (IOException ioe) {
                buf.free();
                session.closeNow();
                this.removeNow(session);
                return 0;
            }
        }
        session.increaseWrittenBytes(localWrittenBytes, currentTime);
        if (!buf.hasRemaining() || (!hasFragmentation && localWrittenBytes != 0)) {
            final Object originalMessage = req.getOriginalRequest().getMessage();
            if (originalMessage instanceof IoBuffer) {
                buf = (IoBuffer)req.getOriginalRequest().getMessage();
                final int pos = buf.position();
                buf.reset();
                this.fireMessageSent(session, req);
                buf.position(pos);
            }
            else {
                this.fireMessageSent(session, req);
            }
        }
        return localWrittenBytes;
    }
    
    private int writeFile(final S session, final WriteRequest req, final boolean hasFragmentation, final int maxLength, final long currentTime) throws Exception {
        final FileRegion region = (FileRegion)req.getMessage();
        int localWrittenBytes;
        if (region.getRemainingBytes() > 0L) {
            int length;
            if (hasFragmentation) {
                length = (int)Math.min(region.getRemainingBytes(), maxLength);
            }
            else {
                length = (int)Math.min(2147483647L, region.getRemainingBytes());
            }
            localWrittenBytes = this.transferFile(session, region, length);
            region.update(localWrittenBytes);
        }
        else {
            localWrittenBytes = 0;
        }
        session.increaseWrittenBytes(localWrittenBytes, currentTime);
        if (region.getRemainingBytes() <= 0L || (!hasFragmentation && localWrittenBytes != 0)) {
            this.fireMessageSent(session, req);
        }
        return localWrittenBytes;
    }
    
    private void fireMessageSent(final S session, final WriteRequest req) {
        session.setCurrentWriteRequest(null);
        final IoFilterChain filterChain = session.getFilterChain();
        filterChain.fireMessageSent(req);
    }
    
    private void updateTrafficMask() {
        for (int queueSize = this.trafficControllingSessions.size(); queueSize > 0; --queueSize) {
            final S session = this.trafficControllingSessions.poll();
            if (session == null) {
                return;
            }
            final SessionState state = this.getState(session);
            switch (state) {
                case OPENED: {
                    this.updateTrafficControl(session);
                    break;
                }
                case CLOSING: {
                    break;
                }
                case OPENING: {
                    this.trafficControllingSessions.add(session);
                    break;
                }
                default: {
                    throw new IllegalStateException(String.valueOf(state));
                }
            }
        }
    }
    
    @Override
    public void updateTrafficControl(final S session) {
        try {
            this.setInterestedInRead(session, !session.isReadSuspended());
        }
        catch (Exception e) {
            final IoFilterChain filterChain = session.getFilterChain();
            filterChain.fireExceptionCaught(e);
        }
        try {
            this.setInterestedInWrite(session, !session.getWriteRequestQueue().isEmpty(session) && !session.isWriteSuspended());
        }
        catch (Exception e) {
            final IoFilterChain filterChain = session.getFilterChain();
            filterChain.fireExceptionCaught(e);
        }
    }
    
    static {
        LOG = LoggerFactory.getLogger(IoProcessor.class);
        threadIds = new ConcurrentHashMap<Class<?>, AtomicInteger>();
    }
    
    private class Processor implements Runnable
    {
        @Override
        public void run() {
            assert AbstractPollingIoProcessor.this.processorRef.get() == this;
            int nSessions = 0;
            AbstractPollingIoProcessor.this.lastIdleCheckTime = System.currentTimeMillis();
            int nbTries = 10;
            while (true) {
                try {
                    while (true) {
                        final long t0 = System.currentTimeMillis();
                        final int selected = AbstractPollingIoProcessor.this.select(1000L);
                        final long t2 = System.currentTimeMillis();
                        final long delta = t2 - t0;
                        if (!AbstractPollingIoProcessor.this.wakeupCalled.getAndSet(false) && selected == 0 && delta < 100L) {
                            if (AbstractPollingIoProcessor.this.isBrokenConnection()) {
                                AbstractPollingIoProcessor.LOG.warn("Broken connection");
                            }
                            else if (nbTries == 0) {
                                AbstractPollingIoProcessor.LOG.warn("Create a new selector. Selected is 0, delta = " + delta);
                                AbstractPollingIoProcessor.this.registerNewSelector();
                                nbTries = 10;
                            }
                            else {
                                --nbTries;
                            }
                        }
                        else {
                            nbTries = 10;
                        }
                        nSessions += AbstractPollingIoProcessor.this.handleNewSessions();
                        AbstractPollingIoProcessor.this.updateTrafficMask();
                        if (selected > 0) {
                            AbstractPollingIoProcessor.this.process();
                        }
                        final long currentTime = System.currentTimeMillis();
                        AbstractPollingIoProcessor.this.flush(currentTime);
                        nSessions -= AbstractPollingIoProcessor.this.removeSessions();
                        AbstractPollingIoProcessor.this.notifyIdleSessions(currentTime);
                        if (nSessions == 0) {
                            AbstractPollingIoProcessor.this.processorRef.set(null);
                            if (AbstractPollingIoProcessor.this.newSessions.isEmpty() && AbstractPollingIoProcessor.this.isSelectorEmpty()) {
                                assert AbstractPollingIoProcessor.this.processorRef.get() != this;
                                break;
                            }
                            else {
                                assert AbstractPollingIoProcessor.this.processorRef.get() != this;
                                if (!AbstractPollingIoProcessor.this.processorRef.compareAndSet(null, this)) {
                                    assert AbstractPollingIoProcessor.this.processorRef.get() != this;
                                    break;
                                }
                                else {
                                    assert AbstractPollingIoProcessor.this.processorRef.get() == this;
                                }
                            }
                        }
                        if (AbstractPollingIoProcessor.this.isDisposing()) {
                            boolean hasKeys = false;
                            final Iterator<S> i = AbstractPollingIoProcessor.this.allSessions();
                            while (i.hasNext()) {
                                final IoSession session = i.next();
                                if (session.isActive()) {
                                    AbstractPollingIoProcessor.this.scheduleRemove((AbstractIoSession)session);
                                    hasKeys = true;
                                }
                            }
                            if (!hasKeys) {
                                continue;
                            }
                            AbstractPollingIoProcessor.this.wakeup();
                        }
                    }
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
            try {
                synchronized (AbstractPollingIoProcessor.this.disposalLock) {
                    if (AbstractPollingIoProcessor.this.disposing) {
                        AbstractPollingIoProcessor.this.doDispose();
                    }
                }
            }
            catch (Exception e) {
                ExceptionMonitor.getInstance().exceptionCaught(e);
            }
            finally {
                AbstractPollingIoProcessor.this.disposalFuture.setValue(true);
            }
        }
    }
}
