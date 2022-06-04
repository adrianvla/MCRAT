// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FtpException;
import java.io.IOException;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.apache.ftpserver.command.Command;
import java.util.HashMap;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class OPTS extends AbstractCommand
{
    private final Logger LOG;
    private static final HashMap<String, Command> COMMAND_MAP;
    
    public OPTS() {
        this.LOG = LoggerFactory.getLogger(OPTS.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        String argument = request.getArgument();
        if (argument == null) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "OPTS", null));
            return;
        }
        final int spaceIndex = argument.indexOf(32);
        if (spaceIndex != -1) {
            argument = argument.substring(0, spaceIndex);
        }
        argument = argument.toUpperCase();
        final String optsRequest = "OPTS_" + argument;
        final Command command = OPTS.COMMAND_MAP.get(optsRequest);
        try {
            if (command != null) {
                command.execute(session, context, request);
            }
            else {
                session.resetState();
                session.write(LocalizedFtpReply.translate(session, request, context, 502, "OPTS.not.implemented", argument));
            }
        }
        catch (Exception ex) {
            this.LOG.warn("OPTS.execute()", ex);
            session.resetState();
            session.write(LocalizedFtpReply.translate(session, request, context, 500, "OPTS", null));
        }
    }
    
    static {
        (COMMAND_MAP = new HashMap<String, Command>(16)).put("OPTS_MLST", new OPTS_MLST());
        OPTS.COMMAND_MAP.put("OPTS_UTF8", new OPTS_UTF8());
    }
}
