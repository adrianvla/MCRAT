// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

public interface Authority
{
    boolean canAuthorize(final AuthorizationRequest p0);
    
    AuthorizationRequest authorize(final AuthorizationRequest p0);
}
