// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.transport.socket.nio;

import java.net.SocketException;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.transport.socket.AbstractSocketSessionConfig;
import org.apache.mina.core.service.DefaultTransportMetadata;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSessionConfig;
import java.net.SocketAddress;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.filter.ssl.SslFilter;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ByteChannel;
import java.net.InetSocketAddress;
import org.apache.mina.transport.socket.SocketSessionConfig;
import java.net.Socket;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.TransportMetadata;

class NioSocketSession extends NioSession
{
    static final TransportMetadata METADATA;
    
    public NioSocketSession(final IoService service, final IoProcessor<NioSession> processor, final SocketChannel channel) {
        super(processor, service, channel);
        (this.config = new SessionConfigImpl()).setAll(service.getSessionConfig());
    }
    
    private Socket getSocket() {
        return ((SocketChannel)this.channel).socket();
    }
    
    @Override
    public TransportMetadata getTransportMetadata() {
        return NioSocketSession.METADATA;
    }
    
    @Override
    public SocketSessionConfig getConfig() {
        return (SocketSessionConfig)this.config;
    }
    
    @Override
    SocketChannel getChannel() {
        return (SocketChannel)this.channel;
    }
    
    @Override
    public InetSocketAddress getRemoteAddress() {
        if (this.channel == null) {
            return null;
        }
        final Socket socket = this.getSocket();
        if (socket == null) {
            return null;
        }
        return (InetSocketAddress)socket.getRemoteSocketAddress();
    }
    
    @Override
    public InetSocketAddress getLocalAddress() {
        if (this.channel == null) {
            return null;
        }
        final Socket socket = this.getSocket();
        if (socket == null) {
            return null;
        }
        return (InetSocketAddress)socket.getLocalSocketAddress();
    }
    
    protected void destroy(final NioSession session) throws IOException {
        final ByteChannel ch = session.getChannel();
        final SelectionKey key = session.getSelectionKey();
        if (key != null) {
            key.cancel();
        }
        ch.close();
    }
    
    @Override
    public InetSocketAddress getServiceAddress() {
        return (InetSocketAddress)super.getServiceAddress();
    }
    
    @Override
    public final boolean isSecured() {
        final IoFilterChain chain = this.getFilterChain();
        final IoFilter sslFilter = chain.get(SslFilter.class);
        return sslFilter != null && ((SslFilter)sslFilter).isSslStarted(this);
    }
    
    static {
        METADATA = new DefaultTransportMetadata("nio", "socket", false, true, InetSocketAddress.class, SocketSessionConfig.class, (Class<?>[])new Class[] { IoBuffer.class, FileRegion.class });
    }
    
    private class SessionConfigImpl extends AbstractSocketSessionConfig
    {
        @Override
        public boolean isKeepAlive() {
            try {
                return NioSocketSession.this.getSocket().getKeepAlive();
            }
            catch (SocketException e) {
                throw new RuntimeIoException(e);
            }
        }
        
        @Override
        public void setKeepAlive(final boolean on) {
            try {
                NioSocketSession.this.getSocket().setKeepAlive(on);
            }
            catch (SocketException e) {
                throw new RuntimeIoException(e);
            }
        }
        
        @Override
        public boolean isOobInline() {
            try {
                return NioSocketSession.this.getSocket().getOOBInline();
            }
            catch (SocketException e) {
                throw new RuntimeIoException(e);
            }
        }
        
        @Override
        public void setOobInline(final boolean on) {
            try {
                NioSocketSession.this.getSocket().setOOBInline(on);
            }
            catch (SocketException e) {
                throw new RuntimeIoException(e);
            }
        }
        
        @Override
        public boolean isReuseAddress() {
            try {
                return NioSocketSession.this.getSocket().getReuseAddress();
            }
            catch (SocketException e) {
                throw new RuntimeIoException(e);
            }
        }
        
        @Override
        public void setReuseAddress(final boolean on) {
            try {
                NioSocketSession.this.getSocket().setReuseAddress(on);
            }
            catch (SocketException e) {
                throw new RuntimeIoException(e);
            }
        }
        
        @Override
        public int getSoLinger() {
            try {
                return NioSocketSession.this.getSocket().getSoLinger();
            }
            catch (SocketException e) {
                throw new RuntimeIoException(e);
            }
        }
        
        @Override
        public void setSoLinger(final int linger) {
            try {
                if (linger < 0) {
                    NioSocketSession.this.getSocket().setSoLinger(false, 0);
                }
                else {
                    NioSocketSession.this.getSocket().setSoLinger(true, linger);
                }
            }
            catch (SocketException e) {
                throw new RuntimeIoException(e);
            }
        }
        
        @Override
        public boolean isTcpNoDelay() {
            if (!NioSocketSession.this.isConnected()) {
                return false;
            }
            try {
                return NioSocketSession.this.getSocket().getTcpNoDelay();
            }
            catch (SocketException e) {
                throw new RuntimeIoException(e);
            }
        }
        
        @Override
        public void setTcpNoDelay(final boolean on) {
            try {
                NioSocketSession.this.getSocket().setTcpNoDelay(on);
            }
            catch (SocketException e) {
                throw new RuntimeIoException(e);
            }
        }
        
        @Override
        public int getTrafficClass() {
            try {
                return NioSocketSession.this.getSocket().getTrafficClass();
            }
            catch (SocketException e) {
                throw new RuntimeIoException(e);
            }
        }
        
        @Override
        public void setTrafficClass(final int tc) {
            try {
                NioSocketSession.this.getSocket().setTrafficClass(tc);
            }
            catch (SocketException e) {
                throw new RuntimeIoException(e);
            }
        }
        
        @Override
        public int getSendBufferSize() {
            try {
                return NioSocketSession.this.getSocket().getSendBufferSize();
            }
            catch (SocketException e) {
                throw new RuntimeIoException(e);
            }
        }
        
        @Override
        public void setSendBufferSize(final int size) {
            try {
                NioSocketSession.this.getSocket().setSendBufferSize(size);
            }
            catch (SocketException e) {
                throw new RuntimeIoException(e);
            }
        }
        
        @Override
        public int getReceiveBufferSize() {
            try {
                return NioSocketSession.this.getSocket().getReceiveBufferSize();
            }
            catch (SocketException e) {
                throw new RuntimeIoException(e);
            }
        }
        
        @Override
        public void setReceiveBufferSize(final int size) {
            try {
                NioSocketSession.this.getSocket().setReceiveBufferSize(size);
            }
            catch (SocketException e) {
                throw new RuntimeIoException(e);
            }
        }
    }
}
