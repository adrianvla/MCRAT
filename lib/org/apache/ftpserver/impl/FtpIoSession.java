// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.write.WriteRequest;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.filter.ssl.SslFilter;
import java.security.cert.Certificate;
import java.util.UUID;
import org.apache.ftpserver.ftplet.DataType;
import org.apache.ftpserver.ftplet.Structure;
import org.apache.ftpserver.ftplet.FtpFile;
import org.slf4j.LoggerFactory;
import java.util.Date;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.FileSystemView;
import java.net.InetSocketAddress;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.service.IoService;
import java.net.SocketAddress;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.session.IoSessionConfig;
import java.util.Set;
import org.apache.mina.core.future.CloseFuture;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.mina.core.session.IoSession;

public class FtpIoSession implements IoSession
{
    public static final String ATTRIBUTE_PREFIX = "org.apache.ftpserver.";
    private static final String ATTRIBUTE_USER_ARGUMENT = "org.apache.ftpserver.user-argument";
    private static final String ATTRIBUTE_SESSION_ID = "org.apache.ftpserver.session-id";
    private static final String ATTRIBUTE_USER = "org.apache.ftpserver.user";
    private static final String ATTRIBUTE_LANGUAGE = "org.apache.ftpserver.language";
    private static final String ATTRIBUTE_LOGIN_TIME = "org.apache.ftpserver.login-time";
    private static final String ATTRIBUTE_DATA_CONNECTION = "org.apache.ftpserver.data-connection";
    private static final String ATTRIBUTE_FILE_SYSTEM = "org.apache.ftpserver.file-system";
    private static final String ATTRIBUTE_RENAME_FROM = "org.apache.ftpserver.rename-from";
    private static final String ATTRIBUTE_FILE_OFFSET = "org.apache.ftpserver.file-offset";
    private static final String ATTRIBUTE_DATA_TYPE = "org.apache.ftpserver.data-type";
    private static final String ATTRIBUTE_STRUCTURE = "org.apache.ftpserver.structure";
    private static final String ATTRIBUTE_FAILED_LOGINS = "org.apache.ftpserver.failed-logins";
    private static final String ATTRIBUTE_LISTENER = "org.apache.ftpserver.listener";
    private static final String ATTRIBUTE_MAX_IDLE_TIME = "org.apache.ftpserver.max-idle-time";
    private static final String ATTRIBUTE_LAST_ACCESS_TIME = "org.apache.ftpserver.last-access-time";
    private static final String ATTRIBUTE_CACHED_REMOTE_ADDRESS = "org.apache.ftpserver.cached-remote-address";
    private final IoSession wrappedSession;
    private final FtpServerContext context;
    private FtpReply lastReply;
    
    @Override
    public CloseFuture close() {
        return this.wrappedSession.close();
    }
    
    @Override
    public CloseFuture close(final boolean immediately) {
        return this.wrappedSession.close(immediately);
    }
    
    @Override
    public CloseFuture closeNow() {
        return this.wrappedSession.closeNow();
    }
    
    @Override
    public CloseFuture closeOnFlush() {
        return this.wrappedSession.closeOnFlush();
    }
    
    @Override
    public boolean containsAttribute(final Object key) {
        return this.wrappedSession.containsAttribute(key);
    }
    
    @Override
    public Object getAttachment() {
        return this.wrappedSession.getAttachment();
    }
    
    @Override
    public Object getAttribute(final Object key) {
        return this.wrappedSession.getAttribute(key);
    }
    
    @Override
    public Object getAttribute(final Object key, final Object defaultValue) {
        return this.wrappedSession.getAttribute(key, defaultValue);
    }
    
    @Override
    public Set<Object> getAttributeKeys() {
        return this.wrappedSession.getAttributeKeys();
    }
    
    @Override
    public int getBothIdleCount() {
        return this.wrappedSession.getBothIdleCount();
    }
    
    @Override
    public CloseFuture getCloseFuture() {
        return this.wrappedSession.getCloseFuture();
    }
    
    @Override
    public IoSessionConfig getConfig() {
        return this.wrappedSession.getConfig();
    }
    
    @Override
    public long getCreationTime() {
        return this.wrappedSession.getCreationTime();
    }
    
    @Override
    public IoFilterChain getFilterChain() {
        return this.wrappedSession.getFilterChain();
    }
    
    @Override
    public IoHandler getHandler() {
        return this.wrappedSession.getHandler();
    }
    
    @Override
    public long getId() {
        return this.wrappedSession.getId();
    }
    
    @Override
    public int getIdleCount(final IdleStatus status) {
        return this.wrappedSession.getIdleCount(status);
    }
    
    @Override
    public long getLastBothIdleTime() {
        return this.wrappedSession.getLastBothIdleTime();
    }
    
