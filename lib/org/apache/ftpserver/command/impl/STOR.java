// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FtpException;
import java.io.OutputStream;
import org.apache.ftpserver.ftplet.DataConnection;
import java.net.InetAddress;
import org.apache.ftpserver.ftplet.DataConnectionFactory;
import org.apache.ftpserver.util.IoUtils;
import java.io.IOException;
import java.net.SocketException;
import org.apache.ftpserver.impl.ServerFtpStatistics;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.DefaultFtpReply;
import org.apache.ftpserver.impl.IODataConnectionFactory;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.impl.LocalizedDataTransferFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class STOR extends AbstractCommand
{
    private final Logger LOG;
    
    public STOR() {
        this.LOG = LoggerFactory.getLogger(STOR.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        try {
            final long skipLen = session.getFileOffset();
            String fileName = request.getArgument();
            if (fileName == null) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 501, "STOR", null, null));
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
            FtpFile file = null;
            try {
                file = session.getFileSystemView().getFile(fileName);
            }
            catch (Exception ex) {
                this.LOG.debug("Exception getting file object", ex);
            }
            if (file == null) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 550, "STOR.invalid", fileName, file));
                return;
            }
            fileName = file.getAbsolutePath();
            if (!file.isWritable()) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 550, "STOR.permission", fileName, file));
                return;
            }
            session.write(LocalizedFtpReply.translate(session, request, context, 150, "STOR", fileName)).awaitUninterruptibly(10000L);
            DataConnection dataConnection;
            try {
                dataConnection = session.getDataConnection().openConnection();
            }
            catch (Exception e) {
                this.LOG.debug("Exception getting the input data stream", e);
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 425, "STOR", fileName, file));
                return;
            }
            boolean failure = false;
            OutputStream outStream = null;
            long transSz = 0L;
            try {
                outStream = file.createOutputStream(skipLen);
                transSz = dataConnection.transferFromClient(session.getFtpletSession(), outStream);
                if (outStream != null) {
                    outStream.close();
                }
                this.LOG.info("File uploaded {}", fileName);
                final ServerFtpStatistics ftpStat = (ServerFtpStatistics)context.getFtpStatistics();
                ftpStat.setUpload(session, file, transSz);
            }
            catch (SocketException ex2) {
                this.LOG.debug("Socket exception during data transfer", ex2);
                failure = true;
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 426, "STOR", fileName, file));
            }
            catch (IOException ex3) {
                this.LOG.debug("IOException during data transfer", ex3);
                failure = true;
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 551, "STOR", fileName, file));
            }
            finally {
                IoUtils.close(outStream);
            }
            if (!failure) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 226, "STOR", fileName, file, transSz));
            }
        }
        finally {
            session.resetState();
            session.getDataConnection().closeDataConnection();
        }
    }
}
