// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

import java.util.UUID;
import java.util.Date;
import java.security.cert.Certificate;
import java.net.InetSocketAddress;

public interface FtpSession
{
    InetSocketAddress getClientAddress();
    
    InetSocketAddress getServerAddress();
    
    DataConnectionFactory getDataConnection();
    
    Certificate[] getClientCertificates();
    
    Date getConnectionTime();
    
    Date getLoginTime();
    
    int getFailedLogins();
    
    Date getLastAccessTime();
    
    int getMaxIdleTime();
    
    void setMaxIdleTime(final int p0);
    
    User getUser();
    
    String getUserArgument();
    
    String getLanguage();
    
    boolean isLoggedIn();
    
    FileSystemView getFileSystemView();
    
    long getFileOffset();
    
    FtpFile getRenameFrom();
    
    DataType getDataType();
    
    Structure getStructure();
    
    Object getAttribute(final String p0);
    
    void setAttribute(final String p0, final Object p1);
    
    void removeAttribute(final String p0);
    
    void write(final FtpReply p0) throws FtpException;
    
    boolean isSecure();
    
    UUID getSessionId();
}
