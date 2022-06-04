// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.util;

public interface StringMap extends ReadOnlyStringMap
{
    void clear();
    
    boolean equals(final Object p0);
    
    void freeze();
    
    int hashCode();
    
    boolean isFrozen();
    
    void putAll(final ReadOnlyStringMap p0);
    
    void putValue(final String p0, final Object p1);
    
    void remove(final String p0);
}
