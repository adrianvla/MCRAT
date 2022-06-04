// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.service;

import org.slf4j.LoggerFactory;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;

public class IoHandlerAdapter implements IoHandler
{
    private static final Logger LOGGER;
    
    @Override
    public void sessionCreated(final IoSession session) throws Exception {
    }
    
    @Override
    public void sessionOpened(final IoSession session) throws Exception {
    }
    
    @Override
    public void sessionClosed(final IoSession session) throws Exception {
    }
    
    @Override
    public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
    }
    
    @Override
    public void exceptionCaught(final IoSession session, final Throwable cause) throws Exception {
        if (IoHandlerAdapter.LOGGER.isWarnEnabled()) {
            IoHandlerAdapter.LOGGER.warn("EXCEPTION, please implement " + this.getClass().getName() + ".exceptionCaught() for proper handling:", cause);
        }
    }
    
    @Override
    public void messageReceived(final IoSession session, final Object message) throws Exception {
    }
    
    @Override
    public void messageSent(final IoSession session, final Object message) throws Exception {
    }
    
    @Override
    public void inputClosed(final IoSession session) throws Exception {
        session.closeNow();
    }
    
    static {
        LOGGER = LoggerFactory.getLogger(IoHandlerAdapter.class);
    }
}
