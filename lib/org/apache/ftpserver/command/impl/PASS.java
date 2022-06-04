// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FtpException;
import java.io.IOException;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import java.net.InetSocketAddress;
import org.apache.ftpserver.usermanager.impl.UserMetadata;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.impl.ServerFtpStatistics;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class PASS extends AbstractCommand
{
    private final Logger LOG;
    
    public PASS() {
        this.LOG = LoggerFactory.getLogger(PASS.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        boolean success = false;
        final ServerFtpStatistics stat = (ServerFtpStatistics)context.getFtpStatistics();
        try {
            session.resetState();
            final String password = request.getArgument();
            final String userName = session.getUserArgument();
            if (userName == null && session.getUser() == null) {
                session.write(LocalizedFtpReply.translate(session, request, context, 503, "PASS", null));
                return;
            }
            if (session.isLoggedIn()) {
                session.write(LocalizedFtpReply.translate(session, request, context, 202, "PASS", null));
                return;
            }
            final boolean anonymous = userName != null && userName.equals("anonymous");
            if (anonymous) {
                final int currAnonLogin = stat.getCurrentAnonymousLoginNumber();
                final int maxAnonLogin = context.getConnectionConfig().getMaxAnonymousLogins();
                if (maxAnonLogin == 0) {
                    this.LOG.debug("Currently {} anonymous users logged in, unlimited allowed", (Object)currAnonLogin);
                }
                else {
                    this.LOG.debug("Currently {} out of {} anonymous users logged in", (Object)currAnonLogin, maxAnonLogin);
                }
                if (currAnonLogin >= maxAnonLogin) {
                    this.LOG.debug("Too many anonymous users logged in, user will be disconnected");
                    session.write(LocalizedFtpReply.translate(session, request, context, 421, "PASS.anonymous", null));
                    return;
                }
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
                session.write(LocalizedFtpReply.translate(session, request, context, 421, "PASS.login", null));
                return;
            }
            final UserManager userManager = context.getUserManager();
            User authenticatedUser = null;
            try {
                final UserMetadata userMetadata = new UserMetadata();
                if (session.getRemoteAddress() instanceof InetSocketAddress) {
                    userMetadata.setInetAddress(((InetSocketAddress)session.getRemoteAddress()).getAddress());
                }
                userMetadata.setCertificateChain(session.getClientCertificates());
                Authentication auth;
                if (anonymous) {
                    auth = new AnonymousAuthentication(userMetadata);
                }
                else {
                    auth = new UsernamePasswordAuthentication(userName, password, userMetadata);
                }
                authenticatedUser = userManager.authenticate(auth);
            }
            catch (AuthenticationFailedException e2) {
                this.LOG.warn("User failed to log in");
            }
            catch (Exception e) {
                authenticatedUser = null;
                this.LOG.warn("PASS.execute()", e);
            }
            final User oldUser = session.getUser();
            final String oldUserArgument = session.getUserArgument();
            final int oldMaxIdleTime = session.getMaxIdleTime();
            if (authenticatedUser != null) {
                if (!authenticatedUser.getEnabled()) {
                    session.write(LocalizedFtpReply.translate(session, request, context, 530, "PASS", null));
                    return;
                }
                session.setUser(authenticatedUser);
                session.setUserArgument(null);
                session.setMaxIdleTime(authenticatedUser.getMaxIdleTime());
                success = true;
            }
            else {
                session.setUser(null);
            }
            if (!success) {
                session.setUser(oldUser);
                session.setUserArgument(oldUserArgument);
                session.setMaxIdleTime(oldMaxIdleTime);
                this.delayAfterLoginFailure(context.getConnectionConfig().getLoginFailureDelay());
                this.LOG.warn("Login failure - " + userName);
                session.write(LocalizedFtpReply.translate(session, request, context, 530, "PASS", userName));
                stat.setLoginFail(session);
                session.increaseFailedLogins();
                final int maxAllowedLoginFailues = context.getConnectionConfig().getMaxLoginFailures();
                if (maxAllowedLoginFailues != 0 && session.getFailedLogins() >= maxAllowedLoginFailues) {
                    this.LOG.warn("User exceeded the number of allowed failed logins, session will be closed");
                    session.close(false).awaitUninterruptibly(10000L);
                }
                return;
            }
            final FileSystemFactory fmanager = context.getFileSystemManager();
            final FileSystemView fsview = fmanager.createFileSystemView(authenticatedUser);
            session.setLogin(fsview);
            stat.setLogin(session);
            session.write(LocalizedFtpReply.translate(session, request, context, 230, "PASS", userName));
            if (anonymous) {
                this.LOG.info("Anonymous login success - " + password);
            }
            else {
                this.LOG.info("Login success - " + userName);
            }
        }
        finally {
            if (!success) {
                session.reinitialize();
            }
        }
    }
    
    private void delayAfterLoginFailure(final int loginFailureDelay) {
        if (loginFailureDelay > 0) {
            this.LOG.debug("Waiting for " + loginFailureDelay + " milliseconds due to login failure");
            try {
                Thread.sleep(loginFailureDelay);
            }
            catch (InterruptedException ex) {}
        }
    }
}
