// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import java.net.InetAddress;
import org.apache.ftpserver.DataConnectionException;
import java.net.InetSocketAddress;
import org.apache.ftpserver.ftplet.DataConnectionFactory;

public interface ServerDataConnectionFactory extends DataConnectionFactory
{
    void initActiveDataConnection(final InetSocketAddress p0);
    
    InetSocketAddress initPassiveDataConnection() throws DataConnectionException;
    
    void setSecure(final boolean p0);
    
    void setServerControlAddress(final InetAddress p0);
    
    void setZipMode(final boolean p0);
    
    boolean isTimeout(final long p0);
    
    void dispose();
    
    boolean isSecure();
    
    boolean isZipMode();
    
    InetAddress getInetAddress();
    
    int getPort();
}
