// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.tokenizer;

public class AnchorToken extends Token
{
    private String instanceName;
    
    public AnchorToken() {
        super(TokenType.ANCHOR);
    }
    
    public String getInstanceName() {
        return this.instanceName;
    }
    
    public void setInstanceName(final String instanceName) {
        this.instanceName = instanceName;
    }
    
    @Override
    public String toString() {
        return "<" + this.type + " aliasName='" + this.instanceName + "'>";
    }
}
