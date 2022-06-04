// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.parser;

public abstract class CollectionStartEvent extends NodeEvent
{
    public final String tag;
    public final boolean isImplicit;
    public final boolean isFlowStyle;
    
    protected CollectionStartEvent(final EventType eventType, final String anchor, final String tag, final boolean isImplicit, final boolean isFlowStyle) {
        super(eventType, anchor);
        this.tag = tag;
        this.isImplicit = isImplicit;
        this.isFlowStyle = isFlowStyle;
    }
    
    @Override
    public String toString() {
        return "<" + this.type + " anchor='" + this.anchor + "' tag='" + this.tag + "' implicit='" + this.isImplicit + "' flowStyle='" + this.isFlowStyle + "'>";
    }
}
