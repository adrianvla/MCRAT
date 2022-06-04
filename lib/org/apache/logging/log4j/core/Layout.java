// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core;

import java.util.Map;
import org.apache.logging.log4j.core.layout.Encoder;
import java.io.Serializable;

public interface Layout<T extends Serializable> extends Encoder<LogEvent>
{
    public static final String ELEMENT_TYPE = "layout";
    
    byte[] getFooter();
    
    byte[] getHeader();
    
    byte[] toByteArray(final LogEvent p0);
    
    T toSerializable(final LogEvent p0);
    
    String getContentType();
    
    Map<String, String> getContentFormat();
}
