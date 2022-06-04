// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

public interface FtpletContext
{
    UserManager getUserManager();
    
    FileSystemFactory getFileSystemManager();
    
    FtpStatistics getFtpStatistics();
    
    Ftplet getFtplet(final String p0);
}
