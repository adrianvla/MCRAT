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

public class ABOR extends AbstractCommand
{
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException {
        session.resetState();
        session.getDataConnection().closeDataConnection();
        session.write(LocalizedFtpReply.translate(session, request, context, 226, "ABOR", null));
    }
}
