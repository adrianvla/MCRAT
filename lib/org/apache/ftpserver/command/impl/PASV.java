// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.net.UnknownHostException;
import org.apache.ftpserver.ftplet.FtpException;
import java.io.IOException;
import java.net.InetAddress;
import org.apache.ftpserver.impl.ServerDataConnectionFactory;
import org.apache.ftpserver.DataConnectionException;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.util.SocketAddressEncoder;
import java.net.InetSocketAddress;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class PASV extends AbstractCommand
{
    private final Logger LOG;
    
    public PASV() {
        this.LOG = LoggerFactory.getLogger(PASV.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        final ServerDataConnectionFactory dataCon = session.getDataConnection();
        final String externalPassiveAddress = this.getPassiveExternalAddress(session);
        try {
            final InetSocketAddress dataConAddress = dataCon.initPassiveDataConnection();
            InetAddress servAddr;
            if (externalPassiveAddress != null) {
                servAddr = this.resolveAddress(externalPassiveAddress);
            }
            else {
                servAddr = dataConAddress.getAddress();
            }
            final InetSocketAddress externalDataConAddress = new InetSocketAddress(servAddr, dataConAddress.getPort());
            final String addrStr = SocketAddressEncoder.encode(externalDataConAddress);
            session.write(LocalizedFtpReply.translate(session, request, context, 227, "PASV", addrStr));
        }
        catch (DataConnectionException e) {
            this.LOG.warn("Failed to open passive data connection", e);
            session.write(LocalizedFtpReply.translate(session, request, context, 425, "PASV", null));
        }
    }
    
    private InetAddress resolveAddress(final String host) throws DataConnectionException {
        try {
            return InetAddress.getByName(host);
        }
        catch (UnknownHostException ex) {
            throw new DataConnectionException(ex.getLocalizedMessage(), ex);
        }
    }
    
    protected String getPassiveExternalAddress(final FtpIoSession session) {
        return session.getListener().getDataConnectionConfiguration().getPassiveExernalAddress();
    }
}