    @Override
    public long getLastIdleTime(final IdleStatus status) {
        return this.wrappedSession.getLastIdleTime(status);
    }
    
    @Override
    public long getLastIoTime() {
        return this.wrappedSession.getLastIoTime();
    }
    
    @Override
    public long getLastReadTime() {
        return this.wrappedSession.getLastReadTime();
    }
    
    @Override
    public long getLastReaderIdleTime() {
        return this.wrappedSession.getLastReaderIdleTime();
    }
    
    @Override
    public long getLastWriteTime() {
        return this.wrappedSession.getLastWriteTime();
    }
    
    @Override
    public long getLastWriterIdleTime() {
        return this.wrappedSession.getLastWriterIdleTime();
    }
    
    @Override
    public SocketAddress getLocalAddress() {
        return this.wrappedSession.getLocalAddress();
    }
    
    @Override
    public long getReadBytes() {
        return this.wrappedSession.getReadBytes();
    }
    
    @Override
    public double getReadBytesThroughput() {
        return this.wrappedSession.getReadBytesThroughput();
    }
    
    @Override
    public long getReadMessages() {
        return this.wrappedSession.getReadMessages();
    }
    
    @Override
    public double getReadMessagesThroughput() {
        return this.wrappedSession.getReadMessagesThroughput();
    }
    
    @Override
    public int getReaderIdleCount() {
        return this.wrappedSession.getReaderIdleCount();
    }
    
    @Override
    public SocketAddress getRemoteAddress() {
        final SocketAddress address = this.wrappedSession.getRemoteAddress();
        if (address == null && this.containsAttribute("org.apache.ftpserver.cached-remote-address")) {
            return (SocketAddress)this.getAttribute("org.apache.ftpserver.cached-remote-address");
        }
        this.setAttribute("org.apache.ftpserver.cached-remote-address", address);
        return address;
    }
    
    @Override
    public long getScheduledWriteBytes() {
        return this.wrappedSession.getScheduledWriteBytes();
    }
    
    @Override
    public int getScheduledWriteMessages() {
        return this.wrappedSession.getScheduledWriteMessages();
    }
    
    @Override
    public IoService getService() {
        return this.wrappedSession.getService();
    }
    
    @Override
    public SocketAddress getServiceAddress() {
        return this.wrappedSession.getServiceAddress();
    }
    
    @Override
    public TransportMetadata getTransportMetadata() {
        return this.wrappedSession.getTransportMetadata();
    }
    
    @Override
    public int getWriterIdleCount() {
        return this.wrappedSession.getWriterIdleCount();
    }
    
    @Override
    public long getWrittenBytes() {
        return this.wrappedSession.getWrittenBytes();
    }
    
    @Override
    public double getWrittenBytesThroughput() {
        return this.wrappedSession.getWrittenBytesThroughput();
    }
    
    @Override
    public long getWrittenMessages() {
        return this.wrappedSession.getWrittenMessages();
    }
    
    @Override
    public double getWrittenMessagesThroughput() {
        return this.wrappedSession.getWrittenMessagesThroughput();
    }
    
    @Override
    public boolean isClosing() {
        return this.wrappedSession.isClosing();
    }
    
    @Override
    public boolean isConnected() {
        return this.wrappedSession.isConnected();
    }
    
    @Override
    public boolean isActive() {
        return this.wrappedSession.isActive();
    }
    
    @Override
    public boolean isIdle(final IdleStatus status) {
        return this.wrappedSession.isIdle(status);
    }
    
    @Override
    public ReadFuture read() {
        return this.wrappedSession.read();
    }
    
    @Override
    public Object removeAttribute(final Object key) {
        return this.wrappedSession.removeAttribute(key);
    }
    
    @Override
    public boolean removeAttribute(final Object key, final Object value) {
        return this.wrappedSession.removeAttribute(key, value);
    }
    
    @Override
    public boolean replaceAttribute(final Object key, final Object oldValue, final Object newValue) {
        return this.wrappedSession.replaceAttribute(key, oldValue, newValue);
    }
    
    @Override
    public void resumeRead() {
        this.wrappedSession.resumeRead();
    }
    
    @Override
    public void resumeWrite() {
        this.wrappedSession.resumeWrite();
    }
    
    @Override
    public Object setAttachment(final Object attachment) {
        return this.wrappedSession.setAttachment(attachment);
    }
    
    @Override
    public Object setAttribute(final Object key) {
        return this.wrappedSession.setAttribute(key);
    }
    
    @Override
    public Object setAttribute(final Object key, final Object value) {
        return this.wrappedSession.setAttribute(key, value);
    }
    
    @Override
    public Object setAttributeIfAbsent(final Object key) {
        return this.wrappedSession.setAttributeIfAbsent(key);
    }
    
