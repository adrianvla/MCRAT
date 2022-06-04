// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config;

import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.util.Supplier;

public interface LocationAwareReliabilityStrategy
{
    void log(final Supplier<LoggerConfig> p0, final String p1, final String p2, final StackTraceElement p3, final Marker p4, final Level p5, final Message p6, final Throwable p7);
}
