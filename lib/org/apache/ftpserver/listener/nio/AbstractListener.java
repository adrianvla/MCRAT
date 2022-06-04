// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.listener.nio;

import java.util.Iterator;
import java.util.Collection;
import org.apache.ftpserver.ipfilter.RemoteIpFilter;
import org.apache.ftpserver.ipfilter.IpFilterType;
import org.apache.ftpserver.DataConnectionConfiguration;
import org.apache.ftpserver.ipfilter.SessionFilter;
import org.apache.mina.filter.firewall.Subnet;
import java.net.InetAddress;
import java.util.List;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.listener.Listener;

public abstract class AbstractListener implements Listener
{
    private final String serverAddress;
    private int port;
    private final SslConfiguration ssl;
    private final boolean implicitSsl;
    private final int idleTimeout;
    private final List<InetAddress> blockedAddresses;
    private final List<Subnet> blockedSubnets;
    private final SessionFilter sessionFilter;
    private final DataConnectionConfiguration dataConnectionConfig;
    
    @Deprecated
    public AbstractListener(final String serverAddress, final int port, final boolean implicitSsl, final SslConfiguration sslConfiguration, final DataConnectionConfiguration dataConnectionConfig, final int idleTimeout, final List<InetAddress> blockedAddresses, final List<Subnet> blockedSubnets) {
        this.port = 21;
        this.serverAddress = serverAddress;
        this.port = port;
        this.implicitSsl = implicitSsl;
        this.dataConnectionConfig = dataConnectionConfig;
        this.ssl = sslConfiguration;
        this.idleTimeout = idleTimeout;
        this.sessionFilter = createBlackListFilter(blockedAddresses, blockedSubnets);
        this.blockedAddresses = blockedAddresses;
        this.blockedSubnets = blockedSubnets;
    }
    
    public AbstractListener(final String serverAddress, final int port, final boolean implicitSsl, final SslConfiguration sslConfiguration, final DataConnectionConfiguration dataConnectionConfig, final int idleTimeout, final SessionFilter sessionFilter) {
        this.port = 21;
        this.serverAddress = serverAddress;
        this.port = port;
        this.implicitSsl = implicitSsl;
        this.dataConnectionConfig = dataConnectionConfig;
        this.ssl = sslConfiguration;
        this.idleTimeout = idleTimeout;
        this.sessionFilter = sessionFilter;
        this.blockedAddresses = null;
        this.blockedSubnets = null;
    }
    
    private static SessionFilter createBlackListFilter(final List<InetAddress> blockedAddresses, final List<Subnet> blockedSubnets) {
        if (blockedAddresses == null && blockedSubnets == null) {
            return null;
        }
        final RemoteIpFilter ipFilter = new RemoteIpFilter(IpFilterType.DENY);
        if (blockedSubnets != null) {
            ipFilter.addAll(blockedSubnets);
        }
        if (blockedAddresses != null) {
            for (final InetAddress address : blockedAddresses) {
                ipFilter.add(new Subnet(address, 32));
            }
        }
        return ipFilter;
    }
    
    @Override
    public boolean isImplicitSsl() {
        return this.implicitSsl;
    }
    
    @Override
    public int getPort() {
        return this.port;
    }
    
    protected void setPort(final int port) {
        this.port = port;
    }
    
    @Override
    public String getServerAddress() {
        return this.serverAddress;
    }
    
    @Override
    public SslConfiguration getSslConfiguration() {
        return this.ssl;
    }
    
    @Override
    public DataConnectionConfiguration getDataConnectionConfiguration() {
        return this.dataConnectionConfig;
    }
    
    @Override
    public int getIdleTimeout() {
        return this.idleTimeout;
    }
    
    @Override
    public List<InetAddress> getBlockedAddresses() {
        return this.blockedAddresses;
    }
    
    @Override
    public List<Subnet> getBlockedSubnets() {
        return this.blockedSubnets;
    }
    
    @Override
    public SessionFilter getSessionFilter() {
        return this.sessionFilter;
    }
}
