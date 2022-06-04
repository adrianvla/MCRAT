// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config.builder.api;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.util.Builder;

public interface ComponentBuilder<T extends ComponentBuilder<T>> extends Builder<Component>
{
    T addAttribute(final String p0, final String p1);
    
    T addAttribute(final String p0, final Level p1);
    
    T addAttribute(final String p0, final Enum<?> p1);
    
    T addAttribute(final String p0, final int p1);
    
    T addAttribute(final String p0, final boolean p1);
    
    T addAttribute(final String p0, final Object p1);
    
    T addComponent(final ComponentBuilder<?> p0);
    
    String getName();
    
    ConfigurationBuilder<? extends Configuration> getBuilder();
}
