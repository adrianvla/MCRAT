// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.impl;

import org.apache.logging.log4j.spi.ReadOnlyThreadContextMap;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import java.util.Map;
import java.util.HashMap;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.ContextDataInjector;
import org.apache.logging.log4j.util.StringMap;
import org.apache.logging.log4j.core.config.Property;
import java.util.List;

public class ThreadContextDataInjector
{
    public static void copyProperties(final List<Property> properties, final StringMap result) {
        if (properties != null) {
            for (int i = 0; i < properties.size(); ++i) {
                final Property prop = properties.get(i);
                result.putValue(prop.getName(), prop.getValue());
            }
        }
    }
    
    public static class ForDefaultThreadContextMap implements ContextDataInjector
    {
        @Override
        public StringMap injectContextData(final List<Property> props, final StringMap ignore) {
            final Map<String, String> copy = ThreadContext.getImmutableContext();
            if (props == null || props.isEmpty()) {
                return copy.isEmpty() ? ContextDataFactory.emptyFrozenContextData() : frozenStringMap(copy);
            }
            final StringMap result = new JdkMapAdapterStringMap(new HashMap<String, String>(copy));
            for (int i = 0; i < props.size(); ++i) {
                final Property prop = props.get(i);
                if (!copy.containsKey(prop.getName())) {
                    result.putValue(prop.getName(), prop.getValue());
                }
            }
            result.freeze();
            return result;
        }
        
        private static JdkMapAdapterStringMap frozenStringMap(final Map<String, String> copy) {
            final JdkMapAdapterStringMap result = new JdkMapAdapterStringMap(copy);
            result.freeze();
            return result;
        }
        
        @Override
        public ReadOnlyStringMap rawContextData() {
            final ReadOnlyThreadContextMap map = ThreadContext.getThreadContextMap();
            if (map instanceof ReadOnlyStringMap) {
                return (ReadOnlyStringMap)map;
            }
            final Map<String, String> copy = ThreadContext.getImmutableContext();
            return copy.isEmpty() ? ContextDataFactory.emptyFrozenContextData() : new JdkMapAdapterStringMap(copy);
        }
    }
    
    public static class ForGarbageFreeThreadContextMap implements ContextDataInjector
    {
        @Override
        public StringMap injectContextData(final List<Property> props, final StringMap reusable) {
            ThreadContextDataInjector.copyProperties(props, reusable);
            final ReadOnlyStringMap immutableCopy = ThreadContext.getThreadContextMap().getReadOnlyContextData();
            reusable.putAll(immutableCopy);
            return reusable;
        }
        
        @Override
        public ReadOnlyStringMap rawContextData() {
            return ThreadContext.getThreadContextMap().getReadOnlyContextData();
        }
    }
    
    public static class ForCopyOnWriteThreadContextMap implements ContextDataInjector
    {
        @Override
        public StringMap injectContextData(final List<Property> props, final StringMap ignore) {
            final StringMap immutableCopy = ThreadContext.getThreadContextMap().getReadOnlyContextData();
            if (props == null || props.isEmpty()) {
                return immutableCopy;
            }
            final StringMap result = ContextDataFactory.createContextData(props.size() + immutableCopy.size());
            ThreadContextDataInjector.copyProperties(props, result);
            result.putAll(immutableCopy);
            return result;
        }
        
        @Override
        public ReadOnlyStringMap rawContextData() {
            return ThreadContext.getThreadContextMap().getReadOnlyContextData();
        }
    }
}
