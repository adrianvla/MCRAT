// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.emitter;

public class EmitterConfig
{
    boolean canonical;
    boolean useVerbatimTags;
    int indentSize;
    int wrapColumn;
    boolean escapeUnicode;
    boolean prettyFlow;
    
    public EmitterConfig() {
        this.useVerbatimTags = true;
        this.indentSize = 3;
        this.wrapColumn = 100;
        this.escapeUnicode = true;
    }
    
    public void setCanonical(final boolean canonical) {
        this.canonical = canonical;
    }
    
    public void setIndentSize(final int indentSize) {
        if (indentSize < 2) {
            throw new IllegalArgumentException("indentSize cannot be less than 2.");
        }
        this.indentSize = indentSize;
    }
    
    public void setWrapColumn(final int wrapColumn) {
        if (wrapColumn <= 4) {
            throw new IllegalArgumentException("wrapColumn must be greater than 4.");
        }
        this.wrapColumn = wrapColumn;
    }
    
    public void setUseVerbatimTags(final boolean useVerbatimTags) {
        this.useVerbatimTags = useVerbatimTags;
    }
    
    public void setEscapeUnicode(final boolean escapeUnicode) {
        this.escapeUnicode = escapeUnicode;
    }
    
    public void setPrettyFlow(final boolean prettyFlow) {
        this.prettyFlow = prettyFlow;
    }
}
