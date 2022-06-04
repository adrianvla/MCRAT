// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.buffer;

import java.util.Iterator;
import java.util.Set;
import java.util.EnumSet;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.io.EOFException;
import java.io.ObjectStreamClass;
import java.io.ObjectInputStream;
import java.nio.BufferOverflowException;
import java.nio.charset.CharsetEncoder;
import java.nio.BufferUnderflowException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CoderResult;
import java.nio.charset.CharsetDecoder;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.CharBuffer;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;

public abstract class AbstractIoBuffer extends IoBuffer
{
    private final boolean derived;
    private boolean autoExpand;
    private boolean autoShrink;
    private boolean recapacityAllowed;
    private int minimumCapacity;
    private static final long BYTE_MASK = 255L;
    private static final long SHORT_MASK = 65535L;
    private static final long INT_MASK = 4294967295L;
    private int mark;
    
    protected AbstractIoBuffer(final IoBufferAllocator allocator, final int initialCapacity) {
        this.recapacityAllowed = true;
        this.mark = -1;
        IoBuffer.setAllocator(allocator);
        this.recapacityAllowed = true;
        this.derived = false;
        this.minimumCapacity = initialCapacity;
    }
    
    protected AbstractIoBuffer(final AbstractIoBuffer parent) {
        this.recapacityAllowed = true;
        this.mark = -1;
        IoBuffer.setAllocator(IoBuffer.getAllocator());
        this.recapacityAllowed = false;
        this.derived = true;
        this.minimumCapacity = parent.minimumCapacity;
    }
    
    @Override
    public final boolean isDirect() {
        return this.buf().isDirect();
    }
    
    @Override
    public final boolean isReadOnly() {
        return this.buf().isReadOnly();
    }
    
    protected abstract void buf(final ByteBuffer p0);
    
    @Override
    public final int minimumCapacity() {
        return this.minimumCapacity;
    }
    
    @Override
    public final IoBuffer minimumCapacity(final int minimumCapacity) {
        if (minimumCapacity < 0) {
            throw new IllegalArgumentException("minimumCapacity: " + minimumCapacity);
        }
        this.minimumCapacity = minimumCapacity;
        return this;
    }
    
    @Override
    public final int capacity() {
        return this.buf().capacity();
    }
    
    @Override
    public final IoBuffer capacity(final int newCapacity) {
        if (!this.recapacityAllowed) {
            throw new IllegalStateException("Derived buffers and their parent can't be expanded.");
        }
        if (newCapacity > this.capacity()) {
            final int pos = this.position();
            final int limit = this.limit();
            final ByteOrder bo = this.order();
            final ByteBuffer oldBuf = this.buf();
            final ByteBuffer newBuf = IoBuffer.getAllocator().allocateNioBuffer(newCapacity, this.isDirect());
            oldBuf.clear();
            newBuf.put(oldBuf);
            this.buf(newBuf);
            this.buf().limit(limit);
            if (this.mark >= 0) {
                this.buf().position(this.mark);
                this.buf().mark();
            }
            this.buf().position(pos);
            this.buf().order(bo);
        }
        return this;
    }
    
    @Override
    public final boolean isAutoExpand() {
        return this.autoExpand && this.recapacityAllowed;
    }
    
    @Override
    public final boolean isAutoShrink() {
        return this.autoShrink && this.recapacityAllowed;
    }
    
    @Override
    public final boolean isDerived() {
        return this.derived;
    }
    
    @Override
    public final IoBuffer setAutoExpand(final boolean autoExpand) {
        if (!this.recapacityAllowed) {
            throw new IllegalStateException("Derived buffers and their parent can't be expanded.");
        }
        this.autoExpand = autoExpand;
        return this;
    }
    
    @Override
    public final IoBuffer setAutoShrink(final boolean autoShrink) {
        if (!this.recapacityAllowed) {
            throw new IllegalStateException("Derived buffers and their parent can't be shrinked.");
        }
        this.autoShrink = autoShrink;
        return this;
    }
    
    @Override
    public final IoBuffer expand(final int expectedRemaining) {
        return this.expand(this.position(), expectedRemaining, false);
    }
    
    private IoBuffer expand(final int expectedRemaining, final boolean autoExpand) {
        return this.expand(this.position(), expectedRemaining, autoExpand);
    }
    
    @Override
    public final IoBuffer expand(final int pos, final int expectedRemaining) {
        return this.expand(pos, expectedRemaining, false);
    }
    
    private IoBuffer expand(final int pos, final int expectedRemaining, final boolean autoExpand) {
        if (!this.recapacityAllowed) {
            throw new IllegalStateException("Derived buffers and their parent can't be expanded.");
        }
        final int end = pos + expectedRemaining;
        int newCapacity;
        if (autoExpand) {
            newCapacity = IoBuffer.normalizeCapacity(end);
        }
        else {
            newCapacity = end;
        }
        if (newCapacity > this.capacity()) {
            this.capacity(newCapacity);
        }
        if (end > this.limit()) {
            this.buf().limit(end);
        }
        return this;
    }
    
    @Override
    public final IoBuffer shrink() {
        if (!this.recapacityAllowed) {
            throw new IllegalStateException("Derived buffers and their parent can't be expanded.");
        }
        final int position = this.position();
        final int capacity = this.capacity();
        final int limit = this.limit();
        if (capacity == limit) {
            return this;
        }
        int newCapacity = capacity;
        final int minCapacity = Math.max(this.minimumCapacity, limit);
        while (true) {
            while (newCapacity >>> 1 >= minCapacity) {
                newCapacity >>>= 1;
                if (minCapacity == 0) {
                    newCapacity = Math.max(minCapacity, newCapacity);
                    if (newCapacity == capacity) {
                        return this;
                    }
                    final ByteOrder bo = this.order();
                    final ByteBuffer oldBuf = this.buf();
                    final ByteBuffer newBuf = IoBuffer.getAllocator().allocateNioBuffer(newCapacity, this.isDirect());
                    oldBuf.position(0);
                    oldBuf.limit(limit);
                    newBuf.put(oldBuf);
                    this.buf(newBuf);
                    this.buf().position(position);
                    this.buf().limit(limit);
                    this.buf().order(bo);
                    this.mark = -1;
                    return this;
                }
            }
            continue;
        }
    }
    
    @Override
    public final int position() {
        return this.buf().position();
    }
    
    @Override
    public final IoBuffer position(final int newPosition) {
        this.autoExpand(newPosition, 0);
        this.buf().position(newPosition);
        if (this.mark > newPosition) {
            this.mark = -1;
        }
        return this;
    }
    
    @Override
    public final int limit() {
        return this.buf().limit();
    }
    
    @Override
    public final IoBuffer limit(final int newLimit) {
        this.autoExpand(newLimit, 0);
        this.buf().limit(newLimit);
        if (this.mark > newLimit) {
            this.mark = -1;
        }
        return this;
    }
    
