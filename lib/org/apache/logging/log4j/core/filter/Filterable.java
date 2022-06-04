// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.filter;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LifeCycle;

public interface Filterable extends LifeCycle
{
    void addFilter(final Filter p0);
    
    void removeFilter(final Filter p0);
    
    Filter getFilter();
    
    boolean hasFilter();
    
    boolean isFiltered(final LogEvent p0);
}
