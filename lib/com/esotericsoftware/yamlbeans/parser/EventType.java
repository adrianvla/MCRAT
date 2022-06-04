// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.parser;

public enum EventType
{
    STREAM_START, 
    STREAM_END, 
    SEQUENCE_START, 
    SEQUENCE_END, 
    SCALAR, 
    MAPPING_START, 
    MAPPING_END, 
    DOCUMENT_START, 
    DOCUMENT_END, 
    ALIAS;
    
    @Override
    public String toString() {
        return this.name().toLowerCase().replace('_', ' ');
    }
}
