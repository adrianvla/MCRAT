// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.filesystem.nativefs;

import org.apache.ftpserver.filesystem.nativefs.impl.NativeFileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import java.io.File;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.ftplet.FileSystemFactory;

public class NativeFileSystemFactory implements FileSystemFactory
{
    private final Logger LOG;
    private boolean createHome;
    private boolean caseInsensitive;
    
    public NativeFileSystemFactory() {
        this.LOG = LoggerFactory.getLogger(NativeFileSystemFactory.class);
    }
    
    public boolean isCreateHome() {
        return this.createHome;
    }
    
    public void setCreateHome(final boolean createHome) {
        this.createHome = createHome;
    }
    
    public boolean isCaseInsensitive() {
        return this.caseInsensitive;
    }
    
    public void setCaseInsensitive(final boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }
    
    @Override
    public FileSystemView createFileSystemView(final User user) throws FtpException {
        synchronized (user) {
            if (this.createHome) {
                final String homeDirStr = user.getHomeDirectory();
                final File homeDir = new File(homeDirStr);
                if (homeDir.isFile()) {
                    this.LOG.warn("Not a directory :: " + homeDirStr);
                    throw new FtpException("Not a directory :: " + homeDirStr);
                }
                if (!homeDir.exists() && !homeDir.mkdirs()) {
                    this.LOG.warn("Cannot create user home :: " + homeDirStr);
                    throw new FtpException("Cannot create user home :: " + homeDirStr);
                }
            }
            final FileSystemView fsView = new NativeFileSystemView(user, this.caseInsensitive);
            return fsView;
        }
    }
}
