// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.usermanager.impl;

import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.Authority;

public class WritePermission implements Authority
{
    private String permissionRoot;
    
    public WritePermission() {
        this.permissionRoot = "/";
    }
    
    public WritePermission(final String permissionRoot) {
        this.permissionRoot = permissionRoot;
    }
    
    @Override
    public AuthorizationRequest authorize(final AuthorizationRequest request) {
        if (!(request instanceof WriteRequest)) {
            return null;
        }
        final WriteRequest writeRequest = (WriteRequest)request;
        final String requestFile = writeRequest.getFile();
        if (requestFile.startsWith(this.permissionRoot)) {
            return writeRequest;
        }
        return null;
    }
    
    @Override
    public boolean canAuthorize(final AuthorizationRequest request) {
        return request instanceof WriteRequest;
    }
}
