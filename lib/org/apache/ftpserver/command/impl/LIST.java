// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.DataConnection;
import java.net.InetAddress;
import org.apache.ftpserver.ftplet.DataConnectionFactory;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.command.impl.listing.ListArgument;
import java.io.IOException;
import java.net.SocketException;
import org.apache.ftpserver.command.impl.listing.FileFormater;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.DefaultFtpReply;
import org.apache.ftpserver.impl.IODataConnectionFactory;
import org.apache.ftpserver.impl.LocalizedDataTransferFtpReply;
import org.apache.ftpserver.command.impl.listing.ListArgumentParser;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.apache.ftpserver.command.impl.listing.DirectoryLister;
import org.apache.ftpserver.command.impl.listing.LISTFileFormater;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class LIST extends AbstractCommand
{
    private final Logger LOG;
    private static final LISTFileFormater LIST_FILE_FORMATER;
    private final DirectoryLister directoryLister;
    
    public LIST() {
        this.LOG = LoggerFactory.getLogger(LIST.class);
        this.directoryLister = new DirectoryLister();
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        try {
            session.resetState();
            final ListArgument parsedArg = ListArgumentParser.parse(request.getArgument());
            final FtpFile file = session.getFileSystemView().getFile(parsedArg.getFile());
            if (!file.doesExist()) {
                this.LOG.debug("Listing on a non-existing file");
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 450, "LIST", null, file));
                return;
            }
            final DataConnectionFactory connFactory = session.getDataConnection();
            if (connFactory instanceof IODataConnectionFactory) {
                final InetAddress address = ((IODataConnectionFactory)connFactory).getInetAddress();
                if (address == null) {
                    session.write(new DefaultFtpReply(503, "PORT or PASV must be issued first"));
                    return;
                }
            }
            session.write(LocalizedFtpReply.translate(session, request, context, 150, "LIST", null));
            DataConnection dataConnection;
            try {
                dataConnection = session.getDataConnection().openConnection();
            }
            catch (Exception e) {
                this.LOG.debug("Exception getting the output data stream", e);
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 425, "LIST", null, file));
                return;
            }
            boolean failure = false;
            final String dirList = this.directoryLister.listFiles(parsedArg, session.getFileSystemView(), LIST.LIST_FILE_FORMATER);
            try {
                dataConnection.transferToClient(session.getFtpletSession(), dirList);
            }
            catch (SocketException ex) {
                this.LOG.debug("Socket exception during list transfer", ex);
                failure = true;
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 426, "LIST", null, file));
            }
            catch (IOException ex2) {
                this.LOG.debug("IOException during list transfer", ex2);
                failure = true;
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 551, "LIST", null, file));
            }
            catch (IllegalArgumentException e2) {
                this.LOG.debug("Illegal list syntax: " + request.getArgument(), e2);
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 501, "LIST", null, file));
            }
            if (!failure) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 226, "LIST", null, file, dirList.length()));
            }
        }
        finally {
            session.getDataConnection().closeDataConnection();
        }
    }
    
    static {
        LIST_FILE_FORMATER = new LISTFileFormater();
    }
}
