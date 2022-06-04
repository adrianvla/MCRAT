// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.message.MessageResource;
import org.apache.ftpserver.command.CommandFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.ftplet.Ftplet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.listener.Listener;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.FtpServer;

public class DefaultFtpServer implements FtpServer
{
    private final Logger LOG;
    private FtpServerContext serverContext;
    private boolean suspended;
    private boolean started;
    
    public DefaultFtpServer(final FtpServerContext serverContext) {
        this.LOG = LoggerFactory.getLogger(DefaultFtpServer.class);
        this.suspended = false;
        this.started = false;
        this.serverContext = serverContext;
    }
    
    @Override
    public void start() throws FtpException {
        if (this.serverContext == null) {
            throw new IllegalStateException("FtpServer has been stopped. Restart is not supported");
        }
        final List<Listener> startedListeners = new ArrayList<Listener>();
        try {
            final Map<String, Listener> listeners = this.serverContext.getListeners();
            for (final Listener listener : listeners.values()) {
                listener.start(this.serverContext);
                startedListeners.add(listener);
            }
            this.serverContext.getFtpletContainer().init(this.serverContext);
            this.started = true;
            this.LOG.info("FTP server started");
        }
        catch (Exception e) {
            for (final Listener listener : startedListeners) {
                listener.stop();
            }
            if (e instanceof FtpException) {
                throw (FtpException)e;
            }
            throw (RuntimeException)e;
        }
    }
    
    @Override
    public void stop() {
        if (this.serverContext == null) {
            return;
        }
        final Map<String, Listener> listeners = this.serverContext.getListeners();
        for (final Listener listener : listeners.values()) {
            listener.stop();
        }
        this.serverContext.getFtpletContainer().destroy();
        if (this.serverContext != null) {
            this.serverContext.dispose();
            this.serverContext = null;
        }
        this.started = false;
    }
    
    @Override
    public boolean isStopped() {
        return !this.started;
    }
    
    @Override
    public void suspend() {
        if (!this.started) {
            return;
        }
        this.LOG.debug("Suspending server");
        final Map<String, Listener> listeners = this.serverContext.getListeners();
        for (final Listener listener : listeners.values()) {
            listener.suspend();
        }
        this.suspended = true;
        this.LOG.debug("Server suspended");
    }
    
    @Override
    public void resume() {
        if (!this.suspended) {
            return;
        }
        this.LOG.debug("Resuming server");
        final Map<String, Listener> listeners = this.serverContext.getListeners();
        for (final Listener listener : listeners.values()) {
            listener.resume();
        }
        this.suspended = false;
        this.LOG.debug("Server resumed");
    }
    
    @Override
    public boolean isSuspended() {
        return this.suspended;
    }
    
    public FtpServerContext getServerContext() {
        return this.serverContext;
    }
    
    public Map<String, Listener> getListeners() {
        return this.getServerContext().getListeners();
    }
    
    public Listener getListener(final String name) {
        return this.getServerContext().getListener(name);
    }
    
    public Map<String, Ftplet> getFtplets() {
        return this.getServerContext().getFtpletContainer().getFtplets();
    }
    
    public UserManager getUserManager() {
        return this.getServerContext().getUserManager();
    }
    
    public FileSystemFactory getFileSystem() {
        return this.getServerContext().getFileSystemManager();
    }
    
    public CommandFactory getCommandFactory() {
        return this.getServerContext().getCommandFactory();
    }
    
    public MessageResource getMessageResource() {
        return this.getServerContext().getMessageResource();
    }
    
    public ConnectionConfig getConnectionConfig() {
        return this.getServerContext().getConnectionConfig();
    }
}
