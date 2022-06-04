// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.service;

import org.apache.mina.core.session.IoSession;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.net.SocketAddress;

public interface IoAcceptor extends IoService
{
    SocketAddress getLocalAddress();
    
    Set<SocketAddress> getLocalAddresses();
    
    SocketAddress getDefaultLocalAddress();
    
    List<SocketAddress> getDefaultLocalAddresses();
    
    void setDefaultLocalAddress(final SocketAddress p0);
    
    void setDefaultLocalAddresses(final SocketAddress p0, final SocketAddress... p1);
    
    void setDefaultLocalAddresses(final Iterable<? extends SocketAddress> p0);
    
    void setDefaultLocalAddresses(final List<? extends SocketAddress> p0);
    
    boolean isCloseOnDeactivation();
    
    void setCloseOnDeactivation(final boolean p0);
    
    void bind() throws IOException;
    
    void bind(final SocketAddress p0) throws IOException;
    
    void bind(final SocketAddress p0, final SocketAddress... p1) throws IOException;
    
    void bind(final SocketAddress... p0) throws IOException;
    
    void bind(final Iterable<? extends SocketAddress> p0) throws IOException;
    
    void unbind();
    
    void unbind(final SocketAddress p0);
    
    void unbind(final SocketAddress p0, final SocketAddress... p1);
    
    void unbind(final Iterable<? extends SocketAddress> p0);
    
    IoSession newSession(final SocketAddress p0, final SocketAddress p1);
}
