// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config.plugins.convert;

import org.apache.logging.log4j.status.StatusLogger;
import java.lang.reflect.ParameterizedType;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import java.util.Collection;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import java.util.UnknownFormatConversionException;
import org.apache.logging.log4j.core.util.TypeUtil;
import java.util.Map;
import java.util.Objects;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentMap;
import org.apache.logging.log4j.Logger;

public class TypeConverterRegistry
{
    private static final Logger LOGGER;
    private static volatile TypeConverterRegistry INSTANCE;
    private static final Object INSTANCE_LOCK;
    private final ConcurrentMap<Type, TypeConverter<?>> registry;
    
    public static TypeConverterRegistry getInstance() {
        TypeConverterRegistry result = TypeConverterRegistry.INSTANCE;
        if (result == null) {
            synchronized (TypeConverterRegistry.INSTANCE_LOCK) {
                result = TypeConverterRegistry.INSTANCE;
                if (result == null) {
                    result = (TypeConverterRegistry.INSTANCE = new TypeConverterRegistry());
                }
            }
        }
        return result;
    }
    
    public TypeConverter<?> findCompatibleConverter(final Type type) {
        Objects.requireNonNull(type, "No type was provided");
        final TypeConverter<?> primary = this.registry.get(type);
        if (primary != null) {
            return primary;
        }
        if (type instanceof Class) {
            final Class<?> clazz = (Class<?>)type;
            if (clazz.isEnum()) {
                final EnumConverter<? extends Enum> converter = new EnumConverter<Enum>(clazz.asSubclass(Enum.class));
                this.registry.putIfAbsent(type, converter);
                return converter;
            }
        }
        for (final Map.Entry<Type, TypeConverter<?>> entry : this.registry.entrySet()) {
            final Type key = entry.getKey();
            if (TypeUtil.isAssignable(type, key)) {
                TypeConverterRegistry.LOGGER.debug("Found compatible TypeConverter<{}> for type [{}].", key, type);
                final TypeConverter<?> value = entry.getValue();
                this.registry.putIfAbsent(type, value);
                return value;
            }
        }
        throw new UnknownFormatConversionException(type.toString());
    }
    
    private TypeConverterRegistry() {
        this.registry = new ConcurrentHashMap<Type, TypeConverter<?>>();
        TypeConverterRegistry.LOGGER.trace("TypeConverterRegistry initializing.");
        final PluginManager manager = new PluginManager("TypeConverter");
        manager.collectPlugins();
        this.loadKnownTypeConverters(manager.getPlugins().values());
        this.registerPrimitiveTypes();
    }
    
    private void loadKnownTypeConverters(final Collection<PluginType<?>> knownTypes) {
        for (final PluginType<?> knownType : knownTypes) {
            final Class<?> clazz = knownType.getPluginClass();
            if (TypeConverter.class.isAssignableFrom(clazz)) {
                final Class<? extends TypeConverter> pluginClass = clazz.asSubclass(TypeConverter.class);
                final Type conversionType = getTypeConverterSupportedType(pluginClass);
                final TypeConverter<?> converter = ReflectionUtil.instantiate(pluginClass);
                if (this.registry.putIfAbsent(conversionType, converter) == null) {
                    continue;
                }
                TypeConverterRegistry.LOGGER.warn("Found a TypeConverter [{}] for type [{}] that already exists.", converter, conversionType);
            }
        }
    }
    
    private static Type getTypeConverterSupportedType(final Class<? extends TypeConverter> typeConverterClass) {
        for (final Type type : typeConverterClass.getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                final ParameterizedType pType = (ParameterizedType)type;
                if (TypeConverter.class.equals(pType.getRawType())) {
                    return pType.getActualTypeArguments()[0];
                }
            }
        }
        return Void.TYPE;
    }
    
    private void registerPrimitiveTypes() {
        this.registerTypeAlias(Boolean.class, Boolean.TYPE);
        this.registerTypeAlias(Byte.class, Byte.TYPE);
        this.registerTypeAlias(Character.class, Character.TYPE);
        this.registerTypeAlias(Double.class, Double.TYPE);
        this.registerTypeAlias(Float.class, Float.TYPE);
        this.registerTypeAlias(Integer.class, Integer.TYPE);
        this.registerTypeAlias(Long.class, Long.TYPE);
        this.registerTypeAlias(Short.class, Short.TYPE);
    }
    
    private void registerTypeAlias(final Type knownType, final Type aliasType) {
        this.registry.putIfAbsent(aliasType, this.registry.get(knownType));
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
        INSTANCE_LOCK = new Object();
    }
}
