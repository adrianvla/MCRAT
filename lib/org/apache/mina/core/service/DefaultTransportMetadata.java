// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.service;

import java.util.Collections;
import org.apache.mina.util.IdentityHashSet;
import java.util.Set;
import org.apache.mina.core.session.IoSessionConfig;
import java.net.SocketAddress;

public class DefaultTransportMetadata implements TransportMetadata
{
    private final String providerName;
    private final String name;
    private final boolean connectionless;
    private final boolean fragmentation;
    private final Class<? extends SocketAddress> addressType;
    private final Class<? extends IoSessionConfig> sessionConfigType;
    private final Set<Class<?>> envelopeTypes;
    
    public DefaultTransportMetadata(String providerName, String name, final boolean connectionless, final boolean fragmentation, final Class<? extends SocketAddress> addressType, final Class<? extends IoSessionConfig> sessionConfigType, final Class<?>... envelopeTypes) {
        if (providerName == null) {
            throw new IllegalArgumentException("providerName");
        }
        if (name == null) {
            throw new IllegalArgumentException("name");
        }
        providerName = providerName.trim().toLowerCase();
        if (providerName.length() == 0) {
            throw new IllegalArgumentException("providerName is empty.");
        }
        name = name.trim().toLowerCase();
        if (name.length() == 0) {
            throw new IllegalArgumentException("name is empty.");
        }
        if (addressType == null) {
            throw new IllegalArgumentException("addressType");
        }
        if (envelopeTypes == null) {
            throw new IllegalArgumentException("envelopeTypes");
        }
        if (envelopeTypes.length == 0) {
            throw new IllegalArgumentException("envelopeTypes is empty.");
        }
        if (sessionConfigType == null) {
            throw new IllegalArgumentException("sessionConfigType");
        }
        this.providerName = providerName;
        this.name = name;
        this.connectionless = connectionless;
        this.fragmentation = fragmentation;
        this.addressType = addressType;
        this.sessionConfigType = sessionConfigType;
        final Set<Class<?>> newEnvelopeTypes = new IdentityHashSet<Class<?>>();
        for (final Class<?> c : envelopeTypes) {
            newEnvelopeTypes.add(c);
        }
        this.envelopeTypes = Collections.unmodifiableSet((Set<? extends Class<?>>)newEnvelopeTypes);
    }
    
    @Override
    public Class<? extends SocketAddress> getAddressType() {
        return this.addressType;
    }
    
    @Override
    public Set<Class<?>> getEnvelopeTypes() {
        return this.envelopeTypes;
    }
    
    @Override
    public Class<? extends IoSessionConfig> getSessionConfigType() {
        return this.sessionConfigType;
    }
    
    @Override
    public String getProviderName() {
        return this.providerName;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public boolean isConnectionless() {
        return this.connectionless;
    }
    
    @Override
    public boolean hasFragmentation() {
        return this.fragmentation;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}
