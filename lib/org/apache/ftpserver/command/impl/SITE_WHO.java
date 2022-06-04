// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FtpException;
import java.io.IOException;
import org.apache.ftpserver.ftplet.User;
import java.util.Iterator;
import java.util.Map;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.ftplet.DefaultFtpReply;
import org.apache.ftpserver.util.DateUtils;
import java.net.InetSocketAddress;
import org.apache.ftpserver.util.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.command.AbstractCommand;

public class SITE_WHO extends AbstractCommand
{
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        final UserManager userManager = context.getUserManager();
        final boolean isAdmin = userManager.isAdmin(session.getUser().getName());
        if (!isAdmin) {
            session.write(LocalizedFtpReply.translate(session, request, context, 530, "SITE", null));
            return;
        }
        final StringBuilder sb = new StringBuilder();
        final Map<Long, IoSession> sessions = session.getService().getManagedSessions();
        sb.append('\n');
        final Iterator<IoSession> sessionIterator = sessions.values().iterator();
        while (sessionIterator.hasNext()) {
            final FtpIoSession managedSession = new FtpIoSession(sessionIterator.next(), context);
            if (!managedSession.isLoggedIn()) {
                continue;
            }
            final User tmpUsr = managedSession.getUser();
            sb.append(StringUtils.pad(tmpUsr.getName(), ' ', true, 16));
            if (managedSession.getRemoteAddress() instanceof InetSocketAddress) {
                sb.append(StringUtils.pad(((InetSocketAddress)managedSession.getRemoteAddress()).getAddress().getHostAddress(), ' ', true, 16));
            }
            sb.append(StringUtils.pad(DateUtils.getISO8601Date(managedSession.getLoginTime().getTime()), ' ', true, 20));
            sb.append(StringUtils.pad(DateUtils.getISO8601Date(managedSession.getLastAccessTime().getTime()), ' ', true, 20));
            sb.append('\n');
        }
        sb.append('\n');
        session.write(new DefaultFtpReply(200, sb.toString()));
    }
}