    @Override
    public final IoBuffer mark() {
        final ByteBuffer byteBuffer = this.buf();
        byteBuffer.mark();
        this.mark = byteBuffer.position();
        return this;
    }
    
    @Override
    public final int markValue() {
        return this.mark;
    }
    
    @Override
    public final IoBuffer reset() {
        this.buf().reset();
        return this;
    }
    
    @Override
    public final IoBuffer clear() {
        this.buf().clear();
        this.mark = -1;
        return this;
    }
    
    @Override
    public final IoBuffer sweep() {
        this.clear();
        return this.fillAndReset(this.remaining());
    }
    
    @Override
    public final IoBuffer sweep(final byte value) {
        this.clear();
        return this.fillAndReset(value, this.remaining());
    }
    
    @Override
    public final IoBuffer flip() {
        this.buf().flip();
        this.mark = -1;
        return this;
    }
    
    @Override
    public final IoBuffer rewind() {
        this.buf().rewind();
        this.mark = -1;
        return this;
    }
    
    @Override
    public final int remaining() {
        final ByteBuffer byteBuffer = this.buf();
        return byteBuffer.limit() - byteBuffer.position();
    }
    
    @Override
    public final boolean hasRemaining() {
        final ByteBuffer byteBuffer = this.buf();
        return byteBuffer.limit() > byteBuffer.position();
    }
    
    @Override
    public final byte get() {
        return this.buf().get();
    }
    
    @Override
    public final short getUnsigned() {
        return (short)(this.get() & 0xFF);
    }
    
    @Override
    public final IoBuffer put(final byte b) {
        this.autoExpand(1);
        this.buf().put(b);
        return this;
    }
    
    @Override
    public IoBuffer putUnsigned(final byte value) {
        this.autoExpand(1);
        this.buf().put((byte)(value & 0xFF));
        return this;
    }
    
    @Override
    public IoBuffer putUnsigned(final int index, final byte value) {
        this.autoExpand(index, 1);
        this.buf().put(index, (byte)(value & 0xFF));
        return this;
    }
    
    @Override
    public IoBuffer putUnsigned(final short value) {
        this.autoExpand(1);
        this.buf().put((byte)(value & 0xFF));
        return this;
    }
    
    @Override
    public IoBuffer putUnsigned(final int index, final short value) {
        this.autoExpand(index, 1);
        this.buf().put(index, (byte)(value & 0xFF));
        return this;
    }
    
    @Override
    public IoBuffer putUnsigned(final int value) {
        this.autoExpand(1);
        this.buf().put((byte)(value & 0xFF));
        return this;
    }
    
    @Override
    public IoBuffer putUnsigned(final int index, final int value) {
        this.autoExpand(index, 1);
        this.buf().put(index, (byte)(value & 0xFF));
        return this;
    }
    
    @Override
    public IoBuffer putUnsigned(final long value) {
        this.autoExpand(1);
        this.buf().put((byte)(value & 0xFFL));
        return this;
    }
    
    @Override
    public IoBuffer putUnsigned(final int index, final long value) {
        this.autoExpand(index, 1);
        this.buf().put(index, (byte)(value & 0xFFL));
        return this;
    }
    
    @Override
    public final byte get(final int index) {
        return this.buf().get(index);
    }
    
    @Override
    public final short getUnsigned(final int index) {
        return (short)(this.get(index) & 0xFF);
    }
    
    @Override
    public final IoBuffer put(final int index, final byte b) {
        this.autoExpand(index, 1);
        this.buf().put(index, b);
        return this;
    }
    
    @Override
    public final IoBuffer get(final byte[] dst, final int offset, final int length) {
        this.buf().get(dst, offset, length);
        return this;
    }
    
    @Override
    public final IoBuffer put(final ByteBuffer src) {
        this.autoExpand(src.remaining());
        this.buf().put(src);
        return this;
    }
    
    @Override
    public final IoBuffer put(final byte[] src, final int offset, final int length) {
        this.autoExpand(length);
        this.buf().put(src, offset, length);
        return this;
    }
    
    @Override
    public final IoBuffer compact() {
        final int remaining = this.remaining();
        final int capacity = this.capacity();
        if (capacity == 0) {
            return this;
        }
        if (this.isAutoShrink() && remaining <= capacity >>> 2 && capacity > this.minimumCapacity) {
            int newCapacity;
            int minCapacity;
            for (newCapacity = capacity, minCapacity = Math.max(this.minimumCapacity, remaining << 1); newCapacity >>> 1 >= minCapacity; newCapacity >>>= 1) {}
            newCapacity = Math.max(minCapacity, newCapacity);
            if (newCapacity == capacity) {
                return this;
            }
            final ByteOrder bo = this.order();
            if (remaining > newCapacity) {
                throw new IllegalStateException("The amount of the remaining bytes is greater than the new capacity.");
            }
            final ByteBuffer oldBuf = this.buf();
            final ByteBuffer newBuf = IoBuffer.getAllocator().allocateNioBuffer(newCapacity, this.isDirect());
            newBuf.put(oldBuf);
            this.buf(newBuf);
            this.buf().order(bo);
        }
        else {
            this.buf().compact();
        }
        this.mark = -1;
        return this;
    }
    
    @Override
    public final ByteOrder order() {
        return this.buf().order();
    }
    
    @Override
    public final IoBuffer order(final ByteOrder bo) {
        this.buf().order(bo);
        return this;
    }
    
    @Override
    public final char getChar() {
        return this.buf().getChar();
    }
    
    @Override
    public final IoBuffer putChar(final char value) {
        this.autoExpand(2);
        this.buf().putChar(value);
        return this;
    }
    
    @Override
    public final char getChar(final int index) {
        return this.buf().getChar(index);
    }
    
    @Override
    public final IoBuffer putChar(final int index, final char value) {
        this.autoExpand(index, 2);
        this.buf().putChar(index, value);
        return this;
    }
    
    @Override
    public final CharBuffer asCharBuffer() {
        return this.buf().asCharBuffer();
    }
    
    @Override
    public final short getShort() {
        return this.buf().getShort();
    }
    
    @Override
    public final IoBuffer putShort(final short value) {
        this.autoExpand(2);
        this.buf().putShort(value);
        return this;
    }
    
    @Override
    public final short getShort(final int index) {
        return this.buf().getShort(index);
    }
    
    @Override
    public final IoBuffer putShort(final int index, final short value) {
        this.autoExpand(index, 2);
        this.buf().putShort(index, value);
        return this;
    }
    
    @Override
    public final ShortBuffer asShortBuffer() {
        return this.buf().asShortBuffer();
    }
    
    @Override
    public final int getInt() {
        return this.buf().getInt();
    }
    
    @Override
    public final IoBuffer putInt(final int value) {
        this.autoExpand(4);
        this.buf().putInt(value);
        return this;
    }
    
    @Override
    public final IoBuffer putUnsignedInt(final byte value) {
        this.autoExpand(4);
        this.buf().putInt(value & 0xFF);
        return this;
    }
    
