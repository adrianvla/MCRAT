// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ipfilter;

import java.util.Iterator;
import java.net.InetSocketAddress;
import org.apache.mina.core.session.IoSession;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.LoggerFactory;
import java.util.Collection;
import java.util.HashSet;
import org.slf4j.Logger;
import org.apache.mina.filter.firewall.Subnet;
import java.util.concurrent.CopyOnWriteArraySet;

public class RemoteIpFilter extends CopyOnWriteArraySet<Subnet> implements SessionFilter
{
    Logger LOGGER;
    private static final long serialVersionUID = 4887092372700628783L;
    private IpFilterType type;
    
    public RemoteIpFilter(final IpFilterType type) {
        this(type, new HashSet<Subnet>(0));
    }
    
    public RemoteIpFilter(final IpFilterType type, final Collection<? extends Subnet> collection) {
        super(collection);
        this.LOGGER = LoggerFactory.getLogger(RemoteIpFilter.class);
        this.type = null;
        this.type = type;
    }
    
    public RemoteIpFilter(final IpFilterType type, final String addresses) throws NumberFormatException, UnknownHostException {
        this.LOGGER = LoggerFactory.getLogger(RemoteIpFilter.class);
        this.type = null;
        this.type = type;
        if (addresses != null) {
            final String[] split;
            final String[] tokens = split = addresses.split("[\\s,]+");
            for (final String token : split) {
                if (token.trim().length() > 0) {
                    this.add(token);
                }
            }
        }
        if (this.LOGGER.isDebugEnabled()) {
            this.LOGGER.debug("Created DefaultIpFilter of type {} with the subnets {}", type, this);
        }
    }
    
    public IpFilterType getType() {
        return this.type;
    }
    
    public void setType(final IpFilterType type) {
        this.type = type;
    }
    
    public boolean add(final String str) throws NumberFormatException, UnknownHostException {
        if (str.trim().length() < 1) {
            throw new IllegalArgumentException("Invalid IP Address or Subnet: " + str);
        }
        final String[] tokens = str.split("/");
        if (tokens.length == 2) {
            return this.add(new Subnet(InetAddress.getByName(tokens[0]), Integer.parseInt(tokens[1])));
        }
        return this.add(new Subnet(InetAddress.getByName(tokens[0]), 32));
    }
    
    @Override
    public boolean accept(final IoSession session) {
        final InetAddress address = ((InetSocketAddress)session.getRemoteAddress()).getAddress();
        switch (this.type) {
            case ALLOW: {
                for (final Subnet subnet : this) {
                    if (subnet.inSubnet(address)) {
                        if (this.LOGGER.isDebugEnabled()) {
                            this.LOGGER.debug("Allowing connection from {} because it matches with the whitelist subnet {}", new Object[] { address, subnet });
                        }
                        return true;
                    }
                }
                if (this.LOGGER.isDebugEnabled()) {
                    this.LOGGER.debug("Denying connection from {} because it does not match any of the whitelist subnets", new Object[] { address });
                }
                return false;
            }
            case DENY: {
                if (this.isEmpty()) {
                    if (this.LOGGER.isDebugEnabled()) {
                        this.LOGGER.debug("Allowing connection from {} because blacklist is empty", new Object[] { address });
                    }
                    return true;
                }
                for (final Subnet subnet : this) {
                    if (subnet.inSubnet(address)) {
                        if (this.LOGGER.isDebugEnabled()) {
                            this.LOGGER.debug("Denying connection from {} because it matches with the blacklist subnet {}", new Object[] { address, subnet });
                        }
                        return false;
                    }
                }
                if (this.LOGGER.isDebugEnabled()) {
                    this.LOGGER.debug("Allowing connection from {} because it does not match any of the blacklist subnets", new Object[] { address });
                }
                return true;
            }
            default: {
                throw new RuntimeException("Unknown or unimplemented filter type: " + this.type);
            }
        }
    }
}
