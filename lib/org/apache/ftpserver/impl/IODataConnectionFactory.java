// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;
import org.apache.ftpserver.ssl.ClientAuth;
import java.net.SocketAddress;
import javax.net.ssl.SSLSocket;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.DataConnection;
import org.apache.ftpserver.DataConnectionException;
import org.apache.ftpserver.ssl.SslConfiguration;
import java.net.InetSocketAddress;
import org.apache.ftpserver.DataConnectionConfiguration;
import org.slf4j.LoggerFactory;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;

public class IODataConnectionFactory implements ServerDataConnectionFactory
{
    private final Logger LOG;
    private FtpServerContext serverContext;
    private Socket dataSoc;
    ServerSocket servSoc;
    InetAddress address;
    int port;
    long requestTime;
    boolean passive;
    boolean secure;
    private boolean isZip;
    InetAddress serverControlAddress;
    FtpIoSession session;
    
    public IODataConnectionFactory(final FtpServerContext serverContext, final FtpIoSession session) {
        this.LOG = LoggerFactory.getLogger(IODataConnectionFactory.class);
        this.port = 0;
        this.requestTime = 0L;
        this.passive = false;
        this.secure = false;
        this.isZip = false;
        this.session = session;
        this.serverContext = serverContext;
        if (session != null && session.getListener() != null && session.getListener().getDataConnectionConfiguration().isImplicitSsl()) {
            this.secure = true;
        }
    }
    
    @Override
    public synchronized void closeDataConnection() {
        if (this.dataSoc != null) {
            try {
                this.dataSoc.close();
            }
            catch (Exception ex) {
                this.LOG.warn("FtpDataConnection.closeDataSocket()", ex);
            }
            this.dataSoc = null;
        }
        if (this.servSoc != null) {
            try {
                this.servSoc.close();
            }
            catch (Exception ex) {
                this.LOG.warn("FtpDataConnection.closeDataSocket()", ex);
            }
            if (this.session != null) {
                final DataConnectionConfiguration dcc = this.session.getListener().getDataConnectionConfiguration();
                if (dcc != null) {
                    dcc.releasePassivePort(this.port);
                }
            }
            this.servSoc = null;
        }
        this.requestTime = 0L;
    }
    
    @Override
    public synchronized void initActiveDataConnection(final InetSocketAddress address) {
        this.closeDataConnection();
        this.passive = false;
        this.address = address.getAddress();
        this.port = address.getPort();
        this.requestTime = System.currentTimeMillis();
    }
    
    private SslConfiguration getSslConfiguration() {
        final DataConnectionConfiguration dataCfg = this.session.getListener().getDataConnectionConfiguration();
        SslConfiguration configuration = dataCfg.getSslConfiguration();
        if (configuration == null) {
            configuration = this.session.getListener().getSslConfiguration();
        }
        return configuration;
    }
    
    @Override
    public synchronized InetSocketAddress initPassiveDataConnection() throws DataConnectionException {
        this.LOG.debug("Initiating passive data connection");
        this.closeDataConnection();
        final int passivePort = this.session.getListener().getDataConnectionConfiguration().requestPassivePort();
        if (passivePort == -1) {
            this.servSoc = null;
            throw new DataConnectionException("Cannot find an available passive port.");
        }
        try {
            final DataConnectionConfiguration dataCfg = this.session.getListener().getDataConnectionConfiguration();
            final String passiveAddress = dataCfg.getPassiveAddress();
            if (passiveAddress == null) {
                this.address = this.serverControlAddress;
            }
            else {
                this.address = this.resolveAddress(dataCfg.getPassiveAddress());
            }
            if (this.secure) {
                this.LOG.debug("Opening SSL passive data connection on address \"{}\" and port {}", this.address, passivePort);
                final SslConfiguration ssl = this.getSslConfiguration();
                if (ssl == null) {
                    throw new DataConnectionException("Data connection SSL required but not configured.");
                }
                this.servSoc = new ServerSocket(passivePort, 0, this.address);
                this.LOG.debug("SSL Passive data connection created on address \"{}\" and port {}", this.address, passivePort);
            }
            else {
                this.LOG.debug("Opening passive data connection on address \"{}\" and port {}", this.address, passivePort);
                this.servSoc = new ServerSocket(passivePort, 0, this.address);
                this.LOG.debug("Passive data connection created on address \"{}\" and port {}", this.address, passivePort);
            }
            this.port = this.servSoc.getLocalPort();
            this.servSoc.setSoTimeout(dataCfg.getIdleTime() * 1000);
            this.passive = true;
            this.requestTime = System.currentTimeMillis();
            return new InetSocketAddress(this.address, this.port);
        }
        catch (Exception ex) {
            this.closeDataConnection();
            throw new DataConnectionException("Failed to initate passive data connection: " + ex.getMessage(), ex);
        }
    }
    
    @Override
    public InetAddress getInetAddress() {
        return this.address;
    }
    
    @Override
    public int getPort() {
        return this.port;
    }
    
    @Override
    public DataConnection openConnection() throws Exception {
        return new IODataConnection(this.createDataSocket(), this.session, this);
    }
    
