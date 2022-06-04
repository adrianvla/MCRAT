// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.buffer;

public class BufferDataException extends RuntimeException
{
    private static final long serialVersionUID = -4138189188602563502L;
    
    public BufferDataException() {
    }
    
    public BufferDataException(final String message) {
        super(message);
    }
    
    public BufferDataException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public BufferDataException(final Throwable cause) {
        super(cause);
    }
}
