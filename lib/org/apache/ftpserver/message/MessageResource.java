// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.message;

import java.util.Map;
import java.util.List;

public interface MessageResource
{
    List<String> getAvailableLanguages();
    
    String getMessage(final int p0, final String p1, final String p2);
    
    Map<String, String> getMessages(final String p0);
}
