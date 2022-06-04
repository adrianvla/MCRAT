// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.io.IOException;
import org.apache.ftpserver.message.MessageResource;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.command.AbstractCommand;

public class HELP extends AbstractCommand
{
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException {
        session.resetState();
        if (!request.hasArgument()) {
            session.write(LocalizedFtpReply.translate(session, request, context, 214, null, null));
            return;
        }
        String ftpCmd = request.getArgument().toUpperCase();
        final MessageResource resource = context.getMessageResource();
        if (resource.getMessage(214, ftpCmd, session.getLanguage()) == null) {
            ftpCmd = null;
        }
        session.write(LocalizedFtpReply.translate(session, request, context, 214, ftpCmd, null));
    }
}
