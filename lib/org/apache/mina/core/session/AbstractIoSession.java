// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.session;

import org.apache.mina.core.write.WriteTimeoutException;
import java.util.Iterator;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.AbstractIoService;
import java.util.Set;
import org.apache.mina.core.write.WriteException;
import org.apache.mina.core.future.IoFuture;
import java.io.IOException;
import org.apache.mina.util.ExceptionMonitor;
import org.apache.mina.core.file.FilenameFileRegion;
import java.io.FileInputStream;
import java.io.File;
import org.apache.mina.core.file.DefaultFileRegion;
import java.nio.channels.FileChannel;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.write.WriteToClosedSessionException;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.future.DefaultWriteFuture;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import org.apache.mina.core.future.DefaultReadFuture;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.future.DefaultCloseFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoHandler;

public abstract class AbstractIoSession implements IoSession
{
    private final IoHandler handler;
    protected IoSessionConfig config;
    private final IoService service;
    private static final AttributeKey READY_READ_FUTURES_KEY;
    private static final AttributeKey WAITING_READ_FUTURES_KEY;
    private static final IoFutureListener<CloseFuture> SCHEDULED_COUNTER_RESETTER;
    public static final WriteRequest CLOSE_REQUEST;
    public static final WriteRequest MESSAGE_SENT_REQUEST;
    private final Object lock;
    private IoSessionAttributeMap attributes;
    private WriteRequestQueue writeRequestQueue;
    private WriteRequest currentWriteRequest;
    private final long creationTime;
    private static AtomicLong idGenerator;
    private long sessionId;
    private final CloseFuture closeFuture;
    private volatile boolean closing;
    private boolean readSuspended;
    private boolean writeSuspended;
    private final AtomicBoolean scheduledForFlush;
    private final AtomicInteger scheduledWriteBytes;
    private final AtomicInteger scheduledWriteMessages;
    private long readBytes;
    private long writtenBytes;
    private long readMessages;
    private long writtenMessages;
    private long lastReadTime;
    private long lastWriteTime;
    private long lastThroughputCalculationTime;
    private long lastReadBytes;
    private long lastWrittenBytes;
    private long lastReadMessages;
    private long lastWrittenMessages;
    private double readBytesThroughput;
    private double writtenBytesThroughput;
    private double readMessagesThroughput;
    private double writtenMessagesThroughput;
    private AtomicInteger idleCountForBoth;
    private AtomicInteger idleCountForRead;
    private AtomicInteger idleCountForWrite;
    private long lastIdleTimeForBoth;
    private long lastIdleTimeForRead;
    private long lastIdleTimeForWrite;
    private boolean deferDecreaseReadBuffer;
    
    protected AbstractIoSession(final IoService service) {
        this.lock = new Object();
        this.closeFuture = new DefaultCloseFuture(this);
        this.readSuspended = false;
        this.writeSuspended = false;
        this.scheduledForFlush = new AtomicBoolean();
        this.scheduledWriteBytes = new AtomicInteger();
        this.scheduledWriteMessages = new AtomicInteger();
        this.idleCountForBoth = new AtomicInteger();
        this.idleCountForRead = new AtomicInteger();
        this.idleCountForWrite = new AtomicInteger();
        this.deferDecreaseReadBuffer = true;
        this.service = service;
        this.handler = service.getHandler();
        final long currentTime = System.currentTimeMillis();
        this.creationTime = currentTime;
        this.lastThroughputCalculationTime = currentTime;
        this.lastReadTime = currentTime;
        this.lastWriteTime = currentTime;
        this.lastIdleTimeForBoth = currentTime;
        this.lastIdleTimeForRead = currentTime;
        this.lastIdleTimeForWrite = currentTime;
        this.closeFuture.addListener((IoFutureListener<?>)AbstractIoSession.SCHEDULED_COUNTER_RESETTER);
        this.sessionId = AbstractIoSession.idGenerator.incrementAndGet();
    }
    
