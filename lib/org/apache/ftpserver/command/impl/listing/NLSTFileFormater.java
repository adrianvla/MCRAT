// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl.listing;

import org.apache.ftpserver.ftplet.FtpFile;

public class NLSTFileFormater implements FileFormater
{
    private static final char[] NEWLINE;
    
    @Override
    public String format(final FtpFile file) {
        final StringBuilder sb = new StringBuilder();
        sb.append(file.getName());
        sb.append(NLSTFileFormater.NEWLINE);
        return sb.toString();
    }
    
    static {
        NEWLINE = new char[] { '\r', '\n' };
    }
}
