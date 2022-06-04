// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.parser;

import java.util.Map;
import com.esotericsoftware.yamlbeans.Version;

public class DocumentStartEvent extends Event
{
    public final boolean isExplicit;
    public final Version version;
    public final Map<String, String> tags;
    
    public DocumentStartEvent(final boolean explicit, final Version version, final Map<String, String> tags) {
        super(EventType.DOCUMENT_START);
        this.isExplicit = explicit;
        this.version = version;
        this.tags = tags;
    }
    
    @Override
    public String toString() {
        return "<" + this.type + " explicit='" + this.isExplicit + "' version='" + this.version + "' tags='" + this.tags + "'>";
    }
}