    @Override
    public final long getId() {
        return this.sessionId;
    }
    
    public abstract IoProcessor getProcessor();
    
    @Override
    public final boolean isConnected() {
        return !this.closeFuture.isClosed();
    }
    
    @Override
    public boolean isActive() {
        return true;
    }
    
    @Override
    public final boolean isClosing() {
        return this.closing || this.closeFuture.isClosed();
    }
    
    @Override
    public boolean isSecured() {
        return false;
    }
    
    @Override
    public final CloseFuture getCloseFuture() {
        return this.closeFuture;
    }
    
    public final boolean isScheduledForFlush() {
        return this.scheduledForFlush.get();
    }
    
    public final void scheduledForFlush() {
        this.scheduledForFlush.set(true);
    }
    
    public final void unscheduledForFlush() {
        this.scheduledForFlush.set(false);
    }
    
    public final boolean setScheduledForFlush(final boolean schedule) {
        if (schedule) {
            return this.scheduledForFlush.compareAndSet(false, schedule);
        }
        this.scheduledForFlush.set(schedule);
        return true;
    }
    
    @Override
    public final CloseFuture close(final boolean rightNow) {
        if (rightNow) {
            return this.closeNow();
        }
        return this.closeOnFlush();
    }
    
    @Override
    public final CloseFuture close() {
        return this.closeNow();
    }
    
    @Override
    public final CloseFuture closeOnFlush() {
        if (!this.isClosing()) {
            this.getWriteRequestQueue().offer(this, AbstractIoSession.CLOSE_REQUEST);
            this.getProcessor().flush(this);
        }
        return this.closeFuture;
    }
    
    @Override
    public final CloseFuture closeNow() {
        synchronized (this.lock) {
            if (this.isClosing()) {
                return this.closeFuture;
            }
            this.closing = true;
            try {
                this.destroy();
            }
            catch (Exception e) {
                final IoFilterChain filterChain = this.getFilterChain();
                filterChain.fireExceptionCaught(e);
            }
        }
        this.getFilterChain().fireFilterClose();
        return this.closeFuture;
    }
    
    protected void destroy() {
        if (this.writeRequestQueue != null) {
            while (!this.writeRequestQueue.isEmpty(this)) {
                final WriteRequest writeRequest = this.writeRequestQueue.poll(this);
                if (writeRequest != null) {
                    final WriteFuture writeFuture = writeRequest.getFuture();
                    if (writeFuture == null) {
                        continue;
                    }
                    writeFuture.setWritten();
                }
            }
        }
    }
    
    @Override
    public IoHandler getHandler() {
        return this.handler;
    }
    
    @Override
    public IoSessionConfig getConfig() {
        return this.config;
    }
    
    @Override
    public final ReadFuture read() {
        if (!this.getConfig().isUseReadOperation()) {
            throw new IllegalStateException("useReadOperation is not enabled.");
        }
        final Queue<ReadFuture> readyReadFutures = this.getReadyReadFutures();
        ReadFuture future;
        synchronized (readyReadFutures) {
            future = readyReadFutures.poll();
            if (future != null) {
                if (future.isClosed()) {
                    readyReadFutures.offer(future);
                }
            }
            else {
                future = new DefaultReadFuture(this);
                this.getWaitingReadFutures().offer(future);
            }
        }
        return future;
    }
    
    public final void offerReadFuture(final Object message) {
        this.newReadFuture().setRead(message);
    }
    
    public final void offerFailedReadFuture(final Throwable exception) {
        this.newReadFuture().setException(exception);
    }
    
    public final void offerClosedReadFuture() {
        final Queue<ReadFuture> readyReadFutures = this.getReadyReadFutures();
        synchronized (readyReadFutures) {
            this.newReadFuture().setClosed();
        }
    }
    
