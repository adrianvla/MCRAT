// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.ssl;

import org.slf4j.LoggerFactory;
import javax.net.ssl.SSLHandshakeException;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.future.WriteFuture;
import java.nio.ByteBuffer;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.filterchain.IoFilter;
import java.net.InetSocketAddress;
import javax.net.ssl.SSLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.SSLEngineResult;
import org.apache.mina.core.buffer.IoBuffer;
import javax.net.ssl.SSLEngine;
import org.apache.mina.core.filterchain.IoFilterEvent;
import java.util.Queue;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;

class SslHandler
{
    private static final Logger LOGGER;
    private final SslFilter sslFilter;
    private final IoSession session;
    private final Queue<IoFilterEvent> preHandshakeEventQueue;
    private final Queue<IoFilterEvent> filterWriteEventQueue;
    private final Queue<IoFilterEvent> messageReceivedEventQueue;
    private SSLEngine sslEngine;
    private IoBuffer inNetBuffer;
    private IoBuffer outNetBuffer;
    private IoBuffer appBuffer;
    private final IoBuffer emptyBuffer;
    private SSLEngineResult.HandshakeStatus handshakeStatus;
    private boolean firstSSLNegociation;
    private boolean handshakeComplete;
    private boolean writingEncryptedData;
    private ReentrantLock sslLock;
    private final AtomicInteger scheduled_events;
    
    SslHandler(final SslFilter sslFilter, final IoSession session) throws SSLException {
        this.preHandshakeEventQueue = new ConcurrentLinkedQueue<IoFilterEvent>();
        this.filterWriteEventQueue = new ConcurrentLinkedQueue<IoFilterEvent>();
        this.messageReceivedEventQueue = new ConcurrentLinkedQueue<IoFilterEvent>();
        this.emptyBuffer = IoBuffer.allocate(0);
        this.sslLock = new ReentrantLock();
        this.scheduled_events = new AtomicInteger(0);
        this.sslFilter = sslFilter;
        this.session = session;
    }
    
    void init() throws SSLException {
        if (this.sslEngine != null) {
            return;
        }
        SslHandler.LOGGER.debug("{} Initializing the SSL Handler", this.sslFilter.getSessionInfo(this.session));
        final InetSocketAddress peer = (InetSocketAddress)this.session.getAttribute(SslFilter.PEER_ADDRESS);
        if (peer == null) {
            this.sslEngine = this.sslFilter.sslContext.createSSLEngine();
        }
        else {
            this.sslEngine = this.sslFilter.sslContext.createSSLEngine(peer.getHostName(), peer.getPort());
        }
        this.sslEngine.setUseClientMode(this.sslFilter.isUseClientMode());
        if (!this.sslEngine.getUseClientMode()) {
            if (this.sslFilter.isWantClientAuth()) {
                this.sslEngine.setWantClientAuth(true);
            }
            if (this.sslFilter.isNeedClientAuth()) {
                this.sslEngine.setNeedClientAuth(true);
            }
        }
        if (this.sslFilter.getEnabledCipherSuites() != null) {
            this.sslEngine.setEnabledCipherSuites(this.sslFilter.getEnabledCipherSuites());
        }
        if (this.sslFilter.getEnabledProtocols() != null) {
            this.sslEngine.setEnabledProtocols(this.sslFilter.getEnabledProtocols());
        }
        this.sslEngine.beginHandshake();
        this.handshakeStatus = this.sslEngine.getHandshakeStatus();
        this.writingEncryptedData = false;
        this.firstSSLNegociation = true;
        this.handshakeComplete = false;
        if (SslHandler.LOGGER.isDebugEnabled()) {
            SslHandler.LOGGER.debug("{} SSL Handler Initialization done.", this.sslFilter.getSessionInfo(this.session));
        }
    }
    
