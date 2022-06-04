// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import java.util.UUID;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import java.security.cert.Certificate;
import org.apache.ftpserver.ftplet.Structure;
import org.apache.ftpserver.ftplet.DataType;
import java.net.InetSocketAddress;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.FtpFile;
import java.util.Date;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.DataConnectionFactory;
import org.apache.ftpserver.ftplet.FtpSession;

public class DefaultFtpSession implements FtpSession
{
    private final FtpIoSession ioSession;
    
    public DefaultFtpSession(final FtpIoSession ioSession) {
        this.ioSession = ioSession;
    }
    
    @Override
    public boolean isLoggedIn() {
        return this.ioSession.isLoggedIn();
    }
    
    @Override
    public DataConnectionFactory getDataConnection() {
        return this.ioSession.getDataConnection();
    }
    
    @Override
    public FileSystemView getFileSystemView() {
        return this.ioSession.getFileSystemView();
    }
    
    @Override
    public Date getConnectionTime() {
        return new Date(this.ioSession.getCreationTime());
    }
    
    @Override
    public Date getLoginTime() {
        return this.ioSession.getLoginTime();
    }
    
    @Override
    public Date getLastAccessTime() {
        return this.ioSession.getLastAccessTime();
    }
    
    @Override
    public long getFileOffset() {
        return this.ioSession.getFileOffset();
    }
    
    @Override
    public FtpFile getRenameFrom() {
        return this.ioSession.getRenameFrom();
    }
    
    @Override
    public String getUserArgument() {
        return this.ioSession.getUserArgument();
    }
    
    @Override
    public String getLanguage() {
        return this.ioSession.getLanguage();
    }
    
    @Override
    public User getUser() {
        return this.ioSession.getUser();
    }
    
    @Override
    public InetSocketAddress getClientAddress() {
        if (this.ioSession.getRemoteAddress() instanceof InetSocketAddress) {
            return (InetSocketAddress)this.ioSession.getRemoteAddress();
        }
        return null;
    }
    
    @Override
    public Object getAttribute(final String name) {
        if (name.startsWith("org.apache.ftpserver.")) {
            throw new IllegalArgumentException("Illegal lookup of internal attribute");
        }
        return this.ioSession.getAttribute(name);
    }
    
    @Override
    public void setAttribute(final String name, final Object value) {
        if (name.startsWith("org.apache.ftpserver.")) {
            throw new IllegalArgumentException("Illegal setting of internal attribute");
        }
        this.ioSession.setAttribute(name, value);
    }
    
    @Override
    public int getMaxIdleTime() {
        return this.ioSession.getMaxIdleTime();
    }
    
    @Override
    public void setMaxIdleTime(final int maxIdleTime) {
        this.ioSession.setMaxIdleTime(maxIdleTime);
    }
    
    @Override
    public DataType getDataType() {
        return this.ioSession.getDataType();
    }
    
    @Override
    public Structure getStructure() {
        return this.ioSession.getStructure();
    }
    
    @Override
    public Certificate[] getClientCertificates() {
        return this.ioSession.getClientCertificates();
    }
    
    @Override
    public InetSocketAddress getServerAddress() {
        if (this.ioSession.getLocalAddress() instanceof InetSocketAddress) {
            return (InetSocketAddress)this.ioSession.getLocalAddress();
        }
        return null;
    }
    
    @Override
    public int getFailedLogins() {
        return this.ioSession.getFailedLogins();
    }
    
    @Override
    public void removeAttribute(final String name) {
        if (name.startsWith("org.apache.ftpserver.")) {
            throw new IllegalArgumentException("Illegal removal of internal attribute");
        }
        this.ioSession.removeAttribute(name);
    }
    
    @Override
    public void write(final FtpReply reply) throws FtpException {
        this.ioSession.write(reply);
    }
    
    @Override
    public boolean isSecure() {
        return this.ioSession.isSecure();
    }
    
    public void increaseWrittenDataBytes(final int increment) {
        this.ioSession.increaseWrittenDataBytes(increment);
    }
    
    public void increaseReadDataBytes(final int increment) {
        this.ioSession.increaseReadDataBytes(increment);
    }
    
    @Override
    public UUID getSessionId() {
        return this.ioSession.getSessionId();
    }
}
