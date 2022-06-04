// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.filterchain;

import org.slf4j.LoggerFactory;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoEventType;
import org.slf4j.Logger;
import org.apache.mina.core.session.IoEvent;

public class IoFilterEvent extends IoEvent
{
    private static final Logger LOGGER;
    private static final boolean DEBUG;
    private final IoFilter.NextFilter nextFilter;
    
    public IoFilterEvent(final IoFilter.NextFilter nextFilter, final IoEventType type, final IoSession session, final Object parameter) {
        super(type, session, parameter);
        if (nextFilter == null) {
            throw new IllegalArgumentException("nextFilter must not be null");
        }
        this.nextFilter = nextFilter;
    }
    
    public IoFilter.NextFilter getNextFilter() {
        return this.nextFilter;
    }
    
    @Override
    public void fire() {
        final IoSession session = this.getSession();
        final IoFilter.NextFilter nextFilter = this.getNextFilter();
        final IoEventType type = this.getType();
        if (IoFilterEvent.DEBUG) {
            IoFilterEvent.LOGGER.debug("Firing a {} event for session {}", type, session.getId());
        }
        switch (type) {
            case MESSAGE_RECEIVED: {
                final Object parameter = this.getParameter();
                nextFilter.messageReceived(session, parameter);
                break;
            }
            case MESSAGE_SENT: {
                final WriteRequest writeRequest = (WriteRequest)this.getParameter();
                nextFilter.messageSent(session, writeRequest);
                break;
            }
            case WRITE: {
                final WriteRequest writeRequest = (WriteRequest)this.getParameter();
                nextFilter.filterWrite(session, writeRequest);
                break;
            }
            case CLOSE: {
                nextFilter.filterClose(session);
                break;
            }
            case EXCEPTION_CAUGHT: {
                final Throwable throwable = (Throwable)this.getParameter();
                nextFilter.exceptionCaught(session, throwable);
                break;
            }
            case SESSION_IDLE: {
                nextFilter.sessionIdle(session, (IdleStatus)this.getParameter());
                break;
            }
            case SESSION_OPENED: {
                nextFilter.sessionOpened(session);
                break;
            }
            case SESSION_CREATED: {
                nextFilter.sessionCreated(session);
                break;
            }
            case SESSION_CLOSED: {
                nextFilter.sessionClosed(session);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown event type: " + type);
            }
        }
        if (IoFilterEvent.DEBUG) {
            IoFilterEvent.LOGGER.debug("Event {} has been fired for session {}", type, session.getId());
        }
    }
    
    static {
        LOGGER = LoggerFactory.getLogger(IoFilterEvent.class);
        DEBUG = IoFilterEvent.LOGGER.isDebugEnabled();
    }
}
