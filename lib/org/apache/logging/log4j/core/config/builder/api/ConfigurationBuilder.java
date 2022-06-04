// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config.builder.api;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.logging.log4j.core.LoggerContext;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.util.Builder;
import org.apache.logging.log4j.core.config.Configuration;

public interface ConfigurationBuilder<T extends Configuration> extends Builder<T>
{
    ConfigurationBuilder<T> add(final ScriptComponentBuilder p0);
    
    ConfigurationBuilder<T> add(final ScriptFileComponentBuilder p0);
    
    ConfigurationBuilder<T> add(final AppenderComponentBuilder p0);
    
    ConfigurationBuilder<T> add(final CustomLevelComponentBuilder p0);
    
    ConfigurationBuilder<T> add(final FilterComponentBuilder p0);
    
    ConfigurationBuilder<T> add(final LoggerComponentBuilder p0);
    
    ConfigurationBuilder<T> add(final RootLoggerComponentBuilder p0);
    
    ConfigurationBuilder<T> addProperty(final String p0, final String p1);
    
    ScriptComponentBuilder newScript(final String p0, final String p1, final String p2);
    
    ScriptFileComponentBuilder newScriptFile(final String p0);
    
    ScriptFileComponentBuilder newScriptFile(final String p0, final String p1);
    
    AppenderComponentBuilder newAppender(final String p0, final String p1);
    
    AppenderRefComponentBuilder newAppenderRef(final String p0);
    
    LoggerComponentBuilder newAsyncLogger(final String p0);
    
    LoggerComponentBuilder newAsyncLogger(final String p0, final boolean p1);
    
    LoggerComponentBuilder newAsyncLogger(final String p0, final Level p1);
    
    LoggerComponentBuilder newAsyncLogger(final String p0, final Level p1, final boolean p2);
    
    LoggerComponentBuilder newAsyncLogger(final String p0, final String p1);
    
    LoggerComponentBuilder newAsyncLogger(final String p0, final String p1, final boolean p2);
    
    RootLoggerComponentBuilder newAsyncRootLogger();
    
    RootLoggerComponentBuilder newAsyncRootLogger(final boolean p0);
    
    RootLoggerComponentBuilder newAsyncRootLogger(final Level p0);
    
    RootLoggerComponentBuilder newAsyncRootLogger(final Level p0, final boolean p1);
    
    RootLoggerComponentBuilder newAsyncRootLogger(final String p0);
    
    RootLoggerComponentBuilder newAsyncRootLogger(final String p0, final boolean p1);
    
     <B extends ComponentBuilder<B>> ComponentBuilder<B> newComponent(final String p0);
    
     <B extends ComponentBuilder<B>> ComponentBuilder<B> newComponent(final String p0, final String p1);
    
     <B extends ComponentBuilder<B>> ComponentBuilder<B> newComponent(final String p0, final String p1, final String p2);
    
    PropertyComponentBuilder newProperty(final String p0, final String p1);
    
    KeyValuePairComponentBuilder newKeyValuePair(final String p0, final String p1);
    
    CustomLevelComponentBuilder newCustomLevel(final String p0, final int p1);
    
    FilterComponentBuilder newFilter(final String p0, final Filter.Result p1, final Filter.Result p2);
    
    FilterComponentBuilder newFilter(final String p0, final String p1, final String p2);
    
    LayoutComponentBuilder newLayout(final String p0);
    
    LoggerComponentBuilder newLogger(final String p0);
    
    LoggerComponentBuilder newLogger(final String p0, final boolean p1);
    
    LoggerComponentBuilder newLogger(final String p0, final Level p1);
    
    LoggerComponentBuilder newLogger(final String p0, final Level p1, final boolean p2);
    
    LoggerComponentBuilder newLogger(final String p0, final String p1);
    
    LoggerComponentBuilder newLogger(final String p0, final String p1, final boolean p2);
    
    RootLoggerComponentBuilder newRootLogger();
    
    RootLoggerComponentBuilder newRootLogger(final boolean p0);
    
    RootLoggerComponentBuilder newRootLogger(final Level p0);
    
    RootLoggerComponentBuilder newRootLogger(final Level p0, final boolean p1);
    
    RootLoggerComponentBuilder newRootLogger(final String p0);
    
    RootLoggerComponentBuilder newRootLogger(final String p0, final boolean p1);
    
    ConfigurationBuilder<T> setAdvertiser(final String p0);
    
    ConfigurationBuilder<T> setConfigurationName(final String p0);
    
    ConfigurationBuilder<T> setConfigurationSource(final ConfigurationSource p0);
    
    ConfigurationBuilder<T> setMonitorInterval(final String p0);
    
    ConfigurationBuilder<T> setPackages(final String p0);
    
    ConfigurationBuilder<T> setShutdownHook(final String p0);
    
    ConfigurationBuilder<T> setShutdownTimeout(final long p0, final TimeUnit p1);
    
    ConfigurationBuilder<T> setStatusLevel(final Level p0);
    
    ConfigurationBuilder<T> setVerbosity(final String p0);
    
    ConfigurationBuilder<T> setDestination(final String p0);
    
    void setLoggerContext(final LoggerContext p0);
    
    ConfigurationBuilder<T> addRootProperty(final String p0, final String p1);
    
    T build(final boolean p0);
    
    void writeXmlConfiguration(final OutputStream p0) throws IOException;
    
    String toXmlConfiguration();
}
