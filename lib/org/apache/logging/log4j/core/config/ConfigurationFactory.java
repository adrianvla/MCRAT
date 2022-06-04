// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config;

import org.apache.logging.log4j.util.Strings;
import org.apache.logging.log4j.core.util.NetUtils;
import org.apache.logging.log4j.core.config.composite.CompositeConfiguration;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.status.StatusLogger;
import java.net.URLConnection;
import java.io.FileNotFoundException;
import org.apache.logging.log4j.Level;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import org.apache.logging.log4j.core.util.FileUtils;
import org.apache.logging.log4j.core.net.UrlConnectionFactory;
import java.net.URL;
import java.net.URI;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.core.util.BasicAuthorizationProvider;
import org.apache.logging.log4j.util.LoaderUtil;
import java.util.Iterator;
import java.util.Map;
import java.util.Comparator;
import java.util.Collections;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import java.util.Collection;
import org.apache.logging.log4j.util.PropertiesUtil;
import java.util.ArrayList;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.core.lookup.Interpolator;
import org.apache.logging.log4j.core.util.AuthorizationProvider;
import java.util.concurrent.locks.Lock;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;

public abstract class ConfigurationFactory extends ConfigurationBuilderFactory
{
    public static final String CONFIGURATION_FACTORY_PROPERTY = "log4j.configurationFactory";
    public static final String CONFIGURATION_FILE_PROPERTY = "log4j.configurationFile";
    public static final String LOG4J1_CONFIGURATION_FILE_PROPERTY = "log4j.configuration";
    public static final String LOG4J1_EXPERIMENTAL = "log4j1.compatibility";
    public static final String AUTHORIZATION_PROVIDER = "log4j2.authorizationProvider";
    public static final String CATEGORY = "ConfigurationFactory";
    protected static final Logger LOGGER;
    protected static final String TEST_PREFIX = "log4j2-test";
    protected static final String DEFAULT_PREFIX = "log4j2";
    protected static final String LOG4J1_VERSION = "1";
    protected static final String LOG4J2_VERSION = "2";
    private static final String CLASS_LOADER_SCHEME = "classloader";
    private static final String CLASS_PATH_SCHEME = "classpath";
    private static volatile List<ConfigurationFactory> factories;
    private static ConfigurationFactory configFactory;
    protected final StrSubstitutor substitutor;
    private static final Lock LOCK;
    private static final String HTTPS = "https";
    private static final String HTTP = "http";
    private static volatile AuthorizationProvider authorizationProvider;
    
    public ConfigurationFactory() {
        this.substitutor = new StrSubstitutor(new Interpolator());
    }
    
    public static ConfigurationFactory getInstance() {
        if (ConfigurationFactory.factories == null) {
            ConfigurationFactory.LOCK.lock();
            try {
                if (ConfigurationFactory.factories == null) {
                    final List<ConfigurationFactory> list = new ArrayList<ConfigurationFactory>();
                    final PropertiesUtil props = PropertiesUtil.getProperties();
                    final String factoryClass = props.getStringProperty("log4j.configurationFactory");
                    if (factoryClass != null) {
                        addFactory(list, factoryClass);
                    }
                    final PluginManager manager = new PluginManager("ConfigurationFactory");
                    manager.collectPlugins();
                    final Map<String, PluginType<?>> plugins = manager.getPlugins();
                    final List<Class<? extends ConfigurationFactory>> ordered = new ArrayList<Class<? extends ConfigurationFactory>>(plugins.size());
                    for (final PluginType<?> type : plugins.values()) {
                        try {
                            ordered.add(type.getPluginClass().asSubclass(ConfigurationFactory.class));
                        }
                        catch (Exception ex) {
                            ConfigurationFactory.LOGGER.warn("Unable to add class {}", type.getPluginClass(), ex);
                        }
                    }
                    Collections.sort(ordered, OrderComparator.getInstance());
                    for (final Class<? extends ConfigurationFactory> clazz : ordered) {
                        addFactory(list, clazz);
                    }
                    ConfigurationFactory.factories = Collections.unmodifiableList((List<? extends ConfigurationFactory>)list);
                    ConfigurationFactory.authorizationProvider = authorizationProvider(props);
                }
            }
            finally {
                ConfigurationFactory.LOCK.unlock();
            }
        }
        ConfigurationFactory.LOGGER.debug("Using configurationFactory {}", ConfigurationFactory.configFactory);
        return ConfigurationFactory.configFactory;
    }
    
