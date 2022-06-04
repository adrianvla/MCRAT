// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.logging;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.filterchain.IoFilter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.mina.core.filterchain.IoFilterAdapter;

public class LoggingFilter extends IoFilterAdapter
{
    private final String name;
    private final Logger logger;
    private LogLevel exceptionCaughtLevel;
    private LogLevel messageSentLevel;
    private LogLevel messageReceivedLevel;
    private LogLevel sessionCreatedLevel;
    private LogLevel sessionOpenedLevel;
    private LogLevel sessionIdleLevel;
    private LogLevel sessionClosedLevel;
    
    public LoggingFilter() {
        this(LoggingFilter.class.getName());
    }
    
    public LoggingFilter(final Class<?> clazz) {
        this(clazz.getName());
    }
    
    public LoggingFilter(final String name) {
        this.exceptionCaughtLevel = LogLevel.WARN;
        this.messageSentLevel = LogLevel.INFO;
        this.messageReceivedLevel = LogLevel.INFO;
        this.sessionCreatedLevel = LogLevel.INFO;
        this.sessionOpenedLevel = LogLevel.INFO;
        this.sessionIdleLevel = LogLevel.INFO;
        this.sessionClosedLevel = LogLevel.INFO;
        if (name == null) {
            this.name = LoggingFilter.class.getName();
        }
        else {
            this.name = name;
        }
        this.logger = LoggerFactory.getLogger(this.name);
    }
    
    public String getName() {
        return this.name;
    }
    
    private void log(final LogLevel eventLevel, final String message, final Throwable cause) {
        switch (eventLevel) {
            case TRACE: {
                this.logger.trace(message, cause);
            }
            case DEBUG: {
                this.logger.debug(message, cause);
            }
            case INFO: {
                this.logger.info(message, cause);
            }
            case WARN: {
                this.logger.warn(message, cause);
            }
            case ERROR: {
                this.logger.error(message, cause);
            }
            default: {}
        }
    }
    
    private void log(final LogLevel eventLevel, final String message, final Object param) {
        switch (eventLevel) {
            case TRACE: {
                this.logger.trace(message, param);
            }
            case DEBUG: {
                this.logger.debug(message, param);
            }
            case INFO: {
                this.logger.info(message, param);
            }
            case WARN: {
                this.logger.warn(message, param);
            }
            case ERROR: {
                this.logger.error(message, param);
            }
            default: {}
        }
    }
    
    private void log(final LogLevel eventLevel, final String message) {
        switch (eventLevel) {
            case TRACE: {
                this.logger.trace(message);
            }
            case DEBUG: {
                this.logger.debug(message);
            }
            case INFO: {
                this.logger.info(message);
            }
            case WARN: {
                this.logger.warn(message);
            }
            case ERROR: {
                this.logger.error(message);
            }
            default: {}
        }
    }
    
    @Override
    public void exceptionCaught(final IoFilter.NextFilter nextFilter, final IoSession session, final Throwable cause) throws Exception {
        this.log(this.exceptionCaughtLevel, "EXCEPTION :", cause);
        nextFilter.exceptionCaught(session, cause);
    }
    
    @Override
    public void messageReceived(final IoFilter.NextFilter nextFilter, final IoSession session, final Object message) throws Exception {
        this.log(this.messageReceivedLevel, "RECEIVED: {}", message);
        nextFilter.messageReceived(session, message);
    }
    
    @Override
    public void messageSent(final IoFilter.NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) throws Exception {
        this.log(this.messageSentLevel, "SENT: {}", writeRequest.getOriginalRequest().getMessage());
        nextFilter.messageSent(session, writeRequest);
    }
    
    @Override
    public void sessionCreated(final IoFilter.NextFilter nextFilter, final IoSession session) throws Exception {
        this.log(this.sessionCreatedLevel, "CREATED");
        nextFilter.sessionCreated(session);
    }
    
    @Override
    public void sessionOpened(final IoFilter.NextFilter nextFilter, final IoSession session) throws Exception {
        this.log(this.sessionOpenedLevel, "OPENED");
        nextFilter.sessionOpened(session);
    }
    
    @Override
    public void sessionIdle(final IoFilter.NextFilter nextFilter, final IoSession session, final IdleStatus status) throws Exception {
        this.log(this.sessionIdleLevel, "IDLE");
        nextFilter.sessionIdle(session, status);
    }
    
    @Override
    public void sessionClosed(final IoFilter.NextFilter nextFilter, final IoSession session) throws Exception {
        this.log(this.sessionClosedLevel, "CLOSED");
        nextFilter.sessionClosed(session);
    }
    
    public void setExceptionCaughtLogLevel(final LogLevel level) {
        this.exceptionCaughtLevel = level;
    }
    
    public LogLevel getExceptionCaughtLogLevel() {
        return this.exceptionCaughtLevel;
    }
    
    public void setMessageReceivedLogLevel(final LogLevel level) {
        this.messageReceivedLevel = level;
    }
    
    public LogLevel getMessageReceivedLogLevel() {
        return this.messageReceivedLevel;
    }
    
    public void setMessageSentLogLevel(final LogLevel level) {
        this.messageSentLevel = level;
    }
    
    public LogLevel getMessageSentLogLevel() {
        return this.messageSentLevel;
    }
    
    public void setSessionCreatedLogLevel(final LogLevel level) {
        this.sessionCreatedLevel = level;
    }
    
    public LogLevel getSessionCreatedLogLevel() {
        return this.sessionCreatedLevel;
    }
    
    public void setSessionOpenedLogLevel(final LogLevel level) {
        this.sessionOpenedLevel = level;
    }
    
    public LogLevel getSessionOpenedLogLevel() {
        return this.sessionOpenedLevel;
    }
    
    public void setSessionIdleLogLevel(final LogLevel level) {
        this.sessionIdleLevel = level;
    }
    
    public LogLevel getSessionIdleLogLevel() {
        return this.sessionIdleLevel;
    }
    
    public void setSessionClosedLogLevel(final LogLevel level) {
        this.sessionClosedLevel = level;
    }
    
    public LogLevel getSessionClosedLogLevel() {
        return this.sessionClosedLevel;
    }
}
