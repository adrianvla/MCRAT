// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.parser;

public class Event
{
    public static final Event MAPPING_END;
    public static final Event SEQUENCE_END;
    public static final Event STREAM_END;
    public static final Event STREAM_START;
    public static final Event DOCUMENT_END_TRUE;
    public static final Event DOCUMENT_END_FALSE;
    public final EventType type;
    
    public Event(final EventType type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return "<" + this.type + ">";
    }
    
    static {
        MAPPING_END = new Event(EventType.MAPPING_END);
        SEQUENCE_END = new Event(EventType.SEQUENCE_END);
        STREAM_END = new Event(EventType.STREAM_END);
        STREAM_START = new Event(EventType.STREAM_START);
        DOCUMENT_END_TRUE = new DocumentEndEvent(true);
        DOCUMENT_END_FALSE = new DocumentEndEvent(false);
    }
}
