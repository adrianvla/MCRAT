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

public class MKD extends AbstractCommand
{
    private final Logger LOG;
    
    public MKD() {
        this.LOG = LoggerFactory.getLogger(MKD.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        String fileName = request.getArgument();
        if (fileName == null) {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 501, "MKD", null, null));
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
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 550, "MKD.invalid", fileName, file));
            return;
        }
        fileName = file.getAbsolutePath();
        if (!file.isWritable()) {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 550, "MKD.permission", fileName, file));
            return;
        }
        if (file.doesExist()) {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 550, "MKD.exists", fileName, file));
            return;
        }
        if (file.mkdir()) {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 257, "MKD", fileName, file));
            final String userName = session.getUser().getName();
            this.LOG.info("Directory create : " + userName + " - " + fileName);
            final ServerFtpStatistics ftpStat = (ServerFtpStatistics)context.getFtpStatistics();
            ftpStat.setMkdir(session, file);
        }
        else {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 550, "MKD", fileName, file));
        }
    }
}
