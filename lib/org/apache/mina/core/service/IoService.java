// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.service;

import org.apache.mina.core.session.IoSessionDataStructureFactory;
import org.apache.mina.core.future.WriteFuture;
import java.util.Set;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSession;
import java.util.Map;

public interface IoService
{
    TransportMetadata getTransportMetadata();
    
    void addListener(final IoServiceListener p0);
    
    void removeListener(final IoServiceListener p0);
    
    boolean isDisposing();
    
    boolean isDisposed();
    
    void dispose();
    
    void dispose(final boolean p0);
    
    IoHandler getHandler();
    
    void setHandler(final IoHandler p0);
    
    Map<Long, IoSession> getManagedSessions();
    
    int getManagedSessionCount();
    
    IoSessionConfig getSessionConfig();
    
    IoFilterChainBuilder getFilterChainBuilder();
    
    void setFilterChainBuilder(final IoFilterChainBuilder p0);
    
    DefaultIoFilterChainBuilder getFilterChain();
    
    boolean isActive();
    
    long getActivationTime();
    
    Set<WriteFuture> broadcast(final Object p0);
    
    IoSessionDataStructureFactory getSessionDataStructureFactory();
    
    void setSessionDataStructureFactory(final IoSessionDataStructureFactory p0);
    
    int getScheduledWriteBytes();
    
    int getScheduledWriteMessages();
    
    IoServiceStatistics getStatistics();
}
