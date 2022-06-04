// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.usermanager.impl;

import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.Authority;

public class TransferRatePermission implements Authority
{
    private int maxDownloadRate;
    private int maxUploadRate;
    
    public TransferRatePermission(final int maxDownloadRate, final int maxUploadRate) {
        this.maxDownloadRate = maxDownloadRate;
        this.maxUploadRate = maxUploadRate;
    }
    
    @Override
    public AuthorizationRequest authorize(final AuthorizationRequest request) {
        if (request instanceof TransferRateRequest) {
            final TransferRateRequest transferRateRequest = (TransferRateRequest)request;
            transferRateRequest.setMaxDownloadRate(this.maxDownloadRate);
            transferRateRequest.setMaxUploadRate(this.maxUploadRate);
            return transferRateRequest;
        }
        return null;
    }
    
    @Override
    public boolean canAuthorize(final AuthorizationRequest request) {
        return request instanceof TransferRateRequest;
    }
}
