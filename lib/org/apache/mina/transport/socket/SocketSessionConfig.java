// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.transport.socket;

import org.apache.mina.core.session.IoSessionConfig;

public interface SocketSessionConfig extends IoSessionConfig
{
    boolean isReuseAddress();
    
    void setReuseAddress(final boolean p0);
    
    int getReceiveBufferSize();
    
    void setReceiveBufferSize(final int p0);
    
    int getSendBufferSize();
    
    void setSendBufferSize(final int p0);
    
    int getTrafficClass();
    
    void setTrafficClass(final int p0);
    
    boolean isKeepAlive();
    
    void setKeepAlive(final boolean p0);
    
    boolean isOobInline();
    
    void setOobInline(final boolean p0);
    
    int getSoLinger();
    
    void setSoLinger(final int p0);
    
    boolean isTcpNoDelay();
    
    void setTcpNoDelay(final boolean p0);
}
