// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.ssl;

import org.apache.mina.core.write.WriteRequestWrapper;
import org.slf4j.LoggerFactory;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import org.apache.mina.core.write.WriteToClosedSessionException;
import org.apache.mina.core.write.WriteRequest;
import javax.net.ssl.SSLHandshakeException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoAcceptor;
import javax.net.ssl.SSLException;
import org.apache.mina.core.filterchain.IoFilter;
import javax.net.ssl.SSLSession;
import org.apache.mina.core.session.IoSession;
import javax.net.ssl.SSLContext;
import org.apache.mina.core.session.AttributeKey;
import org.slf4j.Logger;
import org.apache.mina.core.filterchain.IoFilterAdapter;

public class SslFilter extends IoFilterAdapter
{
    private static final Logger LOGGER;
    public static final AttributeKey SSL_SESSION;
    public static final AttributeKey DISABLE_ENCRYPTION_ONCE;
    public static final AttributeKey USE_NOTIFICATION;
    public static final AttributeKey PEER_ADDRESS;
    public static final SslFilterMessage SESSION_SECURED;
    public static final SslFilterMessage SESSION_UNSECURED;
    private static final AttributeKey NEXT_FILTER;
    private static final AttributeKey SSL_HANDLER;
    final SSLContext sslContext;
    private final boolean autoStart;
    private static final boolean START_HANDSHAKE = true;
    private boolean client;
    private boolean needClientAuth;
    private boolean wantClientAuth;
    private String[] enabledCipherSuites;
    private String[] enabledProtocols;
    
    public SslFilter(final SSLContext sslContext) {
        this(sslContext, true);
    }
    
    public SslFilter(final SSLContext sslContext, final boolean autoStart) {
        if (sslContext == null) {
            throw new IllegalArgumentException("sslContext");
        }
        this.sslContext = sslContext;
        this.autoStart = autoStart;
    }
    
    public SSLSession getSslSession(final IoSession session) {
        return (SSLSession)session.getAttribute(SslFilter.SSL_SESSION);
    }
    
    public boolean startSsl(final IoSession session) throws SSLException {
        final SslHandler sslHandler = this.getSslSessionHandler(session);
        boolean started;
        try {
            synchronized (sslHandler) {
                if (sslHandler.isOutboundDone()) {
                    final IoFilter.NextFilter nextFilter = (IoFilter.NextFilter)session.getAttribute(SslFilter.NEXT_FILTER);
                    sslHandler.destroy();
                    sslHandler.init();
                    sslHandler.handshake(nextFilter);
                    started = true;
                }
                else {
                    started = false;
                }
            }
            sslHandler.flushScheduledEvents();
        }
        catch (SSLException se) {
            sslHandler.release();
            throw se;
        }
        return started;
    }
    
    String getSessionInfo(final IoSession session) {
        final StringBuilder sb = new StringBuilder();
        if (session.getService() instanceof IoAcceptor) {
            sb.append("Session Server");
        }
        else {
            sb.append("Session Client");
        }
        sb.append('[').append(session.getId()).append(']');
        final SslHandler sslHandler = (SslHandler)session.getAttribute(SslFilter.SSL_HANDLER);
        if (sslHandler == null) {
            sb.append("(no sslEngine)");
        }
        else if (this.isSslStarted(session)) {
            if (sslHandler.isHandshakeComplete()) {
                sb.append("(SSL)");
            }
            else {
                sb.append("(ssl...)");
            }
        }
        return sb.toString();
    }
    
    public boolean isSslStarted(final IoSession session) {
        final SslHandler sslHandler = (SslHandler)session.getAttribute(SslFilter.SSL_HANDLER);
        if (sslHandler == null) {
            return false;
        }
        synchronized (sslHandler) {
            return !sslHandler.isOutboundDone();
        }
    }
    
    public WriteFuture stopSsl(final IoSession session) throws SSLException {
        final SslHandler sslHandler = this.getSslSessionHandler(session);
        final IoFilter.NextFilter nextFilter = (IoFilter.NextFilter)session.getAttribute(SslFilter.NEXT_FILTER);
        WriteFuture future;
        try {
            synchronized (sslHandler) {
                future = this.initiateClosure(nextFilter, session);
            }
            sslHandler.flushScheduledEvents();
        }
        catch (SSLException se) {
            sslHandler.release();
            throw se;
        }
        return future;
    }
    
    public boolean isUseClientMode() {
        return this.client;
    }
    
    public void setUseClientMode(final boolean clientMode) {
        this.client = clientMode;
    }
    
    public boolean isNeedClientAuth() {
        return this.needClientAuth;
    }
    
