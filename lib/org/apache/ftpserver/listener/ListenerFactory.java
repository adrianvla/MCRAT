// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.listener;

import org.apache.ftpserver.listener.nio.NioListener;
import java.net.UnknownHostException;
import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.ipfilter.SessionFilter;
import org.apache.mina.filter.firewall.Subnet;
import java.net.InetAddress;
import java.util.List;
import org.apache.ftpserver.DataConnectionConfiguration;
import org.apache.ftpserver.ssl.SslConfiguration;

public class ListenerFactory
{
    private String serverAddress;
    private int port;
    private SslConfiguration ssl;
    private boolean implicitSsl;
    private DataConnectionConfiguration dataConnectionConfig;
    private int idleTimeout;
    private List<InetAddress> blockedAddresses;
    private List<Subnet> blockedSubnets;
    private SessionFilter sessionFilter;
    
    public ListenerFactory() {
        this.port = 21;
        this.implicitSsl = false;
        this.dataConnectionConfig = new DataConnectionConfigurationFactory().createDataConnectionConfiguration();
        this.idleTimeout = 300;
        this.sessionFilter = null;
    }
    
    public ListenerFactory(final Listener listener) {
        this.port = 21;
        this.implicitSsl = false;
        this.dataConnectionConfig = new DataConnectionConfigurationFactory().createDataConnectionConfiguration();
        this.idleTimeout = 300;
        this.sessionFilter = null;
        this.serverAddress = listener.getServerAddress();
        this.port = listener.getPort();
        this.ssl = listener.getSslConfiguration();
        this.implicitSsl = listener.isImplicitSsl();
        this.dataConnectionConfig = listener.getDataConnectionConfiguration();
        this.idleTimeout = listener.getIdleTimeout();
        this.blockedAddresses = listener.getBlockedAddresses();
        this.blockedSubnets = listener.getBlockedSubnets();
        this.sessionFilter = listener.getSessionFilter();
    }
    
    public Listener createListener() {
        try {
            InetAddress.getByName(this.serverAddress);
        }
        catch (UnknownHostException e) {
            throw new FtpServerConfigurationException("Unknown host", e);
        }
        if (this.sessionFilter != null && (this.blockedAddresses != null || this.blockedSubnets != null)) {
            throw new IllegalStateException("Usage of SessionFilter in combination with blockedAddesses/subnets is not supported. ");
        }
        if (this.blockedAddresses != null || this.blockedSubnets != null) {
            return new NioListener(this.serverAddress, this.port, this.implicitSsl, this.ssl, this.dataConnectionConfig, this.idleTimeout, this.blockedAddresses, this.blockedSubnets);
        }
        return new NioListener(this.serverAddress, this.port, this.implicitSsl, this.ssl, this.dataConnectionConfig, this.idleTimeout, this.sessionFilter);
    }
    
    public boolean isImplicitSsl() {
        return this.implicitSsl;
    }
    
    public void setImplicitSsl(final boolean implicitSsl) {
        this.implicitSsl = implicitSsl;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public void setPort(final int port) {
        this.port = port;
    }
    
    public String getServerAddress() {
        return this.serverAddress;
    }
    
    public void setServerAddress(final String serverAddress) {
        this.serverAddress = serverAddress;
    }
    
    public SslConfiguration getSslConfiguration() {
        return this.ssl;
    }
    
    public void setSslConfiguration(final SslConfiguration ssl) {
        this.ssl = ssl;
    }
    
    public DataConnectionConfiguration getDataConnectionConfiguration() {
        return this.dataConnectionConfig;
    }
    
    public void setDataConnectionConfiguration(final DataConnectionConfiguration dataConnectionConfig) {
        this.dataConnectionConfig = dataConnectionConfig;
    }
    
    public int getIdleTimeout() {
        return this.idleTimeout;
    }
    
    public void setIdleTimeout(final int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }
    
    @Deprecated
    public List<InetAddress> getBlockedAddresses() {
        return this.blockedAddresses;
    }
    
    @Deprecated
    public void setBlockedAddresses(final List<InetAddress> blockedAddresses) {
        this.blockedAddresses = blockedAddresses;
    }
    
    @Deprecated
    public List<Subnet> getBlockedSubnets() {
        return this.blockedSubnets;
    }
    
    @Deprecated
    public void setBlockedSubnets(final List<Subnet> blockedSubnets) {
        this.blockedSubnets = blockedSubnets;
    }
    
    public SessionFilter getSessionFilter() {
        return this.sessionFilter;
    }
    
    public void setSessionFilter(final SessionFilter sessionFilter) {
        this.sessionFilter = sessionFilter;
    }
}
