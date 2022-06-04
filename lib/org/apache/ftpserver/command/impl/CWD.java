// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FtpException;
import java.io.IOException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.impl.LocalizedFileActionFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class CWD extends AbstractCommand
{
    private final Logger LOG;
    
    public CWD() {
        this.LOG = LoggerFactory.getLogger(CWD.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        String dirName = "/";
        if (request.hasArgument()) {
            dirName = request.getArgument();
        }
        final FileSystemView fsview = session.getFileSystemView();
        boolean success = false;
        try {
            success = fsview.changeWorkingDirectory(dirName);
        }
        catch (Exception ex) {
            this.LOG.debug("Failed to change directory in file system", ex);
        }
        final FtpFile cwd = fsview.getWorkingDirectory();
        if (success) {
            dirName = cwd.getAbsolutePath();
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 250, "CWD", dirName, cwd));
        }
        else {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 550, "CWD", null, cwd));
        }
    }
}
