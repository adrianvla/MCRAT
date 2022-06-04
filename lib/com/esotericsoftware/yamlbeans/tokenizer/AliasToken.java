// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.tokenizer;

public class AliasToken extends Token
{
    private String instanceName;
    
    public AliasToken() {
        super(TokenType.ALIAS);
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
