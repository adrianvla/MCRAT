// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FtpException;
import java.io.IOException;
import java.net.InetAddress;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginRequest;
import java.net.InetSocketAddress;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.impl.ServerFtpStatistics;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class USER extends AbstractCommand
{
    private final Logger LOG;
    
    public USER() {
        this.LOG = LoggerFactory.getLogger(USER.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        boolean success = false;
        final ServerFtpStatistics stat = (ServerFtpStatistics)context.getFtpStatistics();
        try {
            session.resetState();
            final String userName = request.getArgument();
            if (userName == null) {
                session.write(LocalizedFtpReply.translate(session, request, context, 501, "USER", null));
                return;
            }
            MdcInjectionFilter.setProperty(session, "userName", userName);
            final User user = session.getUser();
            if (session.isLoggedIn()) {
                if (userName.equals(user.getName())) {
                    session.write(LocalizedFtpReply.translate(session, request, context, 230, "USER", null));
                    success = true;
                }
                else {
                    session.write(LocalizedFtpReply.translate(session, request, context, 530, "USER.invalid", null));
                }
                return;
            }
            final boolean anonymous = userName.equals("anonymous");
            if (anonymous && !context.getConnectionConfig().isAnonymousLoginEnabled()) {
                session.write(LocalizedFtpReply.translate(session, request, context, 530, "USER.anonymous", null));
                return;
            }
            final int currAnonLogin = stat.getCurrentAnonymousLoginNumber();
            final int maxAnonLogin = context.getConnectionConfig().getMaxAnonymousLogins();
            if (maxAnonLogin == 0) {
                this.LOG.debug("Currently {} anonymous users logged in, unlimited allowed", (Object)currAnonLogin);
            }
            else {
                this.LOG.debug("Currently {} out of {} anonymous users logged in", (Object)currAnonLogin, maxAnonLogin);
            }
            if (anonymous && currAnonLogin >= maxAnonLogin) {
                this.LOG.debug("Too many anonymous users logged in, user will be disconnected");
                session.write(LocalizedFtpReply.translate(session, request, context, 421, "USER.anonymous", null));
                return;
            }
            final int currLogin = stat.getCurrentLoginNumber();
            final int maxLogin = context.getConnectionConfig().getMaxLogins();
            if (maxLogin == 0) {
                this.LOG.debug("Currently {} users logged in, unlimited allowed", (Object)currLogin);
            }
            else {
                this.LOG.debug("Currently {} out of {} users logged in", (Object)currLogin, maxLogin);
            }
            if (maxLogin != 0 && currLogin >= maxLogin) {
                this.LOG.debug("Too many users logged in, user will be disconnected");
                session.write(LocalizedFtpReply.translate(session, request, context, 421, "USER.login", null));
                return;
            }
            final User configUser = context.getUserManager().getUserByName(userName);
            if (configUser != null) {
                InetAddress address = null;
                if (session.getRemoteAddress() instanceof InetSocketAddress) {
                    address = ((InetSocketAddress)session.getRemoteAddress()).getAddress();
                }
                final ConcurrentLoginRequest loginRequest = new ConcurrentLoginRequest(stat.getCurrentUserLoginNumber(configUser) + 1, stat.getCurrentUserLoginNumber(configUser, address) + 1);
                if (configUser.authorize(loginRequest) == null) {
                    this.LOG.debug("User logged in too many sessions, user will be disconnected");
                    session.write(LocalizedFtpReply.translate(session, request, context, 421, "USER.login", null));
                    return;
                }
            }
            success = true;
            session.setUserArgument(userName);
            if (anonymous) {
                session.write(LocalizedFtpReply.translate(session, request, context, 331, "USER.anonymous", userName));
            }
            else {
                session.write(LocalizedFtpReply.translate(session, request, context, 331, "USER", userName));
            }
        }
        finally {
            if (!success) {
                this.LOG.debug("User failed to login, session will be closed");
                session.close(false).awaitUninterruptibly(10000L);
            }
        }
    }
}
