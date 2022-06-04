// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver;

import org.apache.ftpserver.ftplet.FtpException;

public class DataConnectionException extends FtpException
{
    private static final long serialVersionUID = -1328383839917648987L;
    
    public DataConnectionException() {
    }
    
    public DataConnectionException(final String msg) {
        super(msg);
    }
    
    public DataConnectionException(final Throwable th) {
        super(th);
    }
    
    public DataConnectionException(final String msg, final Throwable th) {
        super(msg, th);
    }
}
