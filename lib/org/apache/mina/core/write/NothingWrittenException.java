// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.write;

import java.util.Collection;

public class NothingWrittenException extends WriteException
{
    private static final long serialVersionUID = -6331979307737691005L;
    
    public NothingWrittenException(final Collection<WriteRequest> requests, final String message, final Throwable cause) {
        super(requests, message, cause);
    }
    
    public NothingWrittenException(final Collection<WriteRequest> requests, final String s) {
        super(requests, s);
    }
    
    public NothingWrittenException(final Collection<WriteRequest> requests, final Throwable cause) {
        super(requests, cause);
    }
    
    public NothingWrittenException(final Collection<WriteRequest> requests) {
        super(requests);
    }
    
    public NothingWrittenException(final WriteRequest request, final String message, final Throwable cause) {
        super(request, message, cause);
    }
    
    public NothingWrittenException(final WriteRequest request, final String s) {
        super(request, s);
    }
    
    public NothingWrittenException(final WriteRequest request, final Throwable cause) {
        super(request, cause);
    }
    
    public NothingWrittenException(final WriteRequest request) {
        super(request);
    }
}
