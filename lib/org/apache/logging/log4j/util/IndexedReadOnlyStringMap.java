// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.util;

public interface IndexedReadOnlyStringMap extends ReadOnlyStringMap
{
    String getKeyAt(final int p0);
    
     <V> V getValueAt(final int p0);
    
    int indexOfKey(final String p0);
}
