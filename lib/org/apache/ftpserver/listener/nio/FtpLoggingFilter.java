// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.listener.nio;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.filterchain.IoFilter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.mina.filter.logging.LoggingFilter;

public class FtpLoggingFilter extends LoggingFilter
{
    private boolean maskPassword;
    private final Logger logger;
    
    public FtpLoggingFilter() {
        this(FtpLoggingFilter.class.getName());
    }
    
    public FtpLoggingFilter(final Class<?> clazz) {
        this(clazz.getName());
    }
    
    public FtpLoggingFilter(final String name) {
        super(name);
        this.maskPassword = true;
        this.logger = LoggerFactory.getLogger(name);
    }
    
    @Override
    public void messageReceived(final IoFilter.NextFilter nextFilter, final IoSession session, final Object message) throws Exception {
        final String request = (String)message;
        String logMessage;
        if (this.maskPassword) {
            if (request.trim().toUpperCase().startsWith("PASS ")) {
                logMessage = "PASS *****";
            }
            else {
                logMessage = request;
            }
        }
        else {
            logMessage = request;
        }
        this.logger.info("RECEIVED: {}", logMessage);
        nextFilter.messageReceived(session, message);
    }
    
    public boolean isMaskPassword() {
        return this.maskPassword;
    }
    
    public void setMaskPassword(final boolean maskPassword) {
        this.maskPassword = maskPassword;
    }
}
