// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import java.net.InetAddress;

public interface StatisticsObserver
{
    void notifyUpload();
    
    void notifyDownload();
    
    void notifyDelete();
    
    void notifyMkdir();
    
    void notifyRmdir();
    
    void notifyLogin(final boolean p0);
    
    void notifyLoginFail(final InetAddress p0);
    
    void notifyLogout(final boolean p0);
    
    void notifyOpenConnection();
    
    void notifyCloseConnection();
}
