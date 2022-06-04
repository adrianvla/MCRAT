// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.filterchain;

public class IoFilterLifeCycleException extends RuntimeException
{
    private static final long serialVersionUID = -5542098881633506449L;
    
    public IoFilterLifeCycleException() {
    }
    
    public IoFilterLifeCycleException(final String message) {
        super(message);
    }
    
    public IoFilterLifeCycleException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public IoFilterLifeCycleException(final Throwable cause) {
        super(cause);
    }
}
