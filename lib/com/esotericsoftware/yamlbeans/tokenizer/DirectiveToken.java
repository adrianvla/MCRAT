// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.tokenizer;

public class DirectiveToken extends Token
{
    private final String directive;
    private final String value;
    
    public DirectiveToken(final String directive, final String value) {
        super(TokenType.DIRECTIVE);
        this.directive = directive;
        this.value = value;
    }
    
    public String getDirective() {
        return this.directive;
    }
    
    public String getValue() {
        return this.value;
    }
    
    @Override
    public String toString() {
        return "<" + this.type + " directive='" + this.directive + "' value='" + this.value + "'>";
    }
}
