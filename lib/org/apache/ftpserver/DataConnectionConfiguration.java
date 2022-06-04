// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver;

import org.apache.ftpserver.ssl.SslConfiguration;

public interface DataConnectionConfiguration
{
    int getIdleTime();
    
    boolean isActiveEnabled();
    
    boolean isActiveIpCheck();
    
    String getActiveLocalAddress();
    
    int getActiveLocalPort();
    
    String getPassiveAddress();
    
    String getPassiveExernalAddress();
    
    String getPassivePorts();
    
    boolean isPassiveIpCheck();
    
    int requestPassivePort();
    
    void releasePassivePort(final int p0);
    
    SslConfiguration getSslConfiguration();
    
    boolean isImplicitSsl();
}
