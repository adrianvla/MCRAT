// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

import java.util.List;

public interface User
{
    String getName();
    
    String getPassword();
    
    List<? extends Authority> getAuthorities();
    
    List<? extends Authority> getAuthorities(final Class<? extends Authority> p0);
    
    AuthorizationRequest authorize(final AuthorizationRequest p0);
    
    int getMaxIdleTime();
    
    boolean getEnabled();
    
    String getHomeDirectory();
}
