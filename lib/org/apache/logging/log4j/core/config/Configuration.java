// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.util.NanoClock;
import org.apache.logging.log4j.core.util.WatchManager;
import org.apache.logging.log4j.core.async.AsyncLoggerConfigDelegate;
import org.apache.logging.log4j.core.script.ScriptManager;
import org.apache.logging.log4j.core.net.Advertiser;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import java.util.List;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import java.util.Map;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.filter.Filterable;

public interface Configuration extends Filterable
{
    public static final String CONTEXT_PROPERTIES = "ContextProperties";
    
    String getName();
    
    LoggerConfig getLoggerConfig(final String p0);
    
     <T extends Appender> T getAppender(final String p0);
    
    Map<String, Appender> getAppenders();
    
    void addAppender(final Appender p0);
    
    Map<String, LoggerConfig> getLoggers();
    
    void addLoggerAppender(final Logger p0, final Appender p1);
    
    void addLoggerFilter(final Logger p0, final Filter p1);
    
    void setLoggerAdditive(final Logger p0, final boolean p1);
    
    void addLogger(final String p0, final LoggerConfig p1);
    
    void removeLogger(final String p0);
    
    List<String> getPluginPackages();
    
    Map<String, String> getProperties();
    
    LoggerConfig getRootLogger();
    
    void addListener(final ConfigurationListener p0);
    
    void removeListener(final ConfigurationListener p0);
    
    StrSubstitutor getStrSubstitutor();
    
    void createConfiguration(final Node p0, final LogEvent p1);
    
     <T> T getComponent(final String p0);
    
    void addComponent(final String p0, final Object p1);
    
    void setAdvertiser(final Advertiser p0);
    
    Advertiser getAdvertiser();
    
    boolean isShutdownHookEnabled();
    
    long getShutdownTimeoutMillis();
    
    ConfigurationScheduler getScheduler();
    
    ConfigurationSource getConfigurationSource();
    
    List<CustomLevelConfig> getCustomLevels();
    
    ScriptManager getScriptManager();
    
    AsyncLoggerConfigDelegate getAsyncLoggerConfigDelegate();
    
    WatchManager getWatchManager();
    
    ReliabilityStrategy getReliabilityStrategy(final LoggerConfig p0);
    
    NanoClock getNanoClock();
    
    void setNanoClock(final NanoClock p0);
    
    LoggerContext getLoggerContext();
}
