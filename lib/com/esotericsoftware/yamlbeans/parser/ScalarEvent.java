// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.parser;

import java.util.Arrays;

public class ScalarEvent extends NodeEvent
{
    public final String tag;
    public final boolean[] implicit;
    public final String value;
    public final char style;
    
    public ScalarEvent(final String anchor, final String tag, final boolean[] implicit, final String value, final char style) {
        super(EventType.SCALAR, anchor);
        this.tag = tag;
        this.implicit = implicit;
        this.value = value;
        this.style = style;
    }
    
    @Override
    public String toString() {
        return "<" + this.type + " value='" + this.value + "' anchor='" + this.anchor + "' tag='" + this.tag + "' implicit='" + Arrays.toString(this.implicit) + "' style='" + ((this.style == '\0') ? "" : Character.valueOf(this.style)) + "'>";
    }
}
