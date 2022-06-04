// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

public interface FileSystemView
{
    FtpFile getHomeDirectory() throws FtpException;
    
    FtpFile getWorkingDirectory() throws FtpException;
    
    boolean changeWorkingDirectory(final String p0) throws FtpException;
    
    FtpFile getFile(final String p0) throws FtpException;
    
    boolean isRandomAccessible() throws FtpException;
    
    void dispose();
}
