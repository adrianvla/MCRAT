// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.codec;

import java.util.Iterator;
import org.apache.mina.core.buffer.IoBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

public abstract class AbstractProtocolEncoderOutput implements ProtocolEncoderOutput
{
    private final Queue<Object> messageQueue;
    private boolean buffersOnly;
    
    public AbstractProtocolEncoderOutput() {
        this.messageQueue = new ConcurrentLinkedQueue<Object>();
        this.buffersOnly = true;
    }
    
    public Queue<Object> getMessageQueue() {
        return this.messageQueue;
    }
    
    @Override
    public void write(final Object encodedMessage) {
        if (encodedMessage instanceof IoBuffer) {
            final IoBuffer buf = (IoBuffer)encodedMessage;
            if (!buf.hasRemaining()) {
                throw new IllegalArgumentException("buf is empty. Forgot to call flip()?");
            }
            this.messageQueue.offer(buf);
        }
        else {
            this.messageQueue.offer(encodedMessage);
            this.buffersOnly = false;
        }
    }
    
    @Override
    public void mergeAll() {
        if (!this.buffersOnly) {
            throw new IllegalStateException("the encoded message list contains a non-buffer.");
        }
        final int size = this.messageQueue.size();
        if (size < 2) {
            return;
        }
        int sum = 0;
        for (final Object b : this.messageQueue) {
            sum += ((IoBuffer)b).remaining();
        }
        final IoBuffer newBuf = IoBuffer.allocate(sum);
        while (true) {
            final IoBuffer buf = this.messageQueue.poll();
            if (buf == null) {
                break;
            }
            newBuf.put(buf);
        }
        newBuf.flip();
        this.messageQueue.add(newBuf);
    }
}