    @Override
    public Object setAttributeIfAbsent(final Object key, final Object value) {
        return this.wrappedSession.setAttributeIfAbsent(key, value);
    }
    
    @Override
    public void suspendRead() {
        this.wrappedSession.suspendRead();
    }
    
    @Override
    public void suspendWrite() {
        this.wrappedSession.suspendWrite();
    }
    
    @Override
    public WriteFuture write(final Object message) {
        final WriteFuture future = this.wrappedSession.write(message);
        this.lastReply = (FtpReply)message;
        return future;
    }
    
    @Override
    public WriteFuture write(final Object message, final SocketAddress destination) {
        final WriteFuture future = this.wrappedSession.write(message, destination);
        this.lastReply = (FtpReply)message;
        return future;
    }
    
    public void resetState() {
        this.removeAttribute("org.apache.ftpserver.rename-from");
        this.removeAttribute("org.apache.ftpserver.file-offset");
    }
    
    public synchronized ServerDataConnectionFactory getDataConnection() {
        if (this.containsAttribute("org.apache.ftpserver.data-connection")) {
            return (ServerDataConnectionFactory)this.getAttribute("org.apache.ftpserver.data-connection");
        }
        final IODataConnectionFactory dataCon = new IODataConnectionFactory(this.context, this);
        dataCon.setServerControlAddress(((InetSocketAddress)this.getLocalAddress()).getAddress());
        this.setAttribute("org.apache.ftpserver.data-connection", dataCon);
        return dataCon;
    }
    
    public FileSystemView getFileSystemView() {
        return (FileSystemView)this.getAttribute("org.apache.ftpserver.file-system");
    }
    
    public User getUser() {
        return (User)this.getAttribute("org.apache.ftpserver.user");
    }
    
    public boolean isLoggedIn() {
        return this.containsAttribute("org.apache.ftpserver.user");
    }
    
    public Listener getListener() {
        return (Listener)this.getAttribute("org.apache.ftpserver.listener");
    }
    
    public void setListener(final Listener listener) {
        this.setAttribute("org.apache.ftpserver.listener", listener);
    }
    
    public FtpSession getFtpletSession() {
        return new DefaultFtpSession(this);
    }
    
    public String getLanguage() {
        return (String)this.getAttribute("org.apache.ftpserver.language");
    }
    
    public void setLanguage(final String language) {
        this.setAttribute("org.apache.ftpserver.language", language);
    }
    
    public String getUserArgument() {
        return (String)this.getAttribute("org.apache.ftpserver.user-argument");
    }
    
    public void setUser(final User user) {
        this.setAttribute("org.apache.ftpserver.user", user);
    }
    
    public void setUserArgument(final String userArgument) {
        this.setAttribute("org.apache.ftpserver.user-argument", userArgument);
    }
    
    public int getMaxIdleTime() {
        return (int)this.getAttribute("org.apache.ftpserver.max-idle-time", 0);
    }
    
    public void setMaxIdleTime(final int maxIdleTime) {
        this.setAttribute("org.apache.ftpserver.max-idle-time", maxIdleTime);
        final int listenerTimeout = this.getListener().getIdleTimeout();
        if (listenerTimeout <= 0 || (maxIdleTime > 0 && maxIdleTime < listenerTimeout)) {
            this.wrappedSession.getConfig().setBothIdleTime(maxIdleTime);
        }
    }
    
    public synchronized void increaseFailedLogins() {
        int failedLogins = (int)this.getAttribute("org.apache.ftpserver.failed-logins", 0);
        ++failedLogins;
        this.setAttribute("org.apache.ftpserver.failed-logins", failedLogins);
    }
    
    public int getFailedLogins() {
        return (int)this.getAttribute("org.apache.ftpserver.failed-logins", 0);
    }
    
    public void setLogin(final FileSystemView fsview) {
        this.setAttribute("org.apache.ftpserver.login-time", new Date());
        this.setAttribute("org.apache.ftpserver.file-system", fsview);
    }
    
    public void reinitialize() {
        this.logoutUser();
        this.removeAttribute("org.apache.ftpserver.user");
        this.removeAttribute("org.apache.ftpserver.user-argument");
        this.removeAttribute("org.apache.ftpserver.login-time");
        this.removeAttribute("org.apache.ftpserver.file-system");
        this.removeAttribute("org.apache.ftpserver.rename-from");
        this.removeAttribute("org.apache.ftpserver.file-offset");
    }
    
    public void logoutUser() {
        final ServerFtpStatistics stats = (ServerFtpStatistics)this.context.getFtpStatistics();
        if (stats != null) {
            stats.setLogout(this);
            LoggerFactory.getLogger(this.getClass()).debug("Statistics login decreased due to user logout");
        }
        else {
            LoggerFactory.getLogger(this.getClass()).warn("Statistics not available in session, can not decrease login  count");
        }
    }
    
