// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FtpException;
import java.io.IOException;
import org.apache.ftpserver.command.Command;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class SITE extends AbstractCommand
{
    private final Logger LOG;
    
    public SITE() {
        this.LOG = LoggerFactory.getLogger(SITE.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        String argument = request.getArgument();
        if (argument != null) {
            final int spaceIndex = argument.indexOf(32);
            if (spaceIndex != -1) {
                argument = argument.substring(0, spaceIndex);
            }
            argument = argument.toUpperCase();
        }
        if (argument == null) {
            session.resetState();
            session.write(LocalizedFtpReply.translate(session, request, context, 200, "SITE", null));
            return;
        }
        final String siteRequest = "SITE_" + argument;
        final Command command = context.getCommandFactory().getCommand(siteRequest);
        try {
            if (command != null) {
                command.execute(session, context, request);
            }
            else {
                session.resetState();
                session.write(LocalizedFtpReply.translate(session, request, context, 502, "SITE", argument));
            }
        }
        catch (Exception ex) {
            this.LOG.warn("SITE.execute()", ex);
            session.resetState();
            session.write(LocalizedFtpReply.translate(session, request, context, 500, "SITE", null));
        }
    }
}
