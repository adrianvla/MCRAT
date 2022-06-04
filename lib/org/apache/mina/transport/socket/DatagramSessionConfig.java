// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.transport.socket;

import org.apache.mina.core.session.IoSessionConfig;

public interface DatagramSessionConfig extends IoSessionConfig
{
    boolean isBroadcast();
    
    void setBroadcast(final boolean p0);
    
    boolean isReuseAddress();
    
    void setReuseAddress(final boolean p0);
    
    int getReceiveBufferSize();
    
    void setReceiveBufferSize(final int p0);
    
    int getSendBufferSize();
    
    void setSendBufferSize(final int p0);
    
    int getTrafficClass();
    
    void setTrafficClass(final int p0);
    
    boolean isCloseOnPortUnreachable();
    
    void setCloseOnPortUnreachable(final boolean p0);
}
