// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.codec;

public class ProtocolEncoderException extends ProtocolCodecException
{
    private static final long serialVersionUID = 8752989973624459604L;
    
    public ProtocolEncoderException() {
    }
    
    public ProtocolEncoderException(final String message) {
        super(message);
    }
    
    public ProtocolEncoderException(final Throwable cause) {
        super(cause);
    }
    
    public ProtocolEncoderException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
