// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.selector;

import java.util.List;
import java.net.URI;
import org.apache.logging.log4j.core.LoggerContext;
import java.util.concurrent.TimeUnit;

public interface ContextSelector
{
    public static final long DEFAULT_STOP_TIMEOUT = 50L;
    
    default void shutdown(final String fqcn, final ClassLoader loader, final boolean currentContext, final boolean allContexts) {
        if (this.hasContext(fqcn, loader, currentContext)) {
            this.getContext(fqcn, loader, currentContext).stop(50L, TimeUnit.MILLISECONDS);
        }
    }
    
    default boolean hasContext(final String fqcn, final ClassLoader loader, final boolean currentContext) {
        return false;
    }
    
    LoggerContext getContext(final String p0, final ClassLoader p1, final boolean p2);
    
    LoggerContext getContext(final String p0, final ClassLoader p1, final boolean p2, final URI p3);
    
    List<LoggerContext> getLoggerContexts();
    
    void removeContext(final LoggerContext p0);
}