    private ReadFuture newReadFuture() {
        final Queue<ReadFuture> readyReadFutures = this.getReadyReadFutures();
        final Queue<ReadFuture> waitingReadFutures = this.getWaitingReadFutures();
        ReadFuture future;
        synchronized (readyReadFutures) {
            future = waitingReadFutures.poll();
            if (future == null) {
                future = new DefaultReadFuture(this);
                readyReadFutures.offer(future);
            }
        }
        return future;
    }
    
    private Queue<ReadFuture> getReadyReadFutures() {
        Queue<ReadFuture> readyReadFutures = (Queue<ReadFuture>)this.getAttribute(AbstractIoSession.READY_READ_FUTURES_KEY);
        if (readyReadFutures == null) {
            readyReadFutures = new ConcurrentLinkedQueue<ReadFuture>();
            final Queue<ReadFuture> oldReadyReadFutures = (Queue<ReadFuture>)this.setAttributeIfAbsent(AbstractIoSession.READY_READ_FUTURES_KEY, readyReadFutures);
            if (oldReadyReadFutures != null) {
                readyReadFutures = oldReadyReadFutures;
            }
        }
        return readyReadFutures;
    }
    
    private Queue<ReadFuture> getWaitingReadFutures() {
        Queue<ReadFuture> waitingReadyReadFutures = (Queue<ReadFuture>)this.getAttribute(AbstractIoSession.WAITING_READ_FUTURES_KEY);
        if (waitingReadyReadFutures == null) {
            waitingReadyReadFutures = new ConcurrentLinkedQueue<ReadFuture>();
            final Queue<ReadFuture> oldWaitingReadyReadFutures = (Queue<ReadFuture>)this.setAttributeIfAbsent(AbstractIoSession.WAITING_READ_FUTURES_KEY, waitingReadyReadFutures);
            if (oldWaitingReadyReadFutures != null) {
                waitingReadyReadFutures = oldWaitingReadyReadFutures;
            }
        }
        return waitingReadyReadFutures;
    }
    
    @Override
    public WriteFuture write(final Object message) {
        return this.write(message, null);
    }
    
    @Override
    public WriteFuture write(Object message, final SocketAddress remoteAddress) {
        if (message == null) {
            throw new IllegalArgumentException("Trying to write a null message : not allowed");
        }
        if (!this.getTransportMetadata().isConnectionless() && remoteAddress != null) {
            throw new UnsupportedOperationException();
        }
        if (this.isClosing() || !this.isConnected()) {
            final WriteFuture future = new DefaultWriteFuture(this);
            final WriteRequest request = new DefaultWriteRequest(message, future, remoteAddress);
            final WriteException writeException = new WriteToClosedSessionException(request);
            future.setException(writeException);
            return future;
        }
        FileChannel openedFileChannel = null;
        try {
            if (message instanceof IoBuffer && !((IoBuffer)message).hasRemaining()) {
                throw new IllegalArgumentException("message is empty. Forgot to call flip()?");
            }
            if (message instanceof FileChannel) {
                final FileChannel fileChannel = (FileChannel)message;
                message = new DefaultFileRegion(fileChannel, 0L, fileChannel.size());
            }
            else if (message instanceof File) {
                final File file = (File)message;
                openedFileChannel = new FileInputStream(file).getChannel();
                message = new FilenameFileRegion(file, openedFileChannel, 0L, openedFileChannel.size());
            }
        }
        catch (IOException e) {
            ExceptionMonitor.getInstance().exceptionCaught(e);
            return DefaultWriteFuture.newNotWrittenFuture(this, e);
        }
        final WriteFuture writeFuture = new DefaultWriteFuture(this);
        final WriteRequest writeRequest = new DefaultWriteRequest(message, writeFuture, remoteAddress);
        final IoFilterChain filterChain = this.getFilterChain();
        filterChain.fireFilterWrite(writeRequest);
        if (openedFileChannel != null) {
            final FileChannel finalChannel = openedFileChannel;
            writeFuture.addListener((IoFutureListener<?>)new IoFutureListener<WriteFuture>() {
                @Override
                public void operationComplete(final WriteFuture future) {
                    try {
                        finalChannel.close();
                    }
                    catch (IOException e) {
                        ExceptionMonitor.getInstance().exceptionCaught(e);
                    }
                }
            });
        }
        return writeFuture;
    }
    