    public static AuthorizationProvider authorizationProvider(final PropertiesUtil props) {
        final String authClass = props.getStringProperty("log4j2.authorizationProvider");
        AuthorizationProvider provider = null;
        if (authClass != null) {
            try {
                final Object obj = LoaderUtil.newInstanceOf(authClass);
                if (obj instanceof AuthorizationProvider) {
                    provider = (AuthorizationProvider)obj;
                }
                else {
                    ConfigurationFactory.LOGGER.warn("{} is not an AuthorizationProvider, using default", obj.getClass().getName());
                }
            }
            catch (Exception ex) {
                ConfigurationFactory.LOGGER.warn("Unable to create {}, using default: {}", authClass, ex.getMessage());
            }
        }
        if (provider == null) {
            provider = new BasicAuthorizationProvider(props);
        }
        return provider;
    }
    
    public static AuthorizationProvider getAuthorizationProvider() {
        return ConfigurationFactory.authorizationProvider;
    }
    
    private static void addFactory(final Collection<ConfigurationFactory> list, final String factoryClass) {
        try {
            addFactory(list, Loader.loadClass(factoryClass).asSubclass(ConfigurationFactory.class));
        }
        catch (Exception ex) {
            ConfigurationFactory.LOGGER.error("Unable to load class {}", factoryClass, ex);
        }
    }
    
    private static void addFactory(final Collection<ConfigurationFactory> list, final Class<? extends ConfigurationFactory> factoryClass) {
        try {
            list.add(ReflectionUtil.instantiate(factoryClass));
        }
        catch (Exception ex) {
            ConfigurationFactory.LOGGER.error("Unable to create instance of {}", factoryClass.getName(), ex);
        }
    }
    
    public static void setConfigurationFactory(final ConfigurationFactory factory) {
        ConfigurationFactory.configFactory = factory;
    }
    
    public static void resetConfigurationFactory() {
        ConfigurationFactory.configFactory = new Factory();
    }
    
    public static void removeConfigurationFactory(final ConfigurationFactory factory) {
        if (ConfigurationFactory.configFactory == factory) {
            ConfigurationFactory.configFactory = new Factory();
        }
    }
    
    protected abstract String[] getSupportedTypes();
    
    protected String getTestPrefix() {
        return "log4j2-test";
    }
    
    protected String getDefaultPrefix() {
        return "log4j2";
    }
    
    protected String getVersion() {
        return "2";
    }
    
    protected boolean isActive() {
        return true;
    }
    
    public abstract Configuration getConfiguration(final LoggerContext p0, final ConfigurationSource p1);
    
    public Configuration getConfiguration(final LoggerContext loggerContext, final String name, final URI configLocation) {
        if (!this.isActive()) {
            return null;
        }
        if (configLocation != null) {
            final ConfigurationSource source = ConfigurationSource.fromUri(configLocation);
            if (source != null) {
                return this.getConfiguration(loggerContext, source);
            }
        }
        return null;
    }
    
    public Configuration getConfiguration(final LoggerContext loggerContext, final String name, final URI configLocation, final ClassLoader loader) {
        if (!this.isActive()) {
            return null;
        }
        if (loader == null) {
            return this.getConfiguration(loggerContext, name, configLocation);
        }
        if (isClassLoaderUri(configLocation)) {
            final String path = extractClassLoaderUriPath(configLocation);
            final ConfigurationSource source = ConfigurationSource.fromResource(path, loader);
            if (source != null) {
                final Configuration configuration = this.getConfiguration(loggerContext, source);
                if (configuration != null) {
                    return configuration;
                }
            }
        }
        return this.getConfiguration(loggerContext, name, configLocation);
    }
    
