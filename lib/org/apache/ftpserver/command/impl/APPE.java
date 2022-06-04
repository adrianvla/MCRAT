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

public class APPE extends AbstractCommand
{
    private final Logger LOG;
    
    public APPE() {
        this.LOG = LoggerFactory.getLogger(APPE.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        try {
            session.resetState();
            String fileName = request.getArgument();
            if (fileName == null) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 501, "APPE", null, null));
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
            catch (Exception e) {
                this.LOG.debug("File system threw exception", e);
            }
            if (file == null) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 550, "APPE.invalid", fileName, null));
                return;
            }
            fileName = file.getAbsolutePath();
            if (file.doesExist() && !file.isFile()) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 550, "APPE.invalid", fileName, file));
                return;
            }
            if (!file.isWritable()) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 550, "APPE.permission", fileName, file));
                return;
            }
            session.write(LocalizedFtpReply.translate(session, request, context, 150, "APPE", fileName));
            DataConnection dataConnection;
            try {
                dataConnection = session.getDataConnection().openConnection();
            }
            catch (Exception e2) {
                this.LOG.debug("Exception when getting data input stream", e2);
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 425, "APPE", fileName, file));
                return;
            }
            boolean failure = false;
            OutputStream os = null;
            long transSz = 0L;
            try {
                long offset = 0L;
                if (file.doesExist()) {
                    offset = file.getSize();
                }
                os = file.createOutputStream(offset);
                transSz = dataConnection.transferFromClient(session.getFtpletSession(), os);
                if (os != null) {
                    os.close();
                }
                this.LOG.info("File uploaded {}", fileName);
                final ServerFtpStatistics ftpStat = (ServerFtpStatistics)context.getFtpStatistics();
                ftpStat.setUpload(session, file, transSz);
            }
            catch (SocketException e3) {
                this.LOG.debug("SocketException during file upload", e3);
                failure = true;
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 426, "APPE", fileName, file));
            }
            catch (IOException e4) {
                this.LOG.debug("IOException during file upload", e4);
                failure = true;
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 551, "APPE", fileName, file));
            }
            finally {
                IoUtils.close(os);
            }
            if (!failure) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 226, "APPE", fileName, file, transSz));
            }
        }
        finally {
            session.getDataConnection().closeDataConnection();
        }
    }
}
