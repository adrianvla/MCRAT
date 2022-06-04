// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.parser;

public abstract class NodeEvent extends Event
{
    public final String anchor;
    
    public NodeEvent(final EventType eventType, final String anchor) {
        super(eventType);
        this.anchor = anchor;
    }
}
