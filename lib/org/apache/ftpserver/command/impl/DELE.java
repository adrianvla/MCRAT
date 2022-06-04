// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FtpException;
import java.io.IOException;
import org.apache.ftpserver.impl.ServerFtpStatistics;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.impl.LocalizedFileActionFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class DELE extends AbstractCommand
{
    private final Logger LOG;
    
    public DELE() {
        this.LOG = LoggerFactory.getLogger(DELE.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        String fileName = request.getArgument();
        if (fileName == null) {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 501, "DELE", null, null));
            return;
        }
        FtpFile file = null;
        try {
            file = session.getFileSystemView().getFile(fileName);
        }
        catch (Exception ex) {
            this.LOG.debug("Could not get file " + fileName, ex);
        }
        if (file == null) {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 550, "DELE.invalid", fileName, null));
            return;
        }
        fileName = file.getAbsolutePath();
        if (file.isDirectory()) {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 550, "DELE.invalid", fileName, file));
            return;
        }
        if (!file.isRemovable()) {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 450, "DELE.permission", fileName, file));
            return;
        }
        if (file.delete()) {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 250, "DELE", fileName, file));
            final String userName = session.getUser().getName();
            this.LOG.info("File delete : " + userName + " - " + fileName);
            final ServerFtpStatistics ftpStat = (ServerFtpStatistics)context.getFtpStatistics();
            ftpStat.setDelete(session, file);
        }
        else {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 450, "DELE", fileName, file));
        }
    }
}
