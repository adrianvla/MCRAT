// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver;

public class FtpServerConfigurationException extends RuntimeException
{
    private static final long serialVersionUID = -1328432839915898987L;
    
    public FtpServerConfigurationException() {
    }
    
    public FtpServerConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public FtpServerConfigurationException(final String message) {
        super(message);
    }
    
    public FtpServerConfigurationException(final Throwable cause) {
        super(cause);
    }
}
