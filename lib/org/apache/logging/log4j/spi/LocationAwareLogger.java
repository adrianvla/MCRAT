// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.spi;

import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.Level;

public interface LocationAwareLogger
{
    void logMessage(final Level p0, final Marker p1, final String p2, final StackTraceElement p3, final Message p4, final Throwable p5);
}
