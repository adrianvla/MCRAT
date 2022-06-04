// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

public class ClassGraphException extends IllegalArgumentException
{
    static final long serialVersionUID = 1L;
    
    ClassGraphException(final String message) {
        super(message);
    }
    
    ClassGraphException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
