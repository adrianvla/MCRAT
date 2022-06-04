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

public class CDUP extends AbstractCommand
{
    private final Logger LOG;
    
    public CDUP() {
        this.LOG = LoggerFactory.getLogger(CDUP.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        final FileSystemView fsview = session.getFileSystemView();
        boolean success = false;
        try {
            success = fsview.changeWorkingDirectory("..");
        }
        catch (Exception ex) {
            this.LOG.debug("Failed to change directory in file system", ex);
        }
        final FtpFile cwd = fsview.getWorkingDirectory();
        if (success) {
            final String dirName = cwd.getAbsolutePath();
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 250, "CDUP", dirName, cwd));
        }
        else {
            session.write(LocalizedFileActionFtpReply.translate(session, request, context, 550, "CDUP", null, cwd));
        }
    }
}
