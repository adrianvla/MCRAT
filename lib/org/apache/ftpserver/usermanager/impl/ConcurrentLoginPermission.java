// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.usermanager.impl;

import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.Authority;

public class ConcurrentLoginPermission implements Authority
{
    private final int maxConcurrentLogins;
    private final int maxConcurrentLoginsPerIP;
    
    public ConcurrentLoginPermission(final int maxConcurrentLogins, final int maxConcurrentLoginsPerIP) {
        this.maxConcurrentLogins = maxConcurrentLogins;
        this.maxConcurrentLoginsPerIP = maxConcurrentLoginsPerIP;
    }
    
    @Override
    public AuthorizationRequest authorize(final AuthorizationRequest request) {
        if (!(request instanceof ConcurrentLoginRequest)) {
            return null;
        }
        final ConcurrentLoginRequest concurrentLoginRequest = (ConcurrentLoginRequest)request;
        if (this.maxConcurrentLogins != 0 && this.maxConcurrentLogins < concurrentLoginRequest.getConcurrentLogins()) {
            return null;
        }
        if (this.maxConcurrentLoginsPerIP != 0 && this.maxConcurrentLoginsPerIP < concurrentLoginRequest.getConcurrentLoginsFromThisIP()) {
            return null;
        }
        concurrentLoginRequest.setMaxConcurrentLogins(this.maxConcurrentLogins);
        concurrentLoginRequest.setMaxConcurrentLoginsPerIP(this.maxConcurrentLoginsPerIP);
        return concurrentLoginRequest;
    }
    
    @Override
    public boolean canAuthorize(final AuthorizationRequest request) {
        return request instanceof ConcurrentLoginRequest;
    }
}
