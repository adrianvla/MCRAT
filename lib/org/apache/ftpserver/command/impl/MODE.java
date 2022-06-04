// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.io.IOException;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.command.AbstractCommand;

public class MODE extends AbstractCommand
{
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException {
        session.resetState();
        if (!request.hasArgument()) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "MODE", null));
            return;
        }
        char md = request.getArgument().charAt(0);
        md = Character.toUpperCase(md);
        if (md == 'S') {
            session.getDataConnection().setZipMode(false);
            session.write(LocalizedFtpReply.translate(session, request, context, 200, "MODE", "S"));
        }
        else if (md == 'Z') {
            session.getDataConnection().setZipMode(true);
            session.write(LocalizedFtpReply.translate(session, request, context, 200, "MODE", "Z"));
        }
        else {
            session.write(LocalizedFtpReply.translate(session, request, context, 504, "MODE", null));
        }
    }
}
