// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.FtpStatistics;

public interface ServerFtpStatistics extends FtpStatistics
{
    void setObserver(final StatisticsObserver p0);
    
    void setFileObserver(final FileObserver p0);
    
    void setUpload(final FtpIoSession p0, final FtpFile p1, final long p2);
    
    void setDownload(final FtpIoSession p0, final FtpFile p1, final long p2);
    
    void setMkdir(final FtpIoSession p0, final FtpFile p1);
    
    void setRmdir(final FtpIoSession p0, final FtpFile p1);
    
    void setDelete(final FtpIoSession p0, final FtpFile p1);
    
    void setOpenConnection(final FtpIoSession p0);
    
    void setCloseConnection(final FtpIoSession p0);
    
    void setLogin(final FtpIoSession p0);
    
    void setLoginFail(final FtpIoSession p0);
    
    void setLogout(final FtpIoSession p0);
    
    void resetStatisticsCounters();
}
