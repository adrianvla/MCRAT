// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.io.IOException;
import org.apache.ftpserver.DataConnectionConfiguration;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.InetAddress;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class EPRT extends AbstractCommand
{
    private final Logger LOG;
    
    public EPRT() {
        this.LOG = LoggerFactory.getLogger(EPRT.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException {
        session.resetState();
        final String arg = request.getArgument();
        if (arg == null) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "EPRT", null));
            return;
        }
        final DataConnectionConfiguration dataCfg = session.getListener().getDataConnectionConfiguration();
        if (!dataCfg.isActiveEnabled()) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "EPRT.disabled", null));
            return;
        }
        String host = null;
        String port = null;
        try {
            final char delim = arg.charAt(0);
            final int lastDelimIdx = arg.indexOf(delim, 3);
            host = arg.substring(3, lastDelimIdx);
            port = arg.substring(lastDelimIdx + 1, arg.length() - 1);
        }
        catch (Exception ex) {
            this.LOG.debug("Exception parsing host and port: " + arg, ex);
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "EPRT", null));
            return;
        }
        InetAddress dataAddr = null;
        try {
            dataAddr = InetAddress.getByName(host);
        }
        catch (UnknownHostException ex2) {
            this.LOG.debug("Unknown host: " + host, ex2);
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "EPRT.host", null));
            return;
        }
        if (dataCfg.isActiveIpCheck() && session.getRemoteAddress() instanceof InetSocketAddress) {
            final InetAddress clientAddr = ((InetSocketAddress)session.getRemoteAddress()).getAddress();
            if (!dataAddr.equals(clientAddr)) {
                session.write(LocalizedFtpReply.translate(session, request, context, 501, "EPRT.mismatch", null));
                return;
            }
        }
        int dataPort = 0;
        try {
            dataPort = Integer.parseInt(port);
        }
        catch (NumberFormatException ex3) {
            this.LOG.debug("Invalid port: " + port, ex3);
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "EPRT.invalid", null));
            return;
        }
        session.getDataConnection().initActiveDataConnection(new InetSocketAddress(dataAddr, dataPort));
        session.write(LocalizedFtpReply.translate(session, request, context, 200, "EPRT", null));
    }
}
