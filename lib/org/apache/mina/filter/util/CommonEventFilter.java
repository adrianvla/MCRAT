// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.util;

import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterEvent;
import org.apache.mina.core.filterchain.IoFilterAdapter;

public abstract class CommonEventFilter extends IoFilterAdapter
{
    protected abstract void filter(final IoFilterEvent p0) throws Exception;
    
    @Override
    public final void sessionCreated(final IoFilter.NextFilter nextFilter, final IoSession session) throws Exception {
        this.filter(new IoFilterEvent(nextFilter, IoEventType.SESSION_CREATED, session, null));
    }
    
    @Override
    public final void sessionOpened(final IoFilter.NextFilter nextFilter, final IoSession session) throws Exception {
        this.filter(new IoFilterEvent(nextFilter, IoEventType.SESSION_OPENED, session, null));
    }
    
    @Override
    public final void sessionClosed(final IoFilter.NextFilter nextFilter, final IoSession session) throws Exception {
        this.filter(new IoFilterEvent(nextFilter, IoEventType.SESSION_CLOSED, session, null));
    }
    
    @Override
    public final void sessionIdle(final IoFilter.NextFilter nextFilter, final IoSession session, final IdleStatus status) throws Exception {
        this.filter(new IoFilterEvent(nextFilter, IoEventType.SESSION_IDLE, session, status));
    }
    
    @Override
    public final void exceptionCaught(final IoFilter.NextFilter nextFilter, final IoSession session, final Throwable cause) throws Exception {
        this.filter(new IoFilterEvent(nextFilter, IoEventType.EXCEPTION_CAUGHT, session, cause));
    }
    
    @Override
    public final void messageReceived(final IoFilter.NextFilter nextFilter, final IoSession session, final Object message) throws Exception {
        this.filter(new IoFilterEvent(nextFilter, IoEventType.MESSAGE_RECEIVED, session, message));
    }
    
    @Override
    public final void messageSent(final IoFilter.NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) throws Exception {
        this.filter(new IoFilterEvent(nextFilter, IoEventType.MESSAGE_SENT, session, writeRequest));
    }
    
    @Override
    public final void filterWrite(final IoFilter.NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) throws Exception {
        this.filter(new IoFilterEvent(nextFilter, IoEventType.WRITE, session, writeRequest));
    }
    
    @Override
    public final void filterClose(final IoFilter.NextFilter nextFilter, final IoSession session) throws Exception {
        this.filter(new IoFilterEvent(nextFilter, IoEventType.CLOSE, session, null));
    }
}
