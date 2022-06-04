// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FtpException;
import java.io.IOException;
import org.apache.ftpserver.impl.ServerDataConnectionFactory;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.DataConnectionConfiguration;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.command.AbstractCommand;

public class PROT extends AbstractCommand
{
    private SslConfiguration getSslConfiguration(final FtpIoSession session) {
        final DataConnectionConfiguration dataCfg = session.getListener().getDataConnectionConfiguration();
        SslConfiguration configuration = dataCfg.getSslConfiguration();
        if (configuration == null) {
            configuration = session.getListener().getSslConfiguration();
        }
        return configuration;
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        String arg = request.getArgument();
        if (arg == null) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "PROT", null));
            return;
        }
        arg = arg.toUpperCase();
        final ServerDataConnectionFactory dcon = session.getDataConnection();
        if (arg.equals("C")) {
            dcon.setSecure(false);
            session.write(LocalizedFtpReply.translate(session, request, context, 200, "PROT", null));
        }
        else if (arg.equals("P")) {
            if (this.getSslConfiguration(session) == null) {
                session.write(LocalizedFtpReply.translate(session, request, context, 431, "PROT", null));
            }
            else {
                dcon.setSecure(true);
                session.write(LocalizedFtpReply.translate(session, request, context, 200, "PROT", null));
            }
        }
        else {
            session.write(LocalizedFtpReply.translate(session, request, context, 504, "PROT", null));
        }
    }
}
