// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.parser;

public class SequenceStartEvent extends CollectionStartEvent
{
    public SequenceStartEvent(final String anchor, final String tag, final boolean implicit, final boolean flowStyle) {
        super(EventType.SEQUENCE_START, anchor, tag, implicit, flowStyle);
    }
}
