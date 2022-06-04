// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.DataConnectionConfiguration;

public class DefaultDataConnectionConfiguration implements DataConnectionConfiguration
{
    private final int idleTime;
    private final SslConfiguration ssl;
    private final boolean activeEnabled;
    private final String activeLocalAddress;
    private final int activeLocalPort;
    private final boolean activeIpCheck;
    private final String passiveAddress;
    private final String passiveExternalAddress;
    private final PassivePorts passivePorts;
    private final boolean passiveIpCheck;
    private final boolean implicitSsl;
    
    public DefaultDataConnectionConfiguration(final int idleTime, final SslConfiguration ssl, final boolean activeEnabled, final boolean activeIpCheck, final String activeLocalAddress, final int activeLocalPort, final String passiveAddress, final PassivePorts passivePorts, final String passiveExternalAddress, final boolean passiveIpCheck, final boolean implicitSsl) {
        this.idleTime = idleTime;
        this.ssl = ssl;
        this.activeEnabled = activeEnabled;
        this.activeIpCheck = activeIpCheck;
        this.activeLocalAddress = activeLocalAddress;
        this.activeLocalPort = activeLocalPort;
        this.passiveAddress = passiveAddress;
        this.passivePorts = passivePorts;
        this.passiveExternalAddress = passiveExternalAddress;
        this.passiveIpCheck = passiveIpCheck;
        this.implicitSsl = implicitSsl;
    }
    
    @Override
    public int getIdleTime() {
        return this.idleTime;
    }
    
    @Override
    public boolean isActiveEnabled() {
        return this.activeEnabled;
    }
    
    @Override
    public boolean isActiveIpCheck() {
        return this.activeIpCheck;
    }
    
    @Override
    public String getActiveLocalAddress() {
        return this.activeLocalAddress;
    }
    
    @Override
    public int getActiveLocalPort() {
        return this.activeLocalPort;
    }
    
    @Override
    public String getPassiveAddress() {
        return this.passiveAddress;
    }
    
    @Override
    public String getPassiveExernalAddress() {
        return this.passiveExternalAddress;
    }
    
    @Override
    public boolean isPassiveIpCheck() {
        return this.passiveIpCheck;
    }
    
    @Override
    public synchronized int requestPassivePort() {
        return this.passivePorts.reserveNextPort();
    }
    
    @Override
    public String getPassivePorts() {
        return this.passivePorts.toString();
    }
    
    @Override
    public synchronized void releasePassivePort(final int port) {
        this.passivePorts.releasePort(port);
    }
    
    @Override
    public SslConfiguration getSslConfiguration() {
        return this.ssl;
    }
    
    @Override
    public boolean isImplicitSsl() {
        return this.implicitSsl;
    }
}
