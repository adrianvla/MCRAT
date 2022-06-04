// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.ftpserver.impl.ServerDataConnectionFactory;
import org.apache.ftpserver.DataConnectionException;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.command.AbstractCommand;

public class EPSV extends AbstractCommand
{
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException {
        session.resetState();
        final ServerDataConnectionFactory dataCon = session.getDataConnection();
        try {
            final InetSocketAddress dataConAddress = dataCon.initPassiveDataConnection();
            final int servPort = dataConAddress.getPort();
            final String portStr = "|||" + servPort + '|';
            session.write(LocalizedFtpReply.translate(session, request, context, 229, "EPSV", portStr));
        }
        catch (DataConnectionException e) {
            session.write(LocalizedFtpReply.translate(session, request, context, 425, "EPSV", null));
        }
    }
}