    public void setNeedClientAuth(final boolean needClientAuth) {
        this.needClientAuth = needClientAuth;
    }
    
    public boolean isWantClientAuth() {
        return this.wantClientAuth;
    }
    
    public void setWantClientAuth(final boolean wantClientAuth) {
        this.wantClientAuth = wantClientAuth;
    }
    
    public String[] getEnabledCipherSuites() {
        return this.enabledCipherSuites;
    }
    
    public void setEnabledCipherSuites(final String[] cipherSuites) {
        this.enabledCipherSuites = cipherSuites;
    }
    
    public String[] getEnabledProtocols() {
        return this.enabledProtocols;
    }
    
    public void setEnabledProtocols(final String[] protocols) {
        this.enabledProtocols = protocols;
    }
    
    @Override
    public void onPreAdd(final IoFilterChain parent, final String name, final IoFilter.NextFilter nextFilter) throws SSLException {
        if (parent.contains(SslFilter.class)) {
            final String msg = "Only one SSL filter is permitted in a chain.";
            SslFilter.LOGGER.error(msg);
            throw new IllegalStateException(msg);
        }
        SslFilter.LOGGER.debug("Adding the SSL Filter {} to the chain", name);
        final IoSession session = parent.getSession();
        session.setAttribute(SslFilter.NEXT_FILTER, nextFilter);
        final SslHandler sslHandler = new SslHandler(this, session);
        if (this.enabledCipherSuites == null || this.enabledCipherSuites.length == 0) {
            this.enabledCipherSuites = this.sslContext.getServerSocketFactory().getSupportedCipherSuites();
        }
        sslHandler.init();
        session.setAttribute(SslFilter.SSL_HANDLER, sslHandler);
    }
    
    @Override
    public void onPostAdd(final IoFilterChain parent, final String name, final IoFilter.NextFilter nextFilter) throws SSLException {
        if (this.autoStart) {
            this.initiateHandshake(nextFilter, parent.getSession());
        }
    }
    
    @Override
    public void onPreRemove(final IoFilterChain parent, final String name, final IoFilter.NextFilter nextFilter) throws SSLException {
        final IoSession session = parent.getSession();
        this.stopSsl(session);
        session.removeAttribute(SslFilter.NEXT_FILTER);
        session.removeAttribute(SslFilter.SSL_HANDLER);
    }
    
    @Override
    public void sessionClosed(final IoFilter.NextFilter nextFilter, final IoSession session) throws SSLException {
        final SslHandler sslHandler = this.getSslSessionHandler(session);
        try {
            synchronized (sslHandler) {
                sslHandler.destroy();
            }
        }
        finally {
            nextFilter.sessionClosed(session);
        }
    }
    
    @Override
    public void messageReceived(final IoFilter.NextFilter nextFilter, final IoSession session, final Object message) throws SSLException {
        if (SslFilter.LOGGER.isDebugEnabled()) {
            SslFilter.LOGGER.debug("{}: Message received : {}", this.getSessionInfo(session), message);
        }
        final SslHandler sslHandler = this.getSslSessionHandler(session);
        synchronized (sslHandler) {
            if (!this.isSslStarted(session) && sslHandler.isInboundDone()) {
                sslHandler.scheduleMessageReceived(nextFilter, message);
            }
            else {
                final IoBuffer buf = (IoBuffer)message;
                try {
                    sslHandler.messageReceived(nextFilter, buf.buf());
                    this.handleSslData(nextFilter, sslHandler);
                    if (sslHandler.isInboundDone()) {
                        if (sslHandler.isOutboundDone()) {
                            sslHandler.destroy();
                        }
                        else {
                            this.initiateClosure(nextFilter, session);
                        }
                        if (buf.hasRemaining()) {
                            sslHandler.scheduleMessageReceived(nextFilter, buf);
                        }
                    }
                }
                catch (SSLException ssle) {
                    if (!sslHandler.isHandshakeComplete()) {
                        final SSLException newSsle = new SSLHandshakeException("SSL handshake failed.");
                        newSsle.initCause(ssle);
                        ssle = newSsle;
                        session.closeNow();
                    }
                    else {
                        sslHandler.release();
                    }
                    throw ssle;
                }
            }
        }
        sslHandler.flushScheduledEvents();
    }
    
    @Override
    public void messageSent(final IoFilter.NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) {
        if (writeRequest instanceof EncryptedWriteRequest) {
            final EncryptedWriteRequest wrappedRequest = (EncryptedWriteRequest)writeRequest;
            nextFilter.messageSent(session, wrappedRequest.getParentRequest());
        }
    }
    
