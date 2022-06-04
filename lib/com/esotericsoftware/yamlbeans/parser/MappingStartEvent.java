// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.parser;

public class MappingStartEvent extends CollectionStartEvent
{
    public MappingStartEvent(final String anchor, final String tag, final boolean implicit, final boolean flowStyle) {
        super(EventType.MAPPING_START, anchor, tag, implicit, flowStyle);
    }
}
