// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.buffer;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;

public class SimpleBufferAllocator implements IoBufferAllocator
{
    @Override
    public IoBuffer allocate(final int capacity, final boolean direct) {
        return this.wrap(this.allocateNioBuffer(capacity, direct));
    }
    
    @Override
    public ByteBuffer allocateNioBuffer(final int capacity, final boolean direct) {
        ByteBuffer nioBuffer;
        if (direct) {
            nioBuffer = ByteBuffer.allocateDirect(capacity);
        }
        else {
            nioBuffer = ByteBuffer.allocate(capacity);
        }
        return nioBuffer;
    }
    
    @Override
    public IoBuffer wrap(final ByteBuffer nioBuffer) {
        return new SimpleBuffer(nioBuffer);
    }
    
    @Override
    public void dispose() {
    }
    
    private class SimpleBuffer extends AbstractIoBuffer
    {
        private ByteBuffer buf;
        
        protected SimpleBuffer(final ByteBuffer buf) {
            super(SimpleBufferAllocator.this, buf.capacity());
            (this.buf = buf).order(ByteOrder.BIG_ENDIAN);
        }
        
        protected SimpleBuffer(final SimpleBuffer parent, final ByteBuffer buf) {
            super(parent);
            this.buf = buf;
        }
        
        @Override
        public ByteBuffer buf() {
            return this.buf;
        }
        
        @Override
        protected void buf(final ByteBuffer buf) {
            this.buf = buf;
        }
        
        @Override
        protected IoBuffer duplicate0() {
            return new SimpleBuffer(this, this.buf.duplicate());
        }
        
        @Override
        protected IoBuffer slice0() {
            return new SimpleBuffer(this, this.buf.slice());
        }
        
        @Override
        protected IoBuffer asReadOnlyBuffer0() {
            return new SimpleBuffer(this, this.buf.asReadOnlyBuffer());
        }
        
        @Override
        public byte[] array() {
            return this.buf.array();
        }
        
        @Override
        public int arrayOffset() {
            return this.buf.arrayOffset();
        }
        
        @Override
        public boolean hasArray() {
            return this.buf.hasArray();
        }
        
        @Override
        public void free() {
        }
    }
}
