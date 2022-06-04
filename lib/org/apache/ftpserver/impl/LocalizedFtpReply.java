// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.DefaultFtpReply;

public class LocalizedFtpReply extends DefaultFtpReply
{
    public static LocalizedFtpReply translate(final FtpIoSession session, final FtpRequest request, final FtpServerContext context, final int code, final String subId, final String basicMsg) {
        final String msg = FtpReplyTranslator.translateMessage(session, request, context, code, subId, basicMsg);
        return new LocalizedFtpReply(code, msg);
    }
    
    public LocalizedFtpReply(final int code, final String message) {
        super(code, message);
    }
}
