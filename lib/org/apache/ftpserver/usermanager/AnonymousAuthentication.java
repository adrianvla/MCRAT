// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.usermanager;

import org.apache.ftpserver.usermanager.impl.UserMetadata;
import org.apache.ftpserver.ftplet.Authentication;

public class AnonymousAuthentication implements Authentication
{
    private UserMetadata userMetadata;
    
    public AnonymousAuthentication() {
    }
    
    public AnonymousAuthentication(final UserMetadata userMetadata) {
        this.userMetadata = userMetadata;
    }
    
    public UserMetadata getUserMetadata() {
        return this.userMetadata;
    }
}
