// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.spi;

import java.util.Map;

public interface ObjectThreadContextMap extends CleanableThreadContextMap
{
     <V> V getValue(final String p0);
    
     <V> void putValue(final String p0, final V p1);
    
     <V> void putAllValues(final Map<String, V> p0);
}
