// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.lookup;

import org.apache.logging.log4j.core.LogEvent;

public interface StrLookup
{
    public static final String CATEGORY = "Lookup";
    
    String lookup(final String p0);
    
    String lookup(final LogEvent p0, final String p1);
}
