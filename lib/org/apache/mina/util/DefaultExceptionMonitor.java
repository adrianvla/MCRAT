// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.util;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DefaultExceptionMonitor extends ExceptionMonitor
{
    private static final Logger LOGGER;
    
    @Override
    public void exceptionCaught(final Throwable cause) {
        if (cause instanceof Error) {
            throw (Error)cause;
        }
        DefaultExceptionMonitor.LOGGER.warn("Unexpected exception.", cause);
    }
    
    static {
        LOGGER = LoggerFactory.getLogger(DefaultExceptionMonitor.class);
    }
}
