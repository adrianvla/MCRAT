// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.impl;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Property;
import java.util.List;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;

public interface LocationAwareLogEventFactory
{
    LogEvent createEvent(final String p0, final Marker p1, final String p2, final StackTraceElement p3, final Level p4, final Message p5, final List<Property> p6, final Throwable p7);
}
