// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.service;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public interface IoHandler
{
    void sessionCreated(final IoSession p0) throws Exception;
    
    void sessionOpened(final IoSession p0) throws Exception;
    
    void sessionClosed(final IoSession p0) throws Exception;
    
    void sessionIdle(final IoSession p0, final IdleStatus p1) throws Exception;
    
    void exceptionCaught(final IoSession p0, final Throwable p1) throws Exception;
    
    void messageReceived(final IoSession p0, final Object p1) throws Exception;
    
    void messageSent(final IoSession p0, final Object p1) throws Exception;
    
    void inputClosed(final IoSession p0) throws Exception;
}
