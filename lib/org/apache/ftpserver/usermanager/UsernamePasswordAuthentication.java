// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.usermanager;

import org.apache.ftpserver.usermanager.impl.UserMetadata;
import org.apache.ftpserver.ftplet.Authentication;

public class UsernamePasswordAuthentication implements Authentication
{
    private String username;
    private String password;
    private UserMetadata userMetadata;
    
    public UsernamePasswordAuthentication(final String username, final String password) {
        this.username = username;
        this.password = password;
    }
    
    public UsernamePasswordAuthentication(final String username, final String password, final UserMetadata userMetadata) {
        this(username, password);
        this.userMetadata = userMetadata;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public UserMetadata getUserMetadata() {
        return this.userMetadata;
    }
}
