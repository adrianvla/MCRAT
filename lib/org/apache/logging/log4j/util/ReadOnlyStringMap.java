// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.util;

import java.util.Map;
import java.io.Serializable;

public interface ReadOnlyStringMap extends Serializable
{
    Map<String, String> toMap();
    
    boolean containsKey(final String p0);
    
     <V> void forEach(final BiConsumer<String, ? super V> p0);
    
     <V, S> void forEach(final TriConsumer<String, ? super V, S> p0, final S p1);
    
     <V> V getValue(final String p0);
    
    boolean isEmpty();
    
    int size();
}
