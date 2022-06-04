// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.scalar;

import com.esotericsoftware.yamlbeans.YamlException;

public interface ScalarSerializer<T>
{
    String write(final T p0) throws YamlException;
    
    T read(final String p0) throws YamlException;
}
