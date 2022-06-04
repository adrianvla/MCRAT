// 
// Decompiled by Procyon v0.5.36
// 

package org.slf4j.impl;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.MDC;
import org.slf4j.spi.MDCAdapter;

public class Log4jMDCAdapter implements MDCAdapter
{
    public void clear() {
        final Map map = MDC.getContext();
        if (map != null) {
            map.clear();
        }
    }
    
    public String get(final String key) {
        return (String)MDC.get(key);
    }
    
    public void put(final String key, final String val) {
        MDC.put(key, val);
    }
    
    public void remove(final String key) {
        MDC.remove(key);
    }
    
    public Map getCopyOfContextMap() {
        final Map old = MDC.getContext();
        if (old != null) {
            return new HashMap(old);
        }
        return null;
    }
    
    public void setContextMap(final Map contextMap) {
        final Map old = MDC.getContext();
        if (old == null) {
            for (final Map.Entry mapEntry : contextMap.entrySet()) {
                MDC.put(mapEntry.getKey(), mapEntry.getValue());
            }
        }
        else {
            old.clear();
            old.putAll(contextMap);
        }
    }
}
