// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.session;

import java.io.Serializable;

public final class AttributeKey implements Serializable
{
    private static final long serialVersionUID = -583377473376683096L;
    private final String name;
    
    public AttributeKey(final Class<?> source, final String name) {
        this.name = source.getName() + '.' + name + '@' + Integer.toHexString(this.hashCode());
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    @Override
    public int hashCode() {
        final int h = 629 + ((this.name == null) ? 0 : this.name.hashCode());
        return h;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AttributeKey)) {
            return false;
        }
        final AttributeKey other = (AttributeKey)obj;
        return this.name.equals(other.name);
    }
}