    public void setFileOffset(final long fileOffset) {
        this.setAttribute("org.apache.ftpserver.file-offset", fileOffset);
    }
    
    public void setRenameFrom(final FtpFile renFr) {
        this.setAttribute("org.apache.ftpserver.rename-from", renFr);
    }
    
    public FtpFile getRenameFrom() {
        return (FtpFile)this.getAttribute("org.apache.ftpserver.rename-from");
    }
    
    public long getFileOffset() {
        return (long)this.getAttribute("org.apache.ftpserver.file-offset", 0L);
    }
    
    public void setStructure(final Structure structure) {
        this.setAttribute("org.apache.ftpserver.structure", structure);
    }
    
    public void setDataType(final DataType dataType) {
        this.setAttribute("org.apache.ftpserver.data-type", dataType);
    }
    
    public UUID getSessionId() {
        synchronized (this.wrappedSession) {
            if (!this.wrappedSession.containsAttribute("org.apache.ftpserver.session-id")) {
                this.wrappedSession.setAttribute("org.apache.ftpserver.session-id", UUID.randomUUID());
            }
            return (UUID)this.wrappedSession.getAttribute("org.apache.ftpserver.session-id");
        }
    }
    
    public FtpIoSession(final IoSession wrappedSession, final FtpServerContext context) {
        this.lastReply = null;
        this.wrappedSession = wrappedSession;
        this.context = context;
    }
    
    public Structure getStructure() {
        return (Structure)this.getAttribute("org.apache.ftpserver.structure", Structure.FILE);
    }
    
    public DataType getDataType() {
        return (DataType)this.getAttribute("org.apache.ftpserver.data-type", DataType.ASCII);
    }
    
    public Date getLoginTime() {
        return (Date)this.getAttribute("org.apache.ftpserver.login-time");
    }
    
    public Date getLastAccessTime() {
        return (Date)this.getAttribute("org.apache.ftpserver.last-access-time");
    }
    
    public Certificate[] getClientCertificates() {
        if (this.getFilterChain().contains(SslFilter.class)) {
            final SslFilter sslFilter = (SslFilter)this.getFilterChain().get(SslFilter.class);
            final SSLSession sslSession = sslFilter.getSslSession(this);
            if (sslSession != null) {
                try {
                    return sslSession.getPeerCertificates();
                }
                catch (SSLPeerUnverifiedException ex) {}
            }
        }
        return null;
    }
    
    public void updateLastAccessTime() {
        this.setAttribute("org.apache.ftpserver.last-access-time", new Date());
    }
    
    @Override
    public Object getCurrentWriteMessage() {
        return this.wrappedSession.getCurrentWriteMessage();
    }
    
    @Override
    public WriteRequest getCurrentWriteRequest() {
        return this.wrappedSession.getCurrentWriteRequest();
    }
    
    @Override
    public boolean isBothIdle() {
        return this.wrappedSession.isBothIdle();
    }
    
    @Override
    public boolean isReaderIdle() {
        return this.wrappedSession.isReaderIdle();
    }
    
    @Override
    public boolean isWriterIdle() {
        return this.wrappedSession.isWriterIdle();
    }
    
    public boolean isSecure() {
        return this.getFilterChain().contains(SslFilter.class);
    }
    
    public void increaseWrittenDataBytes(final int increment) {
        if (this.wrappedSession instanceof AbstractIoSession) {
            ((AbstractIoSession)this.wrappedSession).increaseScheduledWriteBytes(increment);
            ((AbstractIoSession)this.wrappedSession).increaseWrittenBytes(increment, System.currentTimeMillis());
        }
    }
    
    public void increaseReadDataBytes(final int increment) {
        if (this.wrappedSession instanceof AbstractIoSession) {
            ((AbstractIoSession)this.wrappedSession).increaseReadBytes(increment, System.currentTimeMillis());
        }
    }
    
    public FtpReply getLastReply() {
        return this.lastReply;
    }
    
    @Override
    public WriteRequestQueue getWriteRequestQueue() {
        return this.wrappedSession.getWriteRequestQueue();
    }
    
    @Override
    public boolean isReadSuspended() {
        return this.wrappedSession.isReadSuspended();
    }
    
    @Override
    public boolean isWriteSuspended() {
        return this.wrappedSession.isWriteSuspended();
    }
    
    @Override
    public void setCurrentWriteRequest(final WriteRequest currentWriteRequest) {
        this.wrappedSession.setCurrentWriteRequest(currentWriteRequest);
    }
    
    @Override
    public void updateThroughput(final long currentTime, final boolean force) {
        this.wrappedSession.updateThroughput(currentTime, force);
    }
    
    @Override
    public boolean isSecured() {
        return this.getFilterChain().contains(SslFilter.class);
    }
}
