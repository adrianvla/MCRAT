// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.listener.nio;

import java.util.Iterator;
import java.util.Map;
import org.apache.mina.core.session.IoSession;
import java.util.HashSet;
import org.apache.ftpserver.impl.FtpIoSession;
import java.util.Set;
import java.io.IOException;
import java.net.SocketAddress;
import org.apache.mina.core.service.IoHandler;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.ssl.ClientAuth;
import java.security.GeneralSecurityException;
import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import java.util.concurrent.Executor;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.ftpserver.ipfilter.MinaSessionFilter;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.ftpserver.ipfilter.SessionFilter;
import org.apache.ftpserver.impl.DefaultFtpHandler;
import org.slf4j.LoggerFactory;
import org.apache.mina.filter.firewall.Subnet;
import java.net.InetAddress;
import java.util.List;
import org.apache.ftpserver.DataConnectionConfiguration;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpHandler;
import java.net.InetSocketAddress;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.slf4j.Logger;

public class NioListener extends AbstractListener
{
    private final Logger LOG;
    private SocketAcceptor acceptor;
    private InetSocketAddress address;
    boolean suspended;
    private FtpHandler handler;
    private FtpServerContext context;
    
    @Deprecated
    public NioListener(final String serverAddress, final int port, final boolean implicitSsl, final SslConfiguration sslConfiguration, final DataConnectionConfiguration dataConnectionConfig, final int idleTimeout, final List<InetAddress> blockedAddresses, final List<Subnet> blockedSubnets) {
        super(serverAddress, port, implicitSsl, sslConfiguration, dataConnectionConfig, idleTimeout, blockedAddresses, blockedSubnets);
        this.LOG = LoggerFactory.getLogger(NioListener.class);
        this.suspended = false;
        this.handler = new DefaultFtpHandler();
    }
    
    public NioListener(final String serverAddress, final int port, final boolean implicitSsl, final SslConfiguration sslConfiguration, final DataConnectionConfiguration dataConnectionConfig, final int idleTimeout, final SessionFilter sessionFilter) {
        super(serverAddress, port, implicitSsl, sslConfiguration, dataConnectionConfig, idleTimeout, sessionFilter);
        this.LOG = LoggerFactory.getLogger(NioListener.class);
        this.suspended = false;
        this.handler = new DefaultFtpHandler();
    }
    
    @Override
    public synchronized void start(final FtpServerContext context) {
        if (!this.isStopped()) {
            throw new IllegalStateException("Listener already started");
        }
        try {
            this.context = context;
            this.acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors());
            if (this.getServerAddress() != null) {
                this.address = new InetSocketAddress(this.getServerAddress(), this.getPort());
            }
            else {
                this.address = new InetSocketAddress(this.getPort());
            }
            this.acceptor.setReuseAddress(true);
            this.acceptor.getSessionConfig().setReadBufferSize(2048);
            this.acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, this.getIdleTimeout());
            this.acceptor.getSessionConfig().setReceiveBufferSize(512);
            final MdcInjectionFilter mdcFilter = new MdcInjectionFilter();
            this.acceptor.getFilterChain().addLast("mdcFilter", mdcFilter);
            final SessionFilter sessionFilter = this.getSessionFilter();
            if (sessionFilter != null) {
                this.acceptor.getFilterChain().addLast("sessionFilter", new MinaSessionFilter(sessionFilter));
            }
            this.acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(context.getThreadPoolExecutor()));
            this.acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new FtpServerProtocolCodecFactory()));
            this.acceptor.getFilterChain().addLast("mdcFilter2", mdcFilter);
            this.acceptor.getFilterChain().addLast("logger", new FtpLoggingFilter());
            if (this.isImplicitSsl()) {
                final SslConfiguration ssl = this.getSslConfiguration();
                SslFilter sslFilter;
                try {
                    sslFilter = new SslFilter(ssl.getSSLContext());
                }
                catch (GeneralSecurityException e3) {
                    throw new FtpServerConfigurationException("SSL could not be initialized, check configuration");
                }
                if (ssl.getClientAuth() == ClientAuth.NEED) {
                    sslFilter.setNeedClientAuth(true);
                }
                else if (ssl.getClientAuth() == ClientAuth.WANT) {
                    sslFilter.setWantClientAuth(true);
                }
                if (ssl.getEnabledCipherSuites() != null) {
                    sslFilter.setEnabledCipherSuites(ssl.getEnabledCipherSuites());
                }
                this.acceptor.getFilterChain().addFirst("sslFilter", sslFilter);
            }
            this.handler.init(context, this);
            this.acceptor.setHandler(new FtpHandlerAdapter(context, this.handler));
            try {
                this.acceptor.bind(this.address);
            }
            catch (IOException e) {
                throw new FtpServerConfigurationException("Failed to bind to address " + this.address + ", check configuration", e);
            }
            this.updatePort();
        }
        catch (RuntimeException e2) {
            this.stop();
            throw e2;
        }
    }
    
    private void updatePort() {
        this.setPort(this.acceptor.getLocalAddress().getPort());
    }
    
    @Override
    public synchronized void stop() {
        if (this.acceptor != null) {
            this.acceptor.unbind();
            this.acceptor.dispose();
            this.acceptor = null;
        }
        this.context = null;
    }
    
    @Override
    public boolean isStopped() {
        return this.acceptor == null;
    }
    
    @Override
    public boolean isSuspended() {
        return this.suspended;
    }
    
    @Override
    public synchronized void resume() {
        if (this.acceptor != null && this.suspended) {
            try {
                this.LOG.debug("Resuming listener");
                this.acceptor.bind(this.address);
                this.LOG.debug("Listener resumed");
                this.updatePort();
                this.suspended = false;
            }
            catch (IOException e) {
                this.LOG.error("Failed to resume listener", e);
            }
        }
    }
    
    @Override
    public synchronized void suspend() {
        if (this.acceptor != null && !this.suspended) {
            this.LOG.debug("Suspending listener");
            this.acceptor.unbind();
            this.suspended = true;
            this.LOG.debug("Listener suspended");
        }
    }
    
    @Override
    public synchronized Set<FtpIoSession> getActiveSessions() {
        final Map<Long, IoSession> sessions = this.acceptor.getManagedSessions();
        final Set<FtpIoSession> ftpSessions = new HashSet<FtpIoSession>();
        for (final IoSession session : sessions.values()) {
            ftpSessions.add(new FtpIoSession(session, this.context));
        }
        return ftpSessions;
    }
}
