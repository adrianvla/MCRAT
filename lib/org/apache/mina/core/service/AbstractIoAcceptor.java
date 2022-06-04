// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.service;

import org.apache.mina.core.RuntimeIoException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashSet;
import java.util.Collections;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import org.apache.mina.core.session.IoSessionConfig;
import java.util.Set;
import java.net.SocketAddress;
import java.util.List;

public abstract class AbstractIoAcceptor extends AbstractIoService implements IoAcceptor
{
    private final List<SocketAddress> defaultLocalAddresses;
    private final List<SocketAddress> unmodifiableDefaultLocalAddresses;
    private final Set<SocketAddress> boundAddresses;
    private boolean disconnectOnUnbind;
    protected final Object bindLock;
    
    protected AbstractIoAcceptor(final IoSessionConfig sessionConfig, final Executor executor) {
        super(sessionConfig, executor);
        this.defaultLocalAddresses = new ArrayList<SocketAddress>();
        this.unmodifiableDefaultLocalAddresses = Collections.unmodifiableList((List<? extends SocketAddress>)this.defaultLocalAddresses);
        this.boundAddresses = new HashSet<SocketAddress>();
        this.disconnectOnUnbind = true;
        this.bindLock = new Object();
        this.defaultLocalAddresses.add(null);
    }
    
    @Override
    public SocketAddress getLocalAddress() {
        final Set<SocketAddress> localAddresses = this.getLocalAddresses();
        if (localAddresses.isEmpty()) {
            return null;
        }
        return localAddresses.iterator().next();
    }
    
    @Override
    public final Set<SocketAddress> getLocalAddresses() {
        final Set<SocketAddress> localAddresses = new HashSet<SocketAddress>();
        synchronized (this.boundAddresses) {
            localAddresses.addAll(this.boundAddresses);
        }
        return localAddresses;
    }
    
    @Override
    public SocketAddress getDefaultLocalAddress() {
        if (this.defaultLocalAddresses.isEmpty()) {
            return null;
        }
        return this.defaultLocalAddresses.iterator().next();
    }
    
    @Override
    public final void setDefaultLocalAddress(final SocketAddress localAddress) {
        this.setDefaultLocalAddresses(localAddress, new SocketAddress[0]);
    }
    
    @Override
    public final List<SocketAddress> getDefaultLocalAddresses() {
        return this.unmodifiableDefaultLocalAddresses;
    }
    
    @Override
    public final void setDefaultLocalAddresses(final List<? extends SocketAddress> localAddresses) {
        if (localAddresses == null) {
            throw new IllegalArgumentException("localAddresses");
        }
        this.setDefaultLocalAddresses((Iterable<? extends SocketAddress>)localAddresses);
    }
    
    @Override
    public final void setDefaultLocalAddresses(final Iterable<? extends SocketAddress> localAddresses) {
        if (localAddresses == null) {
            throw new IllegalArgumentException("localAddresses");
        }
        synchronized (this.bindLock) {
            synchronized (this.boundAddresses) {
                if (!this.boundAddresses.isEmpty()) {
                    throw new IllegalStateException("localAddress can't be set while the acceptor is bound.");
                }
                final Collection<SocketAddress> newLocalAddresses = new ArrayList<SocketAddress>();
                for (final SocketAddress a : localAddresses) {
                    this.checkAddressType(a);
                    newLocalAddresses.add(a);
                }
                if (newLocalAddresses.isEmpty()) {
                    throw new IllegalArgumentException("empty localAddresses");
                }
                this.defaultLocalAddresses.clear();
                this.defaultLocalAddresses.addAll(newLocalAddresses);
            }
        }
    }
    
