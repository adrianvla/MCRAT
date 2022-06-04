// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans;

import java.io.IOException;

public class YamlException extends IOException
{
    public YamlException() {
    }
    
    public YamlException(final String message, final Throwable cause) {
        super(message);
        this.initCause(cause);
    }
    
    public YamlException(final String message) {
        super(message);
    }
    
    public YamlException(final Throwable cause) {
        this.initCause(cause);
    }
}
