// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.usermanager.impl;

import org.apache.ftpserver.ftplet.AuthorizationRequest;

public class ConcurrentLoginRequest implements AuthorizationRequest
{
    private final int concurrentLogins;
    private final int concurrentLoginsFromThisIP;
    private int maxConcurrentLogins;
    private int maxConcurrentLoginsPerIP;
    
    public ConcurrentLoginRequest(final int concurrentLogins, final int concurrentLoginsFromThisIP) {
        this.maxConcurrentLogins = 0;
        this.maxConcurrentLoginsPerIP = 0;
        this.concurrentLogins = concurrentLogins;
        this.concurrentLoginsFromThisIP = concurrentLoginsFromThisIP;
    }
    
    public int getConcurrentLogins() {
        return this.concurrentLogins;
    }
    
    public int getConcurrentLoginsFromThisIP() {
        return this.concurrentLoginsFromThisIP;
    }
    
    public int getMaxConcurrentLogins() {
        return this.maxConcurrentLogins;
    }
    
    void setMaxConcurrentLogins(final int maxConcurrentLogins) {
        this.maxConcurrentLogins = maxConcurrentLogins;
    }
    
    public int getMaxConcurrentLoginsPerIP() {
        return this.maxConcurrentLoginsPerIP;
    }
    
    void setMaxConcurrentLoginsPerIP(final int maxConcurrentLoginsPerIP) {
        this.maxConcurrentLoginsPerIP = maxConcurrentLoginsPerIP;
    }
}