    @Override
    public void exceptionCaught(final IoFilter.NextFilter nextFilter, final IoSession session, Throwable cause) throws Exception {
        if (cause instanceof WriteToClosedSessionException) {
            final WriteToClosedSessionException e = (WriteToClosedSessionException)cause;
            final List<WriteRequest> failedRequests = e.getRequests();
            boolean containsCloseNotify = false;
            for (final WriteRequest r : failedRequests) {
                if (this.isCloseNotify(r.getMessage())) {
                    containsCloseNotify = true;
                    break;
                }
            }
            if (containsCloseNotify) {
                if (failedRequests.size() == 1) {
                    return;
                }
                final List<WriteRequest> newFailedRequests = new ArrayList<WriteRequest>(failedRequests.size() - 1);
                for (final WriteRequest r2 : failedRequests) {
                    if (!this.isCloseNotify(r2.getMessage())) {
                        newFailedRequests.add(r2);
                    }
                }
                if (newFailedRequests.isEmpty()) {
                    return;
                }
                cause = new WriteToClosedSessionException(newFailedRequests, cause.getMessage(), cause.getCause());
            }
        }
        nextFilter.exceptionCaught(session, cause);
    }
    
    private boolean isCloseNotify(final Object message) {
        if (!(message instanceof IoBuffer)) {
            return false;
        }
        final IoBuffer buf = (IoBuffer)message;
        final int offset = buf.position();
        return buf.get(offset + 0) == 21 && buf.get(offset + 1) == 3 && (buf.get(offset + 2) == 0 || buf.get(offset + 2) == 1 || buf.get(offset + 2) == 2 || buf.get(offset + 2) == 3) && buf.get(offset + 3) == 0;
    }
    
    @Override
    public void filterWrite(final IoFilter.NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) throws SSLException {
        if (SslFilter.LOGGER.isDebugEnabled()) {
            SslFilter.LOGGER.debug("{}: Writing Message : {}", this.getSessionInfo(session), writeRequest);
        }
        boolean needsFlush = true;
        final SslHandler sslHandler = this.getSslSessionHandler(session);
        try {
            synchronized (sslHandler) {
                if (!this.isSslStarted(session)) {
                    sslHandler.scheduleFilterWrite(nextFilter, writeRequest);
                }
                else if (session.containsAttribute(SslFilter.DISABLE_ENCRYPTION_ONCE)) {
                    session.removeAttribute(SslFilter.DISABLE_ENCRYPTION_ONCE);
                    sslHandler.scheduleFilterWrite(nextFilter, writeRequest);
                }
                else {
                    final IoBuffer buf = (IoBuffer)writeRequest.getMessage();
                    if (sslHandler.isWritingEncryptedData()) {
                        sslHandler.scheduleFilterWrite(nextFilter, writeRequest);
                    }
                    else if (sslHandler.isHandshakeComplete()) {
                        buf.mark();
                        sslHandler.encrypt(buf.buf());
                        final IoBuffer encryptedBuffer = sslHandler.fetchOutNetBuffer();
                        sslHandler.scheduleFilterWrite(nextFilter, new EncryptedWriteRequest(writeRequest, encryptedBuffer));
                    }
                    else {
                        if (session.isConnected()) {
                            sslHandler.schedulePreHandshakeWriteRequest(nextFilter, writeRequest);
                        }
                        needsFlush = false;
                    }
                }
            }
            if (needsFlush) {
                sslHandler.flushScheduledEvents();
            }
        }
        catch (SSLException se) {
            sslHandler.release();
            throw se;
        }
    }
    
    @Override
    public void filterClose(final IoFilter.NextFilter nextFilter, final IoSession session) throws SSLException {
        final SslHandler sslHandler = (SslHandler)session.getAttribute(SslFilter.SSL_HANDLER);
        if (sslHandler == null) {
            nextFilter.filterClose(session);
            return;
        }
        WriteFuture future = null;
        try {
            synchronized (sslHandler) {
                if (this.isSslStarted(session)) {
                    future = this.initiateClosure(nextFilter, session);
                    future.addListener((IoFutureListener<?>)new IoFutureListener<IoFuture>() {
                        @Override
                        public void operationComplete(final IoFuture future) {
                            nextFilter.filterClose(session);
                        }
                    });
                }
            }
            sslHandler.flushScheduledEvents();
        }
        catch (SSLException se) {
            sslHandler.release();
            throw se;
        }
        finally {
            if (future == null) {
                nextFilter.filterClose(session);
            }
        }
    }
    
