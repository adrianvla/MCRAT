// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.write;

import java.util.Collection;

public class WriteToClosedSessionException extends WriteException
{
    private static final long serialVersionUID = 5550204573739301393L;
    
    public WriteToClosedSessionException(final Collection<WriteRequest> requests, final String message, final Throwable cause) {
        super(requests, message, cause);
    }
    
    public WriteToClosedSessionException(final Collection<WriteRequest> requests, final String s) {
        super(requests, s);
    }
    
    public WriteToClosedSessionException(final Collection<WriteRequest> requests, final Throwable cause) {
        super(requests, cause);
    }
    
    public WriteToClosedSessionException(final Collection<WriteRequest> requests) {
        super(requests);
    }
    
    public WriteToClosedSessionException(final WriteRequest request, final String message, final Throwable cause) {
        super(request, message, cause);
    }
    
    public WriteToClosedSessionException(final WriteRequest request, final String s) {
        super(request, s);
    }
    
    public WriteToClosedSessionException(final WriteRequest request, final Throwable cause) {
        super(request, cause);
    }
    
    public WriteToClosedSessionException(final WriteRequest request) {
        super(request);
    }
}
