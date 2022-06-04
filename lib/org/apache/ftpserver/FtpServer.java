// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver;

import org.apache.ftpserver.ftplet.FtpException;

public interface FtpServer
{
    void start() throws FtpException;
    
    void stop();
    
    boolean isStopped();
    
    void suspend();
    
    void resume();
    
    boolean isSuspended();
}
