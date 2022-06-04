// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.RenameFtpReply;

public class LocalizedRenameFtpReply extends LocalizedFtpReply implements RenameFtpReply
{
    private final FtpFile from;
    private final FtpFile to;
    
    public LocalizedRenameFtpReply(final int code, final String message, final FtpFile from, final FtpFile to) {
        super(code, message);
        this.from = from;
        this.to = to;
    }
    
    @Override
    public FtpFile getFrom() {
        return this.from;
    }
    
    @Override
    public FtpFile getTo() {
        return this.to;
    }
    
    public static LocalizedRenameFtpReply translate(final FtpIoSession session, final FtpRequest request, final FtpServerContext context, final int code, final String subId, final String basicMsg, final FtpFile from, final FtpFile to) {
        final String msg = FtpReplyTranslator.translateMessage(session, request, context, code, subId, basicMsg);
        return new LocalizedRenameFtpReply(code, msg, from, to);
    }
}
