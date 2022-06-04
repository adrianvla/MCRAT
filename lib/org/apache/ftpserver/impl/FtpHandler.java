// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.mina.core.session.IdleStatus;
import org.apache.ftpserver.listener.Listener;

public interface FtpHandler
{
    void init(final FtpServerContext p0, final Listener p1);
    
    void sessionCreated(final FtpIoSession p0) throws Exception;
    
    void sessionOpened(final FtpIoSession p0) throws Exception;
    
    void sessionClosed(final FtpIoSession p0) throws Exception;
    
    void sessionIdle(final FtpIoSession p0, final IdleStatus p1) throws Exception;
    
    void exceptionCaught(final FtpIoSession p0, final Throwable p1) throws Exception;
    
    void messageReceived(final FtpIoSession p0, final FtpRequest p1) throws Exception;
    
    void messageSent(final FtpIoSession p0, final FtpReply p1) throws Exception;
}
