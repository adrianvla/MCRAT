// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

public interface FileSystemFactory
{
    FileSystemView createFileSystemView(final User p0) throws FtpException;
}
