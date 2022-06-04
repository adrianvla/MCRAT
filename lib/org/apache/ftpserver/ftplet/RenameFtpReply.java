// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

public interface RenameFtpReply extends FtpReply
{
    FtpFile getFrom();
    
    FtpFile getTo();
}
