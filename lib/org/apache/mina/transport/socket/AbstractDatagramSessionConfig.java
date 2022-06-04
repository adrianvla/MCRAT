// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.transport.socket;

import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.AbstractIoSessionConfig;

public abstract class AbstractDatagramSessionConfig extends AbstractIoSessionConfig implements DatagramSessionConfig
{
    private boolean closeOnPortUnreachable;
    
    public AbstractDatagramSessionConfig() {
        this.closeOnPortUnreachable = true;
    }
    
    @Override
    public void setAll(final IoSessionConfig config) {
        super.setAll(config);
        if (!(config instanceof DatagramSessionConfig)) {
            return;
        }
        if (config instanceof AbstractDatagramSessionConfig) {
            final AbstractDatagramSessionConfig cfg = (AbstractDatagramSessionConfig)config;
            if (cfg.isBroadcastChanged()) {
                this.setBroadcast(cfg.isBroadcast());
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
            if (cfg.isTrafficClassChanged() && this.getTrafficClass() != cfg.getTrafficClass()) {
                this.setTrafficClass(cfg.getTrafficClass());
            }
        }
        else {
            final DatagramSessionConfig cfg2 = (DatagramSessionConfig)config;
            this.setBroadcast(cfg2.isBroadcast());
            this.setReceiveBufferSize(cfg2.getReceiveBufferSize());
            this.setReuseAddress(cfg2.isReuseAddress());
            this.setSendBufferSize(cfg2.getSendBufferSize());
            if (this.getTrafficClass() != cfg2.getTrafficClass()) {
                this.setTrafficClass(cfg2.getTrafficClass());
            }
        }
    }
    
    protected boolean isBroadcastChanged() {
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
    
    protected boolean isTrafficClassChanged() {
        return true;
    }
    
    @Override
    public boolean isCloseOnPortUnreachable() {
        return this.closeOnPortUnreachable;
    }
    
    @Override
    public void setCloseOnPortUnreachable(final boolean closeOnPortUnreachable) {
        this.closeOnPortUnreachable = closeOnPortUnreachable;
    }
}
