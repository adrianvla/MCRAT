// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ssl.impl;

import javax.net.ssl.SSLEngine;
import java.security.cert.X509Certificate;
import java.security.PrivateKey;
import java.net.Socket;
import java.security.Principal;
import javax.net.ssl.KeyManager;
import javax.net.ssl.X509ExtendedKeyManager;

public final class ExtendedAliasKeyManager extends X509ExtendedKeyManager
{
    private final X509ExtendedKeyManager delegate;
    private final String serverKeyAlias;
    
    public ExtendedAliasKeyManager(final KeyManager mgr, final String keyAlias) {
        this.delegate = (X509ExtendedKeyManager)mgr;
        this.serverKeyAlias = keyAlias;
    }
    
    @Override
    public String chooseClientAlias(final String[] keyType, final Principal[] issuers, final Socket socket) {
        return this.delegate.chooseClientAlias(keyType, issuers, socket);
    }
    
    @Override
    public String chooseServerAlias(final String keyType, final Principal[] issuers, final Socket socket) {
        if (this.serverKeyAlias == null) {
            return this.delegate.chooseServerAlias(keyType, issuers, socket);
        }
        final PrivateKey key = this.delegate.getPrivateKey(this.serverKeyAlias);
        if (key == null) {
            return null;
        }
        if (key.getAlgorithm().equals(keyType)) {
            return this.serverKeyAlias;
        }
        return null;
    }
    
    @Override
    public X509Certificate[] getCertificateChain(final String alias) {
        return this.delegate.getCertificateChain(alias);
    }
    
    @Override
    public String[] getClientAliases(final String keyType, final Principal[] issuers) {
        return this.delegate.getClientAliases(keyType, issuers);
    }
    
    @Override
    public String[] getServerAliases(final String keyType, final Principal[] issuers) {
        return this.delegate.getServerAliases(keyType, issuers);
    }
    
    @Override
    public PrivateKey getPrivateKey(final String alias) {
        return this.delegate.getPrivateKey(alias);
    }
    
    @Override
    public String chooseEngineClientAlias(final String[] keyType, final Principal[] issuers, final SSLEngine engine) {
        return this.delegate.chooseEngineClientAlias(keyType, issuers, engine);
    }
    
    @Override
    public String chooseEngineServerAlias(final String keyType, final Principal[] issuers, final SSLEngine engine) {
        if (this.serverKeyAlias == null) {
            return this.delegate.chooseEngineServerAlias(keyType, issuers, engine);
        }
        final PrivateKey key = this.delegate.getPrivateKey(this.serverKeyAlias);
        if (key == null) {
            return null;
        }
        if (key.getAlgorithm().equals(keyType)) {
            return this.serverKeyAlias;
        }
        return null;
    }
}
