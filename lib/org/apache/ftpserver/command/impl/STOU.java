// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.DataConnection;
import java.io.OutputStream;
import java.net.InetAddress;
import org.apache.ftpserver.ftplet.DataConnectionFactory;
import org.apache.ftpserver.util.IoUtils;
import java.io.IOException;
import java.net.SocketException;
import org.apache.ftpserver.impl.ServerFtpStatistics;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.impl.LocalizedDataTransferFtpReply;
import org.apache.ftpserver.ftplet.DefaultFtpReply;
import org.apache.ftpserver.impl.IODataConnectionFactory;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class STOU extends AbstractCommand
{
    private final Logger LOG;
    
    public STOU() {
        this.LOG = LoggerFactory.getLogger(STOU.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        try {
            final DataConnectionFactory connFactory = session.getDataConnection();
            if (connFactory instanceof IODataConnectionFactory) {
                final InetAddress address = ((IODataConnectionFactory)connFactory).getInetAddress();
                if (address == null) {
                    session.write(new DefaultFtpReply(503, "PORT or PASV must be issued first"));
                    return;
                }
            }
            session.resetState();
            final String pathName = request.getArgument();
            FtpFile file = null;
            try {
                String filePrefix;
                if (pathName == null) {
                    filePrefix = "ftp.dat";
                }
                else {
                    final FtpFile dir = session.getFileSystemView().getFile(pathName);
                    if (dir.isDirectory()) {
                        filePrefix = pathName + "/ftp.dat";
                    }
                    else {
                        filePrefix = pathName;
                    }
                }
                file = session.getFileSystemView().getFile(filePrefix);
                if (file != null) {
                    file = this.getUniqueFile(session, file);
                }
            }
            catch (Exception ex) {
                this.LOG.debug("Exception getting file object", ex);
            }
            if (file == null) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 550, "STOU", null, null));
                return;
            }
            final String fileName = file.getAbsolutePath();
            if (!file.isWritable()) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 550, "STOU.permission", fileName, file));
                return;
            }
            session.write(new DefaultFtpReply(150, "FILE: " + fileName));
            boolean failure = false;
            OutputStream os = null;
            DataConnection dataConnection;
            try {
                dataConnection = session.getDataConnection().openConnection();
            }
            catch (Exception e) {
                this.LOG.debug("Exception getting the input data stream", e);
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 425, "STOU", fileName, file));
                return;
            }
            long transSz = 0L;
            try {
                os = file.createOutputStream(0L);
                transSz = dataConnection.transferFromClient(session.getFtpletSession(), os);
                if (os != null) {
                    os.close();
                }
                this.LOG.info("File uploaded {}", fileName);
                final ServerFtpStatistics ftpStat = (ServerFtpStatistics)context.getFtpStatistics();
                if (ftpStat != null) {
                    ftpStat.setUpload(session, file, transSz);
                }
            }
            catch (SocketException ex2) {
                this.LOG.debug("Socket exception during data transfer", ex2);
                failure = true;
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 426, "STOU", fileName, file));
            }
            catch (IOException ex3) {
                this.LOG.debug("IOException during data transfer", ex3);
                failure = true;
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 551, "STOU", fileName, file));
            }
            finally {
                IoUtils.close(os);
            }
            if (!failure) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context, 226, "STOU", fileName, file, transSz));
            }
        }
        finally {
            session.getDataConnection().closeDataConnection();
        }
    }
    
    protected FtpFile getUniqueFile(final FtpIoSession session, final FtpFile oldFile) throws FtpException {
        FtpFile newFile = oldFile;
        final FileSystemView fsView = session.getFileSystemView();
        final String fileName = newFile.getAbsolutePath();
        while (newFile.doesExist()) {
            newFile = fsView.getFile(fileName + '.' + System.currentTimeMillis());
            if (newFile == null) {
                break;
            }
        }
        return newFile;
    }
}
