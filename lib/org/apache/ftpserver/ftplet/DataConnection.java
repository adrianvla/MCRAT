// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface DataConnection
{
    long transferFromClient(final FtpSession p0, final OutputStream p1) throws IOException;
    
    long transferToClient(final FtpSession p0, final InputStream p1) throws IOException;
    
    void transferToClient(final FtpSession p0, final String p1) throws IOException;
}
