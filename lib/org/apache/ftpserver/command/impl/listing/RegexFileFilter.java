// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl.listing;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.util.RegularExpr;

public class RegexFileFilter implements FileFilter
{
    private final RegularExpr regex;
    private final FileFilter wrappedFilter;
    
    public RegexFileFilter(final String regex) {
        this(regex, null);
    }
    
    public RegexFileFilter(final String regex, final FileFilter wrappedFilter) {
        this.regex = new RegularExpr(regex);
        this.wrappedFilter = wrappedFilter;
    }
    
    @Override
    public boolean accept(final FtpFile file) {
        return (this.wrappedFilter == null || this.wrappedFilter.accept(file)) && this.regex.isMatch(file.getName());
    }
}
