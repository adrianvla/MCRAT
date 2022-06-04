// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.write;

import java.util.Collection;

public class WriteTimeoutException extends WriteException
{
    private static final long serialVersionUID = 3906931157944579121L;
    
    public WriteTimeoutException(final Collection<WriteRequest> requests, final String message, final Throwable cause) {
        super(requests, message, cause);
    }
    
    public WriteTimeoutException(final Collection<WriteRequest> requests, final String s) {
        super(requests, s);
    }
    
    public WriteTimeoutException(final Collection<WriteRequest> requests, final Throwable cause) {
        super(requests, cause);
    }
    
    public WriteTimeoutException(final Collection<WriteRequest> requests) {
        super(requests);
    }
    
    public WriteTimeoutException(final WriteRequest request, final String message, final Throwable cause) {
        super(request, message, cause);
    }
    
    public WriteTimeoutException(final WriteRequest request, final String s) {
        super(request, s);
    }
    
    public WriteTimeoutException(final WriteRequest request, final Throwable cause) {
        super(request, cause);
    }
    
    public WriteTimeoutException(final WriteRequest request) {
        super(request);
    }
}
