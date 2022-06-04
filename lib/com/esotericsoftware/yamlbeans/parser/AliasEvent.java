// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.parser;

public class AliasEvent extends NodeEvent
{
    public AliasEvent(final String anchor) {
        super(EventType.ALIAS, anchor);
    }
    
    @Override
    public String toString() {
        return "<" + this.type + " anchor='" + this.anchor + "'>";
    }
}
