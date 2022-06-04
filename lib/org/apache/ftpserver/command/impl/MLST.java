// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.io.IOException;
import org.apache.ftpserver.command.impl.listing.FileFormater;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.command.impl.listing.ListArgument;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.command.impl.listing.MLSTFileFormater;
import org.apache.ftpserver.command.impl.listing.ListArgumentParser;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class MLST extends AbstractCommand
{
    private final Logger LOG;
    
    public MLST() {
        this.LOG = LoggerFactory.getLogger(MLST.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException {
        session.resetState();
        final ListArgument parsedArg = ListArgumentParser.parse(request.getArgument());
        FtpFile file = null;
        try {
            file = session.getFileSystemView().getFile(parsedArg.getFile());
            if (file != null && file.doesExist()) {
                final FileFormater formater = new MLSTFileFormater((String[])session.getAttribute("MLST.types"));
                session.write(LocalizedFtpReply.translate(session, request, context, 250, "MLST", formater.format(file)));
            }
            else {
                session.write(LocalizedFtpReply.translate(session, request, context, 501, "MLST", null));
            }
        }
        catch (FtpException ex) {
            this.LOG.debug("Exception sending the file listing", ex);
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "MLST", null));
        }
    }
}
