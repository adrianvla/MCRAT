// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.io.IOException;
import org.apache.ftpserver.ftplet.Structure;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class STRU extends AbstractCommand
{
    private final Logger LOG;
    
    public STRU() {
        this.LOG = LoggerFactory.getLogger(STRU.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException {
        session.resetState();
        if (!request.hasArgument()) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "STRU", null));
            return;
        }
        final char stru = request.getArgument().charAt(0);
        try {
            session.setStructure(Structure.parseArgument(stru));
            session.write(LocalizedFtpReply.translate(session, request, context, 200, "STRU", null));
        }
        catch (IllegalArgumentException e) {
            this.LOG.debug("Illegal structure argument: " + request.getArgument(), e);
            session.write(LocalizedFtpReply.translate(session, request, context, 504, "STRU", null));
        }
    }
}