    public void initiateHandshake(final IoSession session) throws SSLException {
        final IoFilterChain filterChain = session.getFilterChain();
        if (filterChain == null) {
            throw new SSLException("No filter chain");
        }
        final IoFilter.NextFilter nextFilter = filterChain.getNextFilter(SslFilter.class);
        if (nextFilter == null) {
            throw new SSLException("No SSL next filter in the chain");
        }
        this.initiateHandshake(nextFilter, session);
    }
    
    private void initiateHandshake(final IoFilter.NextFilter nextFilter, final IoSession session) throws SSLException {
        SslFilter.LOGGER.debug("{} : Starting the first handshake", this.getSessionInfo(session));
        final SslHandler sslHandler = this.getSslSessionHandler(session);
        try {
            synchronized (sslHandler) {
                sslHandler.handshake(nextFilter);
            }
            sslHandler.flushScheduledEvents();
        }
        catch (SSLException se) {
            sslHandler.release();
            throw se;
        }
    }
    
    private WriteFuture initiateClosure(final IoFilter.NextFilter nextFilter, final IoSession session) throws SSLException {
        final SslHandler sslHandler = this.getSslSessionHandler(session);
        WriteFuture future = null;
        try {
            if (!sslHandler.closeOutbound()) {
                return DefaultWriteFuture.newNotWrittenFuture(session, new IllegalStateException("SSL session is shut down already."));
            }
            future = sslHandler.writeNetBuffer(nextFilter);
            if (future == null) {
                future = DefaultWriteFuture.newWrittenFuture(session);
            }
            if (sslHandler.isInboundDone()) {
                sslHandler.destroy();
            }
            if (session.containsAttribute(SslFilter.USE_NOTIFICATION)) {
                sslHandler.scheduleMessageReceived(nextFilter, SslFilter.SESSION_UNSECURED);
            }
        }
        catch (SSLException se) {
            sslHandler.release();
            throw se;
        }
        return future;
    }
    
    private void handleSslData(final IoFilter.NextFilter nextFilter, final SslHandler sslHandler) throws SSLException {
        if (SslFilter.LOGGER.isDebugEnabled()) {
            SslFilter.LOGGER.debug("{}: Processing the SSL Data ", this.getSessionInfo(sslHandler.getSession()));
        }
        if (sslHandler.isHandshakeComplete()) {
            sslHandler.flushPreHandshakeEvents();
        }
        sslHandler.writeNetBuffer(nextFilter);
        this.handleAppDataRead(nextFilter, sslHandler);
    }
    
    private void handleAppDataRead(final IoFilter.NextFilter nextFilter, final SslHandler sslHandler) {
        final IoBuffer readBuffer = sslHandler.fetchAppBuffer();
        if (readBuffer.hasRemaining()) {
            sslHandler.scheduleMessageReceived(nextFilter, readBuffer);
        }
    }
    
    private SslHandler getSslSessionHandler(final IoSession session) {
        final SslHandler sslHandler = (SslHandler)session.getAttribute(SslFilter.SSL_HANDLER);
        if (sslHandler == null) {
            throw new IllegalStateException();
        }
        if (sslHandler.getSslFilter() != this) {
            throw new IllegalArgumentException("Not managed by this filter.");
        }
        return sslHandler;
    }
    
    static {
        LOGGER = LoggerFactory.getLogger(SslFilter.class);
        SSL_SESSION = new AttributeKey(SslFilter.class, "session");
        DISABLE_ENCRYPTION_ONCE = new AttributeKey(SslFilter.class, "disableOnce");
        USE_NOTIFICATION = new AttributeKey(SslFilter.class, "useNotification");
        PEER_ADDRESS = new AttributeKey(SslFilter.class, "peerAddress");
        SESSION_SECURED = new SslFilterMessage("SESSION_SECURED");
        SESSION_UNSECURED = new SslFilterMessage("SESSION_UNSECURED");
        NEXT_FILTER = new AttributeKey(SslFilter.class, "nextFilter");
        SSL_HANDLER = new AttributeKey(SslFilter.class, "handler");
    }
    
    public static class SslFilterMessage
    {
        private final String name;
        
        private SslFilterMessage(final String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return this.name;
        }
    }
    
    private static class EncryptedWriteRequest extends WriteRequestWrapper
    {
        private final IoBuffer encryptedMessage;
        
        private EncryptedWriteRequest(final WriteRequest writeRequest, final IoBuffer encryptedMessage) {
            super(writeRequest);
            this.encryptedMessage = encryptedMessage;
        }
        
        @Override
        public Object getMessage() {
            return this.encryptedMessage;
        }
    }
}
