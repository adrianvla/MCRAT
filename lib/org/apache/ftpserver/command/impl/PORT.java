// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.io.IOException;
import java.net.InetAddress;
import org.apache.ftpserver.DataConnectionConfiguration;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import org.apache.ftpserver.util.IllegalInetAddressException;
import org.apache.ftpserver.util.IllegalPortException;
import org.apache.ftpserver.util.SocketAddressEncoder;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class PORT extends AbstractCommand
{
    private final Logger LOG;
    
    public PORT() {
        this.LOG = LoggerFactory.getLogger(PORT.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException {
        session.resetState();
        if (!request.hasArgument()) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "PORT", null));
            return;
        }
        final DataConnectionConfiguration dataCfg = session.getListener().getDataConnectionConfiguration();
        if (!dataCfg.isActiveEnabled()) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "PORT.disabled", null));
            return;
        }
        InetSocketAddress address;
        try {
            address = SocketAddressEncoder.decode(request.getArgument());
            if (address.getPort() == 0) {
                throw new IllegalPortException("PORT port must not be 0");
            }
        }
        catch (IllegalInetAddressException e3) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "PORT", null));
            return;
        }
        catch (IllegalPortException e) {
            this.LOG.debug("Invalid data port: " + request.getArgument(), e);
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "PORT.invalid", null));
            return;
        }
        catch (UnknownHostException e2) {
            this.LOG.debug("Unknown host", e2);
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "PORT.host", null));
            return;
        }
        if (dataCfg.isActiveIpCheck() && session.getRemoteAddress() instanceof InetSocketAddress) {
            final InetAddress clientAddr = ((InetSocketAddress)session.getRemoteAddress()).getAddress();
            if (!address.getAddress().equals(clientAddr)) {
                session.write(LocalizedFtpReply.translate(session, request, context, 501, "PORT.mismatch", null));
                return;
            }
        }
        session.getDataConnection().initActiveDataConnection(address);
        session.write(LocalizedFtpReply.translate(session, request, context, 200, "PORT", null));
    }
}
