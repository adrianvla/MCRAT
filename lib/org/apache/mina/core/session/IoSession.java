// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.session;

import org.apache.mina.core.write.WriteRequest;
import java.util.Set;
import org.apache.mina.core.future.CloseFuture;
import java.net.SocketAddress;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoService;

public interface IoSession
{
    long getId();
    
    IoService getService();
    
    IoHandler getHandler();
    
    IoSessionConfig getConfig();
    
    IoFilterChain getFilterChain();
    
    WriteRequestQueue getWriteRequestQueue();
    
    TransportMetadata getTransportMetadata();
    
    ReadFuture read();
    
    WriteFuture write(final Object p0);
    
    WriteFuture write(final Object p0, final SocketAddress p1);
    
    @Deprecated
    CloseFuture close(final boolean p0);
    
    CloseFuture closeNow();
    
    CloseFuture closeOnFlush();
    
    @Deprecated
    CloseFuture close();
    
    @Deprecated
    Object getAttachment();
    
    @Deprecated
    Object setAttachment(final Object p0);
    
    Object getAttribute(final Object p0);
    
    Object getAttribute(final Object p0, final Object p1);
    
    Object setAttribute(final Object p0, final Object p1);
    
    Object setAttribute(final Object p0);
    
    Object setAttributeIfAbsent(final Object p0, final Object p1);
    
    Object setAttributeIfAbsent(final Object p0);
    
    Object removeAttribute(final Object p0);
    
    boolean removeAttribute(final Object p0, final Object p1);
    
    boolean replaceAttribute(final Object p0, final Object p1, final Object p2);
    
    boolean containsAttribute(final Object p0);
    
    Set<Object> getAttributeKeys();
    
    boolean isConnected();
    
    boolean isActive();
    
    boolean isClosing();
    
    boolean isSecured();
    
    CloseFuture getCloseFuture();
    
    SocketAddress getRemoteAddress();
    
    SocketAddress getLocalAddress();
    
    SocketAddress getServiceAddress();
    
    void setCurrentWriteRequest(final WriteRequest p0);
    
    void suspendRead();
    
    void suspendWrite();
    
    void resumeRead();
    
    void resumeWrite();
    
    boolean isReadSuspended();
    
    boolean isWriteSuspended();
    
    void updateThroughput(final long p0, final boolean p1);
    
    long getReadBytes();
    
    long getWrittenBytes();
    
    long getReadMessages();
    
    long getWrittenMessages();
    
    double getReadBytesThroughput();
    
    double getWrittenBytesThroughput();
    
    double getReadMessagesThroughput();
    
    double getWrittenMessagesThroughput();
    
    int getScheduledWriteMessages();
    
    long getScheduledWriteBytes();
    
    Object getCurrentWriteMessage();
    
    WriteRequest getCurrentWriteRequest();
    
    long getCreationTime();
    
    long getLastIoTime();
    
    long getLastReadTime();
    
    long getLastWriteTime();
    
    boolean isIdle(final IdleStatus p0);
    
    boolean isReaderIdle();
    
    boolean isWriterIdle();
    
    boolean isBothIdle();
    
    int getIdleCount(final IdleStatus p0);
    
    int getReaderIdleCount();
    
    int getWriterIdleCount();
    
    int getBothIdleCount();
    
    long getLastIdleTime(final IdleStatus p0);
    
    long getLastReaderIdleTime();
    
    long getLastWriterIdleTime();
    
    long getLastBothIdleTime();
}
