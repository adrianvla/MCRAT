// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.log4j.spi;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;

public interface HierarchyEventListener
{
    void addAppenderEvent(final Category p0, final Appender p1);
    
    void removeAppenderEvent(final Category p0, final Appender p1);
}
