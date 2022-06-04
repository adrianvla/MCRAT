// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.firewall;

import java.net.Inet6Address;
import java.net.Inet4Address;
import java.net.InetAddress;

public class Subnet
{
    private static final int IP_MASK_V4 = Integer.MIN_VALUE;
    private static final long IP_MASK_V6 = Long.MIN_VALUE;
    private static final int BYTE_MASK = 255;
    private InetAddress subnet;
    private int subnetInt;
    private long subnetLong;
    private long subnetMask;
    private int suffix;
    
    public Subnet(final InetAddress subnet, final int mask) {
        if (subnet == null) {
            throw new IllegalArgumentException("Subnet address can not be null");
        }
        if (!(subnet instanceof Inet4Address) && !(subnet instanceof Inet6Address)) {
            throw new IllegalArgumentException("Only IPv4 and IPV6 supported");
        }
        if (subnet instanceof Inet4Address) {
            if (mask < 0 || mask > 32) {
                throw new IllegalArgumentException("Mask has to be an integer between 0 and 32 for an IPV4 address");
            }
            this.subnet = subnet;
            this.subnetInt = this.toInt(subnet);
            this.suffix = mask;
            this.subnetMask = Integer.MIN_VALUE >> mask - 1;
        }
        else {
            if (mask < 0 || mask > 128) {
                throw new IllegalArgumentException("Mask has to be an integer between 0 and 128 for an IPV6 address");
            }
            this.subnet = subnet;
            this.subnetLong = this.toLong(subnet);
            this.suffix = mask;
            this.subnetMask = Long.MIN_VALUE >> mask - 1;
        }
    }
    
    private int toInt(final InetAddress inetAddress) {
        final byte[] address = inetAddress.getAddress();
        int result = 0;
        for (int i = 0; i < address.length; ++i) {
            result <<= 8;
            result |= (address[i] & 0xFF);
        }
        return result;
    }
    
    private long toLong(final InetAddress inetAddress) {
        final byte[] address = inetAddress.getAddress();
        long result = 0L;
        for (int i = 0; i < address.length; ++i) {
            result <<= 8;
            result |= (address[i] & 0xFF);
        }
        return result;
    }
    
    private long toSubnet(final InetAddress address) {
        if (address instanceof Inet4Address) {
            return this.toInt(address) & (int)this.subnetMask;
        }
        return this.toLong(address) & this.subnetMask;
    }
    
    public boolean inSubnet(final InetAddress address) {
        if (address.isAnyLocalAddress()) {
            return true;
        }
        if (address instanceof Inet4Address) {
            return (int)this.toSubnet(address) == this.subnetInt;
        }
        return this.toSubnet(address) == this.subnetLong;
    }
    
    @Override
    public String toString() {
        return this.subnet.getHostAddress() + "/" + this.suffix;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Subnet)) {
            return false;
        }
        final Subnet other = (Subnet)obj;
        return other.subnetInt == this.subnetInt && other.suffix == this.suffix;
    }
}