    @Override
    public final void setDefaultLocalAddresses(final SocketAddress firstLocalAddress, SocketAddress... otherLocalAddresses) {
        if (otherLocalAddresses == null) {
            otherLocalAddresses = new SocketAddress[0];
        }
        final Collection<SocketAddress> newLocalAddresses = new ArrayList<SocketAddress>(otherLocalAddresses.length + 1);
        newLocalAddresses.add(firstLocalAddress);
        for (final SocketAddress a : otherLocalAddresses) {
            newLocalAddresses.add(a);
        }
        this.setDefaultLocalAddresses(newLocalAddresses);
    }
    
    @Override
    public final boolean isCloseOnDeactivation() {
        return this.disconnectOnUnbind;
    }
    
    @Override
    public final void setCloseOnDeactivation(final boolean disconnectClientsOnUnbind) {
        this.disconnectOnUnbind = disconnectClientsOnUnbind;
    }
    
    @Override
    public final void bind() throws IOException {
        this.bind(this.getDefaultLocalAddresses());
    }
    
    @Override
    public final void bind(final SocketAddress localAddress) throws IOException {
        if (localAddress == null) {
            throw new IllegalArgumentException("localAddress");
        }
        final List<SocketAddress> localAddresses = new ArrayList<SocketAddress>(1);
        localAddresses.add(localAddress);
        this.bind(localAddresses);
    }
    
    @Override
    public final void bind(final SocketAddress... addresses) throws IOException {
        if (addresses == null || addresses.length == 0) {
            this.bind(this.getDefaultLocalAddresses());
            return;
        }
        final List<SocketAddress> localAddresses = new ArrayList<SocketAddress>(2);
        for (final SocketAddress address : addresses) {
            localAddresses.add(address);
        }
        this.bind(localAddresses);
    }
    
    @Override
    public final void bind(final SocketAddress firstLocalAddress, final SocketAddress... addresses) throws IOException {
        if (firstLocalAddress == null) {
            this.bind(this.getDefaultLocalAddresses());
        }
        if (addresses == null || addresses.length == 0) {
            this.bind(this.getDefaultLocalAddresses());
            return;
        }
        final List<SocketAddress> localAddresses = new ArrayList<SocketAddress>(2);
        localAddresses.add(firstLocalAddress);
        for (final SocketAddress address : addresses) {
            localAddresses.add(address);
        }
        this.bind(localAddresses);
    }
    
    @Override
    public final void bind(final Iterable<? extends SocketAddress> localAddresses) throws IOException {
        if (this.isDisposing()) {
            throw new IllegalStateException("The Accpetor disposed is being disposed.");
        }
        if (localAddresses == null) {
            throw new IllegalArgumentException("localAddresses");
        }
        final List<SocketAddress> localAddressesCopy = new ArrayList<SocketAddress>();
        for (final SocketAddress a : localAddresses) {
            this.checkAddressType(a);
            localAddressesCopy.add(a);
        }
        if (localAddressesCopy.isEmpty()) {
            throw new IllegalArgumentException("localAddresses is empty.");
        }
        boolean activate = false;
        synchronized (this.bindLock) {
            synchronized (this.boundAddresses) {
                if (this.boundAddresses.isEmpty()) {
                    activate = true;
                }
            }
            if (this.getHandler() == null) {
                throw new IllegalStateException("handler is not set.");
            }
            try {
                final Set<SocketAddress> addresses = this.bindInternal(localAddressesCopy);
                synchronized (this.boundAddresses) {
                    this.boundAddresses.addAll(addresses);
                }
            }
            catch (IOException e) {
                throw e;
            }
            catch (RuntimeException e2) {
                throw e2;
            }
            catch (Exception e3) {
                throw new RuntimeIoException("Failed to bind to: " + this.getLocalAddresses(), e3);
            }
        }
        if (activate) {
            this.getListeners().fireServiceActivated();
        }
    }
    
    @Override
    public final void unbind() {
        this.unbind(this.getLocalAddresses());
    }
    
    @Override
    public final void unbind(final SocketAddress localAddress) {
        if (localAddress == null) {
            throw new IllegalArgumentException("localAddress");
        }
        final List<SocketAddress> localAddresses = new ArrayList<SocketAddress>(1);
        localAddresses.add(localAddress);
        this.unbind(localAddresses);
    }
    
