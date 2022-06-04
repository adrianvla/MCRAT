// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.util;

import org.apache.logging.log4j.core.config.Reconfigurable;
import org.apache.logging.log4j.core.config.ConfigurationListener;
import java.util.List;

public interface Watcher
{
    public static final String CATEGORY = "Watcher";
    public static final String ELEMENT_TYPE = "watcher";
    
    List<ConfigurationListener> getListeners();
    
    void modified();
    
    boolean isModified();
    
    long getLastModified();
    
    void watching(final Source p0);
    
    Source getSource();
    
    Watcher newWatcher(final Reconfigurable p0, final List<ConfigurationListener> p1, final long p2);
}