    @Override
    public final Object getAttachment() {
        return this.getAttribute("");
    }
    
    @Override
    public final Object setAttachment(final Object attachment) {
        return this.setAttribute("", attachment);
    }
    
    @Override
    public final Object getAttribute(final Object key) {
        return this.getAttribute(key, null);
    }
    
    @Override
    public final Object getAttribute(final Object key, final Object defaultValue) {
        return this.attributes.getAttribute(this, key, defaultValue);
    }
    
    @Override
    public final Object setAttribute(final Object key, final Object value) {
        return this.attributes.setAttribute(this, key, value);
    }
    
    @Override
    public final Object setAttribute(final Object key) {
        return this.setAttribute(key, Boolean.TRUE);
    }
    
    @Override
    public final Object setAttributeIfAbsent(final Object key, final Object value) {
        return this.attributes.setAttributeIfAbsent(this, key, value);
    }
    
    @Override
    public final Object setAttributeIfAbsent(final Object key) {
        return this.setAttributeIfAbsent(key, Boolean.TRUE);
    }
    
    @Override
    public final Object removeAttribute(final Object key) {
        return this.attributes.removeAttribute(this, key);
    }
    
    @Override
    public final boolean removeAttribute(final Object key, final Object value) {
        return this.attributes.removeAttribute(this, key, value);
    }
    
    @Override
    public final boolean replaceAttribute(final Object key, final Object oldValue, final Object newValue) {
        return this.attributes.replaceAttribute(this, key, oldValue, newValue);
    }
    
    @Override
    public final boolean containsAttribute(final Object key) {
        return this.attributes.containsAttribute(this, key);
    }
    
    @Override
    public final Set<Object> getAttributeKeys() {
        return this.attributes.getAttributeKeys(this);
    }
    
    public final IoSessionAttributeMap getAttributeMap() {
        return this.attributes;
    }
    
    public final void setAttributeMap(final IoSessionAttributeMap attributes) {
        this.attributes = attributes;
    }
    
    public final void setWriteRequestQueue(final WriteRequestQueue writeRequestQueue) {
        this.writeRequestQueue = writeRequestQueue;
    }
    
    @Override
    public final void suspendRead() {
        this.readSuspended = true;
        if (this.isClosing() || !this.isConnected()) {
            return;
        }
        this.getProcessor().updateTrafficControl(this);
    }
    
    @Override
    public final void suspendWrite() {
        this.writeSuspended = true;
        if (this.isClosing() || !this.isConnected()) {
            return;
        }
        this.getProcessor().updateTrafficControl(this);
    }
    
    @Override
    public final void resumeRead() {
        this.readSuspended = false;
        if (this.isClosing() || !this.isConnected()) {
            return;
        }
        this.getProcessor().updateTrafficControl(this);
    }
    
    @Override
    public final void resumeWrite() {
        this.writeSuspended = false;
        if (this.isClosing() || !this.isConnected()) {
            return;
        }
        this.getProcessor().updateTrafficControl(this);
    }
    
    @Override
    public boolean isReadSuspended() {
        return this.readSuspended;
    }
    
    @Override
    public boolean isWriteSuspended() {
        return this.writeSuspended;
    }
    
    @Override
    public final long getReadBytes() {
        return this.readBytes;
    }
    
    @Override
    public final long getWrittenBytes() {
        return this.writtenBytes;
    }
    
    @Override
    public final long getReadMessages() {
        return this.readMessages;
    }
    
    @Override
    public final long getWrittenMessages() {
        return this.writtenMessages;
    }
    
    @Override
    public final double getReadBytesThroughput() {
        return this.readBytesThroughput;
    }
    
    @Override
    public final double getWrittenBytesThroughput() {
        return this.writtenBytesThroughput;
    }
    
    @Override
    public final double getReadMessagesThroughput() {
        return this.readMessagesThroughput;
    }
    
