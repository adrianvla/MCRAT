// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ssl;

import org.apache.ftpserver.ssl.impl.DefaultSslConfiguration;
import java.security.GeneralSecurityException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.ftpserver.util.IoUtils;
import org.apache.ftpserver.FtpServerConfigurationException;
import java.io.FileInputStream;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;
import java.security.KeyStore;
import org.slf4j.LoggerFactory;
import java.io.File;
import org.slf4j.Logger;

public class SslConfigurationFactory
{
    private final Logger LOG;
    private File keystoreFile;
    private String keystorePass;
    private String keystoreType;
    private String keystoreAlgorithm;
    private File trustStoreFile;
    private String trustStorePass;
    private String trustStoreType;
    private String trustStoreAlgorithm;
    private String sslProtocol;
    private ClientAuth clientAuth;
    private String keyPass;
    private String keyAlias;
    private String[] enabledCipherSuites;
    
    public SslConfigurationFactory() {
        this.LOG = LoggerFactory.getLogger(SslConfigurationFactory.class);
        this.keystoreFile = new File("./res/.keystore");
        this.keystoreType = KeyStore.getDefaultType();
        this.keystoreAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
        this.trustStoreType = KeyStore.getDefaultType();
        this.trustStoreAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        this.sslProtocol = "TLS";
        this.clientAuth = ClientAuth.NONE;
    }
    
    public File getKeystoreFile() {
        return this.keystoreFile;
    }
    
    public void setKeystoreFile(final File keyStoreFile) {
        this.keystoreFile = keyStoreFile;
    }
    
    public String getKeystorePassword() {
        return this.keystorePass;
    }
    
    public void setKeystorePassword(final String keystorePass) {
        this.keystorePass = keystorePass;
    }
    
    public String getKeystoreType() {
        return this.keystoreType;
    }
    
    public void setKeystoreType(final String keystoreType) {
        this.keystoreType = keystoreType;
    }
    
    public String getKeystoreAlgorithm() {
        return this.keystoreAlgorithm;
    }
    
    public void setKeystoreAlgorithm(final String keystoreAlgorithm) {
        this.keystoreAlgorithm = keystoreAlgorithm;
    }
    
    public String getSslProtocol() {
        return this.sslProtocol;
    }
    
    public void setSslProtocol(final String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }
    
    public void setClientAuthentication(final String clientAuthReqd) {
        if ("true".equalsIgnoreCase(clientAuthReqd) || "yes".equalsIgnoreCase(clientAuthReqd) || "need".equalsIgnoreCase(clientAuthReqd)) {
            this.clientAuth = ClientAuth.NEED;
        }
        else if ("want".equalsIgnoreCase(clientAuthReqd)) {
            this.clientAuth = ClientAuth.WANT;
        }
        else {
            this.clientAuth = ClientAuth.NONE;
        }
    }
    
    public String getKeyPassword() {
        return this.keyPass;
    }
    
    public void setKeyPassword(final String keyPass) {
        this.keyPass = keyPass;
    }
    
    public File getTruststoreFile() {
        return this.trustStoreFile;
    }
    
    public void setTruststoreFile(final File trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }
    
    public String getTruststorePassword() {
        return this.trustStorePass;
    }
    
    public void setTruststorePassword(final String trustStorePass) {
        this.trustStorePass = trustStorePass;
    }
    
    public String getTruststoreType() {
        return this.trustStoreType;
    }
    
    public void setTruststoreType(final String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }
    
    public String getTruststoreAlgorithm() {
        return this.trustStoreAlgorithm;
    }
    
    public void setTruststoreAlgorithm(final String trustStoreAlgorithm) {
        this.trustStoreAlgorithm = trustStoreAlgorithm;
    }
    
    private KeyStore loadStore(final File storeFile, final String storeType, final String storePass) throws IOException, GeneralSecurityException {
        InputStream fin = null;
        try {
            if (storeFile.exists()) {
                this.LOG.debug("Trying to load store from file");
                fin = new FileInputStream(storeFile);
            }
            else {
                this.LOG.debug("Trying to load store from classpath");
                fin = this.getClass().getClassLoader().getResourceAsStream(storeFile.getPath());
                if (fin == null) {
                    throw new FtpServerConfigurationException("Key store could not be loaded from " + storeFile.getPath());
                }
            }
            final KeyStore store = KeyStore.getInstance(storeType);
            store.load(fin, storePass.toCharArray());
            return store;
        }
        finally {
            IoUtils.close(fin);
        }
    }
    
    public SslConfiguration createSslConfiguration() {
        try {
            this.LOG.debug("Loading key store from \"{}\", using the key store type \"{}\"", this.keystoreFile.getAbsolutePath(), this.keystoreType);
            final KeyStore keyStore = this.loadStore(this.keystoreFile, this.keystoreType, this.keystorePass);
            KeyStore trustStore;
            if (this.trustStoreFile != null) {
                this.LOG.debug("Loading trust store from \"{}\", using the key store type \"{}\"", this.trustStoreFile.getAbsolutePath(), this.trustStoreType);
                trustStore = this.loadStore(this.trustStoreFile, this.trustStoreType, this.trustStorePass);
            }
            else {
                trustStore = keyStore;
            }
            String keyPassToUse;
            if (this.keyPass == null) {
                keyPassToUse = this.keystorePass;
            }
            else {
                keyPassToUse = this.keyPass;
            }
            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(this.keystoreAlgorithm);
            keyManagerFactory.init(keyStore, keyPassToUse.toCharArray());
            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(this.trustStoreAlgorithm);
            trustManagerFactory.init(trustStore);
            return new DefaultSslConfiguration(keyManagerFactory, trustManagerFactory, this.clientAuth, this.sslProtocol, this.enabledCipherSuites, this.keyAlias);
        }
        catch (Exception ex) {
            this.LOG.error("DefaultSsl.configure()", ex);
            throw new FtpServerConfigurationException("DefaultSsl.configure()", ex);
        }
    }
    
    public ClientAuth getClientAuth() {
        return this.clientAuth;
    }
    
    public String[] getEnabledCipherSuites() {
        if (this.enabledCipherSuites != null) {
            return this.enabledCipherSuites.clone();
        }
        return null;
    }
    
    public void setEnabledCipherSuites(final String[] enabledCipherSuites) {
        if (enabledCipherSuites != null) {
            this.enabledCipherSuites = enabledCipherSuites.clone();
        }
        else {
            this.enabledCipherSuites = null;
        }
    }
    
    public String getKeyAlias() {
        return this.keyAlias;
    }
    
    public void setKeyAlias(final String keyAlias) {
        this.keyAlias = keyAlias;
    }
}
