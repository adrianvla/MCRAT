// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.mina.core.session.IdleStatus;
import org.apache.ftpserver.command.Command;
import org.apache.ftpserver.command.CommandFactory;
import java.io.IOException;
import org.apache.mina.core.write.WriteToClosedSessionException;
import org.apache.ftpserver.ftplet.DefaultFtpReply;
import java.nio.charset.MalformedInputException;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftpletcontainer.FtpletContainer;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.slf4j.LoggerFactory;
import org.apache.ftpserver.listener.Listener;
import org.slf4j.Logger;

public class DefaultFtpHandler implements FtpHandler
{
    private final Logger LOG;
    private static final String[] NON_AUTHENTICATED_COMMANDS;
    private FtpServerContext context;
    private Listener listener;
    
    public DefaultFtpHandler() {
        this.LOG = LoggerFactory.getLogger(DefaultFtpHandler.class);
    }
    
    @Override
    public void init(final FtpServerContext context, final Listener listener) {
        this.context = context;
        this.listener = listener;
    }
    
    @Override
    public void sessionCreated(final FtpIoSession session) throws Exception {
        session.setListener(this.listener);
        final ServerFtpStatistics stats = (ServerFtpStatistics)this.context.getFtpStatistics();
        if (stats != null) {
            stats.setOpenConnection(session);
        }
    }
    
    @Override
    public void sessionOpened(final FtpIoSession session) throws Exception {
        final FtpletContainer ftplets = this.context.getFtpletContainer();
        FtpletResult ftpletRet;
        try {
            ftpletRet = ftplets.onConnect(session.getFtpletSession());
        }
        catch (Exception e) {
            this.LOG.debug("Ftplet threw exception", e);
            ftpletRet = FtpletResult.DISCONNECT;
        }
        if (ftpletRet == FtpletResult.DISCONNECT) {
            this.LOG.debug("Ftplet returned DISCONNECT, session will be closed");
            session.close(false).awaitUninterruptibly(10000L);
        }
        else {
            session.updateLastAccessTime();
            session.write(LocalizedFtpReply.translate(session, null, this.context, 220, null, null));
        }
    }
    
    @Override
    public void sessionClosed(final FtpIoSession session) throws Exception {
        this.LOG.debug("Closing session");
        try {
            this.context.getFtpletContainer().onDisconnect(session.getFtpletSession());
        }
        catch (Exception e) {
            this.LOG.warn("Ftplet threw an exception on disconnect", e);
        }
        try {
            final ServerDataConnectionFactory dc = session.getDataConnection();
            if (dc != null) {
                dc.closeDataConnection();
            }
        }
        catch (Exception e) {
            this.LOG.warn("Data connection threw an exception on disconnect", e);
        }
        final FileSystemView fs = session.getFileSystemView();
        if (fs != null) {
            try {
                fs.dispose();
            }
            catch (Exception e2) {
                this.LOG.warn("FileSystemView threw an exception on disposal", e2);
            }
        }
        final ServerFtpStatistics stats = (ServerFtpStatistics)this.context.getFtpStatistics();
        if (stats != null) {
            stats.setLogout(session);
            stats.setCloseConnection(session);
            this.LOG.debug("Statistics login and connection count decreased due to session close");
        }
        else {
            this.LOG.warn("Statistics not available in session, can not decrease login and connection count");
        }
        this.LOG.debug("Session closed");
    }
    
    @Override
    public void exceptionCaught(final FtpIoSession session, final Throwable cause) throws Exception {
        if (cause instanceof ProtocolDecoderException && cause.getCause() instanceof MalformedInputException) {
            this.LOG.warn("Client sent command that could not be decoded: {}", ((ProtocolDecoderException)cause).getHexdump());
            session.write(new DefaultFtpReply(501, "Invalid character in command"));
        }
        else if (cause instanceof WriteToClosedSessionException) {
            final WriteToClosedSessionException writeToClosedSessionException = (WriteToClosedSessionException)cause;
            this.LOG.warn("Client closed connection before all replies could be sent, last reply was {}", writeToClosedSessionException.getRequest());
            session.close(false).awaitUninterruptibly(10000L);
        }
        else {
            this.LOG.error("Exception caught, closing session", cause);
            session.close(false).awaitUninterruptibly(10000L);
        }
    }
    
    private boolean isCommandOkWithoutAuthentication(final String command) {
        boolean okay = false;
        for (final String allowed : DefaultFtpHandler.NON_AUTHENTICATED_COMMANDS) {
            if (allowed.equals(command)) {
                okay = true;
                break;
            }
        }
        return okay;
    }
    
    @Override
    public void messageReceived(final FtpIoSession session, final FtpRequest request) throws Exception {
        try {
            session.updateLastAccessTime();
            final String commandName = request.getCommand();
            final CommandFactory commandFactory = this.context.getCommandFactory();
            final Command command = commandFactory.getCommand(commandName);
            if (!session.isLoggedIn() && !this.isCommandOkWithoutAuthentication(commandName)) {
                session.write(LocalizedFtpReply.translate(session, request, this.context, 530, "permission", null));
                return;
            }
            final FtpletContainer ftplets = this.context.getFtpletContainer();
            FtpletResult ftpletRet;
            try {
                ftpletRet = ftplets.beforeCommand(session.getFtpletSession(), request);
            }
            catch (Exception e) {
                this.LOG.debug("Ftplet container threw exception", e);
                ftpletRet = FtpletResult.DISCONNECT;
            }
            if (ftpletRet == FtpletResult.DISCONNECT) {
                this.LOG.debug("Ftplet returned DISCONNECT, session will be closed");
                session.close(false).awaitUninterruptibly(10000L);
                return;
            }
            if (ftpletRet != FtpletResult.SKIP) {
                if (command != null) {
                    synchronized (session) {
                        command.execute(session, this.context, request);
                    }
                }
                else {
                    session.write(LocalizedFtpReply.translate(session, request, this.context, 502, "not.implemented", null));
                }
                try {
                    ftpletRet = ftplets.afterCommand(session.getFtpletSession(), request, session.getLastReply());
                }
                catch (Exception e) {
                    this.LOG.debug("Ftplet container threw exception", e);
                    ftpletRet = FtpletResult.DISCONNECT;
                }
                if (ftpletRet == FtpletResult.DISCONNECT) {
                    this.LOG.debug("Ftplet returned DISCONNECT, session will be closed");
                    session.close(false).awaitUninterruptibly(10000L);
                }
            }
        }
        catch (Exception ex) {
            try {
                session.write(LocalizedFtpReply.translate(session, request, this.context, 550, null, null));
            }
            catch (Exception ex2) {}
            if (ex instanceof IOException) {
                throw (IOException)ex;
            }
            this.LOG.warn("RequestHandler.service()", ex);
        }
    }
    
    @Override
    public void sessionIdle(final FtpIoSession session, final IdleStatus status) throws Exception {
        this.LOG.info("Session idle, closing");
        session.close(false).awaitUninterruptibly(10000L);
    }
    
    @Override
    public void messageSent(final FtpIoSession session, final FtpReply reply) throws Exception {
    }
    
    static {
        NON_AUTHENTICATED_COMMANDS = new String[] { "USER", "PASS", "AUTH", "QUIT", "PROT", "PBSZ" };
    }
}