    @Override
    public final double getWrittenMessagesThroughput() {
        return this.writtenMessagesThroughput;
    }
    
    @Override
    public final void updateThroughput(final long currentTime, final boolean force) {
        final int interval = (int)(currentTime - this.lastThroughputCalculationTime);
        final long minInterval = this.getConfig().getThroughputCalculationIntervalInMillis();
        if ((minInterval == 0L || interval < minInterval) && !force) {
            return;
        }
        this.readBytesThroughput = (this.readBytes - this.lastReadBytes) * 1000.0 / interval;
        this.writtenBytesThroughput = (this.writtenBytes - this.lastWrittenBytes) * 1000.0 / interval;
        this.readMessagesThroughput = (this.readMessages - this.lastReadMessages) * 1000.0 / interval;
        this.writtenMessagesThroughput = (this.writtenMessages - this.lastWrittenMessages) * 1000.0 / interval;
        this.lastReadBytes = this.readBytes;
        this.lastWrittenBytes = this.writtenBytes;
        this.lastReadMessages = this.readMessages;
        this.lastWrittenMessages = this.writtenMessages;
        this.lastThroughputCalculationTime = currentTime;
    }
    
    @Override
    public final long getScheduledWriteBytes() {
        return this.scheduledWriteBytes.get();
    }
    
    @Override
    public final int getScheduledWriteMessages() {
        return this.scheduledWriteMessages.get();
    }
    
    protected void setScheduledWriteBytes(final int byteCount) {
        this.scheduledWriteBytes.set(byteCount);
    }
    
    protected void setScheduledWriteMessages(final int messages) {
        this.scheduledWriteMessages.set(messages);
    }
    
    public final void increaseReadBytes(final long increment, final long currentTime) {
        if (increment <= 0L) {
            return;
        }
        this.readBytes += increment;
        this.lastReadTime = currentTime;
        this.idleCountForBoth.set(0);
        this.idleCountForRead.set(0);
        if (this.getService() instanceof AbstractIoService) {
            ((AbstractIoService)this.getService()).getStatistics().increaseReadBytes(increment, currentTime);
        }
    }
    
    public final void increaseReadMessages(final long currentTime) {
        ++this.readMessages;
        this.lastReadTime = currentTime;
        this.idleCountForBoth.set(0);
        this.idleCountForRead.set(0);
        if (this.getService() instanceof AbstractIoService) {
            ((AbstractIoService)this.getService()).getStatistics().increaseReadMessages(currentTime);
        }
    }
    
    public final void increaseWrittenBytes(final int increment, final long currentTime) {
        if (increment <= 0) {
            return;
        }
        this.writtenBytes += increment;
        this.lastWriteTime = currentTime;
        this.idleCountForBoth.set(0);
        this.idleCountForWrite.set(0);
        if (this.getService() instanceof AbstractIoService) {
            ((AbstractIoService)this.getService()).getStatistics().increaseWrittenBytes(increment, currentTime);
        }
        this.increaseScheduledWriteBytes(-increment);
    }
    
    public final void increaseWrittenMessages(final WriteRequest request, final long currentTime) {
        final Object message = request.getMessage();
        if (message instanceof IoBuffer) {
            final IoBuffer b = (IoBuffer)message;
            if (b.hasRemaining()) {
                return;
            }
        }
        ++this.writtenMessages;
        this.lastWriteTime = currentTime;
        if (this.getService() instanceof AbstractIoService) {
            ((AbstractIoService)this.getService()).getStatistics().increaseWrittenMessages(currentTime);
        }
        this.decreaseScheduledWriteMessages();
    }
    
    public final void increaseScheduledWriteBytes(final int increment) {
        this.scheduledWriteBytes.addAndGet(increment);
        if (this.getService() instanceof AbstractIoService) {
            ((AbstractIoService)this.getService()).getStatistics().increaseScheduledWriteBytes(increment);
        }
    }
    
