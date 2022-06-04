// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.filterchain;

import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class IoFilterAdapter implements IoFilter
{
    @Override
    public void init() throws Exception {
    }
    
    @Override
    public void destroy() throws Exception {
    }
    
    @Override
    public void onPreAdd(final IoFilterChain parent, final String name, final NextFilter nextFilter) throws Exception {
    }
    
    @Override
    public void onPostAdd(final IoFilterChain parent, final String name, final NextFilter nextFilter) throws Exception {
    }
    
    @Override
    public void onPreRemove(final IoFilterChain parent, final String name, final NextFilter nextFilter) throws Exception {
    }
    
    @Override
    public void onPostRemove(final IoFilterChain parent, final String name, final NextFilter nextFilter) throws Exception {
    }
    
    @Override
    public void sessionCreated(final NextFilter nextFilter, final IoSession session) throws Exception {
        nextFilter.sessionCreated(session);
    }
    
    @Override
    public void sessionOpened(final NextFilter nextFilter, final IoSession session) throws Exception {
        nextFilter.sessionOpened(session);
    }
    
    @Override
    public void sessionClosed(final NextFilter nextFilter, final IoSession session) throws Exception {
        nextFilter.sessionClosed(session);
    }
    
    @Override
    public void sessionIdle(final NextFilter nextFilter, final IoSession session, final IdleStatus status) throws Exception {
        nextFilter.sessionIdle(session, status);
    }
    
    @Override
    public void exceptionCaught(final NextFilter nextFilter, final IoSession session, final Throwable cause) throws Exception {
        nextFilter.exceptionCaught(session, cause);
    }
    
    @Override
    public void messageReceived(final NextFilter nextFilter, final IoSession session, final Object message) throws Exception {
        nextFilter.messageReceived(session, message);
    }
    
    @Override
    public void messageSent(final NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) throws Exception {
        nextFilter.messageSent(session, writeRequest);
    }
    
    @Override
    public void filterWrite(final NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) throws Exception {
        nextFilter.filterWrite(session, writeRequest);
    }
    
    @Override
    public void filterClose(final NextFilter nextFilter, final IoSession session) throws Exception {
        nextFilter.filterClose(session);
    }
    
    @Override
    public void inputClosed(final NextFilter nextFilter, final IoSession session) throws Exception {
        nextFilter.inputClosed(session);
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
