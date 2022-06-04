// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.file;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.io.File;

public class FilenameFileRegion extends DefaultFileRegion
{
    private final File file;
    
    public FilenameFileRegion(final File file, final FileChannel channel) throws IOException {
        this(file, channel, 0L, file.length());
    }
    
    public FilenameFileRegion(final File file, final FileChannel channel, final long position, final long remainingBytes) {
        super(channel, position, remainingBytes);
        if (file == null) {
            throw new IllegalArgumentException("file can not be null");
        }
        this.file = file;
    }
    
    @Override
    public String getFilename() {
        return this.file.getAbsolutePath();
    }
}
