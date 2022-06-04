// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ssl;

import javax.net.ssl.SSLContext;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLSocketFactory;

public interface SslConfiguration
{
    SSLSocketFactory getSocketFactory() throws GeneralSecurityException;
    
    SSLContext getSSLContext() throws GeneralSecurityException;
    
    SSLContext getSSLContext(final String p0) throws GeneralSecurityException;
    
    String[] getEnabledCipherSuites();
    
    ClientAuth getClientAuth();
}
