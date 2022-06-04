// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import org.apache.ftpserver.ftplet.DataType;
import java.io.Writer;
import java.io.OutputStreamWriter;
import org.apache.ftpserver.util.IoUtils;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.usermanager.impl.TransferRateRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import java.util.zip.DeflaterOutputStream;
import java.io.OutputStream;
import java.util.zip.InflaterInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.LoggerFactory;
import java.net.Socket;
import org.slf4j.Logger;
import org.apache.ftpserver.ftplet.DataConnection;

public class IODataConnection implements DataConnection
{
    private final Logger LOG;
    private static final byte[] EOL;
    private final FtpIoSession session;
    private final Socket socket;
    private final ServerDataConnectionFactory factory;
    
    public IODataConnection(final Socket socket, final FtpIoSession session, final ServerDataConnectionFactory factory) {
        this.LOG = LoggerFactory.getLogger(IODataConnection.class);
        this.session = session;
        this.socket = socket;
        this.factory = factory;
    }
    
    private InputStream getDataInputStream() throws IOException {
        try {
            final Socket dataSoc = this.socket;
            if (dataSoc == null) {
                throw new IOException("Cannot open data connection.");
            }
            InputStream is = dataSoc.getInputStream();
            if (this.factory.isZipMode()) {
                is = new InflaterInputStream(is);
            }
            return is;
        }
        catch (IOException ex) {
            this.factory.closeDataConnection();
            throw ex;
        }
    }
    
    private OutputStream getDataOutputStream() throws IOException {
        try {
            final Socket dataSoc = this.socket;
            if (dataSoc == null) {
                throw new IOException("Cannot open data connection.");
            }
            OutputStream os = dataSoc.getOutputStream();
            if (this.factory.isZipMode()) {
                os = new DeflaterOutputStream(os);
            }
            return os;
        }
        catch (IOException ex) {
            this.factory.closeDataConnection();
            throw ex;
        }
    }
    
    @Override
    public final long transferFromClient(final FtpSession session, final OutputStream out) throws IOException {
        TransferRateRequest transferRateRequest = new TransferRateRequest();
        transferRateRequest = (TransferRateRequest)session.getUser().authorize(transferRateRequest);
        int maxRate = 0;
        if (transferRateRequest != null) {
            maxRate = transferRateRequest.getMaxUploadRate();
        }
        final InputStream is = this.getDataInputStream();
        try {
            return this.transfer(session, false, is, out, maxRate);
        }
        finally {
            IoUtils.close(is);
        }
    }
    
    @Override
    public final long transferToClient(final FtpSession session, final InputStream in) throws IOException {
        TransferRateRequest transferRateRequest = new TransferRateRequest();
        transferRateRequest = (TransferRateRequest)session.getUser().authorize(transferRateRequest);
        int maxRate = 0;
        if (transferRateRequest != null) {
            maxRate = transferRateRequest.getMaxDownloadRate();
        }
        final OutputStream out = this.getDataOutputStream();
        try {
            return this.transfer(session, true, in, out, maxRate);
        }
        finally {
            IoUtils.close(out);
        }
    }
    
    @Override
    public final void transferToClient(final FtpSession session, final String str) throws IOException {
        final OutputStream out = this.getDataOutputStream();
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(out, "UTF-8");
            writer.write(str);
            if (session instanceof DefaultFtpSession) {
                ((DefaultFtpSession)session).increaseWrittenDataBytes(str.getBytes("UTF-8").length);
            }
        }
        finally {
            if (writer != null) {
                writer.flush();
            }
            IoUtils.close(writer);
        }
    }
    
    private final long transfer(final FtpSession session, final boolean isWrite, final InputStream in, final OutputStream out, final int maxRate) throws IOException {
        long transferredSize = 0L;
        final boolean isAscii = session.getDataType() == DataType.ASCII;
        final long startTime = System.currentTimeMillis();
        final byte[] buff = new byte[4096];
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = IoUtils.getBufferedInputStream(in);
            bos = IoUtils.getBufferedOutputStream(out);
            DefaultFtpSession defaultFtpSession = null;
            if (session instanceof DefaultFtpSession) {
                defaultFtpSession = (DefaultFtpSession)session;
            }
            byte lastByte = 0;
            while (true) {
                if (maxRate > 0) {
                    long interval = System.currentTimeMillis() - startTime;
                    if (interval == 0L) {
                        interval = 1L;
                    }
                    final long currRate = transferredSize * 1000L / interval;
                    if (currRate > maxRate) {
                        try {
                            Thread.sleep(50L);
                            continue;
                        }
                        catch (InterruptedException ex) {
                            break;
                        }
                    }
                }
                final int count = bis.read(buff);
                if (count == -1) {
                    break;
                }
                if (defaultFtpSession != null) {
                    if (isWrite) {
                        defaultFtpSession.increaseWrittenDataBytes(count);
                    }
                    else {
                        defaultFtpSession.increaseReadDataBytes(count);
                    }
                }
                if (isAscii) {
                    for (final byte b : buff) {
                        if (isWrite) {
                            if (b == 10 && lastByte != 13) {
                                bos.write(13);
                            }
                            bos.write(b);
                        }
                        else if (b == 10) {
                            if (lastByte != 13) {
                                bos.write(IODataConnection.EOL);
                            }
                        }
                        else if (b == 13) {
                            bos.write(IODataConnection.EOL);
                        }
                        else {
                            bos.write(b);
                        }
                        lastByte = b;
                    }
                }
                else {
                    bos.write(buff, 0, count);
                }
                transferredSize += count;
                this.notifyObserver();
            }
        }
        catch (IOException e) {
            this.LOG.warn("Exception during data transfer, closing data connection socket", e);
            this.factory.closeDataConnection();
            throw e;
        }
        catch (RuntimeException e2) {
            this.LOG.warn("Exception during data transfer, closing data connection socket", e2);
            this.factory.closeDataConnection();
            throw e2;
        }
        finally {
            if (bos != null) {
                bos.flush();
            }
        }
        return transferredSize;
    }
    
    protected void notifyObserver() {
        this.session.updateLastAccessTime();
    }
    
    static {
        EOL = System.getProperty("line.separator").getBytes();
    }
}
