// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.parser;

public class DocumentEndEvent extends Event
{
    public final boolean isExplicit;
    
    public DocumentEndEvent(final boolean isExplicit) {
        super(EventType.DOCUMENT_END);
        this.isExplicit = isExplicit;
    }
    
    @Override
    public String toString() {
        return "<" + this.type + " explicit='" + this.isExplicit + "'>";
    }
}
