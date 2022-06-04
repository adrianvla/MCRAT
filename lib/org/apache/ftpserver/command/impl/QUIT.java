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

public class QUIT extends AbstractCommand
{
    private final Logger LOG;
    
    public QUIT() {
        this.LOG = LoggerFactory.getLogger(QUIT.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException {
        session.resetState();
        session.write(LocalizedFtpReply.translate(session, request, context, 221, "QUIT", null));
        this.LOG.debug("QUIT received, closing session");
        session.close(false).awaitUninterruptibly(10000L);
        session.getDataConnection().closeDataConnection();
    }
}
