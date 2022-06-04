// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

public interface DataConnectionFactory
{
    DataConnection openConnection() throws Exception;
    
    boolean isSecure();
    
    void closeDataConnection();
}
