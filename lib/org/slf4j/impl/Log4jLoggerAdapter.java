// 
// Decompiled by Procyon v0.5.36
// 

package org.slf4j.impl;

import java.util.Map;
import org.apache.log4j.Category;
import org.apache.log4j.spi.ThrowableInformation;
import org.apache.log4j.spi.LocationInfo;
import org.slf4j.event.LoggingEvent;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.apache.log4j.Priority;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import java.io.Serializable;
import org.slf4j.spi.LocationAwareLogger;
import org.slf4j.helpers.MarkerIgnoringBase;

public final class Log4jLoggerAdapter extends MarkerIgnoringBase implements LocationAwareLogger, Serializable
{
    private static final long serialVersionUID = 6182834493563598289L;
    final transient org.apache.log4j.Logger logger;
    static final String FQCN;
    final boolean traceCapable;
    
    Log4jLoggerAdapter(final org.apache.log4j.Logger logger) {
        this.logger = logger;
        this.name = logger.getName();
        this.traceCapable = this.isTraceCapable();
    }
    
    private boolean isTraceCapable() {
        try {
            this.logger.isTraceEnabled();
            return true;
        }
        catch (NoSuchMethodError e) {
            return false;
        }
    }
    
    public boolean isTraceEnabled() {
        if (this.traceCapable) {
            return this.logger.isTraceEnabled();
        }
        return this.logger.isDebugEnabled();
    }
    
    public void trace(final String msg) {
        this.logger.log(Log4jLoggerAdapter.FQCN, this.traceCapable ? Level.TRACE : Level.DEBUG, msg, null);
    }
    
    public void trace(final String format, final Object arg) {
        if (this.isTraceEnabled()) {
            final FormattingTuple ft = MessageFormatter.format(format, arg);
            this.logger.log(Log4jLoggerAdapter.FQCN, this.traceCapable ? Level.TRACE : Level.DEBUG, ft.getMessage(), ft.getThrowable());
        }
    }
    
    public void trace(final String format, final Object arg1, final Object arg2) {
        if (this.isTraceEnabled()) {
            final FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            this.logger.log(Log4jLoggerAdapter.FQCN, this.traceCapable ? Level.TRACE : Level.DEBUG, ft.getMessage(), ft.getThrowable());
        }
    }
    
    public void trace(final String format, final Object... arguments) {
        if (this.isTraceEnabled()) {
            final FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            this.logger.log(Log4jLoggerAdapter.FQCN, this.traceCapable ? Level.TRACE : Level.DEBUG, ft.getMessage(), ft.getThrowable());
        }
    }
    
