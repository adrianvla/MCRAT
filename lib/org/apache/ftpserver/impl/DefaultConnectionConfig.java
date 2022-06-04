// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import org.apache.ftpserver.ConnectionConfig;

public class DefaultConnectionConfig implements ConnectionConfig
{
    private final int maxLogins;
    private final boolean anonymousLoginEnabled;
    private final int maxAnonymousLogins;
    private final int maxLoginFailures;
    private final int loginFailureDelay;
    private final int maxThreads;
    
    public DefaultConnectionConfig() {
        this(true, 500, 10, 10, 3, 0);
    }
    
    public DefaultConnectionConfig(final boolean anonymousLoginEnabled, final int loginFailureDelay, final int maxLogins, final int maxAnonymousLogins, final int maxLoginFailures, final int maxThreads) {
        this.anonymousLoginEnabled = anonymousLoginEnabled;
        this.loginFailureDelay = loginFailureDelay;
        this.maxLogins = maxLogins;
        this.maxAnonymousLogins = maxAnonymousLogins;
        this.maxLoginFailures = maxLoginFailures;
        this.maxThreads = maxThreads;
    }
    
    @Override
    public int getLoginFailureDelay() {
        return this.loginFailureDelay;
    }
    
    @Override
    public int getMaxAnonymousLogins() {
        return this.maxAnonymousLogins;
    }
    
    @Override
    public int getMaxLoginFailures() {
        return this.maxLoginFailures;
    }
    
    @Override
    public int getMaxLogins() {
        return this.maxLogins;
    }
    
    @Override
    public boolean isAnonymousLoginEnabled() {
        return this.anonymousLoginEnabled;
    }
    
    @Override
    public int getMaxThreads() {
        return this.maxThreads;
    }
}
