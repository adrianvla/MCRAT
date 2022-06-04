// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.spi;

import org.apache.logging.log4j.message.MessageFactory;

public interface LoggerContext
{
    Object getExternalContext();
    
    default Object getObject(final String key) {
        return null;
    }
    
    default Object putObject(final String key, final Object value) {
        return null;
    }
    
    default Object putObjectIfAbsent(final String key, final Object value) {
        return null;
    }
    
    default Object removeObject(final String key) {
        return null;
    }
    
    default boolean removeObject(final String key, final Object value) {
        return false;
    }
    
    ExtendedLogger getLogger(final String p0);
    
    ExtendedLogger getLogger(final String p0, final MessageFactory p1);
    
    boolean hasLogger(final String p0);
    
    boolean hasLogger(final String p0, final MessageFactory p1);
    
    boolean hasLogger(final String p0, final Class<? extends MessageFactory> p1);
}
