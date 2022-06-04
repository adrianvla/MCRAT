// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.filesystem.nativefs.impl;

import java.io.File;
import java.io.FileFilter;

public class NameEqualsFileFilter implements FileFilter
{
    private final String nameToMatch;
    private final boolean caseInsensitive;
    
    public NameEqualsFileFilter(final String nameToMatch, final boolean caseInsensitive) {
        this.nameToMatch = nameToMatch;
        this.caseInsensitive = caseInsensitive;
    }
    
    @Override
    public boolean accept(final File file) {
        if (this.caseInsensitive) {
            return file.getName().equalsIgnoreCase(this.nameToMatch);
        }
        return file.getName().equals(this.nameToMatch);
    }
}
