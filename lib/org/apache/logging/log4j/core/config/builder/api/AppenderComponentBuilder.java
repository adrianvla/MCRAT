// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config.builder.api;

public interface AppenderComponentBuilder extends FilterableComponentBuilder<AppenderComponentBuilder>
{
    AppenderComponentBuilder add(final LayoutComponentBuilder p0);
    
    String getName();
}
