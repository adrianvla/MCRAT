// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.command.impl.listing.FileFormater;
import org.apache.ftpserver.command.impl.listing.ListArgument;
import org.apache.ftpserver.ftplet.DataConnection;
import java.net.InetAddress;
import org.apache.ftpserver.ftplet.DataConnectionFactory;
import java.io.IOException;
import java.net.SocketException;
import org.apache.ftpserver.command.impl.listing.ListArgumentParser;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.DefaultFtpReply;
import org.apache.ftpserver.impl.IODataConnectionFactory;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.apache.ftpserver.command.impl.listing.DirectoryLister;
import org.apache.ftpserver.command.impl.listing.LISTFileFormater;
import org.apache.ftpserver.command.impl.listing.NLSTFileFormater;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class NLST extends AbstractCommand
{
    private final Logger LOG;
    private static final NLSTFileFormater NLST_FILE_FORMATER;
    private static final LISTFileFormater LIST_FILE_FORMATER;
    private final DirectoryLister directoryLister;
    
    public NLST() {
        this.LOG = LoggerFactory.getLogger(NLST.class);
        this.directoryLister = new DirectoryLister();
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        try {
            session.resetState();
            final DataConnectionFactory connFactory = session.getDataConnection();
            if (connFactory instanceof IODataConnectionFactory) {
                final InetAddress address = ((IODataConnectionFactory)connFactory).getInetAddress();
                if (address == null) {
                    session.write(new DefaultFtpReply(503, "PORT or PASV must be issued first"));
                    return;
                }
            }
            session.write(LocalizedFtpReply.translate(session, request, context, 150, "NLST", null));
            DataConnection dataConnection;
            try {
                dataConnection = session.getDataConnection().openConnection();
            }
            catch (Exception e) {
                this.LOG.debug("Exception getting the output data stream", e);
                session.write(LocalizedFtpReply.translate(session, request, context, 425, "NLST", null));
                return;
            }
            boolean failure = false;
            try {
                final ListArgument parsedArg = ListArgumentParser.parse(request.getArgument());
                FileFormater formater;
                if (parsedArg.hasOption('l')) {
                    formater = NLST.LIST_FILE_FORMATER;
                }
                else {
                    formater = NLST.NLST_FILE_FORMATER;
                }
                dataConnection.transferToClient(session.getFtpletSession(), this.directoryLister.listFiles(parsedArg, session.getFileSystemView(), formater));
            }
            catch (SocketException ex) {
                this.LOG.debug("Socket exception during data transfer", ex);
                failure = true;
                session.write(LocalizedFtpReply.translate(session, request, context, 426, "NLST", null));
            }
            catch (IOException ex2) {
                this.LOG.debug("IOException during data transfer", ex2);
                failure = true;
                session.write(LocalizedFtpReply.translate(session, request, context, 551, "NLST", null));
            }
            catch (IllegalArgumentException e2) {
                this.LOG.debug("Illegal listing syntax: " + request.getArgument(), e2);
                session.write(LocalizedFtpReply.translate(session, request, context, 501, "LIST", null));
            }
            if (!failure) {
                session.write(LocalizedFtpReply.translate(session, request, context, 226, "NLST", null));
            }
        }
        finally {
            session.getDataConnection().closeDataConnection();
        }
    }
    
    static {
        NLST_FILE_FORMATER = new NLSTFileFormater();
        LIST_FILE_FORMATER = new LISTFileFormater();
    }
}
