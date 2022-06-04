// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

import java.net.InetAddress;
import java.util.Date;

public interface FtpStatistics
{
    Date getStartTime();
    
    int getTotalUploadNumber();
    
    int getTotalDownloadNumber();
    
    int getTotalDeleteNumber();
    
    long getTotalUploadSize();
    
    long getTotalDownloadSize();
    
    int getTotalDirectoryCreated();
    
    int getTotalDirectoryRemoved();
    
    int getTotalConnectionNumber();
    
    int getCurrentConnectionNumber();
    
    int getTotalLoginNumber();
    
    int getTotalFailedLoginNumber();
    
    int getCurrentLoginNumber();
    
    int getTotalAnonymousLoginNumber();
    
    int getCurrentAnonymousLoginNumber();
    
    int getCurrentUserLoginNumber(final User p0);
    
    int getCurrentUserLoginNumber(final User p0, final InetAddress p1);
}
