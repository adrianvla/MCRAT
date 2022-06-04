// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.usermanager.impl;

import org.apache.ftpserver.ftplet.AuthorizationRequest;

public class TransferRateRequest implements AuthorizationRequest
{
    private int maxDownloadRate;
    private int maxUploadRate;
    
    public TransferRateRequest() {
        this.maxDownloadRate = 0;
        this.maxUploadRate = 0;
    }
    
    public int getMaxDownloadRate() {
        return this.maxDownloadRate;
    }
    
    public void setMaxDownloadRate(final int maxDownloadRate) {
        this.maxDownloadRate = maxDownloadRate;
    }
    
    public int getMaxUploadRate() {
        return this.maxUploadRate;
    }
    
    public void setMaxUploadRate(final int maxUploadRate) {
        this.maxUploadRate = maxUploadRate;
    }
}
