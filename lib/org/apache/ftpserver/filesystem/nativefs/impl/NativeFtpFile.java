// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.filesystem.nativefs.impl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.usermanager.impl.WriteRequest;
import org.slf4j.LoggerFactory;
import org.apache.ftpserver.ftplet.User;
import java.io.File;
import org.slf4j.Logger;
import org.apache.ftpserver.ftplet.FtpFile;

public class NativeFtpFile implements FtpFile
{
    private final Logger LOG;
    private final String fileName;
    private final File file;
    private final User user;
    
    protected NativeFtpFile(final String fileName, final File file, final User user) {
        this.LOG = LoggerFactory.getLogger(NativeFtpFile.class);
        if (fileName == null) {
            throw new IllegalArgumentException("fileName can not be null");
        }
        if (file == null) {
            throw new IllegalArgumentException("file can not be null");
        }
        if (fileName.length() == 0) {
            throw new IllegalArgumentException("fileName can not be empty");
        }
        if (fileName.charAt(0) != '/') {
            throw new IllegalArgumentException("fileName must be an absolut path");
        }
        this.fileName = fileName;
        this.file = file;
        this.user = user;
    }
    
    @Override
    public String getAbsolutePath() {
        String fullName = this.fileName;
        final int filelen = fullName.length();
        if (filelen != 1 && fullName.charAt(filelen - 1) == '/') {
            fullName = fullName.substring(0, filelen - 1);
        }
        return fullName;
    }
    
    @Override
    public String getName() {
        if (this.fileName.equals("/")) {
            return "/";
        }
        String shortName = this.fileName;
        final int filelen = this.fileName.length();
        if (shortName.charAt(filelen - 1) == '/') {
            shortName = shortName.substring(0, filelen - 1);
        }
        final int slashIndex = shortName.lastIndexOf(47);
        if (slashIndex != -1) {
            shortName = shortName.substring(slashIndex + 1);
        }
        return shortName;
    }
    
    @Override
    public boolean isHidden() {
        return this.file.isHidden();
    }
    
    @Override
    public boolean isDirectory() {
        return this.file.isDirectory();
    }
    
    @Override
    public boolean isFile() {
        return this.file.isFile();
    }
    
    @Override
    public boolean doesExist() {
        return this.file.exists();
    }
    
    @Override
    public long getSize() {
        return this.file.length();
    }
    
    @Override
    public String getOwnerName() {
        return "user";
    }
    
    @Override
    public String getGroupName() {
        return "group";
    }
    
    @Override
    public int getLinkCount() {
        return this.file.isDirectory() ? 3 : 1;
    }
    
    @Override
    public long getLastModified() {
        return this.file.lastModified();
    }
    
    @Override
    public boolean setLastModified(final long time) {
        return this.file.setLastModified(time);
    }
    
    @Override
    public boolean isReadable() {
        return this.file.canRead();
    }
    
    @Override
    public boolean isWritable() {
        this.LOG.debug("Checking authorization for " + this.getAbsolutePath());
        if (this.user.authorize(new WriteRequest(this.getAbsolutePath())) == null) {
            this.LOG.debug("Not authorized");
            return false;
        }
        this.LOG.debug("Checking if file exists");
        if (this.file.exists()) {
            this.LOG.debug("Checking can write: " + this.file.canWrite());
            return this.file.canWrite();
        }
        this.LOG.debug("Authorized");
        return true;
    }
    
    @Override
    public boolean isRemovable() {
        if ("/".equals(this.fileName)) {
            return false;
        }
        final String fullName = this.getAbsolutePath();
        if (this.user.authorize(new WriteRequest(fullName)) == null) {
            return false;
        }
        final int indexOfSlash = fullName.lastIndexOf(47);
        String parentFullName;
        if (indexOfSlash == 0) {
            parentFullName = "/";
        }
        else {
            parentFullName = fullName.substring(0, indexOfSlash);
        }
        final NativeFtpFile parentObject = new NativeFtpFile(parentFullName, this.file.getAbsoluteFile().getParentFile(), this.user);
        return parentObject.isWritable();
    }
    
    @Override
    public boolean delete() {
        boolean retVal = false;
        if (this.isRemovable()) {
            retVal = this.file.delete();
        }
        return retVal;
    }
    
    @Override
    public boolean move(final FtpFile dest) {
        boolean retVal = false;
        if (dest.isWritable() && this.isReadable()) {
            final File destFile = ((NativeFtpFile)dest).file;
            retVal = (!destFile.exists() && this.file.renameTo(destFile));
        }
        return retVal;
    }
    
    @Override
    public boolean mkdir() {
        boolean retVal = false;
        if (this.isWritable()) {
            retVal = this.file.mkdir();
        }
        return retVal;
    }
    
    @Override
    public File getPhysicalFile() {
        return this.file;
    }
    
    @Override
    public List<FtpFile> listFiles() {
        if (!this.file.isDirectory()) {
            return null;
        }
        final File[] files = this.file.listFiles();
        if (files == null) {
            return null;
        }
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(final File f1, final File f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });
        String virtualFileStr = this.getAbsolutePath();
        if (virtualFileStr.charAt(virtualFileStr.length() - 1) != '/') {
            virtualFileStr += '/';
        }
        final FtpFile[] virtualFiles = new FtpFile[files.length];
        for (int i = 0; i < files.length; ++i) {
            final File fileObj = files[i];
            final String fileName = virtualFileStr + fileObj.getName();
            virtualFiles[i] = new NativeFtpFile(fileName, fileObj, this.user);
        }
        return Collections.unmodifiableList((List<? extends FtpFile>)Arrays.asList((T[])virtualFiles));
    }
    
    @Override
    public OutputStream createOutputStream(final long offset) throws IOException {
        if (!this.isWritable()) {
            throw new IOException("No write permission : " + this.file.getName());
        }
        final RandomAccessFile raf = new RandomAccessFile(this.file, "rw");
        raf.setLength(offset);
        raf.seek(offset);
        return new FileOutputStream(raf.getFD()) {
            @Override
            public void close() throws IOException {
                super.close();
                raf.close();
            }
        };
    }
    
    @Override
    public InputStream createInputStream(final long offset) throws IOException {
        if (!this.isReadable()) {
            throw new IOException("No read permission : " + this.file.getName());
        }
        final RandomAccessFile raf = new RandomAccessFile(this.file, "r");
        raf.seek(offset);
        return new FileInputStream(raf.getFD()) {
            @Override
            public void close() throws IOException {
                super.close();
                raf.close();
            }
        };
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof NativeFtpFile) {
            String thisCanonicalPath;
            String otherCanonicalPath;
            try {
                thisCanonicalPath = this.file.getCanonicalPath();
                otherCanonicalPath = ((NativeFtpFile)obj).file.getCanonicalPath();
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to get the canonical path", e);
            }
            return thisCanonicalPath.equals(otherCanonicalPath);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        try {
            return this.file.getCanonicalPath().hashCode();
        }
        catch (IOException e) {
            return 0;
        }
    }
}
