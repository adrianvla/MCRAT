// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.tokenizer;

public class TagToken extends Token
{
    private final String handle;
    private final String suffix;
    
    public TagToken(final String handle, final String suffix) {
        super(TokenType.TAG);
        this.handle = handle;
        this.suffix = suffix;
    }
    
    public String getHandle() {
        return this.handle;
    }
    
    public String getSuffix() {
        return this.suffix;
    }
    
    @Override
    public String toString() {
        return "<" + this.type + " handle='" + this.handle + "' suffix='" + this.suffix + "'>";
    }
}
