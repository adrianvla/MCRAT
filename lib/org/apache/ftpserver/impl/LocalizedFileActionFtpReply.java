// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.FileActionFtpReply;

public class LocalizedFileActionFtpReply extends LocalizedFtpReply implements FileActionFtpReply
{
    private final FtpFile file;
    
    public LocalizedFileActionFtpReply(final int code, final String message, final FtpFile file) {
        super(code, message);
        this.file = file;
    }
    
    @Override
    public FtpFile getFile() {
        return this.file;
    }
    
    public static LocalizedFileActionFtpReply translate(final FtpIoSession session, final FtpRequest request, final FtpServerContext context, final int code, final String subId, final String basicMsg, final FtpFile file) {
        final String msg = FtpReplyTranslator.translateMessage(session, request, context, code, subId, basicMsg);
        return new LocalizedFileActionFtpReply(code, msg, file);
    }
}
