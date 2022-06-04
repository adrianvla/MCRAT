// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.util.Arrays;
import java.security.GeneralSecurityException;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.ssl.ClientAuth;
import java.io.IOException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import java.util.List;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class AUTH extends AbstractCommand
{
    private static final String SSL_SESSION_FILTER_NAME = "sslSessionFilter";
    private final Logger LOG;
    private static final List<String> VALID_AUTH_TYPES;
    
    public AUTH() {
        this.LOG = LoggerFactory.getLogger(AUTH.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        if (!request.hasArgument()) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "AUTH", null));
            return;
        }
        if (session.getListener().getSslConfiguration() == null) {
            session.write(LocalizedFtpReply.translate(session, request, context, 431, "AUTH", null));
            return;
        }
        if (session.getFilterChain().contains(SslFilter.class)) {
            session.write(LocalizedFtpReply.translate(session, request, context, 534, "AUTH", null));
            return;
        }
        String authType = request.getArgument().toUpperCase();
        if (AUTH.VALID_AUTH_TYPES.contains(authType)) {
            if (authType.equals("TLS-C")) {
                authType = "TLS";
            }
            else if (authType.equals("TLS-P")) {
                authType = "SSL";
            }
            try {
                this.secureSession(session, authType);
                session.write(LocalizedFtpReply.translate(session, request, context, 234, "AUTH." + authType, null));
                return;
            }
            catch (FtpException ex) {
                throw ex;
            }
            catch (Exception ex2) {
                this.LOG.warn("AUTH.execute()", ex2);
                throw new FtpException("AUTH.execute()", ex2);
            }
        }
        session.write(LocalizedFtpReply.translate(session, request, context, 502, "AUTH", null));
    }
    
    private void secureSession(final FtpIoSession session, final String type) throws GeneralSecurityException, FtpException {
        final SslConfiguration ssl = session.getListener().getSslConfiguration();
        if (ssl != null) {
            session.setAttribute(SslFilter.DISABLE_ENCRYPTION_ONCE);
            final SslFilter sslFilter = new SslFilter(ssl.getSSLContext());
            if (ssl.getClientAuth() == ClientAuth.NEED) {
                sslFilter.setNeedClientAuth(true);
            }
            else if (ssl.getClientAuth() == ClientAuth.WANT) {
                sslFilter.setWantClientAuth(true);
            }
            if (ssl.getEnabledCipherSuites() != null) {
                sslFilter.setEnabledCipherSuites(ssl.getEnabledCipherSuites());
            }
            session.getFilterChain().addFirst("sslSessionFilter", sslFilter);
            if ("SSL".equals(type)) {
                session.getDataConnection().setSecure(true);
            }
            return;
        }
        throw new FtpException("Socket factory SSL not configured");
    }
    
    static {
        VALID_AUTH_TYPES = Arrays.asList("SSL", "TLS", "TLS-C", "TLS-P");
    }
}
