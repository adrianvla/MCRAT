// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl.listing;

import org.apache.ftpserver.util.DateUtils;
import org.apache.ftpserver.ftplet.FtpFile;

public class MLSTFileFormater implements FileFormater
{
    private static final String[] DEFAULT_TYPES;
    private static final char[] NEWLINE;
    private String[] selectedTypes;
    
    public MLSTFileFormater(final String[] selectedTypes) {
        this.selectedTypes = MLSTFileFormater.DEFAULT_TYPES;
        if (selectedTypes != null) {
            this.selectedTypes = selectedTypes.clone();
        }
    }
    
    @Override
    public String format(final FtpFile file) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.selectedTypes.length; ++i) {
            final String type = this.selectedTypes[i];
            if (type.equalsIgnoreCase("size")) {
                sb.append("Size=");
                sb.append(String.valueOf(file.getSize()));
                sb.append(';');
            }
            else if (type.equalsIgnoreCase("modify")) {
                final String timeStr = DateUtils.getFtpDate(file.getLastModified());
                sb.append("Modify=");
                sb.append(timeStr);
                sb.append(';');
            }
            else if (type.equalsIgnoreCase("type")) {
                if (file.isFile()) {
                    sb.append("Type=file;");
                }
                else if (file.isDirectory()) {
                    sb.append("Type=dir;");
                }
            }
            else if (type.equalsIgnoreCase("perm")) {
                sb.append("Perm=");
                if (file.isReadable()) {
                    if (file.isFile()) {
                        sb.append('r');
                    }
                    else if (file.isDirectory()) {
                        sb.append('e');
                        sb.append('l');
                    }
                }
                if (file.isWritable()) {
                    if (file.isFile()) {
                        sb.append('a');
                        sb.append('d');
                        sb.append('f');
                        sb.append('w');
                    }
                    else if (file.isDirectory()) {
                        sb.append('f');
                        sb.append('p');
                        sb.append('c');
                        sb.append('m');
                    }
                }
                sb.append(';');
            }
        }
        sb.append(' ');
        sb.append(file.getName());
        sb.append(MLSTFileFormater.NEWLINE);
        return sb.toString();
    }
    
    static {
        DEFAULT_TYPES = new String[] { "Size", "Modify", "Type" };
        NEWLINE = new char[] { '\r', '\n' };
    }
}
