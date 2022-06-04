// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl.listing;

import java.util.Arrays;
import org.apache.ftpserver.util.DateUtils;
import org.apache.ftpserver.ftplet.FtpFile;

public class LISTFileFormater implements FileFormater
{
    private static final char DELIM = ' ';
    private static final char[] NEWLINE;
    
    @Override
    public String format(final FtpFile file) {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getPermission(file));
        sb.append(' ');
        sb.append(' ');
        sb.append(' ');
        sb.append(String.valueOf(file.getLinkCount()));
        sb.append(' ');
        sb.append(file.getOwnerName());
        sb.append(' ');
        sb.append(file.getGroupName());
        sb.append(' ');
        sb.append(this.getLength(file));
        sb.append(' ');
        sb.append(this.getLastModified(file));
        sb.append(' ');
        sb.append(file.getName());
        sb.append(LISTFileFormater.NEWLINE);
        return sb.toString();
    }
    
    private String getLength(final FtpFile file) {
        final String initStr = "            ";
        long sz = 0L;
        if (file.isFile()) {
            sz = file.getSize();
        }
        final String szStr = String.valueOf(sz);
        if (szStr.length() > initStr.length()) {
            return szStr;
        }
        return initStr.substring(0, initStr.length() - szStr.length()) + szStr;
    }
    
    private String getLastModified(final FtpFile file) {
        return DateUtils.getUnixDate(file.getLastModified());
    }
    
    private char[] getPermission(final FtpFile file) {
        final char[] permission = new char[10];
        Arrays.fill(permission, '-');
        permission[0] = (file.isDirectory() ? 'd' : '-');
        permission[1] = (file.isReadable() ? 'r' : '-');
        permission[2] = (file.isWritable() ? 'w' : '-');
        permission[3] = (file.isDirectory() ? 'x' : '-');
        return permission;
    }
    
    static {
        NEWLINE = new char[] { '\r', '\n' };
    }
}