    static boolean isClassLoaderUri(final URI uri) {
        if (uri == null) {
            return false;
        }
        final String scheme = uri.getScheme();
        return scheme == null || scheme.equals("classloader") || scheme.equals("classpath");
    }
    
    static String extractClassLoaderUriPath(final URI uri) {
        return (uri.getScheme() == null) ? uri.getPath() : uri.getSchemeSpecificPart();
    }
    
    protected ConfigurationSource getInputFromString(final String config, final ClassLoader loader) {
        try {
            final URL url = new URL(config);
            final URLConnection urlConnection = UrlConnectionFactory.createConnection(url);
            final File file = FileUtils.fileFromUri(url.toURI());
            if (file != null) {
                return new ConfigurationSource(urlConnection.getInputStream(), FileUtils.fileFromUri(url.toURI()));
            }
            return new ConfigurationSource(urlConnection.getInputStream(), url, urlConnection.getLastModified());
        }
        catch (Exception ex) {
            final ConfigurationSource source = ConfigurationSource.fromResource(config, loader);
            if (source == null) {
                try {
                    final File file = new File(config);
                    return new ConfigurationSource(new FileInputStream(file), file);
                }
                catch (FileNotFoundException fnfe) {
                    ConfigurationFactory.LOGGER.catching(Level.DEBUG, fnfe);
                }
            }
            return source;
        }
    }
    
    static List<ConfigurationFactory> getFactories() {
        return ConfigurationFactory.factories;
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
        ConfigurationFactory.factories = null;
        ConfigurationFactory.configFactory = new Factory();
        LOCK = new ReentrantLock();
        ConfigurationFactory.authorizationProvider = null;
    }
    
    private static class Factory extends ConfigurationFactory
    {
        private static final String ALL_TYPES = "*";
        
