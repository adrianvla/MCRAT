// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core;

import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.StringMap;
import org.apache.logging.log4j.core.config.Property;
import java.util.List;

public interface ContextDataInjector
{
    StringMap injectContextData(final List<Property> p0, final StringMap p1);
    
    ReadOnlyStringMap rawContextData();
}
