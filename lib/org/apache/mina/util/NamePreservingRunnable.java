// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.util;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class NamePreservingRunnable implements Runnable
{
    private static final Logger LOGGER;
    private final String newName;
    private final Runnable runnable;
    
    public NamePreservingRunnable(final Runnable runnable, final String newName) {
        this.runnable = runnable;
        this.newName = newName;
    }
    
    @Override
    public void run() {
        final Thread currentThread = Thread.currentThread();
        final String oldName = currentThread.getName();
        if (this.newName != null) {
            this.setName(currentThread, this.newName);
        }
        try {
            this.runnable.run();
        }
        finally {
            this.setName(currentThread, oldName);
        }
    }
    
    private void setName(final Thread thread, final String name) {
        try {
            thread.setName(name);
        }
        catch (SecurityException se) {
            if (NamePreservingRunnable.LOGGER.isWarnEnabled()) {
                NamePreservingRunnable.LOGGER.warn("Failed to set the thread name.", se);
            }
        }
    }
    
    static {
        LOGGER = LoggerFactory.getLogger(NamePreservingRunnable.class);
    }
}
