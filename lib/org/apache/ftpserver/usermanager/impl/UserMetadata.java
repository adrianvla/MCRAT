// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.usermanager.impl;

import java.net.InetAddress;
import java.security.cert.Certificate;

public class UserMetadata
{
    private Certificate[] certificateChain;
    private InetAddress inetAddress;
    
    public Certificate[] getCertificateChain() {
        if (this.certificateChain != null) {
            return this.certificateChain.clone();
        }
        return null;
    }
    
    public void setCertificateChain(final Certificate[] certificateChain) {
        if (certificateChain != null) {
            this.certificateChain = certificateChain.clone();
        }
        else {
            this.certificateChain = null;
        }
    }
    
    public InetAddress getInetAddress() {
        return this.inetAddress;
    }
    
    public void setInetAddress(final InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }
}