    void destroy() {
        if (this.sslEngine == null) {
            return;
        }
        try {
            this.sslEngine.closeInbound();
        }
        catch (SSLException e) {
            SslHandler.LOGGER.debug("Unexpected exception from SSLEngine.closeInbound().", e);
        }
        Label_0065: {
            if (this.outNetBuffer != null) {
                this.outNetBuffer.capacity(this.sslEngine.getSession().getPacketBufferSize());
                break Label_0065;
            }
            this.createOutNetBuffer(0);
            try {
                do {
                    this.outNetBuffer.clear();
                } while (this.sslEngine.wrap(this.emptyBuffer.buf(), this.outNetBuffer.buf()).bytesProduced() > 0);
            }
            catch (SSLException ex) {}
            finally {
                this.outNetBuffer.free();
                this.outNetBuffer = null;
            }
        }
        this.sslEngine.closeOutbound();
        this.sslEngine = null;
        this.preHandshakeEventQueue.clear();
    }
    
    SslFilter getSslFilter() {
        return this.sslFilter;
    }
    
    IoSession getSession() {
        return this.session;
    }
    
    boolean isWritingEncryptedData() {
        return this.writingEncryptedData;
    }
    
    boolean isHandshakeComplete() {
        return this.handshakeComplete;
    }
    
    boolean isInboundDone() {
        return this.sslEngine == null || this.sslEngine.isInboundDone();
    }
    
    boolean isOutboundDone() {
        return this.sslEngine == null || this.sslEngine.isOutboundDone();
    }
    
    boolean needToCompleteHandshake() {
        return this.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP && !this.isInboundDone();
    }
    
    void schedulePreHandshakeWriteRequest(final IoFilter.NextFilter nextFilter, final WriteRequest writeRequest) {
        this.preHandshakeEventQueue.add(new IoFilterEvent(nextFilter, IoEventType.WRITE, this.session, writeRequest));
    }
    
    void flushPreHandshakeEvents() throws SSLException {
        IoFilterEvent scheduledWrite;
        while ((scheduledWrite = this.preHandshakeEventQueue.poll()) != null) {
            this.sslFilter.filterWrite(scheduledWrite.getNextFilter(), this.session, (WriteRequest)scheduledWrite.getParameter());
        }
    }
    
    void scheduleFilterWrite(final IoFilter.NextFilter nextFilter, final WriteRequest writeRequest) {
        this.filterWriteEventQueue.add(new IoFilterEvent(nextFilter, IoEventType.WRITE, this.session, writeRequest));
    }
    
    void scheduleMessageReceived(final IoFilter.NextFilter nextFilter, final Object message) {
        this.messageReceivedEventQueue.add(new IoFilterEvent(nextFilter, IoEventType.MESSAGE_RECEIVED, this.session, message));
    }
    
    void flushScheduledEvents() {
        this.scheduled_events.incrementAndGet();
        if (this.sslLock.tryLock()) {
            try {
                while (true) {
                    IoFilterEvent event;
                    if ((event = this.filterWriteEventQueue.poll()) != null) {
                        final IoFilter.NextFilter nextFilter = event.getNextFilter();
                        nextFilter.filterWrite(this.session, (WriteRequest)event.getParameter());
                    }
                    else {
                        while ((event = this.messageReceivedEventQueue.poll()) != null) {
                            final IoFilter.NextFilter nextFilter = event.getNextFilter();
                            nextFilter.messageReceived(this.session, event.getParameter());
                        }
                        if (this.scheduled_events.decrementAndGet() <= 0) {
                            break;
                        }
                        continue;
                    }
                }
            }
            finally {
                this.sslLock.unlock();
            }
        }
    }
    
    void messageReceived(final IoFilter.NextFilter nextFilter, final ByteBuffer buf) throws SSLException {
        if (SslHandler.LOGGER.isDebugEnabled()) {
            if (!this.isOutboundDone()) {
                SslHandler.LOGGER.debug("{} Processing the received message", this.sslFilter.getSessionInfo(this.session));
            }
            else {
                SslHandler.LOGGER.debug("{} Processing the received message", this.sslFilter.getSessionInfo(this.session));
            }
        }
        if (this.inNetBuffer == null) {
            this.inNetBuffer = IoBuffer.allocate(buf.remaining()).setAutoExpand(true);
        }
        this.inNetBuffer.put(buf);
        if (!this.handshakeComplete) {
            this.handshake(nextFilter);
        }
        else {
            this.inNetBuffer.flip();
            if (!this.inNetBuffer.hasRemaining()) {
                return;
            }
            final SSLEngineResult res = this.unwrap();
            if (this.inNetBuffer.hasRemaining()) {
                this.inNetBuffer.compact();
            }
            else {
                this.inNetBuffer.free();
                this.inNetBuffer = null;
            }
            this.checkStatus(res);
            this.renegotiateIfNeeded(nextFilter, res);
        }
        if (this.isInboundDone()) {
            final int inNetBufferPosition = (this.inNetBuffer == null) ? 0 : this.inNetBuffer.position();
            buf.position(buf.position() - inNetBufferPosition);
            if (this.inNetBuffer != null) {
                this.inNetBuffer.free();
                this.inNetBuffer = null;
            }
        }
    }
    
