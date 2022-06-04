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

public class RMD extends AbstractCommand
{
    private final Logger LOG;
    
    public RMD() {
        this.LOG = LoggerFactory.getLogger(RMD.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        String fileName = request.getArgument();
        if (fileName == null) {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 501, "RMD", null, null));
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
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 550, "RMD.permission", fileName, file));
            return;
        }
        fileName = file.getAbsolutePath();
        if (!file.isDirectory()) {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 550, "RMD.invalid", fileName, file));
            return;
        }
        final FtpFile cwd = session.getFileSystemView().getWorkingDirectory();
        if (file.equals(cwd)) {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 450, "RMD.busy", fileName, file));
            return;
        }
        if (!file.isRemovable()) {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 550, "RMD.permission", fileName, file));
            return;
        }
        if (file.delete()) {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 250, "RMD", fileName, file));
            final String userName = session.getUser().getName();
            this.LOG.info("Directory remove : " + userName + " - " + fileName);
            final ServerFtpStatistics ftpStat = (ServerFtpStatistics)context.getFtpStatistics();
            ftpStat.setRmdir(session, file);
        }
        else {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 450, "RMD", fileName, file));
        }
    }
}
