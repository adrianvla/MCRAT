// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.io.IOException;
import org.apache.ftpserver.ftplet.FtpFile;
import java.util.Date;
import java.text.ParseException;
import org.apache.ftpserver.util.DateUtils;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class MFMT extends AbstractCommand
{
    private final Logger LOG;
    
    public MFMT() {
        this.LOG = LoggerFactory.getLogger(MFMT.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException {
        session.resetState();
        final String argument = request.getArgument();
        if (argument == null || argument.trim().length() == 0) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "MFMT.invalid", null));
            return;
        }
        final String[] arguments = argument.split(" ", 2);
        if (arguments.length != 2) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "MFMT.invalid", null));
            return;
        }
        final String timestamp = arguments[0].trim();
        try {
            final Date time = DateUtils.parseFTPDate(timestamp);
            final String fileName = arguments[1].trim();
            FtpFile file = null;
            try {
                file = session.getFileSystemView().getFile(fileName);
            }
            catch (Exception ex) {
                this.LOG.debug("Exception getting the file object: " + fileName, ex);
            }
            if (file == null || !file.doesExist()) {
                session.write(LocalizedFtpReply.translate(session, request, context, 550, "MFMT.filemissing", fileName));
                return;
            }
            if (!file.isFile()) {
                session.write(LocalizedFtpReply.translate(session, request, context, 501, "MFMT.invalid", null));
                return;
            }
            if (!file.setLastModified(time.getTime())) {
                session.write(LocalizedFtpReply.translate(session, request, context, 450, "MFMT", fileName));
                return;
            }
            session.write(LocalizedFtpReply.translate(session, request, context, 213, "MFMT", "ModifyTime=" + timestamp + "; " + fileName));
        }
        catch (ParseException e) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "MFMT.invalid", null));
        }
    }
}
