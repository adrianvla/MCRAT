// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.io.IOException;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.DataType;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class TYPE extends AbstractCommand
{
    private final Logger LOG;
    
    public TYPE() {
        this.LOG = LoggerFactory.getLogger(TYPE.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException {
        session.resetState();
        if (request.hasArgument()) {
            final char type = request.getArgument().charAt(0);
            try {
                session.setDataType(DataType.parseArgument(type));
                session.write(LocalizedFtpReply.translate(session, request, context, 200, "TYPE", null));
            }
            catch (IllegalArgumentException e) {
                this.LOG.debug("Illegal type argument: " + request.getArgument(), e);
                session.write(LocalizedFtpReply.translate(session, request, context, 504, "TYPE", null));
            }
            return;
        }
        session.write(LocalizedFtpReply.translate(session, request, context, 501, "TYPE", null));
    }
}