    @Override
    public final void unbind(final SocketAddress firstLocalAddress, final SocketAddress... otherLocalAddresses) {
        if (firstLocalAddress == null) {
            throw new IllegalArgumentException("firstLocalAddress");
        }
        if (otherLocalAddresses == null) {
            throw new IllegalArgumentException("otherLocalAddresses");
        }
        final List<SocketAddress> localAddresses = new ArrayList<SocketAddress>();
        localAddresses.add(firstLocalAddress);
        Collections.addAll(localAddresses, otherLocalAddresses);
        this.unbind(localAddresses);
    }
    
    @Override
    public final void unbind(final Iterable<? extends SocketAddress> localAddresses) {
        if (localAddresses == null) {
            throw new IllegalArgumentException("localAddresses");
        }
        boolean deactivate = false;
        synchronized (this.bindLock) {
            synchronized (this.boundAddresses) {
                if (this.boundAddresses.isEmpty()) {
                    return;
                }
                final List<SocketAddress> localAddressesCopy = new ArrayList<SocketAddress>();
                int specifiedAddressCount = 0;
                for (final SocketAddress a : localAddresses) {
                    ++specifiedAddressCount;
                    if (a != null && this.boundAddresses.contains(a)) {
                        localAddressesCopy.add(a);
                    }
                }
                if (specifiedAddressCount == 0) {
                    throw new IllegalArgumentException("localAddresses is empty.");
                }
                if (!localAddressesCopy.isEmpty()) {
                    try {
                        this.unbind0(localAddressesCopy);
                    }
                    catch (RuntimeException e) {
                        throw e;
                    }
                    catch (Exception e2) {
                        throw new RuntimeIoException("Failed to unbind from: " + this.getLocalAddresses(), e2);
                    }
                    this.boundAddresses.removeAll(localAddressesCopy);
                    if (this.boundAddresses.isEmpty()) {
                        deactivate = true;
                    }
                }
            }
        }
        if (deactivate) {
            this.getListeners().fireServiceDeactivated();
        }
    }
    
    protected abstract Set<SocketAddress> bindInternal(final List<? extends SocketAddress> p0) throws Exception;
    
    protected abstract void unbind0(final List<? extends SocketAddress> p0) throws Exception;
    
    @Override
    public String toString() {
        final TransportMetadata m = this.getTransportMetadata();
        return '(' + m.getProviderName() + ' ' + m.getName() + " acceptor: " + (this.isActive() ? ("localAddress(es): " + this.getLocalAddresses() + ", managedSessionCount: " + this.getManagedSessionCount()) : "not bound") + ')';
    }
    
    private void checkAddressType(final SocketAddress a) {
        if (a != null && !this.getTransportMetadata().getAddressType().isAssignableFrom(a.getClass())) {
            throw new IllegalArgumentException("localAddress type: " + a.getClass().getSimpleName() + " (expected: " + this.getTransportMetadata().getAddressType().getSimpleName() + ")");
        }
    }
    
    public static class AcceptorOperationFuture extends ServiceOperationFuture
    {
        private final List<SocketAddress> localAddresses;
        
        public AcceptorOperationFuture(final List<? extends SocketAddress> localAddresses) {
            this.localAddresses = new ArrayList<SocketAddress>(localAddresses);
        }
        
        public final List<SocketAddress> getLocalAddresses() {
            return Collections.unmodifiableList((List<? extends SocketAddress>)this.localAddresses);
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Acceptor operation : ");
            if (this.localAddresses != null) {
                boolean isFirst = true;
                for (final SocketAddress address : this.localAddresses) {
                    if (isFirst) {
                        isFirst = false;
                    }
                    else {
                        sb.append(", ");
                    }
                    sb.append(address);
                }
            }
            return sb.toString();
        }
    }
}
