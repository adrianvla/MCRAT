// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.service;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IdleStatus;
import java.util.EventListener;

public interface IoServiceListener extends EventListener
{
    void serviceActivated(final IoService p0) throws Exception;
    
    void serviceIdle(final IoService p0, final IdleStatus p1) throws Exception;
    
    void serviceDeactivated(final IoService p0) throws Exception;
    
    void sessionCreated(final IoSession p0) throws Exception;
    
    void sessionClosed(final IoSession p0) throws Exception;
    
    void sessionDestroyed(final IoSession p0) throws Exception;
}
