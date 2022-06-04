// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl.listing;

import org.apache.ftpserver.ftplet.FtpException;
import java.util.ArrayList;
import java.io.IOException;
import org.apache.ftpserver.ftplet.FileSystemView;
import java.util.Iterator;
import org.apache.ftpserver.ftplet.FtpFile;
import java.util.List;

public class DirectoryLister
{
    private String traverseFiles(final List<? extends FtpFile> files, final FileFilter filter, final FileFormater formater) {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.traverseFiles(files, filter, formater, true));
        sb.append(this.traverseFiles(files, filter, formater, false));
        return sb.toString();
    }
    
    private String traverseFiles(final List<? extends FtpFile> files, final FileFilter filter, final FileFormater formater, final boolean matchDirs) {
        final StringBuilder sb = new StringBuilder();
        for (final FtpFile file : files) {
            if (file == null) {
                continue;
            }
            if ((filter != null && !filter.accept(file)) || file.isDirectory() != matchDirs) {
                continue;
            }
            sb.append(formater.format(file));
        }
        return sb.toString();
    }
    
    public String listFiles(final ListArgument argument, final FileSystemView fileSystemView, final FileFormater formater) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final List<? extends FtpFile> files = this.listFiles(fileSystemView, argument.getFile());
        if (files != null) {
            FileFilter filter = null;
            if (!argument.hasOption('a')) {
                filter = new VisibleFileFilter();
            }
            if (argument.getPattern() != null) {
                filter = new RegexFileFilter(argument.getPattern(), filter);
            }
            sb.append(this.traverseFiles(files, filter, formater));
        }
        return sb.toString();
    }
    
    private List<? extends FtpFile> listFiles(final FileSystemView fileSystemView, final String file) {
        List<? extends FtpFile> files = null;
        try {
            final FtpFile virtualFile = fileSystemView.getFile(file);
            if (virtualFile.isFile()) {
                final List<FtpFile> auxFiles = new ArrayList<FtpFile>();
                auxFiles.add(virtualFile);
                files = auxFiles;
            }
            else {
                files = virtualFile.listFiles();
            }
        }
        catch (FtpException ex) {}
        return files;
    }
}
