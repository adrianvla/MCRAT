// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.filterchain;

import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public interface IoFilter
{
    void init() throws Exception;
    
    void destroy() throws Exception;
    
    void onPreAdd(final IoFilterChain p0, final String p1, final NextFilter p2) throws Exception;
    
    void onPostAdd(final IoFilterChain p0, final String p1, final NextFilter p2) throws Exception;
    
    void onPreRemove(final IoFilterChain p0, final String p1, final NextFilter p2) throws Exception;
    
    void onPostRemove(final IoFilterChain p0, final String p1, final NextFilter p2) throws Exception;
    
    void sessionCreated(final NextFilter p0, final IoSession p1) throws Exception;
    
    void sessionOpened(final NextFilter p0, final IoSession p1) throws Exception;
    
    void sessionClosed(final NextFilter p0, final IoSession p1) throws Exception;
    
    void sessionIdle(final NextFilter p0, final IoSession p1, final IdleStatus p2) throws Exception;
    
    void exceptionCaught(final NextFilter p0, final IoSession p1, final Throwable p2) throws Exception;
    
    void inputClosed(final NextFilter p0, final IoSession p1) throws Exception;
    
    void messageReceived(final NextFilter p0, final IoSession p1, final Object p2) throws Exception;
    
    void messageSent(final NextFilter p0, final IoSession p1, final WriteRequest p2) throws Exception;
    
    void filterClose(final NextFilter p0, final IoSession p1) throws Exception;
    
    void filterWrite(final NextFilter p0, final IoSession p1, final WriteRequest p2) throws Exception;
    
    public interface NextFilter
    {
        void sessionCreated(final IoSession p0);
        
        void sessionOpened(final IoSession p0);
        
        void sessionClosed(final IoSession p0);
        
        void sessionIdle(final IoSession p0, final IdleStatus p1);
        
        void exceptionCaught(final IoSession p0, final Throwable p1);
        
        void inputClosed(final IoSession p0);
        
        void messageReceived(final IoSession p0, final Object p1);
        
        void messageSent(final IoSession p0, final WriteRequest p1);
        
        void filterWrite(final IoSession p0, final WriteRequest p1);
        
        void filterClose(final IoSession p0);
    }
}
