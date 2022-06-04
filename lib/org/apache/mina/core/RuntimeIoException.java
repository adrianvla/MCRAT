// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core;

public class RuntimeIoException extends RuntimeException
{
    private static final long serialVersionUID = 9029092241311939548L;
    
    public RuntimeIoException() {
    }
    
    public RuntimeIoException(final String message) {
        super(message);
    }
    
    public RuntimeIoException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public RuntimeIoException(final Throwable cause) {
        super(cause);
    }
}