    @Override
    public final IoBuffer putUnsignedInt(final int index, final byte value) {
        this.autoExpand(index, 4);
        this.buf().putInt(index, value & 0xFF);
        return this;
    }
    
    @Override
    public final IoBuffer putUnsignedInt(final short value) {
        this.autoExpand(4);
        this.buf().putInt(value & 0xFFFF);
        return this;
    }
    
    @Override
    public final IoBuffer putUnsignedInt(final int index, final short value) {
        this.autoExpand(index, 4);
        this.buf().putInt(index, value & 0xFFFF);
        return this;
    }
    
    @Override
    public final IoBuffer putUnsignedInt(final int value) {
        this.autoExpand(4);
        this.buf().putInt(value);
        return this;
    }
    
    @Override
    public final IoBuffer putUnsignedInt(final int index, final int value) {
        this.autoExpand(index, 4);
        this.buf().putInt(index, value);
        return this;
    }
    
    @Override
    public final IoBuffer putUnsignedInt(final long value) {
        this.autoExpand(4);
        this.buf().putInt((int)(value & -1L));
        return this;
    }
    
    @Override
    public final IoBuffer putUnsignedInt(final int index, final long value) {
        this.autoExpand(index, 4);
        this.buf().putInt(index, (int)(value & 0xFFFFFFFFL));
        return this;
    }
    
    @Override
    public final IoBuffer putUnsignedShort(final byte value) {
        this.autoExpand(2);
        this.buf().putShort((short)(value & 0xFF));
        return this;
    }
    
    @Override
    public final IoBuffer putUnsignedShort(final int index, final byte value) {
        this.autoExpand(index, 2);
        this.buf().putShort(index, (short)(value & 0xFF));
        return this;
    }
    
    @Override
    public final IoBuffer putUnsignedShort(final short value) {
        this.autoExpand(2);
        this.buf().putShort(value);
        return this;
    }
    
    @Override
    public final IoBuffer putUnsignedShort(final int index, final short value) {
        this.autoExpand(index, 2);
        this.buf().putShort(index, value);
        return this;
    }
    
    @Override
    public final IoBuffer putUnsignedShort(final int value) {
        this.autoExpand(2);
        this.buf().putShort((short)value);
        return this;
    }
    
    @Override
    public final IoBuffer putUnsignedShort(final int index, final int value) {
        this.autoExpand(index, 2);
        this.buf().putShort(index, (short)value);
        return this;
    }
    
    @Override
    public final IoBuffer putUnsignedShort(final long value) {
        this.autoExpand(2);
        this.buf().putShort((short)value);
        return this;
    }
    
    @Override
    public final IoBuffer putUnsignedShort(final int index, final long value) {
        this.autoExpand(index, 2);
        this.buf().putShort(index, (short)value);
        return this;
    }
    
    @Override
    public final int getInt(final int index) {
        return this.buf().getInt(index);
    }
    
    @Override
    public final IoBuffer putInt(final int index, final int value) {
        this.autoExpand(index, 4);
        this.buf().putInt(index, value);
        return this;
    }
    
    @Override
    public final IntBuffer asIntBuffer() {
        return this.buf().asIntBuffer();
    }
    
    @Override
    public final long getLong() {
        return this.buf().getLong();
    }
    
    @Override
    public final IoBuffer putLong(final long value) {
        this.autoExpand(8);
        this.buf().putLong(value);
        return this;
    }
    
    @Override
    public final long getLong(final int index) {
        return this.buf().getLong(index);
    }
    
    @Override
    public final IoBuffer putLong(final int index, final long value) {
        this.autoExpand(index, 8);
        this.buf().putLong(index, value);
        return this;
    }
    
    @Override
    public final LongBuffer asLongBuffer() {
        return this.buf().asLongBuffer();
    }
    
    @Override
    public final float getFloat() {
        return this.buf().getFloat();
    }
    
    @Override
    public final IoBuffer putFloat(final float value) {
        this.autoExpand(4);
        this.buf().putFloat(value);
        return this;
    }
    
    @Override
    public final float getFloat(final int index) {
        return this.buf().getFloat(index);
    }
    
    @Override
    public final IoBuffer putFloat(final int index, final float value) {
        this.autoExpand(index, 4);
        this.buf().putFloat(index, value);
        return this;
    }
    
    @Override
    public final FloatBuffer asFloatBuffer() {
        return this.buf().asFloatBuffer();
    }
    
    @Override
    public final double getDouble() {
        return this.buf().getDouble();
    }
    
    @Override
    public final IoBuffer putDouble(final double value) {
        this.autoExpand(8);
        this.buf().putDouble(value);
        return this;
    }
    
    @Override
    public final double getDouble(final int index) {
        return this.buf().getDouble(index);
    }
    
    @Override
    public final IoBuffer putDouble(final int index, final double value) {
        this.autoExpand(index, 8);
        this.buf().putDouble(index, value);
        return this;
    }
    
    @Override
    public final DoubleBuffer asDoubleBuffer() {
        return this.buf().asDoubleBuffer();
    }
    
    @Override
    public final IoBuffer asReadOnlyBuffer() {
        this.recapacityAllowed = false;
        return this.asReadOnlyBuffer0();
    }
    
    protected abstract IoBuffer asReadOnlyBuffer0();
    
    @Override
    public final IoBuffer duplicate() {
        this.recapacityAllowed = false;
        return this.duplicate0();
    }
    
    protected abstract IoBuffer duplicate0();
    
    @Override
    public final IoBuffer slice() {
        this.recapacityAllowed = false;
        return this.slice0();
    }
    