    public final void increaseScheduledWriteMessages() {
        this.scheduledWriteMessages.incrementAndGet();
        if (this.getService() instanceof AbstractIoService) {
            ((AbstractIoService)this.getService()).getStatistics().increaseScheduledWriteMessages();
        }
    }
    
    private void decreaseScheduledWriteMessages() {
        this.scheduledWriteMessages.decrementAndGet();
        if (this.getService() instanceof AbstractIoService) {
            ((AbstractIoService)this.getService()).getStatistics().decreaseScheduledWriteMessages();
        }
    }
    
    public final void decreaseScheduledBytesAndMessages(final WriteRequest request) {
        final Object message = request.getMessage();
        if (message instanceof IoBuffer) {
            final IoBuffer b = (IoBuffer)message;
            if (b.hasRemaining()) {
                this.increaseScheduledWriteBytes(-((IoBuffer)message).remaining());
            }
            else {
                this.decreaseScheduledWriteMessages();
            }
        }
        else {
            this.decreaseScheduledWriteMessages();
        }
    }
    
    @Override
    public final WriteRequestQueue getWriteRequestQueue() {
        if (this.writeRequestQueue == null) {
            throw new IllegalStateException();
        }
        return this.writeRequestQueue;
    }
    
    @Override
    public final WriteRequest getCurrentWriteRequest() {
        return this.currentWriteRequest;
    }
    
    @Override
    public final Object getCurrentWriteMessage() {
        final WriteRequest req = this.getCurrentWriteRequest();
        if (req == null) {
            return null;
        }
        return req.getMessage();
    }
    
    @Override
    public final void setCurrentWriteRequest(final WriteRequest currentWriteRequest) {
        this.currentWriteRequest = currentWriteRequest;
    }
    
    public final void increaseReadBufferSize() {
        final int newReadBufferSize = this.getConfig().getReadBufferSize() << 1;
        if (newReadBufferSize <= this.getConfig().getMaxReadBufferSize()) {
            this.getConfig().setReadBufferSize(newReadBufferSize);
        }
        else {
            this.getConfig().setReadBufferSize(this.getConfig().getMaxReadBufferSize());
        }
        this.deferDecreaseReadBuffer = true;
    }
    
    public final void decreaseReadBufferSize() {
        if (this.deferDecreaseReadBuffer) {
            this.deferDecreaseReadBuffer = false;
            return;
        }
        if (this.getConfig().getReadBufferSize() > this.getConfig().getMinReadBufferSize()) {
            this.getConfig().setReadBufferSize(this.getConfig().getReadBufferSize() >>> 1);
        }
        this.deferDecreaseReadBuffer = true;
    }
    
    @Override
    public final long getCreationTime() {
        return this.creationTime;
    }
    
    @Override
    public final long getLastIoTime() {
        return Math.max(this.lastReadTime, this.lastWriteTime);
    }
    
    @Override
    public final long getLastReadTime() {
        return this.lastReadTime;
    }
    
    @Override
    public final long getLastWriteTime() {
        return this.lastWriteTime;
    }
    
    @Override
    public final boolean isIdle(final IdleStatus status) {
        if (status == IdleStatus.BOTH_IDLE) {
            return this.idleCountForBoth.get() > 0;
        }
        if (status == IdleStatus.READER_IDLE) {
            return this.idleCountForRead.get() > 0;
        }
        if (status == IdleStatus.WRITER_IDLE) {
            return this.idleCountForWrite.get() > 0;
        }
        throw new IllegalArgumentException("Unknown idle status: " + status);
    }
    
    @Override
    public final boolean isBothIdle() {
        return this.isIdle(IdleStatus.BOTH_IDLE);
    }
    
    @Override
    public final boolean isReaderIdle() {
        return this.isIdle(IdleStatus.READER_IDLE);
    }
    
    @Override
    public final boolean isWriterIdle() {
        return this.isIdle(IdleStatus.WRITER_IDLE);
    }
    
