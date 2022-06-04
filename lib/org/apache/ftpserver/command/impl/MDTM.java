// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FtpException;
import java.io.IOException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.util.DateUtils;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class MDTM extends AbstractCommand
{
    private final Logger LOG;
    
    public MDTM() {
        this.LOG = LoggerFactory.getLogger(MDTM.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        String fileName = request.getArgument();
        if (fileName == null) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "MDTM", null));
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
            session.write(LocalizedFtpReply.translate(session, request, context, 550, "MDTM", fileName));
            return;
        }
        fileName = file.getAbsolutePath();
        if (file.doesExist()) {
            final String dateStr = DateUtils.getFtpDate(file.getLastModified());
            session.write(LocalizedFtpReply.translate(session, request, context, 213, "MDTM", dateStr));
        }
        else {
            session.write(LocalizedFtpReply.translate(session, request, context, 550, "MDTM", fileName));
        }
    }
}
