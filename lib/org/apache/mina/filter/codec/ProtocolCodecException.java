// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.codec;

public class ProtocolCodecException extends Exception
{
    private static final long serialVersionUID = 5939878548186330695L;
    
    public ProtocolCodecException() {
    }
    
    public ProtocolCodecException(final String message) {
        super(message);
    }
    
    public ProtocolCodecException(final Throwable cause) {
        super(cause);
    }
    
    public ProtocolCodecException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
