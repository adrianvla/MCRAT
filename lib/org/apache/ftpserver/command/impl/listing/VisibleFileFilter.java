// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl.listing;

import org.apache.ftpserver.ftplet.FtpFile;

public class VisibleFileFilter implements FileFilter
{
    private final FileFilter wrappedFilter;
    
    public VisibleFileFilter() {
        this(null);
    }
    
    public VisibleFileFilter(final FileFilter wrappedFilter) {
        this.wrappedFilter = wrappedFilter;
    }
    
    @Override
    public boolean accept(final FtpFile file) {
        return (this.wrappedFilter == null || this.wrappedFilter.accept(file)) && !file.isHidden();
    }
}
