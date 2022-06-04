// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver;

import org.apache.ftpserver.impl.DefaultConnectionConfig;

public class ConnectionConfigFactory
{
    private int maxLogins;
    private boolean anonymousLoginEnabled;
    private int maxAnonymousLogins;
    private int maxLoginFailures;
    private int loginFailureDelay;
    private int maxThreads;
    
    public ConnectionConfigFactory() {
        this.maxLogins = 10;
        this.anonymousLoginEnabled = true;
        this.maxAnonymousLogins = 10;
        this.maxLoginFailures = 3;
        this.loginFailureDelay = 500;
        this.maxThreads = 0;
    }
    
    public ConnectionConfig createConnectionConfig() {
        return new DefaultConnectionConfig(this.anonymousLoginEnabled, this.loginFailureDelay, this.maxLogins, this.maxAnonymousLogins, this.maxLoginFailures, this.maxThreads);
    }
    
    public int getLoginFailureDelay() {
        return this.loginFailureDelay;
    }
    
    public int getMaxAnonymousLogins() {
        return this.maxAnonymousLogins;
    }
    
    public int getMaxLoginFailures() {
        return this.maxLoginFailures;
    }
    
    public int getMaxLogins() {
        return this.maxLogins;
    }
    
    public boolean isAnonymousLoginEnabled() {
        return this.anonymousLoginEnabled;
    }
    
    public void setMaxLogins(final int maxLogins) {
        this.maxLogins = maxLogins;
    }
    
    public int getMaxThreads() {
        return this.maxThreads;
    }
    
    public void setMaxThreads(final int maxThreads) {
        this.maxThreads = maxThreads;
    }
    
    public void setAnonymousLoginEnabled(final boolean anonymousLoginEnabled) {
        this.anonymousLoginEnabled = anonymousLoginEnabled;
    }
    
    public void setMaxAnonymousLogins(final int maxAnonymousLogins) {
        this.maxAnonymousLogins = maxAnonymousLogins;
    }
    
    public void setMaxLoginFailures(final int maxLoginFailures) {
        this.maxLoginFailures = maxLoginFailures;
    }
    
    public void setLoginFailureDelay(final int loginFailureDelay) {
        this.loginFailureDelay = loginFailureDelay;
    }
}
