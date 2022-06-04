// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ssl.impl;

import javax.net.ssl.KeyManager;
import java.security.SecureRandom;
import javax.net.ssl.X509KeyManager;
import org.apache.ftpserver.util.ClassUtils;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;
import org.apache.ftpserver.ssl.ClientAuth;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;
import org.apache.ftpserver.ssl.SslConfiguration;

public class DefaultSslConfiguration implements SslConfiguration
{
    private final KeyManagerFactory keyManagerFactory;
    private final TrustManagerFactory trustManagerFactory;
    private String sslProtocol;
    private final ClientAuth clientAuth;
    private final String keyAlias;
    private final String[] enabledCipherSuites;
    private final SSLContext sslContext;
    private final SSLSocketFactory socketFactory;
    
    public DefaultSslConfiguration(final KeyManagerFactory keyManagerFactory, final TrustManagerFactory trustManagerFactory, final ClientAuth clientAuthReqd, final String sslProtocol, final String[] enabledCipherSuites, final String keyAlias) throws GeneralSecurityException {
        this.sslProtocol = "TLS";
        this.clientAuth = clientAuthReqd;
        this.enabledCipherSuites = enabledCipherSuites;
        this.keyAlias = keyAlias;
        this.keyManagerFactory = keyManagerFactory;
        this.sslProtocol = sslProtocol;
        this.trustManagerFactory = trustManagerFactory;
        this.sslContext = this.initContext();
        this.socketFactory = this.sslContext.getSocketFactory();
    }
    
    @Override
    public SSLSocketFactory getSocketFactory() throws GeneralSecurityException {
        return this.socketFactory;
    }
    
    @Override
    public SSLContext getSSLContext(final String protocol) throws GeneralSecurityException {
        return this.sslContext;
    }
    
    @Override
    public ClientAuth getClientAuth() {
        return this.clientAuth;
    }
    
    @Override
    public SSLContext getSSLContext() throws GeneralSecurityException {
        return this.getSSLContext(this.sslProtocol);
    }
    
    @Override
    public String[] getEnabledCipherSuites() {
        if (this.enabledCipherSuites != null) {
            return this.enabledCipherSuites.clone();
        }
        return null;
    }
    
    private SSLContext initContext() throws GeneralSecurityException {
        final KeyManager[] keyManagers = this.keyManagerFactory.getKeyManagers();
        for (int i = 0; i < keyManagers.length; ++i) {
            if (ClassUtils.extendsClass(keyManagers[i].getClass(), "javax.net.ssl.X509ExtendedKeyManager")) {
                keyManagers[i] = new ExtendedAliasKeyManager(keyManagers[i], this.keyAlias);
            }
            else if (keyManagers[i] instanceof X509KeyManager) {
                keyManagers[i] = new AliasKeyManager(keyManagers[i], this.keyAlias);
            }
        }
        final SSLContext ctx = SSLContext.getInstance(this.sslProtocol);
        ctx.init(keyManagers, this.trustManagerFactory.getTrustManagers(), null);
        return ctx;
    }
}
