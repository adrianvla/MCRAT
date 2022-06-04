// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.DataTransferFtpReply;

public class LocalizedDataTransferFtpReply extends LocalizedFtpReply implements DataTransferFtpReply
{
    private final FtpFile file;
    private final long bytesTransferred;
    
    public LocalizedDataTransferFtpReply(final int code, final String message, final FtpFile file, final long bytesTransferred) {
        super(code, message);
        this.file = file;
        this.bytesTransferred = bytesTransferred;
    }
    
    @Override
    public FtpFile getFile() {
        return this.file;
    }
    
    @Override
    public long getBytesTransferred() {
        return this.bytesTransferred;
    }
    
    public static LocalizedDataTransferFtpReply translate(final FtpIoSession session, final FtpRequest request, final FtpServerContext context, final int code, final String subId, final String basicMsg, final FtpFile file) {
        final String msg = FtpReplyTranslator.translateMessage(session, request, context, code, subId, basicMsg);
        return new LocalizedDataTransferFtpReply(code, msg, file, 0L);
    }
    
    public static LocalizedDataTransferFtpReply translate(final FtpIoSession session, final FtpRequest request, final FtpServerContext context, final int code, final String subId, final String basicMsg, final FtpFile file, final long bytesTransferred) {
        final String msg = FtpReplyTranslator.translateMessage(session, request, context, code, subId, basicMsg);
        return new LocalizedDataTransferFtpReply(code, msg, file, bytesTransferred);
    }
}
