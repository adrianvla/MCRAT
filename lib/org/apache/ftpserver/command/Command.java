// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command;

import org.apache.ftpserver.ftplet.FtpException;
import java.io.IOException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;

public interface Command
{
    void execute(final FtpIoSession p0, final FtpServerContext p1, final FtpRequest p2) throws IOException, FtpException;
}
