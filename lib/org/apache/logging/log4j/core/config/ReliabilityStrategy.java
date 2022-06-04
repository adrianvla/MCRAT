// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.util.Supplier;

public interface ReliabilityStrategy
{
    void log(final Supplier<LoggerConfig> p0, final String p1, final String p2, final Marker p3, final Level p4, final Message p5, final Throwable p6);
    
    void log(final Supplier<LoggerConfig> p0, final LogEvent p1);
    
    LoggerConfig getActiveLoggerConfig(final Supplier<LoggerConfig> p0);
    
    void afterLogEvent();
    
    void beforeStopAppenders();
    
    void beforeStopConfiguration(final Configuration p0);
}
