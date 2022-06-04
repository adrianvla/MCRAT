// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.io.IOException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.ftplet.DefaultFtpReply;
import org.apache.ftpserver.usermanager.impl.TransferRateRequest;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.usermanager.impl.WriteRequest;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class SITE_DESCUSER extends AbstractCommand
{
    private final Logger LOG;
    
    public SITE_DESCUSER() {
        this.LOG = LoggerFactory.getLogger(SITE_DESCUSER.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        final UserManager userManager = context.getUserManager();
        final boolean isAdmin = userManager.isAdmin(session.getUser().getName());
        if (!isAdmin) {
            session.write(LocalizedFtpReply.translate(session, request, context, 530, "SITE", null));
            return;
        }
        final String argument = request.getArgument();
        final int spIndex = argument.indexOf(32);
        if (spIndex == -1) {
            session.write(LocalizedFtpReply.translate(session, request, context, 503, "SITE.DESCUSER", null));
            return;
        }
        final String userName = argument.substring(spIndex + 1);
        final UserManager usrManager = context.getUserManager();
        User user = null;
        try {
            if (usrManager.doesExist(userName)) {
                user = usrManager.getUserByName(userName);
            }
        }
        catch (FtpException ex) {
            this.LOG.debug("Exception trying to get user from user manager", ex);
        }
        if (user == null) {
            session.write(LocalizedFtpReply.translate(session, request, context, 501, "SITE.DESCUSER", userName));
            return;
        }
        final StringBuilder sb = new StringBuilder(128);
        sb.append("\n");
        sb.append("userid          : ").append(user.getName()).append("\n");
        sb.append("userpassword    : ********\n");
        sb.append("homedirectory   : ").append(user.getHomeDirectory()).append("\n");
        sb.append("writepermission : ").append(user.authorize(new WriteRequest()) != null).append("\n");
        sb.append("enableflag      : ").append(user.getEnabled()).append("\n");
        sb.append("idletime        : ").append(user.getMaxIdleTime()).append("\n");
        TransferRateRequest transferRateRequest = new TransferRateRequest();
        transferRateRequest = (TransferRateRequest)session.getUser().authorize(transferRateRequest);
        if (transferRateRequest != null) {
            sb.append("uploadrate      : ").append(transferRateRequest.getMaxUploadRate()).append("\n");
            sb.append("downloadrate    : ").append(transferRateRequest.getMaxDownloadRate()).append("\n");
        }
        else {
            sb.append("uploadrate      : 0\n");
            sb.append("downloadrate    : 0\n");
        }
        sb.append('\n');
        session.write(new DefaultFtpReply(200, sb.toString()));
    }
}
