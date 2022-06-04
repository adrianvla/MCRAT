// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import java.util.ArrayList;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.listener.ListenerFactory;
import java.util.HashMap;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.command.CommandFactoryFactory;
import org.apache.ftpserver.ftpletcontainer.impl.DefaultFtpletContainer;
import org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.message.MessageResourceFactory;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.ftpserver.ftplet.Authority;
import java.util.List;
import org.apache.ftpserver.listener.Listener;
import java.util.Map;
import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.command.CommandFactory;
import org.apache.ftpserver.ftplet.FtpStatistics;
import org.apache.ftpserver.ftpletcontainer.FtpletContainer;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.message.MessageResource;
import org.slf4j.Logger;

public class DefaultFtpServerContext implements FtpServerContext
{
    private final Logger LOG;
    private MessageResource messageResource;
    private UserManager userManager;
    private FileSystemFactory fileSystemManager;
    private FtpletContainer ftpletContainer;
    private FtpStatistics statistics;
    private CommandFactory commandFactory;
    private ConnectionConfig connectionConfig;
    private Map<String, Listener> listeners;
    private static final List<Authority> ADMIN_AUTHORITIES;
    private static final List<Authority> ANON_AUTHORITIES;
    private ThreadPoolExecutor threadPoolExecutor;
    
    public DefaultFtpServerContext() {
        this.LOG = LoggerFactory.getLogger(DefaultFtpServerContext.class);
        this.messageResource = new MessageResourceFactory().createMessageResource();
        this.userManager = new PropertiesUserManagerFactory().createUserManager();
        this.fileSystemManager = new NativeFileSystemFactory();
        this.ftpletContainer = new DefaultFtpletContainer();
        this.statistics = new DefaultFtpStatistics();
        this.commandFactory = new CommandFactoryFactory().createCommandFactory();
        this.connectionConfig = new ConnectionConfigFactory().createConnectionConfig();
        this.listeners = new HashMap<String, Listener>();
        this.threadPoolExecutor = null;
        this.listeners.put("default", new ListenerFactory().createListener());
    }
    
    public void createDefaultUsers() throws Exception {
        final UserManager userManager = this.getUserManager();
        final String adminName = userManager.getAdminName();
        if (!userManager.doesExist(adminName)) {
            this.LOG.info("Creating user : " + adminName);
            final BaseUser adminUser = new BaseUser();
            adminUser.setName(adminName);
            adminUser.setPassword(adminName);
            adminUser.setEnabled(true);
            adminUser.setAuthorities(DefaultFtpServerContext.ADMIN_AUTHORITIES);
            adminUser.setHomeDirectory("./res/home");
            adminUser.setMaxIdleTime(0);
            userManager.save(adminUser);
        }
        if (!userManager.doesExist("anonymous")) {
            this.LOG.info("Creating user : anonymous");
            final BaseUser anonUser = new BaseUser();
            anonUser.setName("anonymous");
            anonUser.setPassword("");
            anonUser.setAuthorities(DefaultFtpServerContext.ANON_AUTHORITIES);
            anonUser.setEnabled(true);
            anonUser.setHomeDirectory("./res/home");
            anonUser.setMaxIdleTime(300);
            userManager.save(anonUser);
        }
    }
    
    @Override
    public UserManager getUserManager() {
        return this.userManager;
    }
    
    @Override
    public FileSystemFactory getFileSystemManager() {
        return this.fileSystemManager;
    }
    
    @Override
    public MessageResource getMessageResource() {
        return this.messageResource;
    }
    
    @Override
    public FtpStatistics getFtpStatistics() {
        return this.statistics;
    }
    
    public void setFtpStatistics(final FtpStatistics statistics) {
        this.statistics = statistics;
    }
    
    @Override
    public FtpletContainer getFtpletContainer() {
        return this.ftpletContainer;
    }
    
    @Override
    public CommandFactory getCommandFactory() {
        return this.commandFactory;
    }
    
    @Override
    public Ftplet getFtplet(final String name) {
        return this.ftpletContainer.getFtplet(name);
    }
    
    @Override
    public void dispose() {
        this.listeners.clear();
        this.ftpletContainer.getFtplets().clear();
        if (this.threadPoolExecutor != null) {
            this.LOG.debug("Shutting down the thread pool executor");
            this.threadPoolExecutor.shutdown();
            try {
                this.threadPoolExecutor.awaitTermination(5000L, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException ex) {}
        }
    }
    
    @Override
    public Listener getListener(final String name) {
        return this.listeners.get(name);
    }
    
    public void setListener(final String name, final Listener listener) {
        this.listeners.put(name, listener);
    }
    
    @Override
    public Map<String, Listener> getListeners() {
        return this.listeners;
    }
    
    public void setListeners(final Map<String, Listener> listeners) {
        this.listeners = listeners;
    }
    
    public void addListener(final String name, final Listener listener) {
        this.listeners.put(name, listener);
    }
    
    public Listener removeListener(final String name) {
        return this.listeners.remove(name);
    }
    
    public void setCommandFactory(final CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }
    
    public void setFileSystemManager(final FileSystemFactory fileSystemManager) {
        this.fileSystemManager = fileSystemManager;
    }
    
    public void setFtpletContainer(final FtpletContainer ftpletContainer) {
        this.ftpletContainer = ftpletContainer;
    }
    
    public void setMessageResource(final MessageResource messageResource) {
        this.messageResource = messageResource;
    }
    
    public void setUserManager(final UserManager userManager) {
        this.userManager = userManager;
    }
    
    @Override
    public ConnectionConfig getConnectionConfig() {
        return this.connectionConfig;
    }
    
    public void setConnectionConfig(final ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }
    
    @Override
    public synchronized ThreadPoolExecutor getThreadPoolExecutor() {
        if (this.threadPoolExecutor == null) {
            int maxThreads = this.connectionConfig.getMaxThreads();
            if (maxThreads < 1) {
                final int maxLogins = this.connectionConfig.getMaxLogins();
                if (maxLogins > 0) {
                    maxThreads = maxLogins;
                }
                else {
                    maxThreads = 16;
                }
            }
            this.LOG.debug("Intializing shared thread pool executor with max threads of {}", (Object)maxThreads);
            this.threadPoolExecutor = new OrderedThreadPoolExecutor(maxThreads);
        }
        return this.threadPoolExecutor;
    }
    
    static {
        ADMIN_AUTHORITIES = new ArrayList<Authority>();
        ANON_AUTHORITIES = new ArrayList<Authority>();
        DefaultFtpServerContext.ADMIN_AUTHORITIES.add(new WritePermission());
        DefaultFtpServerContext.ANON_AUTHORITIES.add(new ConcurrentLoginPermission(20, 2));
        DefaultFtpServerContext.ANON_AUTHORITIES.add(new TransferRatePermission(4800, 4800));
    }
}
