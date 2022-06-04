// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.tokenizer;

public enum TokenType
{
    DOCUMENT_START, 
    DOCUMENT_END, 
    BLOCK_MAPPING_START, 
    BLOCK_SEQUENCE_START, 
    BLOCK_ENTRY, 
    BLOCK_END, 
    FLOW_ENTRY, 
    FLOW_MAPPING_END, 
    FLOW_MAPPING_START, 
    FLOW_SEQUENCE_END, 
    FLOW_SEQUENCE_START, 
    KEY, 
    VALUE, 
    STREAM_END, 
    STREAM_START, 
    ALIAS, 
    ANCHOR, 
    DIRECTIVE, 
    SCALAR, 
    TAG;
    
    @Override
    public String toString() {
        return this.name().toLowerCase().replace('_', ' ');
    }
}