    private synchronized Socket createDataSocket() throws Exception {
        this.dataSoc = null;
        final DataConnectionConfiguration dataConfig = this.session.getListener().getDataConnectionConfiguration();
        try {
            if (!this.passive) {
                if (this.secure) {
                    this.LOG.debug("Opening secure active data connection");
                    final SslConfiguration ssl = this.getSslConfiguration();
                    if (ssl == null) {
                        throw new FtpException("Data connection SSL not configured");
                    }
                    final SSLSocketFactory socFactory = ssl.getSocketFactory();
                    final SSLSocket ssoc = (SSLSocket)socFactory.createSocket();
                    ssoc.setUseClientMode(false);
                    if (ssl.getEnabledCipherSuites() != null) {
                        ssoc.setEnabledCipherSuites(ssl.getEnabledCipherSuites());
                    }
                    this.dataSoc = ssoc;
                }
                else {
                    this.LOG.debug("Opening active data connection");
                    this.dataSoc = new Socket();
                }
                this.dataSoc.setReuseAddress(true);
                InetAddress localAddr = this.resolveAddress(dataConfig.getActiveLocalAddress());
                if (localAddr == null) {
                    localAddr = ((InetSocketAddress)this.session.getLocalAddress()).getAddress();
                }
                final SocketAddress localSocketAddress = new InetSocketAddress(localAddr, dataConfig.getActiveLocalPort());
                this.LOG.debug("Binding active data connection to {}", localSocketAddress);
                this.dataSoc.bind(localSocketAddress);
                this.dataSoc.connect(new InetSocketAddress(this.address, this.port));
            }
            else {
                if (this.secure) {
                    this.LOG.debug("Opening secure passive data connection");
                    final SslConfiguration ssl = this.getSslConfiguration();
                    if (ssl == null) {
                        throw new FtpException("Data connection SSL not configured");
                    }
                    final SSLSocketFactory ssocketFactory = ssl.getSocketFactory();
                    final Socket serverSocket = this.servSoc.accept();
                    final SSLSocket sslSocket = (SSLSocket)ssocketFactory.createSocket(serverSocket, serverSocket.getInetAddress().getHostAddress(), serverSocket.getPort(), true);
                    sslSocket.setUseClientMode(false);
                    if (ssl.getClientAuth() == ClientAuth.NEED) {
                        sslSocket.setNeedClientAuth(true);
                    }
                    else if (ssl.getClientAuth() == ClientAuth.WANT) {
                        sslSocket.setWantClientAuth(true);
                    }
                    if (ssl.getEnabledCipherSuites() != null) {
                        sslSocket.setEnabledCipherSuites(ssl.getEnabledCipherSuites());
                    }
                    this.dataSoc = sslSocket;
                }
                else {
                    this.LOG.debug("Opening passive data connection");
                    this.dataSoc = this.servSoc.accept();
                }
                if (dataConfig.isPassiveIpCheck()) {
                    final InetAddress remoteAddress = ((InetSocketAddress)this.session.getRemoteAddress()).getAddress();
                    final InetAddress dataSocketAddress = this.dataSoc.getInetAddress();
                    if (!dataSocketAddress.equals(remoteAddress)) {
                        this.LOG.warn("Passive IP Check failed. Closing data connection from " + dataSocketAddress + " as it does not match the expected address " + remoteAddress);
                        this.closeDataConnection();
                        return null;
                    }
                }
                final DataConnectionConfiguration dataCfg = this.session.getListener().getDataConnectionConfiguration();
                this.dataSoc.setSoTimeout(dataCfg.getIdleTime() * 1000);
                this.LOG.debug("Passive data connection opened");
            }
        }
        catch (Exception ex) {
            this.closeDataConnection();
            this.LOG.warn("FtpDataConnection.getDataSocket()", ex);
            throw ex;
        }
        this.dataSoc.setSoTimeout(dataConfig.getIdleTime() * 1000);
        if (this.dataSoc instanceof SSLSocket) {
            ((SSLSocket)this.dataSoc).startHandshake();
        }
        return this.dataSoc;
    }
    
    private InetAddress resolveAddress(final String host) throws DataConnectionException {
        if (host == null) {
            return null;
        }
        try {
            return InetAddress.getByName(host);
        }
        catch (UnknownHostException ex) {
            throw new DataConnectionException("Failed to resolve address", ex);
        }
    }
    
    @Override
    public boolean isSecure() {
        return this.secure;
    }
    
    @Override
    public void setSecure(final boolean secure) {
        this.secure = secure;
    }
    
    @Override
    public boolean isZipMode() {
        return this.isZip;
    }
    
    @Override
    public void setZipMode(final boolean zip) {
        this.isZip = zip;
    }
    
    @Override
    public synchronized boolean isTimeout(final long currTime) {
        if (this.requestTime == 0L) {
            return false;
        }
        if (this.dataSoc != null) {
            return false;
        }
        final int maxIdleTime = this.session.getListener().getDataConnectionConfiguration().getIdleTime() * 1000;
        return maxIdleTime != 0 && currTime - this.requestTime >= maxIdleTime;
    }
    
    @Override
    public void dispose() {
        this.closeDataConnection();
    }
    
    @Override
    public void setServerControlAddress(final InetAddress serverControlAddress) {
        this.serverControlAddress = serverControlAddress;
    }
}
