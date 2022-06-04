// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.logging;

public enum LogLevel
{
    TRACE(5), 
    DEBUG(4), 
    INFO(3), 
    WARN(2), 
    ERROR(1), 
    NONE(0);
    
    private int level;
    
    private LogLevel(final int level) {
        this.level = level;
    }
    
    public int getLevel() {
        return this.level;
    }
}
