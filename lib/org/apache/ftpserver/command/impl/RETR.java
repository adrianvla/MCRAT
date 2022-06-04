// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.io.BufferedInputStream;
import org.apache.ftpserver.ftplet.DataType;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.DataConnection;
import java.io.InputStream;
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

public class RETR extends AbstractCommand
{
    private final Logger LOG;
    
    public RETR() {
        this.LOG = LoggerFactory.getLogger(RETR.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        try {
            final long skipLen = session.getFileOffset();
            String fileName = request.getArgument();
            if (fileName == null) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 501, "RETR", null, null));
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
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 550, "RETR.missing", fileName, file));
                return;
            }
            fileName = file.getAbsolutePath();
            if (!file.doesExist()) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 550, "RETR.missing", fileName, file));
                return;
            }
            if (!file.isFile()) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 550, "RETR.invalid", fileName, file));
                return;
            }
            if (!file.isReadable()) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 550, "RETR.permission", fileName, file));
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
            session.write(LocalizedFtpReply.translate(session, request, context, 150, "RETR", null));
            boolean failure = false;
            InputStream is = null;
            DataConnection dataConnection;
            try {
                dataConnection = session.getDataConnection().openConnection();
            }
            catch (Exception e) {
                this.LOG.debug("Exception getting the output data stream", e);
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 425, "RETR", null, file));
                return;
            }
            long transSz = 0L;
            try {
                is = this.openInputStream(session, file, skipLen);
                transSz = dataConnection.transferToClient(session.getFtpletSession(), is);
                if (is != null) {
                    is.close();
                }
                this.LOG.info("File downloaded {}", fileName);
                final ServerFtpStatistics ftpStat = (ServerFtpStatistics)context.getFtpStatistics();
                if (ftpStat != null) {
                    ftpStat.setDownload(session, file, transSz);
                }
            }
            catch (SocketException ex2) {
                this.LOG.debug("Socket exception during data transfer", ex2);
                failure = true;
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 426, "RETR", fileName, file, transSz));
            }
            catch (IOException ex3) {
                this.LOG.debug("IOException during data transfer", ex3);
                failure = true;
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 551, "RETR", fileName, file, transSz));
            }
            finally {
                IoUtils.close(is);
            }
            if (!failure) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 226, "RETR", fileName, file, transSz));
            }
        }
        finally {
            session.resetState();
            session.getDataConnection().closeDataConnection();
        }
    }
    
    public InputStream openInputStream(final FtpIoSession session, final FtpFile file, final long skipLen) throws IOException {
        InputStream in;
        if (session.getDataType() == DataType.ASCII) {
            long offset = 0L;
            in = new BufferedInputStream(file.createInputStream(0L));
            while (offset++ < skipLen) {
                final int c;
                if ((c = in.read()) == -1) {
                    throw new IOException("Cannot skip");
                }
                if (c != 10) {
                    continue;
                }
                ++offset;
            }
        }
        else {
            in = file.createInputStream(skipLen);
        }
        return in;
    }
}