    @Override
    public final int getIdleCount(final IdleStatus status) {
        if (this.getConfig().getIdleTime(status) == 0) {
            if (status == IdleStatus.BOTH_IDLE) {
                this.idleCountForBoth.set(0);
            }
            if (status == IdleStatus.READER_IDLE) {
                this.idleCountForRead.set(0);
            }
            if (status == IdleStatus.WRITER_IDLE) {
                this.idleCountForWrite.set(0);
            }
        }
        if (status == IdleStatus.BOTH_IDLE) {
            return this.idleCountForBoth.get();
        }
        if (status == IdleStatus.READER_IDLE) {
            return this.idleCountForRead.get();
        }
        if (status == IdleStatus.WRITER_IDLE) {
            return this.idleCountForWrite.get();
        }
        throw new IllegalArgumentException("Unknown idle status: " + status);
    }
    
    @Override
    public final long getLastIdleTime(final IdleStatus status) {
        if (status == IdleStatus.BOTH_IDLE) {
            return this.lastIdleTimeForBoth;
        }
        if (status == IdleStatus.READER_IDLE) {
            return this.lastIdleTimeForRead;
        }
        if (status == IdleStatus.WRITER_IDLE) {
            return this.lastIdleTimeForWrite;
        }
        throw new IllegalArgumentException("Unknown idle status: " + status);
    }
    
    public final void increaseIdleCount(final IdleStatus status, final long currentTime) {
        if (status == IdleStatus.BOTH_IDLE) {
            this.idleCountForBoth.incrementAndGet();
            this.lastIdleTimeForBoth = currentTime;
        }
        else if (status == IdleStatus.READER_IDLE) {
            this.idleCountForRead.incrementAndGet();
            this.lastIdleTimeForRead = currentTime;
        }
        else {
            if (status != IdleStatus.WRITER_IDLE) {
                throw new IllegalArgumentException("Unknown idle status: " + status);
            }
            this.idleCountForWrite.incrementAndGet();
            this.lastIdleTimeForWrite = currentTime;
        }
    }
    
    @Override
    public final int getBothIdleCount() {
        return this.getIdleCount(IdleStatus.BOTH_IDLE);
    }
    
    @Override
    public final long getLastBothIdleTime() {
        return this.getLastIdleTime(IdleStatus.BOTH_IDLE);
    }
    
    @Override
    public final long getLastReaderIdleTime() {
        return this.getLastIdleTime(IdleStatus.READER_IDLE);
    }
    
    @Override
    public final long getLastWriterIdleTime() {
        return this.getLastIdleTime(IdleStatus.WRITER_IDLE);
    }
    
    @Override
    public final int getReaderIdleCount() {
        return this.getIdleCount(IdleStatus.READER_IDLE);
    }
    
    @Override
    public final int getWriterIdleCount() {
        return this.getIdleCount(IdleStatus.WRITER_IDLE);
    }
    
    @Override
    public SocketAddress getServiceAddress() {
        final IoService service = this.getService();
        if (service instanceof IoAcceptor) {
            return ((IoAcceptor)service).getLocalAddress();
        }
        return this.getRemoteAddress();
    }
    
    @Override
    public final int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public final boolean equals(final Object o) {
        return super.equals(o);
    }
    
    @Override
    public String toString() {
        if (!this.isConnected() && !this.isClosing()) {
            return "(" + this.getIdAsString() + ") Session disconnected ...";
        }
        String remote = null;
        String local = null;
        try {
            remote = String.valueOf(this.getRemoteAddress());
        }
        catch (Exception e) {
            remote = "Cannot get the remote address informations: " + e.getMessage();
        }
        try {
            local = String.valueOf(this.getLocalAddress());
        }
        catch (Exception ex) {}
        if (this.getService() instanceof IoAcceptor) {
            return "(" + this.getIdAsString() + ": " + this.getServiceName() + ", server, " + remote + " => " + local + ')';
        }
        return "(" + this.getIdAsString() + ": " + this.getServiceName() + ", client, " + local + " => " + remote + ')';
    }
    
