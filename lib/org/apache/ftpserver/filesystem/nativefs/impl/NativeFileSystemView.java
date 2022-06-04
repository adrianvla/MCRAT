// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.filesystem.nativefs.impl;

import java.io.FileFilter;
import java.util.StringTokenizer;
import java.io.File;
import org.apache.ftpserver.ftplet.FtpFile;
import org.slf4j.LoggerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.apache.ftpserver.ftplet.FileSystemView;

public class NativeFileSystemView implements FileSystemView
{
    private final Logger LOG;
    private String rootDir;
    private String currDir;
    private final User user;
    private final boolean caseInsensitive;
    
    protected NativeFileSystemView(final User user) throws FtpException {
        this(user, false);
    }
    
    public NativeFileSystemView(final User user, final boolean caseInsensitive) throws FtpException {
        this.LOG = LoggerFactory.getLogger(NativeFileSystemView.class);
        if (user == null) {
            throw new IllegalArgumentException("user can not be null");
        }
        if (user.getHomeDirectory() == null) {
            throw new IllegalArgumentException("User home directory can not be null");
        }
        this.caseInsensitive = caseInsensitive;
        String rootDir = user.getHomeDirectory();
        rootDir = this.normalizeSeparateChar(rootDir);
        rootDir = this.appendSlash(rootDir);
        this.LOG.debug("Native filesystem view created for user \"{}\" with root \"{}\"", user.getName(), rootDir);
        this.rootDir = rootDir;
        this.user = user;
        this.currDir = "/";
    }
    
    @Override
    public FtpFile getHomeDirectory() {
        return new NativeFtpFile("/", new File(this.rootDir), this.user);
    }
    
    @Override
    public FtpFile getWorkingDirectory() {
        FtpFile fileObj = null;
        if (this.currDir.equals("/")) {
            fileObj = new NativeFtpFile("/", new File(this.rootDir), this.user);
        }
        else {
            final File file = new File(this.rootDir, this.currDir.substring(1));
            fileObj = new NativeFtpFile(this.currDir, file, this.user);
        }
        return fileObj;
    }
    
    @Override
    public FtpFile getFile(final String file) {
        final String physicalName = this.getPhysicalName(this.rootDir, this.currDir, file, this.caseInsensitive);
        final File fileObj = new File(physicalName);
        final String userFileName = physicalName.substring(this.rootDir.length() - 1);
        return new NativeFtpFile(userFileName, fileObj, this.user);
    }
    
    @Override
    public boolean changeWorkingDirectory(String dir) {
        dir = this.getPhysicalName(this.rootDir, this.currDir, dir, this.caseInsensitive);
        final File dirObj = new File(dir);
        if (!dirObj.isDirectory()) {
            return false;
        }
        dir = dir.substring(this.rootDir.length() - 1);
        if (dir.charAt(dir.length() - 1) != '/') {
            dir += '/';
        }
        this.currDir = dir;
        return true;
    }
    
    @Override
    public boolean isRandomAccessible() {
        return true;
    }
    
    @Override
    public void dispose() {
    }
    
    protected String getPhysicalName(final String rootDir, final String currDir, final String fileName, final boolean caseInsensitive) {
        String normalizedRootDir = this.normalizeSeparateChar(rootDir);
        normalizedRootDir = this.appendSlash(normalizedRootDir);
        final String normalizedFileName = this.normalizeSeparateChar(fileName);
        String result;
        if (normalizedFileName.charAt(0) != '/') {
            final String normalizedCurrDir = this.normalize(currDir, "/");
            result = normalizedRootDir + normalizedCurrDir.substring(1);
        }
        else {
            result = normalizedRootDir;
        }
        result = this.trimTrailingSlash(result);
        final StringTokenizer st = new StringTokenizer(normalizedFileName, "/");
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals(".")) {
                continue;
            }
            if (tok.equals("..")) {
                if (!result.startsWith(normalizedRootDir)) {
                    continue;
                }
                final int slashIndex = result.lastIndexOf(47);
                if (slashIndex == -1) {
                    continue;
                }
                result = result.substring(0, slashIndex);
            }
            else if (tok.equals("~")) {
                result = this.trimTrailingSlash(normalizedRootDir);
            }
            else {
                if (caseInsensitive) {
                    final File[] matches = new File(result).listFiles(new NameEqualsFileFilter(tok, true));
                    if (matches != null && matches.length > 0) {
                        tok = matches[0].getName();
                    }
                }
                result = result + '/' + tok;
            }
        }
        if (result.length() + 1 == normalizedRootDir.length()) {
            result += '/';
        }
        if (!result.startsWith(normalizedRootDir)) {
            result = normalizedRootDir;
        }
        return result;
    }
    
    private String appendSlash(final String path) {
        if (path.charAt(path.length() - 1) != '/') {
            return path + '/';
        }
        return path;
    }
    
    private String prependSlash(final String path) {
        if (path.charAt(0) != '/') {
            return '/' + path;
        }
        return path;
    }
    
    private String trimTrailingSlash(final String path) {
        if (path.charAt(path.length() - 1) == '/') {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
    
    private String normalizeSeparateChar(final String pathName) {
        String normalizedPathName = pathName.replace(File.separatorChar, '/');
        normalizedPathName = normalizedPathName.replace('\\', '/');
        return normalizedPathName;
    }
    
    private String normalize(String path, final String defaultPath) {
        if (path == null || path.trim().length() == 0) {
            path = defaultPath;
        }
        path = this.normalizeSeparateChar(path);
        path = this.prependSlash(this.appendSlash(path));
        return path;
    }
}
