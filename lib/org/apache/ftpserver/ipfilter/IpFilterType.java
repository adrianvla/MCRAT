// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ipfilter;

public enum IpFilterType
{
    ALLOW, 
    DENY;
    
    public static IpFilterType parse(final String value) {
        for (final IpFilterType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid IpFilterType: " + value);
    }
}
