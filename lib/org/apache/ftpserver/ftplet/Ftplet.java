// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

import java.io.IOException;

public interface Ftplet
{
    void init(final FtpletContext p0) throws FtpException;
    
    void destroy();
    
    FtpletResult beforeCommand(final FtpSession p0, final FtpRequest p1) throws FtpException, IOException;
    
    FtpletResult afterCommand(final FtpSession p0, final FtpRequest p1, final FtpReply p2) throws FtpException, IOException;
    
    FtpletResult onConnect(final FtpSession p0) throws FtpException, IOException;
    
    FtpletResult onDisconnect(final FtpSession p0) throws FtpException, IOException;
}
