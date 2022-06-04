// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.codec;

import java.util.LinkedList;
import java.util.Queue;

public abstract class AbstractProtocolDecoderOutput implements ProtocolDecoderOutput
{
    private final Queue<Object> messageQueue;
    
    public AbstractProtocolDecoderOutput() {
        this.messageQueue = new LinkedList<Object>();
    }
    
    public Queue<Object> getMessageQueue() {
        return this.messageQueue;
    }
    
    @Override
    public void write(final Object message) {
        if (message == null) {
            throw new IllegalArgumentException("message");
        }
        this.messageQueue.add(message);
    }
}
