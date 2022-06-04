// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl.listing;

import org.apache.ftpserver.ftplet.FtpFile;

public interface FileFilter
{
    boolean accept(final FtpFile p0);
}
