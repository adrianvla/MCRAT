// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftpletcontainer.impl;

import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import java.io.IOException;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.FtpException;
import java.util.Iterator;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.ftpserver.ftplet.Ftplet;
import java.util.Map;
import org.slf4j.Logger;
import org.apache.ftpserver.ftpletcontainer.FtpletContainer;

public class DefaultFtpletContainer implements FtpletContainer
{
    private final Logger LOG;
    private final Map<String, Ftplet> ftplets;
    
    public DefaultFtpletContainer() {
        this(new ConcurrentHashMap<String, Ftplet>());
    }
    
    public DefaultFtpletContainer(final Map<String, Ftplet> ftplets) {
        this.LOG = LoggerFactory.getLogger(DefaultFtpletContainer.class);
        this.ftplets = ftplets;
    }
    
    @Override
    public synchronized Ftplet getFtplet(final String name) {
        if (name == null) {
            return null;
        }
        return this.ftplets.get(name);
    }
    
    @Override
    public synchronized void init(final FtpletContext ftpletContext) throws FtpException {
        for (final Map.Entry<String, Ftplet> entry : this.ftplets.entrySet()) {
            entry.getValue().init(ftpletContext);
        }
    }
    
    @Override
    public synchronized Map<String, Ftplet> getFtplets() {
        return this.ftplets;
    }
    
    @Override
    public void destroy() {
        for (final Map.Entry<String, Ftplet> entry : this.ftplets.entrySet()) {
            try {
                entry.getValue().destroy();
            }
            catch (Exception ex) {
                this.LOG.error(entry.getKey() + " :: FtpletHandler.destroy()", ex);
            }
        }
    }
    
    @Override
    public FtpletResult onConnect(final FtpSession session) throws FtpException, IOException {
        FtpletResult retVal = FtpletResult.DEFAULT;
        for (final Map.Entry<String, Ftplet> entry : this.ftplets.entrySet()) {
            retVal = entry.getValue().onConnect(session);
            if (retVal == null) {
                retVal = FtpletResult.DEFAULT;
            }
            if (retVal != FtpletResult.DEFAULT) {
                break;
            }
        }
        return retVal;
    }
    
    @Override
    public FtpletResult onDisconnect(final FtpSession session) throws FtpException, IOException {
        FtpletResult retVal = FtpletResult.DEFAULT;
        for (final Map.Entry<String, Ftplet> entry : this.ftplets.entrySet()) {
            retVal = entry.getValue().onDisconnect(session);
            if (retVal == null) {
                retVal = FtpletResult.DEFAULT;
            }
            if (retVal != FtpletResult.DEFAULT) {
                break;
            }
        }
        return retVal;
    }
    
    @Override
    public FtpletResult afterCommand(final FtpSession session, final FtpRequest request, final FtpReply reply) throws FtpException, IOException {
        FtpletResult retVal = FtpletResult.DEFAULT;
        for (final Map.Entry<String, Ftplet> entry : this.ftplets.entrySet()) {
            retVal = entry.getValue().afterCommand(session, request, reply);
            if (retVal == null) {
                retVal = FtpletResult.DEFAULT;
            }
            if (retVal != FtpletResult.DEFAULT) {
                break;
            }
        }
        return retVal;
    }
    
    @Override
    public FtpletResult beforeCommand(final FtpSession session, final FtpRequest request) throws FtpException, IOException {
        FtpletResult retVal = FtpletResult.DEFAULT;
        for (final Map.Entry<String, Ftplet> entry : this.ftplets.entrySet()) {
            retVal = entry.getValue().beforeCommand(session, request);
            if (retVal == null) {
                retVal = FtpletResult.DEFAULT;
            }
            if (retVal != FtpletResult.DEFAULT) {
                break;
            }
        }
        return retVal;
    }
}
