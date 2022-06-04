// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.listener;

import org.apache.ftpserver.ipfilter.SessionFilter;
import org.apache.mina.filter.firewall.Subnet;
import java.net.InetAddress;
import java.util.List;
import org.apache.ftpserver.DataConnectionConfiguration;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.impl.FtpIoSession;
import java.util.Set;
import org.apache.ftpserver.impl.FtpServerContext;

public interface Listener
{
    void start(final FtpServerContext p0);
    
    void stop();
    
    boolean isStopped();
    
    void suspend();
    
    void resume();
    
    boolean isSuspended();
    
    Set<FtpIoSession> getActiveSessions();
    
    boolean isImplicitSsl();
    
    SslConfiguration getSslConfiguration();
    
    int getPort();
    
    String getServerAddress();
    
    DataConnectionConfiguration getDataConnectionConfiguration();
    
    int getIdleTimeout();
    
    @Deprecated
    List<InetAddress> getBlockedAddresses();
    
    @Deprecated
    List<Subnet> getBlockedSubnets();
    
    SessionFilter getSessionFilter();
}
