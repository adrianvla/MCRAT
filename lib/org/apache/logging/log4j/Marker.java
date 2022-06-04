// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j;

import java.io.Serializable;

public interface Marker extends Serializable
{
    Marker addParents(final Marker... p0);
    
    boolean equals(final Object p0);
    
    String getName();
    
    Marker[] getParents();
    
    int hashCode();
    
    boolean hasParents();
    
    boolean isInstanceOf(final Marker p0);
    
    boolean isInstanceOf(final String p0);
    
    boolean remove(final Marker p0);
    
    Marker setParents(final Marker... p0);
}
