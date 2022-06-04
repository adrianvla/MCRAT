// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.logging;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.core.session.IoSession;
import java.util.Iterator;
import org.slf4j.MDC;
import java.util.Map;
import org.apache.mina.core.filterchain.IoFilterEvent;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.Arrays;
import java.util.EnumSet;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.filter.util.CommonEventFilter;

public class MdcInjectionFilter extends CommonEventFilter
{
    private static final AttributeKey CONTEXT_KEY;
    private ThreadLocal<Integer> callDepth;
    private EnumSet<MdcKey> mdcKeys;
    
    public MdcInjectionFilter(final EnumSet<MdcKey> keys) {
        this.callDepth = new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return 0;
            }
        };
        this.mdcKeys = keys.clone();
    }
    
    public MdcInjectionFilter(final MdcKey... keys) {
        this.callDepth = new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return 0;
            }
        };
        final Set<MdcKey> keySet = new HashSet<MdcKey>(Arrays.asList(keys));
        this.mdcKeys = EnumSet.copyOf(keySet);
    }
    
    public MdcInjectionFilter() {
        this.callDepth = new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return 0;
            }
        };
        this.mdcKeys = EnumSet.allOf(MdcKey.class);
    }
    
    @Override
    protected void filter(final IoFilterEvent event) throws Exception {
        final int currentCallDepth = this.callDepth.get();
        this.callDepth.set(currentCallDepth + 1);
        final Map<String, String> context = this.getAndFillContext(event.getSession());
        if (currentCallDepth == 0) {
            for (final Map.Entry<String, String> e : context.entrySet()) {
                MDC.put(e.getKey(), e.getValue());
            }
        }
        try {
            event.fire();
        }
        finally {
            if (currentCallDepth == 0) {
                for (final String key : context.keySet()) {
                    MDC.remove(key);
                }
                this.callDepth.remove();
            }
            else {
                this.callDepth.set(currentCallDepth);
            }
        }
    }
    
    private Map<String, String> getAndFillContext(final IoSession session) {
        final Map<String, String> context = getContext(session);
        if (context.isEmpty()) {
            this.fillContext(session, context);
        }
        return context;
    }
    
    private static Map<String, String> getContext(final IoSession session) {
        Map<String, String> context = (Map<String, String>)session.getAttribute(MdcInjectionFilter.CONTEXT_KEY);
        if (context == null) {
            context = new ConcurrentHashMap<String, String>();
            session.setAttribute(MdcInjectionFilter.CONTEXT_KEY, context);
        }
        return context;
    }
    
    protected void fillContext(final IoSession session, final Map<String, String> context) {
        if (this.mdcKeys.contains(MdcKey.handlerClass)) {
            context.put(MdcKey.handlerClass.name(), session.getHandler().getClass().getName());
        }
        if (this.mdcKeys.contains(MdcKey.remoteAddress)) {
            context.put(MdcKey.remoteAddress.name(), session.getRemoteAddress().toString());
        }
        if (this.mdcKeys.contains(MdcKey.localAddress)) {
            context.put(MdcKey.localAddress.name(), session.getLocalAddress().toString());
        }
        if (session.getTransportMetadata().getAddressType() == InetSocketAddress.class) {
            final InetSocketAddress remoteAddress = (InetSocketAddress)session.getRemoteAddress();
            final InetSocketAddress localAddress = (InetSocketAddress)session.getLocalAddress();
            if (this.mdcKeys.contains(MdcKey.remoteIp)) {
                context.put(MdcKey.remoteIp.name(), remoteAddress.getAddress().getHostAddress());
            }
            if (this.mdcKeys.contains(MdcKey.remotePort)) {
                context.put(MdcKey.remotePort.name(), String.valueOf(remoteAddress.getPort()));
            }
            if (this.mdcKeys.contains(MdcKey.localIp)) {
                context.put(MdcKey.localIp.name(), localAddress.getAddress().getHostAddress());
            }
            if (this.mdcKeys.contains(MdcKey.localPort)) {
                context.put(MdcKey.localPort.name(), String.valueOf(localAddress.getPort()));
            }
        }
    }
    
    public static String getProperty(final IoSession session, final String key) {
        if (key == null) {
            throw new IllegalArgumentException("key should not be null");
        }
        final Map<String, String> context = getContext(session);
        final String answer = context.get(key);
        if (answer != null) {
            return answer;
        }
        return MDC.get(key);
    }
    
    public static void setProperty(final IoSession session, final String key, final String value) {
        if (key == null) {
            throw new IllegalArgumentException("key should not be null");
        }
        if (value == null) {
            removeProperty(session, key);
        }
        final Map<String, String> context = getContext(session);
        context.put(key, value);
        MDC.put(key, value);
    }
    
    public static void removeProperty(final IoSession session, final String key) {
        if (key == null) {
            throw new IllegalArgumentException("key should not be null");
        }
        final Map<String, String> context = getContext(session);
        context.remove(key);
        MDC.remove(key);
    }
    
    static {
        CONTEXT_KEY = new AttributeKey(MdcInjectionFilter.class, "context");
    }
    
    public enum MdcKey
    {
        handlerClass, 
        remoteAddress, 
        localAddress, 
        remoteIp, 
        remotePort, 
        localIp, 
        localPort;
    }
}
