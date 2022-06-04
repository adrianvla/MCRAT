// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.io.IOException;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class REST extends AbstractCommand
{
    private final Logger LOG;
    
    public REST() {
        this.LOG = LoggerFactory.getLogger(REST.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException {
        final String argument = request.getArgument();
        if (argument == null) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "REST", null));
            return;
        }
        session.resetState();
        long skipLen = 0L;
        try {
            skipLen = Long.parseLong(argument);
            if (skipLen < 0L) {
                skipLen = 0L;
                session.write(LocalizedFtpReply.translate(session, request, context, 501, "REST.negetive", null));
            }
            else {
                session.write(LocalizedFtpReply.translate(session, request, context, 350, "REST", null));
            }
        }
        catch (NumberFormatException ex) {
            this.LOG.debug("Invalid restart position: " + argument, ex);
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "REST.invalid", null));
        }
        session.setFileOffset(skipLen);
    }
}
