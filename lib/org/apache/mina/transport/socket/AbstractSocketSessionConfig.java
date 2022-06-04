// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.transport.socket;

import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.AbstractIoSessionConfig;

public abstract class AbstractSocketSessionConfig extends AbstractIoSessionConfig implements SocketSessionConfig
{
    @Override
    public void setAll(final IoSessionConfig config) {
        super.setAll(config);
        if (!(config instanceof SocketSessionConfig)) {
            return;
        }
        if (config instanceof AbstractSocketSessionConfig) {
            final AbstractSocketSessionConfig cfg = (AbstractSocketSessionConfig)config;
            if (cfg.isKeepAliveChanged()) {
                this.setKeepAlive(cfg.isKeepAlive());
            }
            if (cfg.isOobInlineChanged()) {
                this.setOobInline(cfg.isOobInline());
            }
            if (cfg.isReceiveBufferSizeChanged()) {
                this.setReceiveBufferSize(cfg.getReceiveBufferSize());
            }
            if (cfg.isReuseAddressChanged()) {
                this.setReuseAddress(cfg.isReuseAddress());
            }
            if (cfg.isSendBufferSizeChanged()) {
                this.setSendBufferSize(cfg.getSendBufferSize());
            }
            if (cfg.isSoLingerChanged()) {
                this.setSoLinger(cfg.getSoLinger());
            }
            if (cfg.isTcpNoDelayChanged()) {
                this.setTcpNoDelay(cfg.isTcpNoDelay());
            }
            if (cfg.isTrafficClassChanged() && this.getTrafficClass() != cfg.getTrafficClass()) {
                this.setTrafficClass(cfg.getTrafficClass());
            }
        }
        else {
            final SocketSessionConfig cfg2 = (SocketSessionConfig)config;
            this.setKeepAlive(cfg2.isKeepAlive());
            this.setOobInline(cfg2.isOobInline());
            this.setReceiveBufferSize(cfg2.getReceiveBufferSize());
            this.setReuseAddress(cfg2.isReuseAddress());
            this.setSendBufferSize(cfg2.getSendBufferSize());
            this.setSoLinger(cfg2.getSoLinger());
            this.setTcpNoDelay(cfg2.isTcpNoDelay());
            if (this.getTrafficClass() != cfg2.getTrafficClass()) {
                this.setTrafficClass(cfg2.getTrafficClass());
            }
        }
    }
    
    protected boolean isKeepAliveChanged() {
        return true;
    }
    
    protected boolean isOobInlineChanged() {
        return true;
    }
    
    protected boolean isReceiveBufferSizeChanged() {
        return true;
    }
    
    protected boolean isReuseAddressChanged() {
        return true;
    }
    
    protected boolean isSendBufferSizeChanged() {
        return true;
    }
    
    protected boolean isSoLingerChanged() {
        return true;
    }
    
    protected boolean isTcpNoDelayChanged() {
        return true;
    }
    
    protected boolean isTrafficClassChanged() {
        return true;
    }
}
