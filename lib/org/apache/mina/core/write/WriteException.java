// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.write;

import java.util.Iterator;
import java.util.Set;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import org.apache.mina.util.MapBackedSet;
import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.List;
import java.io.IOException;

public class WriteException extends IOException
{
    private static final long serialVersionUID = -4174407422754524197L;
    private final List<WriteRequest> requests;
    
    public WriteException(final WriteRequest request) {
        this.requests = asRequestList(request);
    }
    
    public WriteException(final WriteRequest request, final String message) {
        super(message);
        this.requests = asRequestList(request);
    }
    
    public WriteException(final WriteRequest request, final String message, final Throwable cause) {
        super(message);
        this.initCause(cause);
        this.requests = asRequestList(request);
    }
    
    public WriteException(final WriteRequest request, final Throwable cause) {
        this.initCause(cause);
        this.requests = asRequestList(request);
    }
    
    public WriteException(final Collection<WriteRequest> requests) {
        this.requests = asRequestList(requests);
    }
    
    public WriteException(final Collection<WriteRequest> requests, final String message) {
        super(message);
        this.requests = asRequestList(requests);
    }
    
    public WriteException(final Collection<WriteRequest> requests, final String message, final Throwable cause) {
        super(message);
        this.initCause(cause);
        this.requests = asRequestList(requests);
    }
    
    public WriteException(final Collection<WriteRequest> requests, final Throwable cause) {
        this.initCause(cause);
        this.requests = asRequestList(requests);
    }
    
    public List<WriteRequest> getRequests() {
        return this.requests;
    }
    
    public WriteRequest getRequest() {
        return this.requests.get(0);
    }
    
    private static List<WriteRequest> asRequestList(final Collection<WriteRequest> requests) {
        if (requests == null) {
            throw new IllegalArgumentException("requests");
        }
        if (requests.isEmpty()) {
            throw new IllegalArgumentException("requests is empty.");
        }
        final Set<WriteRequest> newRequests = new MapBackedSet<WriteRequest>(new LinkedHashMap<WriteRequest, Boolean>());
        for (final WriteRequest r : requests) {
            newRequests.add(r.getOriginalRequest());
        }
        return Collections.unmodifiableList((List<? extends WriteRequest>)new ArrayList<WriteRequest>(newRequests));
    }
    
    private static List<WriteRequest> asRequestList(final WriteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request");
        }
        final List<WriteRequest> requests = new ArrayList<WriteRequest>(1);
        requests.add(request.getOriginalRequest());
        return Collections.unmodifiableList((List<? extends WriteRequest>)requests);
    }
}
