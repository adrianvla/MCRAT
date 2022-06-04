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

public class SIZE extends AbstractCommand
{
    private final Logger LOG;
    
    public SIZE() {
        this.LOG = LoggerFactory.getLogger(SIZE.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        String fileName = request.getArgument();
        if (fileName == null) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "SIZE", null));
            return;
        }
        FtpFile file = null;
        try {
            file = session.getFileSystemView().getFile(fileName);
        }
        catch (Exception ex) {
            this.LOG.debug("Exception getting file object", ex);
        }
        if (file == null) {
            session.write(LocalizedFtpReply.translate(session, request, context, 550, "SIZE.missing", fileName));
            return;
        }
        fileName = file.getAbsolutePath();
        if (!file.doesExist()) {
            session.write(LocalizedFtpReply.translate(session, request, context, 550, "SIZE.missing", fileName));
        }
        else if (!file.isFile()) {
            session.write(LocalizedFtpReply.translate(session, request, context, 550, "SIZE.invalid", fileName));
        }
        else {
            final String fileLen = String.valueOf(file.getSize());
            session.write(LocalizedFtpReply.translate(session, request, context, 213, "SIZE", fileLen));
        }
    }
}
