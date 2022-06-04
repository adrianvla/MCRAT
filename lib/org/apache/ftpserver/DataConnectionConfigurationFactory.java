// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver;

import java.net.UnknownHostException;
import java.net.InetAddress;
import org.apache.ftpserver.impl.DefaultDataConnectionConfiguration;
import java.util.Collections;
import org.slf4j.LoggerFactory;
import org.apache.ftpserver.impl.PassivePorts;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.slf4j.Logger;

public class DataConnectionConfigurationFactory
{
    private Logger log;
    private int idleTime;
    private SslConfiguration ssl;
    private boolean activeEnabled;
    private String activeLocalAddress;
    private int activeLocalPort;
    private boolean activeIpCheck;
    private String passiveAddress;
    private String passiveExternalAddress;
    private PassivePorts passivePorts;
    private boolean passiveIpCheck;
    private boolean implicitSsl;
    
    public DataConnectionConfigurationFactory() {
        this.log = LoggerFactory.getLogger(DataConnectionConfigurationFactory.class);
        this.idleTime = 300;
        this.activeEnabled = true;
        this.activeLocalPort = 0;
        this.activeIpCheck = false;
        this.passivePorts = new PassivePorts(Collections.emptySet(), true);
        this.passiveIpCheck = false;
    }
    
    public DataConnectionConfiguration createDataConnectionConfiguration() {
        this.checkValidAddresses();
        return new DefaultDataConnectionConfiguration(this.idleTime, this.ssl, this.activeEnabled, this.activeIpCheck, this.activeLocalAddress, this.activeLocalPort, this.passiveAddress, this.passivePorts, this.passiveExternalAddress, this.passiveIpCheck, this.implicitSsl);
    }
    
    private void checkValidAddresses() {
        try {
            InetAddress.getByName(this.passiveAddress);
            InetAddress.getByName(this.passiveExternalAddress);
        }
        catch (UnknownHostException ex) {
            throw new FtpServerConfigurationException("Unknown host", ex);
        }
    }
    
    public int getIdleTime() {
        return this.idleTime;
    }
    
    public void setIdleTime(final int idleTime) {
        this.idleTime = idleTime;
    }
    
    public boolean isActiveEnabled() {
        return this.activeEnabled;
    }
    
    public void setActiveEnabled(final boolean activeEnabled) {
        this.activeEnabled = activeEnabled;
    }
    
    public boolean isActiveIpCheck() {
        return this.activeIpCheck;
    }
    
    public void setActiveIpCheck(final boolean activeIpCheck) {
        this.activeIpCheck = activeIpCheck;
    }
    
    public String getActiveLocalAddress() {
        return this.activeLocalAddress;
    }
    
    public void setActiveLocalAddress(final String activeLocalAddress) {
        this.activeLocalAddress = activeLocalAddress;
    }
    
    public int getActiveLocalPort() {
        return this.activeLocalPort;
    }
    
    public void setActiveLocalPort(final int activeLocalPort) {
        this.activeLocalPort = activeLocalPort;
    }
    
    public String getPassiveAddress() {
        return this.passiveAddress;
    }
    
    public void setPassiveAddress(final String passiveAddress) {
        this.passiveAddress = passiveAddress;
    }
    
    public String getPassiveExternalAddress() {
        return this.passiveExternalAddress;
    }
    
    public void setPassiveExternalAddress(final String passiveExternalAddress) {
        this.passiveExternalAddress = passiveExternalAddress;
    }
    
    public boolean isPassiveIpCheck() {
        return this.passiveIpCheck;
    }
    
    public void setPassiveIpCheck(final boolean passiveIpCheck) {
        this.passiveIpCheck = passiveIpCheck;
    }
    
    public synchronized int requestPassivePort() {
        int dataPort = -1;
        int loopTimes = 2;
        final Thread currThread = Thread.currentThread();
        while (dataPort == -1 && --loopTimes >= 0 && !currThread.isInterrupted()) {
            dataPort = this.passivePorts.reserveNextPort();
            if (dataPort == -1) {
                try {
                    this.log.info("We're waiting for a passive port, might be stuck");
                    this.wait();
                }
                catch (InterruptedException ex) {}
            }
        }
        return dataPort;
    }
    
    public String getPassivePorts() {
        return this.passivePorts.toString();
    }
    
    public void setPassivePorts(final String passivePorts) {
        this.passivePorts = new PassivePorts(passivePorts, true);
    }
    
    public synchronized void releasePassivePort(final int port) {
        this.passivePorts.releasePort(port);
        this.notify();
    }
    
    public SslConfiguration getSslConfiguration() {
        return this.ssl;
    }
    
    public void setSslConfiguration(final SslConfiguration ssl) {
        this.ssl = ssl;
    }
    
    public boolean isImplicitSsl() {
        return this.implicitSsl;
    }
    
    public void setImplicitSsl(final boolean implicitSsl) {
        this.implicitSsl = implicitSsl;
    }
}
