// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

public interface FtpRequest
{
    String getRequestLine();
    
    String getCommand();
    
    String getArgument();
    
    boolean hasArgument();
    
    long getReceivedTime();
}