    @Override
    public final IoBuffer getSlice(final int index, final int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length: " + length);
        }
        final int pos = this.position();
        final int limit = this.limit();
        if (index > limit) {
            throw new IllegalArgumentException("index: " + index);
        }
        final int endIndex = index + length;
        if (endIndex > limit) {
            throw new IndexOutOfBoundsException("index + length (" + endIndex + ") is greater " + "than limit (" + limit + ").");
        }
        this.clear();
        this.limit(endIndex);
        this.position(index);
        final IoBuffer slice = this.slice();
        this.limit(limit);
        this.position(pos);
        return slice;
    }
    
    @Override
    public final IoBuffer getSlice(final int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length: " + length);
        }
        final int pos = this.position();
        final int limit = this.limit();
        final int nextPos = pos + length;
        if (limit < nextPos) {
            throw new IndexOutOfBoundsException("position + length (" + nextPos + ") is greater " + "than limit (" + limit + ").");
        }
        this.limit(pos + length);
        final IoBuffer slice = this.slice();
        this.position(nextPos);
        this.limit(limit);
        return slice;
    }
    
    protected abstract IoBuffer slice0();
    
    @Override
    public int hashCode() {
        int h = 1;
        for (int p = this.position(), i = this.limit() - 1; i >= p; --i) {
            h = 31 * h + this.get(i);
        }
        return h;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof IoBuffer)) {
            return false;
        }
        final IoBuffer that = (IoBuffer)o;
        if (this.remaining() != that.remaining()) {
            return false;
        }
        for (int p = this.position(), i = this.limit() - 1, j = that.limit() - 1; i >= p; --i, --j) {
            final byte v1 = this.get(i);
            final byte v2 = that.get(j);
            if (v1 != v2) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int compareTo(final IoBuffer that) {
        final int n = this.position() + Math.min(this.remaining(), that.remaining());
        int i = this.position();
        int j = that.position();
        while (i < n) {
            final byte v1 = this.get(i);
            final byte v2 = that.get(j);
            if (v1 == v2) {
                ++i;
                ++j;
            }
            else {
                if (v1 < v2) {
                    return -1;
                }
                return 1;
            }
        }
        return this.remaining() - that.remaining();
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        if (this.isDirect()) {
            buf.append("DirectBuffer");
        }
        else {
            buf.append("HeapBuffer");
        }
        buf.append("[pos=");
        buf.append(this.position());
        buf.append(" lim=");
        buf.append(this.limit());
        buf.append(" cap=");
        buf.append(this.capacity());
        buf.append(": ");
        buf.append(this.getHexDump(16));
        buf.append(']');
        return buf.toString();
    }
    
    @Override
    public IoBuffer get(final byte[] dst) {
        return this.get(dst, 0, dst.length);
    }
    
    @Override
    public IoBuffer put(final IoBuffer src) {
        return this.put(src.buf());
    }
    
    @Override
    public IoBuffer put(final byte[] src) {
        return this.put(src, 0, src.length);
    }
    
    @Override
    public int getUnsignedShort() {
        return this.getShort() & 0xFFFF;
    }
    
    @Override
    public int getUnsignedShort(final int index) {
        return this.getShort(index) & 0xFFFF;
    }
    
    @Override
    public long getUnsignedInt() {
        return (long)this.getInt() & 0xFFFFFFFFL;
    }
    
    @Override
    public int getMediumInt() {
        final byte b1 = this.get();
        final byte b2 = this.get();
        final byte b3 = this.get();
        if (ByteOrder.BIG_ENDIAN.equals(this.order())) {
            return this.getMediumInt(b1, b2, b3);
        }
        return this.getMediumInt(b3, b2, b1);
    }
    
    @Override
    public int getUnsignedMediumInt() {
        final int b1 = this.getUnsigned();
        final int b2 = this.getUnsigned();
        final int b3 = this.getUnsigned();
        if (ByteOrder.BIG_ENDIAN.equals(this.order())) {
            return b1 << 16 | b2 << 8 | b3;
        }
        return b3 << 16 | b2 << 8 | b1;
    }
    
    @Override
    public int getMediumInt(final int index) {
        final byte b1 = this.get(index);
        final byte b2 = this.get(index + 1);
        final byte b3 = this.get(index + 2);
        if (ByteOrder.BIG_ENDIAN.equals(this.order())) {
            return this.getMediumInt(b1, b2, b3);
        }
        return this.getMediumInt(b3, b2, b1);
    }
    
    @Override
    public int getUnsignedMediumInt(final int index) {
        final int b1 = this.getUnsigned(index);
        final int b2 = this.getUnsigned(index + 1);
        final int b3 = this.getUnsigned(index + 2);
        if (ByteOrder.BIG_ENDIAN.equals(this.order())) {
            return b1 << 16 | b2 << 8 | b3;
        }
        return b3 << 16 | b2 << 8 | b1;
    }
    
    private int getMediumInt(final byte b1, final byte b2, final byte b3) {
        int ret = (b1 << 16 & 0xFF0000) | (b2 << 8 & 0xFF00) | (b3 & 0xFF);
        if ((b1 & 0x80) == 0x80) {
            ret |= 0xFF000000;
        }
        return ret;
    }
    
    @Override
    public IoBuffer putMediumInt(final int value) {
        final byte b1 = (byte)(value >> 16);
        final byte b2 = (byte)(value >> 8);
        final byte b3 = (byte)value;
        if (ByteOrder.BIG_ENDIAN.equals(this.order())) {
            this.put(b1).put(b2).put(b3);
        }
        else {
            this.put(b3).put(b2).put(b1);
        }
        return this;
    }
    
    @Override
    public IoBuffer putMediumInt(final int index, final int value) {
        final byte b1 = (byte)(value >> 16);
        final byte b2 = (byte)(value >> 8);
        final byte b3 = (byte)value;
        if (ByteOrder.BIG_ENDIAN.equals(this.order())) {
            this.put(index, b1).put(index + 1, b2).put(index + 2, b3);
        }
        else {
            this.put(index, b3).put(index + 1, b2).put(index + 2, b1);
        }
        return this;
    }
    
    @Override
    public long getUnsignedInt(final int index) {
        return (long)this.getInt(index) & 0xFFFFFFFFL;
    }
    
    @Override
    public InputStream asInputStream() {
        return new InputStream() {
            @Override
            public int available() {
                return AbstractIoBuffer.this.remaining();
            }
            
            @Override
            public synchronized void mark(final int readlimit) {
                AbstractIoBuffer.this.mark();
            }
            
            @Override
            public boolean markSupported() {
                return true;
            }
            
            @Override
            public int read() {
                if (AbstractIoBuffer.this.hasRemaining()) {
                    return AbstractIoBuffer.this.get() & 0xFF;
                }
                return -1;
            }
            
            @Override
            public int read(final byte[] b, final int off, final int len) {
                final int remaining = AbstractIoBuffer.this.remaining();
                if (remaining > 0) {
                    final int readBytes = Math.min(remaining, len);
                    AbstractIoBuffer.this.get(b, off, readBytes);
                    return readBytes;
                }
                return -1;
            }
            
            @Override
            public synchronized void reset() {
                AbstractIoBuffer.this.reset();
            }
            
            @Override
            public long skip(final long n) {
                int bytes;
                if (n > 2147483647L) {
                    bytes = AbstractIoBuffer.this.remaining();
                }
                else {
                    bytes = Math.min(AbstractIoBuffer.this.remaining(), (int)n);
                }
                AbstractIoBuffer.this.skip(bytes);
                return bytes;
            }
        };
    }
    
    @Override
    public OutputStream asOutputStream() {
        return new OutputStream() {
            @Override
            public void write(final byte[] b, final int off, final int len) {
                AbstractIoBuffer.this.put(b, off, len);
            }
            
            @Override
            public void write(final int b) {
                AbstractIoBuffer.this.put((byte)b);
            }
        };
    }
    
    @Override
    public String getHexDump() {
        return this.getHexDump(Integer.MAX_VALUE);
    }
    
    @Override
    public String getHexDump(final int lengthLimit) {
        return IoBufferHexDumper.getHexdump(this, lengthLimit);
    }
    
    @Override
    public String getString(final CharsetDecoder decoder) throws CharacterCodingException {
        if (!this.hasRemaining()) {
            return "";
        }
        final boolean utf16 = decoder.charset().name().startsWith("UTF-16");
        final int oldPos = this.position();
        final int oldLimit = this.limit();
        int end = -1;
        int newPos;
        if (!utf16) {
            end = this.indexOf((byte)0);
            if (end < 0) {
                end = (newPos = oldLimit);
            }
            else {
                newPos = end + 1;
            }
        }
        else {
            int i = oldPos;
            while (true) {
                final boolean wasZero = this.get(i) == 0;
                if (++i >= oldLimit) {
                    break;
                }
                if (this.get(i) != 0) {
                    if (++i >= oldLimit) {
                        break;
                    }
                    continue;
                }
                else {
                    if (wasZero) {
                        end = i - 1;
                        break;
                    }
                    continue;
                }
            }
            if (end < 0) {
                end = (newPos = oldPos + (oldLimit - oldPos & 0xFFFFFFFE));
            }
            else if (end + 2 <= oldLimit) {
                newPos = end + 2;
            }
            else {
                newPos = end;
            }
        }
        if (oldPos == end) {
            this.position(newPos);
            return "";
        }
        this.limit(end);
        decoder.reset();
        final int expectedLength = (int)(this.remaining() * decoder.averageCharsPerByte()) + 1;
        CharBuffer out = CharBuffer.allocate(expectedLength);
        while (true) {
            CoderResult cr;
            if (this.hasRemaining()) {
                cr = decoder.decode(this.buf(), out, true);
            }
            else {
                cr = decoder.flush(out);
            }
            if (cr.isUnderflow()) {
                break;
            }
            if (cr.isOverflow()) {
                final CharBuffer o = CharBuffer.allocate(out.capacity() + expectedLength);
                out.flip();
                o.put(out);
                out = o;
            }
            else {
                if (!cr.isError()) {
                    continue;
                }
                this.limit(oldLimit);
                this.position(oldPos);
                cr.throwException();
            }
        }
        this.limit(oldLimit);
        this.position(newPos);
        return out.flip().toString();
    }
    
    @Override
    public String getString(final int fieldSize, final CharsetDecoder decoder) throws CharacterCodingException {
        checkFieldSize(fieldSize);
        if (fieldSize == 0) {
            return "";
        }
        if (!this.hasRemaining()) {
            return "";
        }
        final boolean utf16 = decoder.charset().name().startsWith("UTF-16");
        if (utf16 && (fieldSize & 0x1) != 0x0) {
            throw new IllegalArgumentException("fieldSize is not even.");
        }
        final int oldPos = this.position();
        final int oldLimit = this.limit();
        final int end = oldPos + fieldSize;
        if (oldLimit < end) {
            throw new BufferUnderflowException();
        }
        if (!utf16) {
            int i;
            for (i = oldPos; i < end && this.get(i) != 0; ++i) {}
            if (i == end) {
                this.limit(end);
            }
            else {
                this.limit(i);
            }
        }
        else {
            int i;
            for (i = oldPos; i < end && (this.get(i) != 0 || this.get(i + 1) != 0); i += 2) {}
            if (i == end) {
                this.limit(end);
            }
            else {
                this.limit(i);
            }
        }
        if (!this.hasRemaining()) {
            this.limit(oldLimit);
            this.position(end);
            return "";
        }
        decoder.reset();
        final int expectedLength = (int)(this.remaining() * decoder.averageCharsPerByte()) + 1;
        CharBuffer out = CharBuffer.allocate(expectedLength);
        while (true) {
            CoderResult cr;
            if (this.hasRemaining()) {
                cr = decoder.decode(this.buf(), out, true);
            }
            else {
                cr = decoder.flush(out);
            }
            if (cr.isUnderflow()) {
                break;
            }
            if (cr.isOverflow()) {
                final CharBuffer o = CharBuffer.allocate(out.capacity() + expectedLength);
                out.flip();
                o.put(out);
                out = o;
            }
            else {
                if (!cr.isError()) {
                    continue;
                }
                this.limit(oldLimit);
                this.position(oldPos);
                cr.throwException();
            }
        }
        this.limit(oldLimit);
        this.position(end);
        return out.flip().toString();
    }
    
    @Override
    public IoBuffer putString(final CharSequence val, final CharsetEncoder encoder) throws CharacterCodingException {
        if (val.length() == 0) {
            return this;
        }
        final CharBuffer in = CharBuffer.wrap(val);
        encoder.reset();
        int expandedState = 0;
        while (true) {
            CoderResult cr;
            if (in.hasRemaining()) {
                cr = encoder.encode(in, this.buf(), true);
            }
            else {
                cr = encoder.flush(this.buf());
            }
            if (cr.isUnderflow()) {
                return this;
            }
            if (cr.isOverflow()) {
                if (this.isAutoExpand()) {
                    switch (expandedState) {
                        case 0: {
                            this.autoExpand((int)Math.ceil(in.remaining() * encoder.averageBytesPerChar()));
                            ++expandedState;
                            continue;
                        }
                        case 1: {
                            this.autoExpand((int)Math.ceil(in.remaining() * encoder.maxBytesPerChar()));
                            ++expandedState;
                            continue;
                        }
                        default: {
                            throw new RuntimeException("Expanded by " + (int)Math.ceil(in.remaining() * encoder.maxBytesPerChar()) + " but that wasn't enough for '" + (Object)val + "'");
                        }
                    }
                }
            }
            else {
                expandedState = 0;
            }
            cr.throwException();
        }
    }
    
    @Override
    public IoBuffer putString(final CharSequence val, final int fieldSize, final CharsetEncoder encoder) throws CharacterCodingException {
        checkFieldSize(fieldSize);
        if (fieldSize == 0) {
            return this;
        }
        this.autoExpand(fieldSize);
        final boolean utf16 = encoder.charset().name().startsWith("UTF-16");
        if (utf16 && (fieldSize & 0x1) != 0x0) {
            throw new IllegalArgumentException("fieldSize is not even.");
        }
        final int oldLimit = this.limit();
        final int end = this.position() + fieldSize;
        if (oldLimit < end) {
            throw new BufferOverflowException();
        }
        if (val.length() == 0) {
            if (!utf16) {
                this.put((byte)0);
            }
            else {
                this.put((byte)0);
                this.put((byte)0);
            }
            this.position(end);
            return this;
        }
        final CharBuffer in = CharBuffer.wrap(val);
        this.limit(end);
        encoder.reset();
        while (true) {
            CoderResult cr;
            if (in.hasRemaining()) {
                cr = encoder.encode(in, this.buf(), true);
            }
            else {
                cr = encoder.flush(this.buf());
            }
            if (cr.isUnderflow() || cr.isOverflow()) {
                break;
            }
            cr.throwException();
        }
        this.limit(oldLimit);
        if (this.position() < end) {
            if (!utf16) {
                this.put((byte)0);
            }
            else {
                this.put((byte)0);
                this.put((byte)0);
            }
        }
        this.position(end);
        return this;
    }
    
    @Override
    public String getPrefixedString(final CharsetDecoder decoder) throws CharacterCodingException {
        return this.getPrefixedString(2, decoder);
    }
    
    @Override
    public String getPrefixedString(final int prefixLength, final CharsetDecoder decoder) throws CharacterCodingException {
        if (!this.prefixedDataAvailable(prefixLength)) {
            throw new BufferUnderflowException();
        }
        int fieldSize = 0;
        switch (prefixLength) {
            case 1: {
                fieldSize = this.getUnsigned();
                break;
            }
            case 2: {
                fieldSize = this.getUnsignedShort();
                break;
            }
            case 4: {
                fieldSize = this.getInt();
                break;
            }
        }
        if (fieldSize == 0) {
            return "";
        }
        final boolean utf16 = decoder.charset().name().startsWith("UTF-16");
        if (utf16 && (fieldSize & 0x1) != 0x0) {
            throw new BufferDataException("fieldSize is not even for a UTF-16 string.");
        }
        final int oldLimit = this.limit();
        final int end = this.position() + fieldSize;
        if (oldLimit < end) {
            throw new BufferUnderflowException();
        }
        this.limit(end);
        decoder.reset();
        final int expectedLength = (int)(this.remaining() * decoder.averageCharsPerByte()) + 1;
        CharBuffer out = CharBuffer.allocate(expectedLength);
        while (true) {
            CoderResult cr;
            if (this.hasRemaining()) {
                cr = decoder.decode(this.buf(), out, true);
            }
            else {
                cr = decoder.flush(out);
            }
            if (cr.isUnderflow()) {
                break;
            }
            if (cr.isOverflow()) {
                final CharBuffer o = CharBuffer.allocate(out.capacity() + expectedLength);
                out.flip();
                o.put(out);
                out = o;
            }
            else {
                cr.throwException();
            }
        }
        this.limit(oldLimit);
        this.position(end);
        return out.flip().toString();
    }
    
    @Override
    public IoBuffer putPrefixedString(final CharSequence in, final CharsetEncoder encoder) throws CharacterCodingException {
        return this.putPrefixedString(in, 2, 0, encoder);
    }
    
    @Override
    public IoBuffer putPrefixedString(final CharSequence in, final int prefixLength, final CharsetEncoder encoder) throws CharacterCodingException {
        return this.putPrefixedString(in, prefixLength, 0, encoder);
    }
    
    @Override
    public IoBuffer putPrefixedString(final CharSequence in, final int prefixLength, final int padding, final CharsetEncoder encoder) throws CharacterCodingException {
        return this.putPrefixedString(in, prefixLength, padding, (byte)0, encoder);
    }
    
    @Override
    public IoBuffer putPrefixedString(final CharSequence val, final int prefixLength, final int padding, final byte padValue, final CharsetEncoder encoder) throws CharacterCodingException {
        int maxLength = 0;
        switch (prefixLength) {
            case 1: {
                maxLength = 255;
                break;
            }
            case 2: {
                maxLength = 65535;
                break;
            }
            case 4: {
                maxLength = Integer.MAX_VALUE;
                break;
            }
            default: {
                throw new IllegalArgumentException("prefixLength: " + prefixLength);
            }
        }
        if (val.length() > maxLength) {
            throw new IllegalArgumentException("The specified string is too long.");
        }
        if (val.length() == 0) {
            switch (prefixLength) {
                case 1: {
                    this.put((byte)0);
                    break;
                }
                case 2: {
                    this.putShort((short)0);
                    break;
                }
                case 4: {
                    this.putInt(0);
                    break;
                }
            }
            return this;
        }
        int padMask = 0;
        switch (padding) {
            case 0:
            case 1: {
                padMask = 0;
                break;
            }
            case 2: {
                padMask = 1;
                break;
            }
            case 4: {
                padMask = 3;
                break;
            }
            default: {
                throw new IllegalArgumentException("padding: " + padding);
            }
        }
        final CharBuffer in = CharBuffer.wrap(val);
        this.skip(prefixLength);
        final int oldPos = this.position();
        encoder.reset();
        int expandedState = 0;
        while (true) {
            CoderResult cr;
            if (in.hasRemaining()) {
                cr = encoder.encode(in, this.buf(), true);
            }
            else {
                cr = encoder.flush(this.buf());
            }
            if (this.position() - oldPos > maxLength) {
                throw new IllegalArgumentException("The specified string is too long.");
            }
            if (cr.isUnderflow()) {
                this.fill(padValue, padding - (this.position() - oldPos & padMask));
                final int length = this.position() - oldPos;
                switch (prefixLength) {
                    case 1: {
                        this.put(oldPos - 1, (byte)length);
                        break;
                    }
                    case 2: {
                        this.putShort(oldPos - 2, (short)length);
                        break;
                    }
                    case 4: {
                        this.putInt(oldPos - 4, length);
                        break;
                    }
                }
                return this;
            }
            if (cr.isOverflow()) {
                if (this.isAutoExpand()) {
                    switch (expandedState) {
                        case 0: {
                            this.autoExpand((int)Math.ceil(in.remaining() * encoder.averageBytesPerChar()));
                            ++expandedState;
                            continue;
                        }
                        case 1: {
                            this.autoExpand((int)Math.ceil(in.remaining() * encoder.maxBytesPerChar()));
                            ++expandedState;
                            continue;
                        }
                        default: {
                            throw new RuntimeException("Expanded by " + (int)Math.ceil(in.remaining() * encoder.maxBytesPerChar()) + " but that wasn't enough for '" + (Object)val + "'");
                        }
                    }
                }
            }
            else {
                expandedState = 0;
            }
            cr.throwException();
        }
    }
    
    @Override
    public Object getObject() throws ClassNotFoundException {
        return this.getObject(Thread.currentThread().getContextClassLoader());
    }
    
    @Override
    public Object getObject(final ClassLoader classLoader) throws ClassNotFoundException {
        if (!this.prefixedDataAvailable(4)) {
            throw new BufferUnderflowException();
        }
        final int length = this.getInt();
        if (length <= 4) {
            throw new BufferDataException("Object length should be greater than 4: " + length);
        }
        final int oldLimit = this.limit();
        this.limit(this.position() + length);
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(this.asInputStream()) {
                @Override
                protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
                    final int type = this.read();
                    if (type < 0) {
                        throw new EOFException();
                    }
                    switch (type) {
                        case 0: {
                            return super.readClassDescriptor();
                        }
                        case 1: {
                            final String className = this.readUTF();
                            final Class<?> clazz = Class.forName(className, true, classLoader);
                            return ObjectStreamClass.lookup(clazz);
                        }
                        default: {
                            throw new StreamCorruptedException("Unexpected class descriptor type: " + type);
                        }
                    }
                }
                
                @Override
                protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                    final Class<?> clazz = desc.forClass();
                    if (clazz == null) {
                        final String name = desc.getName();
                        try {
                            return Class.forName(name, false, classLoader);
                        }
                        catch (ClassNotFoundException ex) {
                            return super.resolveClass(desc);
                        }
                    }
                    return clazz;
                }
            };
            return in.readObject();
        }
        catch (IOException e) {
            throw new BufferDataException(e);
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException ex) {}
            this.limit(oldLimit);
        }
    }
    
    @Override
    public IoBuffer putObject(final Object o) {
        final int oldPos = this.position();
        this.skip(4);
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(this.asOutputStream()) {
                @Override
                protected void writeClassDescriptor(final ObjectStreamClass desc) throws IOException {
                    final Class<?> clazz = desc.forClass();
                    if (clazz.isArray() || clazz.isPrimitive() || !Serializable.class.isAssignableFrom(clazz)) {
                        this.write(0);
                        super.writeClassDescriptor(desc);
                    }
                    else {
                        this.write(1);
                        this.writeUTF(desc.getName());
                    }
                }
            };
            out.writeObject(o);
            out.flush();
        }
        catch (IOException e) {
            throw new BufferDataException(e);
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
            }
            catch (IOException ex) {}
        }
        final int newPos = this.position();
        this.position(oldPos);
        this.putInt(newPos - oldPos - 4);
        this.position(newPos);
        return this;
    }
    
    @Override
    public boolean prefixedDataAvailable(final int prefixLength) {
        return this.prefixedDataAvailable(prefixLength, Integer.MAX_VALUE);
    }
    
    @Override
    public boolean prefixedDataAvailable(final int prefixLength, final int maxDataLength) {
        if (this.remaining() < prefixLength) {
            return false;
        }
        int dataLength = 0;
        switch (prefixLength) {
            case 1: {
                dataLength = this.getUnsigned(this.position());
                break;
            }
            case 2: {
                dataLength = this.getUnsignedShort(this.position());
                break;
            }
            case 4: {
                dataLength = this.getInt(this.position());
                break;
            }
            default: {
                throw new IllegalArgumentException("prefixLength: " + prefixLength);
            }
        }
        if (dataLength < 0 || dataLength > maxDataLength) {
            throw new BufferDataException("dataLength: " + dataLength);
        }
        return this.remaining() - prefixLength >= dataLength;
    }
    
    @Override
    public int indexOf(final byte b) {
        if (this.hasArray()) {
            final int arrayOffset = this.arrayOffset();
            final int beginPos = arrayOffset + this.position();
            final int limit = arrayOffset + this.limit();
            final byte[] array = this.array();
            for (int i = beginPos; i < limit; ++i) {
                if (array[i] == b) {
                    return i - arrayOffset;
                }
            }
        }
        else {
            final int beginPos2 = this.position();
            for (int limit2 = this.limit(), j = beginPos2; j < limit2; ++j) {
                if (this.get(j) == b) {
                    return j;
                }
            }
        }
        return -1;
    }
    
    @Override
    public IoBuffer skip(final int size) {
        this.autoExpand(size);
        return this.position(this.position() + size);
    }
    
    @Override
    public IoBuffer fill(final byte value, final int size) {
        this.autoExpand(size);
        int q = size >>> 3;
        int r = size & 0x7;
        if (q > 0) {
            final int intValue = (value & 0xFF) | (value << 8 & 0xFF00) | (value << 16 & 0xFF0000) | value << 24;
            final long longValue = ((long)intValue & 0xFFFFFFFFL) | (long)intValue << 32;
            for (int i = q; i > 0; --i) {
                this.putLong(longValue);
            }
        }
        q = r >>> 2;
        r &= 0x3;
        if (q > 0) {
            final int intValue = (value & 0xFF) | (value << 8 & 0xFF00) | (value << 16 & 0xFF0000) | value << 24;
            this.putInt(intValue);
        }
        q = r >> 1;
        r &= 0x1;
        if (q > 0) {
            final short shortValue = (short)((value & 0xFF) | value << 8);
            this.putShort(shortValue);
        }
        if (r > 0) {
            this.put(value);
        }
        return this;
    }
    
    @Override
    public IoBuffer fillAndReset(final byte value, final int size) {
        this.autoExpand(size);
        final int pos = this.position();
        try {
            this.fill(value, size);
        }
        finally {
            this.position(pos);
        }
        return this;
    }
    
    @Override
    public IoBuffer fill(final int size) {
        this.autoExpand(size);
        int q = size >>> 3;
        int r = size & 0x7;
        for (int i = q; i > 0; --i) {
            this.putLong(0L);
        }
        q = r >>> 2;
        r &= 0x3;
        if (q > 0) {
            this.putInt(0);
        }
        q = r >> 1;
        r &= 0x1;
        if (q > 0) {
            this.putShort((short)0);
        }
        if (r > 0) {
            this.put((byte)0);
        }
        return this;
    }
    
    @Override
    public IoBuffer fillAndReset(final int size) {
        this.autoExpand(size);
        final int pos = this.position();
        try {
            this.fill(size);
        }
        finally {
            this.position(pos);
        }
        return this;
    }
    
    @Override
    public <E extends Enum<E>> E getEnum(final Class<E> enumClass) {
        return this.toEnum(enumClass, this.getUnsigned());
    }
    
    @Override
    public <E extends Enum<E>> E getEnum(final int index, final Class<E> enumClass) {
        return this.toEnum(enumClass, this.getUnsigned(index));
    }
    
    @Override
    public <E extends Enum<E>> E getEnumShort(final Class<E> enumClass) {
        return this.toEnum(enumClass, this.getUnsignedShort());
    }
    
    @Override
    public <E extends Enum<E>> E getEnumShort(final int index, final Class<E> enumClass) {
        return this.toEnum(enumClass, this.getUnsignedShort(index));
    }
    
    @Override
    public <E extends Enum<E>> E getEnumInt(final Class<E> enumClass) {
        return this.toEnum(enumClass, this.getInt());
    }
    
    @Override
    public <E extends Enum<E>> E getEnumInt(final int index, final Class<E> enumClass) {
        return this.toEnum(enumClass, this.getInt(index));
    }
    
    @Override
    public IoBuffer putEnum(final Enum<?> e) {
        if (e.ordinal() > 255L) {
            throw new IllegalArgumentException(this.enumConversionErrorMessage(e, "byte"));
        }
        return this.put((byte)e.ordinal());
    }
    
    @Override
    public IoBuffer putEnum(final int index, final Enum<?> e) {
        if (e.ordinal() > 255L) {
            throw new IllegalArgumentException(this.enumConversionErrorMessage(e, "byte"));
        }
        return this.put(index, (byte)e.ordinal());
    }
    
    @Override
    public IoBuffer putEnumShort(final Enum<?> e) {
        if (e.ordinal() > 65535L) {
            throw new IllegalArgumentException(this.enumConversionErrorMessage(e, "short"));
        }
        return this.putShort((short)e.ordinal());
    }
    
    @Override
    public IoBuffer putEnumShort(final int index, final Enum<?> e) {
        if (e.ordinal() > 65535L) {
            throw new IllegalArgumentException(this.enumConversionErrorMessage(e, "short"));
        }
        return this.putShort(index, (short)e.ordinal());
    }
    
    @Override
    public IoBuffer putEnumInt(final Enum<?> e) {
        return this.putInt(e.ordinal());
    }
    
    @Override
    public IoBuffer putEnumInt(final int index, final Enum<?> e) {
        return this.putInt(index, e.ordinal());
    }
    
    private <E> E toEnum(final Class<E> enumClass, final int i) {
        final E[] enumConstants = enumClass.getEnumConstants();
        if (i > enumConstants.length) {
            throw new IndexOutOfBoundsException(String.format("%d is too large of an ordinal to convert to the enum %s", i, enumClass.getName()));
        }
        return enumConstants[i];
    }
    
    private String enumConversionErrorMessage(final Enum<?> e, final String type) {
        return String.format("%s.%s has an ordinal value too large for a %s", e.getClass().getName(), e.name(), type);
    }
    
    @Override
    public <E extends Enum<E>> EnumSet<E> getEnumSet(final Class<E> enumClass) {
        return this.toEnumSet(enumClass, (long)this.get() & 0xFFL);
    }
    
    @Override
    public <E extends Enum<E>> EnumSet<E> getEnumSet(final int index, final Class<E> enumClass) {
        return this.toEnumSet(enumClass, (long)this.get(index) & 0xFFL);
    }
    
    @Override
    public <E extends Enum<E>> EnumSet<E> getEnumSetShort(final Class<E> enumClass) {
        return this.toEnumSet(enumClass, (long)this.getShort() & 0xFFFFL);
    }
    
    @Override
    public <E extends Enum<E>> EnumSet<E> getEnumSetShort(final int index, final Class<E> enumClass) {
        return this.toEnumSet(enumClass, (long)this.getShort(index) & 0xFFFFL);
    }
    
    @Override
    public <E extends Enum<E>> EnumSet<E> getEnumSetInt(final Class<E> enumClass) {
        return this.toEnumSet(enumClass, (long)this.getInt() & 0xFFFFFFFFL);
    }
    
    @Override
    public <E extends Enum<E>> EnumSet<E> getEnumSetInt(final int index, final Class<E> enumClass) {
        return this.toEnumSet(enumClass, (long)this.getInt(index) & 0xFFFFFFFFL);
    }
    
    @Override
    public <E extends Enum<E>> EnumSet<E> getEnumSetLong(final Class<E> enumClass) {
        return this.toEnumSet(enumClass, this.getLong());
    }
    
    @Override
    public <E extends Enum<E>> EnumSet<E> getEnumSetLong(final int index, final Class<E> enumClass) {
        return this.toEnumSet(enumClass, this.getLong(index));
    }
    
    private <E extends Enum<E>> EnumSet<E> toEnumSet(final Class<E> clazz, final long vector) {
        final EnumSet<E> set = EnumSet.noneOf(clazz);
        long mask = 1L;
        for (final E e : clazz.getEnumConstants()) {
            if ((mask & vector) == mask) {
                set.add(e);
            }
            mask <<= 1;
        }
        return set;
    }
    
    @Override
    public <E extends Enum<E>> IoBuffer putEnumSet(final Set<E> set) {
        final long vector = this.toLong(set);
        if ((vector & 0xFFFFFFFFFFFFFF00L) != 0x0L) {
            throw new IllegalArgumentException("The enum set is too large to fit in a byte: " + set);
        }
        return this.put((byte)vector);
    }
    
    @Override
    public <E extends Enum<E>> IoBuffer putEnumSet(final int index, final Set<E> set) {
        final long vector = this.toLong(set);
        if ((vector & 0xFFFFFFFFFFFFFF00L) != 0x0L) {
            throw new IllegalArgumentException("The enum set is too large to fit in a byte: " + set);
        }
        return this.put(index, (byte)vector);
    }
    
    @Override
    public <E extends Enum<E>> IoBuffer putEnumSetShort(final Set<E> set) {
        final long vector = this.toLong(set);
        if ((vector & 0xFFFFFFFFFFFF0000L) != 0x0L) {
            throw new IllegalArgumentException("The enum set is too large to fit in a short: " + set);
        }
        return this.putShort((short)vector);
    }
    
    @Override
    public <E extends Enum<E>> IoBuffer putEnumSetShort(final int index, final Set<E> set) {
        final long vector = this.toLong(set);
        if ((vector & 0xFFFFFFFFFFFF0000L) != 0x0L) {
            throw new IllegalArgumentException("The enum set is too large to fit in a short: " + set);
        }
        return this.putShort(index, (short)vector);
    }
    
    @Override
    public <E extends Enum<E>> IoBuffer putEnumSetInt(final Set<E> set) {
        final long vector = this.toLong(set);
        if ((vector & 0xFFFFFFFF00000000L) != 0x0L) {
            throw new IllegalArgumentException("The enum set is too large to fit in an int: " + set);
        }
        return this.putInt((int)vector);
    }
    
    @Override
    public <E extends Enum<E>> IoBuffer putEnumSetInt(final int index, final Set<E> set) {
        final long vector = this.toLong(set);
        if ((vector & 0xFFFFFFFF00000000L) != 0x0L) {
            throw new IllegalArgumentException("The enum set is too large to fit in an int: " + set);
        }
        return this.putInt(index, (int)vector);
    }
    
    @Override
    public <E extends Enum<E>> IoBuffer putEnumSetLong(final Set<E> set) {
        return this.putLong(this.toLong(set));
    }
    
    @Override
    public <E extends Enum<E>> IoBuffer putEnumSetLong(final int index, final Set<E> set) {
        return this.putLong(index, this.toLong(set));
    }
    
    private <E extends Enum<E>> long toLong(final Set<E> set) {
        long vector = 0L;
        for (final E e : set) {
            if (e.ordinal() >= 64) {
                throw new IllegalArgumentException("The enum set is too large to fit in a bit vector: " + set);
            }
            vector |= 1L << e.ordinal();
        }
        return vector;
    }
    
    private IoBuffer autoExpand(final int expectedRemaining) {
        if (this.isAutoExpand()) {
            this.expand(expectedRemaining, true);
        }
        return this;
    }
    
    private IoBuffer autoExpand(final int pos, final int expectedRemaining) {
        if (this.isAutoExpand()) {
            this.expand(pos, expectedRemaining, true);
        }
        return this;
    }
    
    private static void checkFieldSize(final int fieldSize) {
        if (fieldSize < 0) {
            throw new IllegalArgumentException("fieldSize cannot be negative: " + fieldSize);
        }
    }
}
