// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.transport.socket.nio;

import org.apache.mina.core.session.AbstractIoSession;
import java.util.Collection;
import java.util.Iterator;
import java.net.ServerSocket;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import org.apache.mina.core.service.TransportMetadata;
import java.util.concurrent.Executor;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.transport.socket.DefaultSocketSessionConfig;
import java.nio.channels.spi.SelectorProvider;
import java.nio.channels.Selector;
import org.apache.mina.transport.socket.SocketAcceptor;
import java.nio.channels.ServerSocketChannel;
import org.apache.mina.core.polling.AbstractPollingIoAcceptor;

public final class NioSocketAcceptor extends AbstractPollingIoAcceptor<NioSession, ServerSocketChannel> implements SocketAcceptor
{
    private volatile Selector selector;
    private volatile SelectorProvider selectorProvider;
    
    public NioSocketAcceptor() {
        super(new DefaultSocketSessionConfig(), NioProcessor.class);
        this.selectorProvider = null;
        ((DefaultSocketSessionConfig)this.getSessionConfig()).init(this);
    }
    
    public NioSocketAcceptor(final int processorCount) {
        super(new DefaultSocketSessionConfig(), NioProcessor.class, processorCount);
        this.selectorProvider = null;
        ((DefaultSocketSessionConfig)this.getSessionConfig()).init(this);
    }
    
    public NioSocketAcceptor(final IoProcessor<NioSession> processor) {
        super(new DefaultSocketSessionConfig(), processor);
        this.selectorProvider = null;
        ((DefaultSocketSessionConfig)this.getSessionConfig()).init(this);
    }
    
    public NioSocketAcceptor(final Executor executor, final IoProcessor<NioSession> processor) {
        super(new DefaultSocketSessionConfig(), executor, processor);
        this.selectorProvider = null;
        ((DefaultSocketSessionConfig)this.getSessionConfig()).init(this);
    }
    
    public NioSocketAcceptor(final int processorCount, final SelectorProvider selectorProvider) {
        super(new DefaultSocketSessionConfig(), NioProcessor.class, processorCount, selectorProvider);
        this.selectorProvider = null;
        ((DefaultSocketSessionConfig)this.getSessionConfig()).init(this);
        this.selectorProvider = selectorProvider;
    }
    
    @Override
    protected void init() throws Exception {
        this.selector = Selector.open();
    }
    
    @Override
    protected void init(final SelectorProvider selectorProvider) throws Exception {
        this.selectorProvider = selectorProvider;
        if (selectorProvider == null) {
            this.selector = Selector.open();
        }
        else {
            this.selector = selectorProvider.openSelector();
        }
    }
    
    @Override
    protected void destroy() throws Exception {
        if (this.selector != null) {
            this.selector.close();
        }
    }
    
    @Override
    public TransportMetadata getTransportMetadata() {
        return NioSocketSession.METADATA;
    }
    
    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress)super.getLocalAddress();
    }
    
    @Override
    public InetSocketAddress getDefaultLocalAddress() {
        return (InetSocketAddress)super.getDefaultLocalAddress();
    }
    
    @Override
    public void setDefaultLocalAddress(final InetSocketAddress localAddress) {
        this.setDefaultLocalAddress(localAddress);
    }
    
    @Override
    protected NioSession accept(final IoProcessor<NioSession> processor, final ServerSocketChannel handle) throws Exception {
        SelectionKey key = null;
        if (handle != null) {
            key = handle.keyFor(this.selector);
        }
        if (key == null || !key.isValid() || !key.isAcceptable()) {
            return null;
        }
        final SocketChannel ch = handle.accept();
        if (ch == null) {
            return null;
        }
        return new NioSocketSession(this, processor, ch);
    }
    
    @Override
    protected ServerSocketChannel open(final SocketAddress localAddress) throws Exception {
        ServerSocketChannel channel = null;
        if (this.selectorProvider != null) {
            channel = this.selectorProvider.openServerSocketChannel();
        }
        else {
            channel = ServerSocketChannel.open();
        }
        boolean success = false;
        try {
            channel.configureBlocking(false);
            final ServerSocket socket = channel.socket();
            socket.setReuseAddress(this.isReuseAddress());
            try {
                socket.bind(localAddress, this.getBacklog());
            }
            catch (IOException ioe) {
                final String newMessage = "Error while binding on " + localAddress + "\n" + "original message : " + ioe.getMessage();
                final Exception e = new IOException(newMessage);
                e.initCause(ioe.getCause());
                channel.close();
                throw e;
            }
            channel.register(this.selector, 16);
            success = true;
        }
        finally {
            if (!success) {
                this.close(channel);
            }
        }
        return channel;
    }
    
    @Override
    protected SocketAddress localAddress(final ServerSocketChannel handle) throws Exception {
        return handle.socket().getLocalSocketAddress();
    }
    
    @Override
    protected int select() throws Exception {
        return this.selector.select();
    }
    
    @Override
    protected Iterator<ServerSocketChannel> selectedHandles() {
        return new ServerSocketChannelIterator((Collection)this.selector.selectedKeys());
    }
    
    @Override
    protected void close(final ServerSocketChannel handle) throws Exception {
        final SelectionKey key = handle.keyFor(this.selector);
        if (key != null) {
            key.cancel();
        }
        handle.close();
    }
    
    @Override
    protected void wakeup() {
        this.selector.wakeup();
    }
    
    private static class ServerSocketChannelIterator implements Iterator<ServerSocketChannel>
    {
        private final Iterator<SelectionKey> iterator;
        
        private ServerSocketChannelIterator(final Collection<SelectionKey> selectedKeys) {
            this.iterator = selectedKeys.iterator();
        }
        
        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }
        
        @Override
        public ServerSocketChannel next() {
            final SelectionKey key = this.iterator.next();
            if (key.isValid() && key.isAcceptable()) {
                return (ServerSocketChannel)key.channel();
            }
            return null;
        }
        
        @Override
        public void remove() {
            this.iterator.remove();
        }
    }
}
