// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftpletcontainer;

import java.util.Map;
import org.apache.ftpserver.ftplet.Ftplet;

public interface FtpletContainer extends Ftplet
{
    Ftplet getFtplet(final String p0);
    
    Map<String, Ftplet> getFtplets();
}