    IoBuffer fetchAppBuffer() {
        if (this.appBuffer == null) {
            return IoBuffer.allocate(0);
        }
        final IoBuffer appBuffer = this.appBuffer.flip();
        this.appBuffer = null;
        return appBuffer.shrink();
    }
    
    IoBuffer fetchOutNetBuffer() {
        final IoBuffer answer = this.outNetBuffer;
        if (answer == null) {
            return this.emptyBuffer;
        }
        this.outNetBuffer = null;
        return answer.shrink();
    }
    
    void encrypt(final ByteBuffer src) throws SSLException {
        if (!this.handshakeComplete) {
            throw new IllegalStateException();
        }
        if (!src.hasRemaining()) {
            if (this.outNetBuffer == null) {
                this.outNetBuffer = this.emptyBuffer;
            }
            return;
        }
        this.createOutNetBuffer(src.remaining());
        while (src.hasRemaining()) {
            final SSLEngineResult result = this.sslEngine.wrap(src, this.outNetBuffer.buf());
            if (result.getStatus() == SSLEngineResult.Status.OK) {
                if (result.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NEED_TASK) {
                    continue;
                }
                this.doTasks();
            }
            else {
                if (result.getStatus() != SSLEngineResult.Status.BUFFER_OVERFLOW) {
                    throw new SSLException("SSLEngine error during encrypt: " + result.getStatus() + " src: " + src + "outNetBuffer: " + this.outNetBuffer);
                }
                this.outNetBuffer.capacity(this.outNetBuffer.capacity() << 1);
                this.outNetBuffer.limit(this.outNetBuffer.capacity());
            }
        }
        this.outNetBuffer.flip();
    }
    
    boolean closeOutbound() throws SSLException {
        if (this.sslEngine == null || this.sslEngine.isOutboundDone()) {
            return false;
        }
        this.sslEngine.closeOutbound();
        this.createOutNetBuffer(0);
        SSLEngineResult result;
        while (true) {
            result = this.sslEngine.wrap(this.emptyBuffer.buf(), this.outNetBuffer.buf());
            if (result.getStatus() != SSLEngineResult.Status.BUFFER_OVERFLOW) {
                break;
            }
            this.outNetBuffer.capacity(this.outNetBuffer.capacity() << 1);
            this.outNetBuffer.limit(this.outNetBuffer.capacity());
        }
        if (result.getStatus() != SSLEngineResult.Status.CLOSED) {
            throw new SSLException("Improper close state: " + result);
        }
        this.outNetBuffer.flip();
        return true;
    }
    
