// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.tokenizer;

public class ScalarToken extends Token
{
    private String value;
    private boolean plain;
    private char style;
    
    public ScalarToken(final String value, final boolean plain) {
        this(value, plain, '\0');
    }
    
    public ScalarToken(final String value, final boolean plain, final char style) {
        super(TokenType.SCALAR);
        this.value = value;
        this.plain = plain;
        this.style = style;
    }
    
    public boolean getPlain() {
        return this.plain;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public char getStyle() {
        return this.style;
    }
    
    @Override
    public String toString() {
        return "<" + this.type + " value='" + this.value + "' plain='" + this.plain + "' style='" + ((this.style == '\0') ? "" : Character.valueOf(this.style)) + "'>";
    }
}
