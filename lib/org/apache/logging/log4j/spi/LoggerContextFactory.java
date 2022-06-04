// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.spi;

import java.net.URI;

public interface LoggerContextFactory
{
    default void shutdown(final String fqcn, final ClassLoader loader, final boolean currentContext, final boolean allContexts) {
        if (this.hasContext(fqcn, loader, currentContext)) {
            final LoggerContext ctx = this.getContext(fqcn, loader, null, currentContext);
            if (ctx instanceof Terminable) {
                ((Terminable)ctx).terminate();
            }
        }
    }
    
    default boolean hasContext(final String fqcn, final ClassLoader loader, final boolean currentContext) {
        return false;
    }
    
    LoggerContext getContext(final String p0, final ClassLoader p1, final Object p2, final boolean p3);
    
    LoggerContext getContext(final String p0, final ClassLoader p1, final Object p2, final boolean p3, final URI p4, final String p5);
    
    void removeContext(final LoggerContext p0);
}
