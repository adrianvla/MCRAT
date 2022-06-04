// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver;

import org.apache.ftpserver.message.MessageResource;
import org.apache.ftpserver.command.CommandFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.ftpletcontainer.FtpletContainer;
import org.apache.ftpserver.ftpletcontainer.impl.DefaultFtpletContainer;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.listener.Listener;
import java.util.Map;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.DefaultFtpServer;
import org.apache.ftpserver.impl.DefaultFtpServerContext;

public class FtpServerFactory
{
    private DefaultFtpServerContext serverContext;
    
    public FtpServerFactory() {
        this.serverContext = new DefaultFtpServerContext();
    }
    
    public FtpServer createServer() {
        return new DefaultFtpServer(this.serverContext);
    }
    
    public Map<String, Listener> getListeners() {
        return this.serverContext.getListeners();
    }
    
    public Listener getListener(final String name) {
        return this.serverContext.getListener(name);
    }
    
    public void addListener(final String name, final Listener listener) {
        this.serverContext.addListener(name, listener);
    }
    
    public void setListeners(final Map<String, Listener> listeners) {
        this.serverContext.setListeners(listeners);
    }
    
    public Map<String, Ftplet> getFtplets() {
        return this.serverContext.getFtpletContainer().getFtplets();
    }
    
    public void setFtplets(final Map<String, Ftplet> ftplets) {
        this.serverContext.setFtpletContainer(new DefaultFtpletContainer(ftplets));
    }
    
    public UserManager getUserManager() {
        return this.serverContext.getUserManager();
    }
    
    public void setUserManager(final UserManager userManager) {
        this.serverContext.setUserManager(userManager);
    }
    
    public FileSystemFactory getFileSystem() {
        return this.serverContext.getFileSystemManager();
    }
    
    public void setFileSystem(final FileSystemFactory fileSystem) {
        this.serverContext.setFileSystemManager(fileSystem);
    }
    
    public CommandFactory getCommandFactory() {
        return this.serverContext.getCommandFactory();
    }
    
    public void setCommandFactory(final CommandFactory commandFactory) {
        this.serverContext.setCommandFactory(commandFactory);
    }
    
    public MessageResource getMessageResource() {
        return this.serverContext.getMessageResource();
    }
    
    public void setMessageResource(final MessageResource messageResource) {
        this.serverContext.setMessageResource(messageResource);
    }
    
    public ConnectionConfig getConnectionConfig() {
        return this.serverContext.getConnectionConfig();
    }
    
    public void setConnectionConfig(final ConnectionConfig connectionConfig) {
        this.serverContext.setConnectionConfig(connectionConfig);
    }
}
