// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

public class AuthenticationFailedException extends FtpException
{
    private static final long serialVersionUID = -1328383839915898987L;
    
    public AuthenticationFailedException() {
    }
    
    public AuthenticationFailedException(final String msg) {
        super(msg);
    }
    
    public AuthenticationFailedException(final Throwable th) {
        super(th);
    }
    
    public AuthenticationFailedException(final String msg, final Throwable th) {
        super(msg, th);
    }
}
