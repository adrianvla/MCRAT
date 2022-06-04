// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.codec;

public class ProtocolDecoderException extends ProtocolCodecException
{
    private static final long serialVersionUID = 3545799879533408565L;
    private String hexdump;
    
    public ProtocolDecoderException() {
    }
    
    public ProtocolDecoderException(final String message) {
        super(message);
    }
    
    public ProtocolDecoderException(final Throwable cause) {
        super(cause);
    }
    
    public ProtocolDecoderException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public String getMessage() {
        String message = super.getMessage();
        if (message == null) {
            message = "";
        }
        if (this.hexdump != null) {
            return message + ((message.length() > 0) ? " " : "") + "(Hexdump: " + this.hexdump + ')';
        }
        return message;
    }
    
    public String getHexdump() {
        return this.hexdump;
    }
    
    public void setHexdump(final String hexdump) {
        if (this.hexdump != null) {
            throw new IllegalStateException("Hexdump cannot be set more than once.");
        }
        this.hexdump = hexdump;
    }
}
