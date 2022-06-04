// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.session;

public class IoSessionInitializationException extends RuntimeException
{
    private static final long serialVersionUID = -1205810145763696189L;
    
    public IoSessionInitializationException() {
    }
    
    public IoSessionInitializationException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public IoSessionInitializationException(final String message) {
        super(message);
    }
    
    public IoSessionInitializationException(final Throwable cause) {
        super(cause);
    }
}
