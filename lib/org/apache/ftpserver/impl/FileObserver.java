// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import org.apache.ftpserver.ftplet.FtpFile;

public interface FileObserver
{
    void notifyUpload(final FtpIoSession p0, final FtpFile p1, final long p2);
    
    void notifyDownload(final FtpIoSession p0, final FtpFile p1, final long p2);
    
    void notifyDelete(final FtpIoSession p0, final FtpFile p1);
    
    void notifyMkdir(final FtpIoSession p0, final FtpFile p1);
    
    void notifyRmdir(final FtpIoSession p0, final FtpFile p1);
}
