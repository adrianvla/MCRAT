// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

public class FtpException extends Exception
{
    private static final long serialVersionUID = -1328383839915898987L;
    
    public FtpException() {
    }
    
    public FtpException(final String msg) {
        super(msg);
    }
    
    public FtpException(final Throwable th) {
        super(th.getMessage());
    }
    
    public FtpException(final String msg, final Throwable th) {
        super(msg);
    }
    
    @Deprecated
    public Throwable getRootCause() {
        return this.getCause();
    }
}
