// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.transport.socket;

import org.apache.mina.core.service.IoService;

public class DefaultSocketSessionConfig extends AbstractSocketSessionConfig
{
    private static final boolean DEFAULT_REUSE_ADDRESS = false;
    private static final int DEFAULT_TRAFFIC_CLASS = 0;
    private static final boolean DEFAULT_KEEP_ALIVE = false;
    private static final boolean DEFAULT_OOB_INLINE = false;
    private static final int DEFAULT_SO_LINGER = -1;
    private static final boolean DEFAULT_TCP_NO_DELAY = false;
    protected IoService parent;
    private boolean defaultReuseAddress;
    private boolean reuseAddress;
    private int receiveBufferSize;
    private int sendBufferSize;
    private int trafficClass;
    private boolean keepAlive;
    private boolean oobInline;
    private int soLinger;
    private boolean tcpNoDelay;
    
    public DefaultSocketSessionConfig() {
        this.receiveBufferSize = -1;
        this.sendBufferSize = -1;
        this.trafficClass = 0;
        this.keepAlive = false;
        this.oobInline = false;
        this.soLinger = -1;
        this.tcpNoDelay = false;
    }
    
    public void init(final IoService parent) {
        this.parent = parent;
        if (parent instanceof SocketAcceptor) {
            this.defaultReuseAddress = true;
        }
        else {
            this.defaultReuseAddress = false;
        }
        this.reuseAddress = this.defaultReuseAddress;
    }
    
    @Override
    public boolean isReuseAddress() {
        return this.reuseAddress;
    }
    
    @Override
    public void setReuseAddress(final boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }
    
    @Override
    public int getReceiveBufferSize() {
        return this.receiveBufferSize;
    }
    
    @Override
    public void setReceiveBufferSize(final int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }
    
    @Override
    public int getSendBufferSize() {
        return this.sendBufferSize;
    }
    
    @Override
    public void setSendBufferSize(final int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }
    
    @Override
    public int getTrafficClass() {
        return this.trafficClass;
    }
    
    @Override
    public void setTrafficClass(final int trafficClass) {
        this.trafficClass = trafficClass;
    }
    
    @Override
    public boolean isKeepAlive() {
        return this.keepAlive;
    }
    
    @Override
    public void setKeepAlive(final boolean keepAlive) {
        this.keepAlive = keepAlive;
    }
    
    @Override
    public boolean isOobInline() {
        return this.oobInline;
    }
    
    @Override
    public void setOobInline(final boolean oobInline) {
        this.oobInline = oobInline;
    }
    
    @Override
    public int getSoLinger() {
        return this.soLinger;
    }
    
    @Override
    public void setSoLinger(final int soLinger) {
        this.soLinger = soLinger;
    }
    
    @Override
    public boolean isTcpNoDelay() {
        return this.tcpNoDelay;
    }
    
    @Override
    public void setTcpNoDelay(final boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }
    
    @Override
    protected boolean isKeepAliveChanged() {
        return this.keepAlive;
    }
    
    @Override
    protected boolean isOobInlineChanged() {
        return this.oobInline;
    }
    
    @Override
    protected boolean isReceiveBufferSizeChanged() {
        return this.receiveBufferSize != -1;
    }
    
    @Override
    protected boolean isReuseAddressChanged() {
        return this.reuseAddress != this.defaultReuseAddress;
    }
    
    @Override
    protected boolean isSendBufferSizeChanged() {
        return this.sendBufferSize != -1;
    }
    
    @Override
    protected boolean isSoLingerChanged() {
        return this.soLinger != -1;
    }
    
    @Override
    protected boolean isTcpNoDelayChanged() {
        return this.tcpNoDelay;
    }
    
    @Override
    protected boolean isTrafficClassChanged() {
        return this.trafficClass != 0;
    }
}