    private void checkStatus(final SSLEngineResult res) throws SSLException {
        final SSLEngineResult.Status status = res.getStatus();
        if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
            throw new SSLException("SSLEngine error during decrypt: " + status + " inNetBuffer: " + this.inNetBuffer + "appBuffer: " + this.appBuffer);
        }
    }
    
    void handshake(final IoFilter.NextFilter nextFilter) throws SSLException {
        while (true) {
            switch (this.handshakeStatus) {
                case FINISHED:
                case NOT_HANDSHAKING: {
                    if (SslHandler.LOGGER.isDebugEnabled()) {
                        SslHandler.LOGGER.debug("{} processing the FINISHED state", this.sslFilter.getSessionInfo(this.session));
                    }
                    this.session.setAttribute(SslFilter.SSL_SESSION, this.sslEngine.getSession());
                    this.handshakeComplete = true;
                    if (this.firstSSLNegociation && this.session.containsAttribute(SslFilter.USE_NOTIFICATION)) {
                        this.firstSSLNegociation = false;
                        this.scheduleMessageReceived(nextFilter, SslFilter.SESSION_SECURED);
                    }
                    if (SslHandler.LOGGER.isDebugEnabled()) {
                        if (!this.isOutboundDone()) {
                            SslHandler.LOGGER.debug("{} is now secured", this.sslFilter.getSessionInfo(this.session));
                        }
                        else {
                            SslHandler.LOGGER.debug("{} is not secured yet", this.sslFilter.getSessionInfo(this.session));
                        }
                    }
                }
                case NEED_TASK: {
                    if (SslHandler.LOGGER.isDebugEnabled()) {
                        SslHandler.LOGGER.debug("{} processing the NEED_TASK state", this.sslFilter.getSessionInfo(this.session));
                    }
                    this.handshakeStatus = this.doTasks();
                    continue;
                }
                case NEED_UNWRAP: {
                    if (SslHandler.LOGGER.isDebugEnabled()) {
                        SslHandler.LOGGER.debug("{} processing the NEED_UNWRAP state", this.sslFilter.getSessionInfo(this.session));
                    }
                    final SSLEngineResult.Status status = this.unwrapHandshake(nextFilter);
                    if ((status == SSLEngineResult.Status.BUFFER_UNDERFLOW && this.handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED) || this.isInboundDone()) {
                        return;
                    }
                    continue;
                }
                case NEED_WRAP: {
                    if (SslHandler.LOGGER.isDebugEnabled()) {
                        SslHandler.LOGGER.debug("{} processing the NEED_WRAP state", this.sslFilter.getSessionInfo(this.session));
                    }
                    if (this.outNetBuffer != null && this.outNetBuffer.hasRemaining()) {
                        return;
                    }
                    this.createOutNetBuffer(0);
                    SSLEngineResult result;
                    while (true) {
                        result = this.sslEngine.wrap(this.emptyBuffer.buf(), this.outNetBuffer.buf());
                        if (result.getStatus() != SSLEngineResult.Status.BUFFER_OVERFLOW) {
                            break;
                        }
                        this.outNetBuffer.capacity(this.outNetBuffer.capacity() << 1);
                        this.outNetBuffer.limit(this.outNetBuffer.capacity());
                    }
                    this.outNetBuffer.flip();
                    this.handshakeStatus = result.getHandshakeStatus();
                    this.writeNetBuffer(nextFilter);
                    continue;
                }
                default: {
                    final String msg = "Invalid Handshaking State" + this.handshakeStatus + " while processing the Handshake for session " + this.session.getId();
                    SslHandler.LOGGER.error(msg);
                    throw new IllegalStateException(msg);
                }
            }
        }
    }
    
    private void createOutNetBuffer(final int expectedRemaining) {
        final int capacity = Math.max(expectedRemaining, this.sslEngine.getSession().getPacketBufferSize());
        if (this.outNetBuffer != null) {
            this.outNetBuffer.capacity(capacity);
        }
        else {
            this.outNetBuffer = IoBuffer.allocate(capacity).minimumCapacity(0);
        }
    }
    
    WriteFuture writeNetBuffer(final IoFilter.NextFilter nextFilter) throws SSLException {
        if (this.outNetBuffer == null || !this.outNetBuffer.hasRemaining()) {
            return null;
        }
        this.writingEncryptedData = true;
        WriteFuture writeFuture = null;
        try {
            final IoBuffer writeBuffer = this.fetchOutNetBuffer();
            writeFuture = new DefaultWriteFuture(this.session);
            this.sslFilter.filterWrite(nextFilter, this.session, new DefaultWriteRequest(writeBuffer, writeFuture));
            while (this.needToCompleteHandshake()) {
                try {
                    this.handshake(nextFilter);
                }
                catch (SSLException ssle) {
                    final SSLException newSsle = new SSLHandshakeException("SSL handshake failed.");
                    newSsle.initCause(ssle);
                    throw newSsle;
                }
                final IoBuffer outNetBuffer = this.fetchOutNetBuffer();
                if (outNetBuffer != null && outNetBuffer.hasRemaining()) {
                    writeFuture = new DefaultWriteFuture(this.session);
                    this.sslFilter.filterWrite(nextFilter, this.session, new DefaultWriteRequest(outNetBuffer, writeFuture));
                }
            }
        }
        finally {
            this.writingEncryptedData = false;
        }
        return writeFuture;
    }
    
    private SSLEngineResult.Status unwrapHandshake(final IoFilter.NextFilter nextFilter) throws SSLException {
        if (this.inNetBuffer != null) {
            this.inNetBuffer.flip();
        }
        if (this.inNetBuffer == null || !this.inNetBuffer.hasRemaining()) {
            return SSLEngineResult.Status.BUFFER_UNDERFLOW;
        }
        SSLEngineResult res = this.unwrap();
        this.handshakeStatus = res.getHandshakeStatus();
        this.checkStatus(res);
        if (this.handshakeStatus == SSLEngineResult.HandshakeStatus.FINISHED && res.getStatus() == SSLEngineResult.Status.OK && this.inNetBuffer.hasRemaining()) {
            res = this.unwrap();
            if (this.inNetBuffer.hasRemaining()) {
                this.inNetBuffer.compact();
            }
            else {
                this.inNetBuffer.free();
                this.inNetBuffer = null;
            }
            this.renegotiateIfNeeded(nextFilter, res);
        }
        else if (this.inNetBuffer.hasRemaining()) {
            this.inNetBuffer.compact();
        }
        else {
            this.inNetBuffer.free();
            this.inNetBuffer = null;
        }
        return res.getStatus();
    }
    
    private void renegotiateIfNeeded(final IoFilter.NextFilter nextFilter, final SSLEngineResult res) throws SSLException {
        if (res.getStatus() != SSLEngineResult.Status.CLOSED && res.getStatus() != SSLEngineResult.Status.BUFFER_UNDERFLOW && res.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            this.handshakeComplete = false;
            this.handshakeStatus = res.getHandshakeStatus();
            this.handshake(nextFilter);
        }
    }
    
    private SSLEngineResult unwrap() throws SSLException {
        if (this.appBuffer == null) {
            this.appBuffer = IoBuffer.allocate(this.inNetBuffer.remaining());
        }
        else {
            this.appBuffer.expand(this.inNetBuffer.remaining());
        }
        SSLEngineResult.Status status = null;
        SSLEngineResult.HandshakeStatus handshakeStatus = null;
        SSLEngineResult res;
        do {
            res = this.sslEngine.unwrap(this.inNetBuffer.buf(), this.appBuffer.buf());
            status = res.getStatus();
            handshakeStatus = res.getHandshakeStatus();
            if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                final int newCapacity = this.sslEngine.getSession().getApplicationBufferSize();
                if (this.appBuffer.remaining() >= newCapacity) {
                    throw new SSLException("SSL buffer overflow");
                }
                this.appBuffer.expand(newCapacity);
            }
        } while ((status == SSLEngineResult.Status.OK || status == SSLEngineResult.Status.BUFFER_OVERFLOW) && (handshakeStatus == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING || handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP));
        return res;
    }
    
    private SSLEngineResult.HandshakeStatus doTasks() {
        Runnable runnable;
        while ((runnable = this.sslEngine.getDelegatedTask()) != null) {
            runnable.run();
        }
        return this.sslEngine.getHandshakeStatus();
    }
    
    static IoBuffer copy(final ByteBuffer src) {
        final IoBuffer copy = IoBuffer.allocate(src.remaining());
        copy.put(src);
        copy.flip();
        return copy;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SSLStatus <");
        if (this.handshakeComplete) {
            sb.append("SSL established");
        }
        else {
            sb.append("Processing Handshake").append("; ");
            sb.append("Status : ").append(this.handshakeStatus).append("; ");
        }
        sb.append(", ");
        sb.append("HandshakeComplete :").append(this.handshakeComplete).append(", ");
        sb.append(">");
        return sb.toString();
    }
    
    void release() {
        if (this.inNetBuffer != null) {
            this.inNetBuffer.free();
            this.inNetBuffer = null;
        }
        if (this.outNetBuffer != null) {
            this.outNetBuffer.free();
            this.outNetBuffer = null;
        }
    }
    
    static {
        LOGGER = LoggerFactory.getLogger(SslHandler.class);
    }
}
