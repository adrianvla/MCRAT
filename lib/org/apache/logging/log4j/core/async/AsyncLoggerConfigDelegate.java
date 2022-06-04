// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.async;

import org.apache.logging.log4j.core.impl.LogEventFactory;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.jmx.RingBufferAdmin;

public interface AsyncLoggerConfigDelegate
{
    RingBufferAdmin createRingBufferAdmin(final String p0, final String p1);
    
    EventRoute getEventRoute(final Level p0);
    
    void enqueueEvent(final LogEvent p0, final AsyncLoggerConfig p1);
    
    boolean tryEnqueue(final LogEvent p0, final AsyncLoggerConfig p1);
    
    void setLogEventFactory(final LogEventFactory p0);
}
