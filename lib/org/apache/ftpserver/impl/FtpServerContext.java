// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import java.util.concurrent.ThreadPoolExecutor;
import org.apache.ftpserver.command.CommandFactory;
import java.util.Map;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.ftpletcontainer.FtpletContainer;
import org.apache.ftpserver.message.MessageResource;
import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.ftplet.FtpletContext;

public interface FtpServerContext extends FtpletContext
{
    ConnectionConfig getConnectionConfig();
    
    MessageResource getMessageResource();
    
    FtpletContainer getFtpletContainer();
    
    Listener getListener(final String p0);
    
    Map<String, Listener> getListeners();
    
    CommandFactory getCommandFactory();
    
    void dispose();
    
    ThreadPoolExecutor getThreadPoolExecutor();
}