        @Override
        public Configuration getConfiguration(final LoggerContext loggerContext, final String name, final URI configLocation) {
            if (configLocation == null) {
                final String configLocationStr = this.substitutor.replace(PropertiesUtil.getProperties().getStringProperty("log4j.configurationFile"));
                if (configLocationStr != null) {
                    final String[] sources = configLocationStr.split(",");
                    if (sources.length > 1) {
                        final List<AbstractConfiguration> configs = new ArrayList<AbstractConfiguration>();
                        for (final String sourceLocation : sources) {
                            final Configuration config = this.getConfiguration(loggerContext, sourceLocation.trim());
                            if (config == null || !(config instanceof AbstractConfiguration)) {
                                Factory.LOGGER.error("Failed to created configuration at {}", sourceLocation);
                                return null;
                            }
                            configs.add((AbstractConfiguration)config);
                        }
                        return new CompositeConfiguration(configs);
                    }
                    return this.getConfiguration(loggerContext, configLocationStr);
                }
                else {
                    final String log4j1ConfigStr = this.substitutor.replace(PropertiesUtil.getProperties().getStringProperty("log4j.configuration"));
                    if (log4j1ConfigStr != null) {
                        System.setProperty("log4j1.compatibility", "true");
                        return this.getConfiguration("1", loggerContext, log4j1ConfigStr);
                    }
                    for (final ConfigurationFactory factory : ConfigurationFactory.getFactories()) {
                        final String[] types = factory.getSupportedTypes();
                        if (types != null) {
                            for (final String type : types) {
                                if (type.equals("*")) {
                                    final Configuration config2 = factory.getConfiguration(loggerContext, name, configLocation);
                                    if (config2 != null) {
                                        return config2;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else {
                final String configLocationStr = configLocation.toString();
                for (final ConfigurationFactory factory : ConfigurationFactory.getFactories()) {
                    final String[] types = factory.getSupportedTypes();
                    if (types != null) {
                        for (final String type : types) {
                            if (type.equals("*") || configLocationStr.endsWith(type)) {
                                final Configuration config2 = factory.getConfiguration(loggerContext, name, configLocation);
                                if (config2 != null) {
                                    return config2;
                                }
                            }
                        }
                    }
                }
            }
            Configuration config3 = this.getConfiguration(loggerContext, true, name);
            if (config3 == null) {
                config3 = this.getConfiguration(loggerContext, true, null);
                if (config3 == null) {
                    config3 = this.getConfiguration(loggerContext, false, name);
                    if (config3 == null) {
                        config3 = this.getConfiguration(loggerContext, false, null);
                    }
                }
            }
            if (config3 != null) {
                return config3;
            }
            Factory.LOGGER.warn("No Log4j 2 configuration file found. Using default configuration (logging only errors to the console), or user programmatically provided configurations. Set system property 'log4j2.debug' to show Log4j 2 internal initialization logging. See https://logging.apache.org/log4j/2.x/manual/configuration.html for instructions on how to configure Log4j 2");
            return new DefaultConfiguration();
        }
        
        private Configuration getConfiguration(final LoggerContext loggerContext, final String configLocationStr) {
            return this.getConfiguration(null, loggerContext, configLocationStr);
        }
        
        private Configuration getConfiguration(final String requiredVersion, final LoggerContext loggerContext, final String configLocationStr) {
            ConfigurationSource source = null;
            try {
                source = ConfigurationSource.fromUri(NetUtils.toURI(configLocationStr));
            }
            catch (Exception ex) {
                Factory.LOGGER.catching(Level.DEBUG, ex);
            }
            if (source == null) {
                final ClassLoader loader = LoaderUtil.getThreadContextClassLoader();
                source = this.getInputFromString(configLocationStr, loader);
            }
            if (source != null) {
                for (final ConfigurationFactory factory : ConfigurationFactory.getFactories()) {
                    if (requiredVersion != null && !factory.getVersion().equals(requiredVersion)) {
                        continue;
                    }
                    final String[] types = factory.getSupportedTypes();
                    if (types == null) {
                        continue;
                    }
                    for (final String type : types) {
                        if (type.equals("*") || configLocationStr.endsWith(type)) {
                            final Configuration config = factory.getConfiguration(loggerContext, source);
                            if (config != null) {
                                return config;
                            }
                        }
                    }
                }
            }
            return null;
        }
        
        private Configuration getConfiguration(final LoggerContext loggerContext, final boolean isTest, final String name) {
            final boolean named = Strings.isNotEmpty(name);
            final ClassLoader loader = LoaderUtil.getThreadContextClassLoader();
            for (final ConfigurationFactory factory : ConfigurationFactory.getFactories()) {
                final String prefix = isTest ? factory.getTestPrefix() : factory.getDefaultPrefix();
                final String[] types = factory.getSupportedTypes();
                if (types == null) {
                    continue;
                }
                for (final String suffix : types) {
                    if (!suffix.equals("*")) {
                        final String configName = named ? (prefix + name + suffix) : (prefix + suffix);
                        final ConfigurationSource source = ConfigurationSource.fromResource(configName, loader);
                        if (source != null) {
                            if (!factory.isActive()) {
                                Factory.LOGGER.warn("Found configuration file {} for inactive ConfigurationFactory {}", configName, factory.getClass().getName());
                            }
                            return factory.getConfiguration(loggerContext, source);
                        }
                    }
                }
            }
            return null;
        }
        
        public String[] getSupportedTypes() {
            return null;
        }
        
        @Override
        public Configuration getConfiguration(final LoggerContext loggerContext, final ConfigurationSource source) {
            if (source != null) {
                final String config = source.getLocation();
                for (final ConfigurationFactory factory : ConfigurationFactory.getFactories()) {
                    final String[] types = factory.getSupportedTypes();
                    if (types != null) {
                        final String[] array = types;
                        final int length = array.length;
                        int i = 0;
                        while (i < length) {
                            final String type = array[i];
                            if (type.equals("*") || (config != null && config.endsWith(type))) {
                                final Configuration c = factory.getConfiguration(loggerContext, source);
                                if (c != null) {
                                    Factory.LOGGER.debug("Loaded configuration from {}", source);
                                    return c;
                                }
                                Factory.LOGGER.error("Cannot determine the ConfigurationFactory to use for {}", config);
                                return null;
                            }
                            else {
                                ++i;
                            }
                        }
                    }
                }
            }
            Factory.LOGGER.error("Cannot process configuration, input source is null");
            return null;
        }
    }
}
