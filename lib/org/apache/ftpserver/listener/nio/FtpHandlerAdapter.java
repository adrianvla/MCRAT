// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.listener.nio;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.DefaultFtpRequest;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.mina.core.session.IoSession;
import org.apache.ftpserver.impl.FtpHandler;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.mina.core.service.IoHandlerAdapter;

public class FtpHandlerAdapter extends IoHandlerAdapter
{
    private final FtpServerContext context;
    private FtpHandler ftpHandler;
    
    public FtpHandlerAdapter(final FtpServerContext context, final FtpHandler ftpHandler) {
        this.context = context;
        this.ftpHandler = ftpHandler;
    }
    
    @Override
    public void exceptionCaught(final IoSession session, final Throwable cause) throws Exception {
        final FtpIoSession ftpSession = new FtpIoSession(session, this.context);
        this.ftpHandler.exceptionCaught(ftpSession, cause);
    }
    
    @Override
    public void messageReceived(final IoSession session, final Object message) throws Exception {
        final FtpIoSession ftpSession = new FtpIoSession(session, this.context);
        final FtpRequest request = new DefaultFtpRequest(message.toString());
        this.ftpHandler.messageReceived(ftpSession, request);
    }
    
    @Override
    public void messageSent(final IoSession session, final Object message) throws Exception {
        final FtpIoSession ftpSession = new FtpIoSession(session, this.context);
        this.ftpHandler.messageSent(ftpSession, (FtpReply)message);
    }
    
    @Override
    public void sessionClosed(final IoSession session) throws Exception {
        final FtpIoSession ftpSession = new FtpIoSession(session, this.context);
        this.ftpHandler.sessionClosed(ftpSession);
    }
    
    @Override
    public void sessionCreated(final IoSession session) throws Exception {
        final FtpIoSession ftpSession = new FtpIoSession(session, this.context);
        MdcInjectionFilter.setProperty(session, "session", ftpSession.getSessionId().toString());
        this.ftpHandler.sessionCreated(ftpSession);
    }
    
    @Override
    public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
        final FtpIoSession ftpSession = new FtpIoSession(session, this.context);
        this.ftpHandler.sessionIdle(ftpSession, status);
    }
    
    @Override
    public void sessionOpened(final IoSession session) throws Exception {
        final FtpIoSession ftpSession = new FtpIoSession(session, this.context);
        this.ftpHandler.sessionOpened(ftpSession);
    }
    
    public FtpHandler getFtpHandler() {
        return this.ftpHandler;
    }
    
    public void setFtpHandler(final FtpHandler handler) {
        this.ftpHandler = handler;
    }
}
