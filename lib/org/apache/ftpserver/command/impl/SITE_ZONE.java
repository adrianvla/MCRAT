// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import org.apache.ftpserver.ftplet.FtpException;
import java.io.IOException;
import org.apache.ftpserver.ftplet.DefaultFtpReply;
import java.util.Date;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import java.text.SimpleDateFormat;
import org.apache.ftpserver.command.AbstractCommand;

public class SITE_ZONE extends AbstractCommand
{
    private static final SimpleDateFormat TIMEZONE_FMT;
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException, FtpException {
        session.resetState();
        final String timezone = SITE_ZONE.TIMEZONE_FMT.format(new Date());
        session.write(new DefaultFtpReply(200, timezone));
    }
    
    static {
        TIMEZONE_FMT = new SimpleDateFormat("Z");
    }
}