    public void trace(final String msg, final Throwable t) {
        this.logger.log(Log4jLoggerAdapter.FQCN, this.traceCapable ? Level.TRACE : Level.DEBUG, msg, t);
    }
    
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }
    
    public void debug(final String msg) {
        this.logger.log(Log4jLoggerAdapter.FQCN, Level.DEBUG, msg, null);
    }
    
    public void debug(final String format, final Object arg) {
        if (this.logger.isDebugEnabled()) {
            final FormattingTuple ft = MessageFormatter.format(format, arg);
            this.logger.log(Log4jLoggerAdapter.FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
        }
    }
    
    public void debug(final String format, final Object arg1, final Object arg2) {
        if (this.logger.isDebugEnabled()) {
            final FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            this.logger.log(Log4jLoggerAdapter.FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
        }
    }
    
    public void debug(final String format, final Object... arguments) {
        if (this.logger.isDebugEnabled()) {
            final FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            this.logger.log(Log4jLoggerAdapter.FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
        }
    }
    
    public void debug(final String msg, final Throwable t) {
        this.logger.log(Log4jLoggerAdapter.FQCN, Level.DEBUG, msg, t);
    }
    
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }
    
    public void info(final String msg) {
        this.logger.log(Log4jLoggerAdapter.FQCN, Level.INFO, msg, null);
    }
    
    public void info(final String format, final Object arg) {
        if (this.logger.isInfoEnabled()) {
            final FormattingTuple ft = MessageFormatter.format(format, arg);
            this.logger.log(Log4jLoggerAdapter.FQCN, Level.INFO, ft.getMessage(), ft.getThrowable());
        }
    }
    
    public void info(final String format, final Object arg1, final Object arg2) {
        if (this.logger.isInfoEnabled()) {
            final FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            this.logger.log(Log4jLoggerAdapter.FQCN, Level.INFO, ft.getMessage(), ft.getThrowable());
        }
    }
    
    public void info(final String format, final Object... argArray) {
        if (this.logger.isInfoEnabled()) {
            final FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
            this.logger.log(Log4jLoggerAdapter.FQCN, Level.INFO, ft.getMessage(), ft.getThrowable());
        }
    }
    
    public void info(final String msg, final Throwable t) {
        this.logger.log(Log4jLoggerAdapter.FQCN, Level.INFO, msg, t);
    }
    
    public boolean isWarnEnabled() {
        return this.logger.isEnabledFor(Level.WARN);
    }
    
    public void warn(final String msg) {
        this.logger.log(Log4jLoggerAdapter.FQCN, Level.WARN, msg, null);
    }
    
    public void warn(final String format, final Object arg) {
        if (this.logger.isEnabledFor(Level.WARN)) {
            final FormattingTuple ft = MessageFormatter.format(format, arg);
            this.logger.log(Log4jLoggerAdapter.FQCN, Level.WARN, ft.getMessage(), ft.getThrowable());
        }
    }
    
    public void warn(final String format, final Object arg1, final Object arg2) {
        if (this.logger.isEnabledFor(Level.WARN)) {
            final FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            this.logger.log(Log4jLoggerAdapter.FQCN, Level.WARN, ft.getMessage(), ft.getThrowable());
        }
    }
    
    public void warn(final String format, final Object... argArray) {
        if (this.logger.isEnabledFor(Level.WARN)) {
            final FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
            this.logger.log(Log4jLoggerAdapter.FQCN, Level.WARN, ft.getMessage(), ft.getThrowable());
        }
    }
    
    public void warn(final String msg, final Throwable t) {
        this.logger.log(Log4jLoggerAdapter.FQCN, Level.WARN, msg, t);
    }
    
    public boolean isErrorEnabled() {
        return this.logger.isEnabledFor(Level.ERROR);
    }
    
    public void error(final String msg) {
        this.logger.log(Log4jLoggerAdapter.FQCN, Level.ERROR, msg, null);
    }
    
    public void error(final String format, final Object arg) {
        if (this.logger.isEnabledFor(Level.ERROR)) {
            final FormattingTuple ft = MessageFormatter.format(format, arg);
            this.logger.log(Log4jLoggerAdapter.FQCN, Level.ERROR, ft.getMessage(), ft.getThrowable());
        }
    }
    
    public void error(final String format, final Object arg1, final Object arg2) {
        if (this.logger.isEnabledFor(Level.ERROR)) {
            final FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            this.logger.log(Log4jLoggerAdapter.FQCN, Level.ERROR, ft.getMessage(), ft.getThrowable());
        }
    }
    
    public void error(final String format, final Object... argArray) {
        if (this.logger.isEnabledFor(Level.ERROR)) {
            final FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
            this.logger.log(Log4jLoggerAdapter.FQCN, Level.ERROR, ft.getMessage(), ft.getThrowable());
        }
    }
    
    public void error(final String msg, final Throwable t) {
        this.logger.log(Log4jLoggerAdapter.FQCN, Level.ERROR, msg, t);
    }
    
    public void log(final Marker marker, final String callerFQCN, final int level, final String msg, final Object[] argArray, final Throwable t) {
        final Level log4jLevel = this.toLog4jLevel(level);
        this.logger.log(callerFQCN, log4jLevel, msg, t);
    }
    
    private Level toLog4jLevel(final int level) {
        Level log4jLevel = null;
        switch (level) {
            case 0: {
                log4jLevel = (this.traceCapable ? Level.TRACE : Level.DEBUG);
                break;
            }
            case 10: {
                log4jLevel = Level.DEBUG;
                break;
            }
            case 20: {
                log4jLevel = Level.INFO;
                break;
            }
            case 30: {
                log4jLevel = Level.WARN;
                break;
            }
            case 40: {
                log4jLevel = Level.ERROR;
                break;
            }
            default: {
                throw new IllegalStateException("Level number " + level + " is not recognized.");
            }
        }
        return log4jLevel;
    }
    
    public void log(final LoggingEvent event) {
        final Level log4jLevel = this.toLog4jLevel(event.getLevel().toInt());
        if (!this.logger.isEnabledFor(log4jLevel)) {
            return;
        }
        final org.apache.log4j.spi.LoggingEvent log4jevent = this.toLog4jEvent(event, log4jLevel);
        this.logger.callAppenders(log4jevent);
    }
    
    private org.apache.log4j.spi.LoggingEvent toLog4jEvent(final LoggingEvent event, final Level log4jLevel) {
        final FormattingTuple ft = MessageFormatter.format(event.getMessage(), event.getArgumentArray(), event.getThrowable());
        final LocationInfo locationInfo = new LocationInfo("NA/SubstituteLogger", "NA/SubstituteLogger", "NA/SubstituteLogger", "0");
        ThrowableInformation ti = null;
        final Throwable t = ft.getThrowable();
        if (t != null) {
            ti = new ThrowableInformation(t);
        }
        final org.apache.log4j.spi.LoggingEvent log4jEvent = new org.apache.log4j.spi.LoggingEvent(Log4jLoggerAdapter.FQCN, this.logger, event.getTimeStamp(), log4jLevel, ft.getMessage(), event.getThreadName(), ti, null, locationInfo, null);
        return log4jEvent;
    }
    
    static {
        FQCN = Log4jLoggerAdapter.class.getName();
    }
}
