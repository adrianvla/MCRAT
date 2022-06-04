// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.session;

import org.apache.mina.core.service.DefaultTransportMetadata;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.core.write.WriteRequest;
import java.io.IOException;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.filterchain.DefaultIoFilterChain;
import java.util.Set;
import java.util.List;
import org.apache.mina.core.service.AbstractIoAcceptor;
import java.util.concurrent.Executor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.service.IoService;
import java.net.SocketAddress;
import org.apache.mina.core.service.TransportMetadata;

public class DummySession extends AbstractIoSession
{
    private static final TransportMetadata TRANSPORT_METADATA;
    private static final SocketAddress ANONYMOUS_ADDRESS;
    private volatile IoService service;
    private volatile IoSessionConfig config;
    private final IoFilterChain filterChain;
    private final IoProcessor<IoSession> processor;
    private volatile IoHandler handler;
    private volatile SocketAddress localAddress;
    private volatile SocketAddress remoteAddress;
    private volatile TransportMetadata transportMetadata;
    
    public DummySession() {
        super(new AbstractIoAcceptor(new AbstractIoSessionConfig() {}, new Executor() {
            @Override
            public void execute(final Runnable command) {
            }
        }) {
            @Override
            protected Set<SocketAddress> bindInternal(final List<? extends SocketAddress> localAddresses) throws Exception {
                throw new UnsupportedOperationException();
            }
            
            @Override
            protected void unbind0(final List<? extends SocketAddress> localAddresses) throws Exception {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public IoSession newSession(final SocketAddress remoteAddress, final SocketAddress localAddress) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public TransportMetadata getTransportMetadata() {
                return DummySession.TRANSPORT_METADATA;
            }
            
            @Override
            protected void dispose0() throws Exception {
            }
            
            @Override
            public IoSessionConfig getSessionConfig() {
                return this.sessionConfig;
            }
        });
        this.config = new AbstractIoSessionConfig() {};
        this.filterChain = new DefaultIoFilterChain(this);
        this.handler = new IoHandlerAdapter();
        this.localAddress = DummySession.ANONYMOUS_ADDRESS;
        this.remoteAddress = DummySession.ANONYMOUS_ADDRESS;
        this.transportMetadata = DummySession.TRANSPORT_METADATA;
        this.processor = new IoProcessor<IoSession>() {
            @Override
            public void add(final IoSession session) {
            }
            
            @Override
            public void flush(final IoSession session) {
                final DummySession s = (DummySession)session;
                final WriteRequest req = s.getWriteRequestQueue().poll(session);
                if (req != null) {
                    final Object m = req.getMessage();
                    if (m instanceof FileRegion) {
                        final FileRegion file = (FileRegion)m;
                        try {
                            file.getFileChannel().position(file.getPosition() + file.getRemainingBytes());
                            file.update(file.getRemainingBytes());
                        }
                        catch (IOException e) {
                            s.getFilterChain().fireExceptionCaught(e);
                        }
                    }
                    DummySession.this.getFilterChain().fireMessageSent(req);
                }
            }
            
            @Override
            public void write(final IoSession session, final WriteRequest writeRequest) {
                final WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();
                writeRequestQueue.offer(session, writeRequest);
                if (!session.isWriteSuspended()) {
                    this.flush(session);
                }
            }
            
            @Override
            public void remove(final IoSession session) {
                if (!session.getCloseFuture().isClosed()) {
                    session.getFilterChain().fireSessionClosed();
                }
            }
            
            @Override
            public void updateTrafficControl(final IoSession session) {
            }
            
            @Override
            public void dispose() {
            }
            
            @Override
            public boolean isDisposed() {
                return false;
            }
            
            @Override
            public boolean isDisposing() {
                return false;
            }
        };
        this.service = super.getService();
        try {
            final IoSessionDataStructureFactory factory = new DefaultIoSessionDataStructureFactory();
            this.setAttributeMap(factory.getAttributeMap(this));
            this.setWriteRequestQueue(factory.getWriteRequestQueue(this));
        }
        catch (Exception e) {
            throw new InternalError();
        }
    }
    
    @Override
    public IoSessionConfig getConfig() {
        return this.config;
    }
    
    public void setConfig(final IoSessionConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config");
        }
        this.config = config;
    }
    
    @Override
    public IoFilterChain getFilterChain() {
        return this.filterChain;
    }
    
    @Override
    public IoHandler getHandler() {
        return this.handler;
    }
    
    public void setHandler(final IoHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler");
        }
        this.handler = handler;
    }
    
    @Override
    public SocketAddress getLocalAddress() {
        return this.localAddress;
    }
    
    @Override
    public SocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }
    
    public void setLocalAddress(final SocketAddress localAddress) {
        if (localAddress == null) {
            throw new IllegalArgumentException("localAddress");
        }
        this.localAddress = localAddress;
    }
    
    public void setRemoteAddress(final SocketAddress remoteAddress) {
        if (remoteAddress == null) {
            throw new IllegalArgumentException("remoteAddress");
        }
        this.remoteAddress = remoteAddress;
    }
    
    @Override
    public IoService getService() {
        return this.service;
    }
    
    public void setService(final IoService service) {
        if (service == null) {
            throw new IllegalArgumentException("service");
        }
        this.service = service;
    }
    
    @Override
    public final IoProcessor<IoSession> getProcessor() {
        return this.processor;
    }
    
    @Override
    public TransportMetadata getTransportMetadata() {
        return this.transportMetadata;
    }
    
    public void setTransportMetadata(final TransportMetadata transportMetadata) {
        if (transportMetadata == null) {
            throw new IllegalArgumentException("transportMetadata");
        }
        this.transportMetadata = transportMetadata;
    }
    
    public void setScheduledWriteBytes(final int byteCount) {
        super.setScheduledWriteBytes(byteCount);
    }
    
    public void setScheduledWriteMessages(final int messages) {
        super.setScheduledWriteMessages(messages);
    }
    
    public void updateThroughput(final boolean force) {
        super.updateThroughput(System.currentTimeMillis(), force);
    }
    
    static {
        TRANSPORT_METADATA = new DefaultTransportMetadata("mina", "dummy", false, false, SocketAddress.class, IoSessionConfig.class, (Class<?>[])new Class[] { Object.class });
        ANONYMOUS_ADDRESS = new SocketAddress() {
            private static final long serialVersionUID = -496112902353454179L;
            
            @Override
            public String toString() {
                return "?";
            }
        };
    }
}
