// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface FtpFile
{
    String getAbsolutePath();
    
    String getName();
    
    boolean isHidden();
    
    boolean isDirectory();
    
    boolean isFile();
    
    boolean doesExist();
    
    boolean isReadable();
    
    boolean isWritable();
    
    boolean isRemovable();
    
    String getOwnerName();
    
    String getGroupName();
    
    int getLinkCount();
    
    long getLastModified();
    
    boolean setLastModified(final long p0);
    
    long getSize();
    
    Object getPhysicalFile();
    
    boolean mkdir();
    
    boolean delete();
    
    boolean move(final FtpFile p0);
    
    List<? extends FtpFile> listFiles();
    
    OutputStream createOutputStream(final long p0) throws IOException;
    
    InputStream createInputStream(final long p0) throws IOException;
}
