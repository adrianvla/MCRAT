// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.codec;

public class RecoverableProtocolDecoderException extends ProtocolDecoderException
{
    private static final long serialVersionUID = -8172624045024880678L;
    
    public RecoverableProtocolDecoderException() {
    }
    
    public RecoverableProtocolDecoderException(final String message) {
        super(message);
    }
    
    public RecoverableProtocolDecoderException(final Throwable cause) {
        super(cause);
    }
    
    public RecoverableProtocolDecoderException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
