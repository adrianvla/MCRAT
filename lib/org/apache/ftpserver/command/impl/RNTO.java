// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FtpException;
import java.io.IOException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.impl.LocalizedRenameFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class RNTO extends AbstractCommand
{
    private final Logger LOG;
    
    public RNTO() {
        this.LOG = LoggerFactory.getLogger(RNTO.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        try {
            String toFileStr = request.getArgument();
            if (toFileStr == null) {
                session.write(LocalizedRenameFtpReply.translate(session, request, context, 501, "RNTO", null, null, null));
                return;
            }
            final FtpFile frFile = session.getRenameFrom();
            if (frFile == null) {
                session.write(LocalizedRenameFtpReply.translate(session, request, context, 503, "RNTO", null, null, null));
                return;
            }
            FtpFile toFile = null;
            try {
                toFile = session.getFileSystemView().getFile(toFileStr);
            }
            catch (Exception ex) {
                this.LOG.debug("Exception getting file object", ex);
            }
            if (toFile == null) {
                session.write(LocalizedRenameFtpReply.translate(session, request, context, 553, "RNTO.invalid", null, frFile, toFile));
                return;
            }
            toFileStr = toFile.getAbsolutePath();
            if (!toFile.isWritable()) {
                session.write(LocalizedRenameFtpReply.translate(session, request, context, 553, "RNTO.permission", null, frFile, toFile));
                return;
            }
            if (!frFile.doesExist()) {
                session.write(LocalizedRenameFtpReply.translate(session, request, context, 553, "RNTO.missing", null, frFile, toFile));
                return;
            }
            final String logFrFileAbsolutePath = frFile.getAbsolutePath();
            if (frFile.move(toFile)) {
                session.write(LocalizedRenameFtpReply.translate(session, request, context, 250, "RNTO", toFileStr, frFile, toFile));
                this.LOG.info("File rename from \"{}\" to \"{}\"", logFrFileAbsolutePath, toFile.getAbsolutePath());
            }
            else {
                session.write(LocalizedRenameFtpReply.translate(session, request, context, 553, "RNTO", toFileStr, frFile, toFile));
            }
        }
        finally {
            session.resetState();
        }
    }
}
