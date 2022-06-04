// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FtpException;
import java.io.IOException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class RNFR extends AbstractCommand
{
    private final Logger LOG;
    
    public RNFR() {
        this.LOG = LoggerFactory.getLogger(RNFR.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        String fileName = request.getArgument();
        if (fileName == null) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "RNFR", null));
            return;
        }
        FtpFile renFr = null;
        try {
            renFr = session.getFileSystemView().getFile(fileName);
        }
        catch (Exception ex) {
            this.LOG.debug("Exception getting file object", ex);
        }
        if (renFr == null) {
            session.write(LocalizedFtpReply.translate(session, request, context, 550, "RNFR", fileName));
        }
        else {
            session.setRenameFrom(renFr);
            fileName = renFr.getAbsolutePath();
            session.write(LocalizedFtpReply.translate(session, request, context, 350, "RNFR", fileName));
        }
    }
}
