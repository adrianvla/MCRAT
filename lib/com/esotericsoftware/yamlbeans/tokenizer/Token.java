// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.tokenizer;

public class Token
{
    static final Token DOCUMENT_START;
    static final Token DOCUMENT_END;
    static final Token BLOCK_MAPPING_START;
    static final Token BLOCK_SEQUENCE_START;
    static final Token BLOCK_ENTRY;
    static final Token BLOCK_END;
    static final Token FLOW_ENTRY;
    static final Token FLOW_MAPPING_END;
    static final Token FLOW_MAPPING_START;
    static final Token FLOW_SEQUENCE_END;
    static final Token FLOW_SEQUENCE_START;
    static final Token KEY;
    static final Token VALUE;
    static final Token STREAM_END;
    static final Token STREAM_START;
    public final TokenType type;
    
    public Token(final TokenType type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return "<" + this.type + ">";
    }
    
    static {
        DOCUMENT_START = new Token(TokenType.DOCUMENT_START);
        DOCUMENT_END = new Token(TokenType.DOCUMENT_END);
        BLOCK_MAPPING_START = new Token(TokenType.BLOCK_MAPPING_START);
        BLOCK_SEQUENCE_START = new Token(TokenType.BLOCK_SEQUENCE_START);
        BLOCK_ENTRY = new Token(TokenType.BLOCK_ENTRY);
        BLOCK_END = new Token(TokenType.BLOCK_END);
        FLOW_ENTRY = new Token(TokenType.FLOW_ENTRY);
        FLOW_MAPPING_END = new Token(TokenType.FLOW_MAPPING_END);
        FLOW_MAPPING_START = new Token(TokenType.FLOW_MAPPING_START);
        FLOW_SEQUENCE_END = new Token(TokenType.FLOW_SEQUENCE_END);
        FLOW_SEQUENCE_START = new Token(TokenType.FLOW_SEQUENCE_START);
        KEY = new Token(TokenType.KEY);
        VALUE = new Token(TokenType.VALUE);
        STREAM_END = new Token(TokenType.STREAM_END);
        STREAM_START = new Token(TokenType.STREAM_START);
    }
}
