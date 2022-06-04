// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.io.IOException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.command.impl.listing.ListArgument;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.impl.LocalizedFileActionFtpReply;
import org.apache.ftpserver.command.impl.listing.FileFormater;
import org.apache.ftpserver.impl.LocalizedDataTransferFtpReply;
import org.apache.ftpserver.command.impl.listing.ListArgumentParser;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.command.impl.listing.DirectoryLister;
import org.apache.ftpserver.command.impl.listing.LISTFileFormater;
import org.apache.ftpserver.command.AbstractCommand;

public class STAT extends AbstractCommand
{
    private static final LISTFileFormater LIST_FILE_FORMATER;
    private final DirectoryLister directoryLister;
    
    public STAT() {
        this.directoryLister = new DirectoryLister();
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException {
        session.resetState();
        if (request.getArgument() != null) {
            final ListArgument parsedArg = ListArgumentParser.parse(request.getArgument());
            FtpFile file = null;
            try {
                file = session.getFileSystemView().getFile(parsedArg.getFile());
                if (!file.doesExist()) {
                    session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 450, "LIST", null, file));
                    return;
                }
                final String dirList = this.directoryLister.listFiles(parsedArg, session.getFileSystemView(), STAT.LIST_FILE_FORMATER);
                int replyCode;
                if (file.isDirectory()) {
                    replyCode = 212;
                }
                else {
                    replyCode = 213;
                }
                session.write(LocalizedFileActionFtpReply.translate(session, request, context, replyCode, "STAT", dirList, file));
            }
            catch (FtpException e) {
                session.write(LocalizedFileActionFtpReply.translate(session, request, context, 450, "STAT", null, file));
            }
        }
        else {
            session.write(LocalizedFtpReply.translate(session, request, context, 211, "STAT", null));
        }
    }
    
    static {
        LIST_FILE_FORMATER = new LISTFileFormater();
    }
}