    private String getIdAsString() {
        final String id = Long.toHexString(this.getId()).toUpperCase();
        if (id.length() <= 8) {
            return "0x00000000".substring(0, 10 - id.length()) + id;
        }
        return "0x" + id;
    }
    
    private String getServiceName() {
        final TransportMetadata tm = this.getTransportMetadata();
        if (tm == null) {
            return "null";
        }
        return tm.getProviderName() + ' ' + tm.getName();
    }
    
    @Override
    public IoService getService() {
        return this.service;
    }
    
    public static void notifyIdleness(final Iterator<? extends IoSession> sessions, final long currentTime) {
        while (sessions.hasNext()) {
            final IoSession session = (IoSession)sessions.next();
            if (!session.getCloseFuture().isClosed()) {
                notifyIdleSession(session, currentTime);
            }
        }
    }
    
    public static void notifyIdleSession(final IoSession session, final long currentTime) {
        notifyIdleSession0(session, currentTime, session.getConfig().getIdleTimeInMillis(IdleStatus.BOTH_IDLE), IdleStatus.BOTH_IDLE, Math.max(session.getLastIoTime(), session.getLastIdleTime(IdleStatus.BOTH_IDLE)));
        notifyIdleSession0(session, currentTime, session.getConfig().getIdleTimeInMillis(IdleStatus.READER_IDLE), IdleStatus.READER_IDLE, Math.max(session.getLastReadTime(), session.getLastIdleTime(IdleStatus.READER_IDLE)));
        notifyIdleSession0(session, currentTime, session.getConfig().getIdleTimeInMillis(IdleStatus.WRITER_IDLE), IdleStatus.WRITER_IDLE, Math.max(session.getLastWriteTime(), session.getLastIdleTime(IdleStatus.WRITER_IDLE)));
        notifyWriteTimeout(session, currentTime);
    }
    
    private static void notifyIdleSession0(final IoSession session, final long currentTime, final long idleTime, final IdleStatus status, final long lastIoTime) {
        if (idleTime > 0L && lastIoTime != 0L && currentTime - lastIoTime >= idleTime) {
            session.getFilterChain().fireSessionIdle(status);
        }
    }
    
    private static void notifyWriteTimeout(final IoSession session, final long currentTime) {
        final long writeTimeout = session.getConfig().getWriteTimeoutInMillis();
        if (writeTimeout > 0L && currentTime - session.getLastWriteTime() >= writeTimeout && !session.getWriteRequestQueue().isEmpty(session)) {
            final WriteRequest request = session.getCurrentWriteRequest();
            if (request != null) {
                session.setCurrentWriteRequest(null);
                final WriteTimeoutException cause = new WriteTimeoutException(request);
                request.getFuture().setException(cause);
                session.getFilterChain().fireExceptionCaught(cause);
                session.closeNow();
            }
        }
    }
    
    static {
        READY_READ_FUTURES_KEY = new AttributeKey(AbstractIoSession.class, "readyReadFutures");
        WAITING_READ_FUTURES_KEY = new AttributeKey(AbstractIoSession.class, "waitingReadFutures");
        SCHEDULED_COUNTER_RESETTER = new IoFutureListener<CloseFuture>() {
            @Override
            public void operationComplete(final CloseFuture future) {
                final AbstractIoSession session = (AbstractIoSession)future.getSession();
                session.scheduledWriteBytes.set(0);
                session.scheduledWriteMessages.set(0);
                session.readBytesThroughput = 0.0;
                session.readMessagesThroughput = 0.0;
                session.writtenBytesThroughput = 0.0;
                session.writtenMessagesThroughput = 0.0;
            }
        };
        CLOSE_REQUEST = new DefaultWriteRequest(new Object());
        MESSAGE_SENT_REQUEST = new DefaultWriteRequest(DefaultWriteRequest.EMPTY_MESSAGE);
        AbstractIoSession.idGenerator = new AtomicLong(0L);
    }
}
