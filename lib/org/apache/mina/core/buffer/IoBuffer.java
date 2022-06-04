// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.buffer;

import java.util.Set;
import java.util.EnumSet;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CharacterCodingException;
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

public abstract class IoBuffer implements Comparable<IoBuffer>
{
    private static IoBufferAllocator allocator;
    private static boolean useDirectBuffer;
    
    public static IoBufferAllocator getAllocator() {
        return IoBuffer.allocator;
    }
    
    public static void setAllocator(final IoBufferAllocator newAllocator) {
        if (newAllocator == null) {
            throw new IllegalArgumentException("allocator");
        }
        final IoBufferAllocator oldAllocator = IoBuffer.allocator;
        IoBuffer.allocator = newAllocator;
        if (null != oldAllocator) {
            oldAllocator.dispose();
        }
    }
    
    public static boolean isUseDirectBuffer() {
        return IoBuffer.useDirectBuffer;
    }
    
    public static void setUseDirectBuffer(final boolean useDirectBuffer) {
        IoBuffer.useDirectBuffer = useDirectBuffer;
    }
    
    public static IoBuffer allocate(final int capacity) {
        return allocate(capacity, IoBuffer.useDirectBuffer);
    }
    
    public static IoBuffer allocate(final int capacity, final boolean useDirectBuffer) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity: " + capacity);
        }
        return IoBuffer.allocator.allocate(capacity, useDirectBuffer);
    }
    
    public static IoBuffer wrap(final ByteBuffer nioBuffer) {
        return IoBuffer.allocator.wrap(nioBuffer);
    }
    
    public static IoBuffer wrap(final byte[] byteArray) {
        return wrap(ByteBuffer.wrap(byteArray));
    }
    
    public static IoBuffer wrap(final byte[] byteArray, final int offset, final int length) {
        return wrap(ByteBuffer.wrap(byteArray, offset, length));
    }
    
    protected static int normalizeCapacity(final int requestedCapacity) {
        if (requestedCapacity < 0) {
            return Integer.MAX_VALUE;
        }
        int newCapacity = Integer.highestOneBit(requestedCapacity);
        newCapacity <<= ((newCapacity < requestedCapacity) ? 1 : 0);
        return (newCapacity < 0) ? Integer.MAX_VALUE : newCapacity;
    }
    
    protected IoBuffer() {
    }
    
    public abstract void free();
    
    public abstract ByteBuffer buf();
    
    public abstract boolean isDirect();
    
    public abstract boolean isDerived();
    
    public abstract boolean isReadOnly();
    
    public abstract int minimumCapacity();
    
    public abstract IoBuffer minimumCapacity(final int p0);
    
    public abstract int capacity();
    
    public abstract IoBuffer capacity(final int p0);
    
    public abstract boolean isAutoExpand();
    
    public abstract IoBuffer setAutoExpand(final boolean p0);
    
    public abstract boolean isAutoShrink();
    
    public abstract IoBuffer setAutoShrink(final boolean p0);
    
    public abstract IoBuffer expand(final int p0);
    
    public abstract IoBuffer expand(final int p0, final int p1);
    
    public abstract IoBuffer shrink();
    
    public abstract int position();
    
    public abstract IoBuffer position(final int p0);
    
    public abstract int limit();
    
    public abstract IoBuffer limit(final int p0);
    
    public abstract IoBuffer mark();
    
    public abstract int markValue();
    
    public abstract IoBuffer reset();
    
    public abstract IoBuffer clear();
    
    public abstract IoBuffer sweep();
    
    public abstract IoBuffer sweep(final byte p0);
    
    public abstract IoBuffer flip();
    
    public abstract IoBuffer rewind();
    
    public abstract int remaining();
    
    public abstract boolean hasRemaining();
    
    public abstract IoBuffer duplicate();
    
    public abstract IoBuffer slice();
    
    public abstract IoBuffer asReadOnlyBuffer();
    
    public abstract boolean hasArray();
    
    public abstract byte[] array();
    
    public abstract int arrayOffset();
    
    public abstract byte get();
    
    public abstract short getUnsigned();
    
    public abstract IoBuffer put(final byte p0);
    
    public abstract byte get(final int p0);
    
    public abstract short getUnsigned(final int p0);
    
    public abstract IoBuffer put(final int p0, final byte p1);
    
    public abstract IoBuffer get(final byte[] p0, final int p1, final int p2);
    
    public abstract IoBuffer get(final byte[] p0);
    
    public abstract IoBuffer getSlice(final int p0, final int p1);
    
    public abstract IoBuffer getSlice(final int p0);
    
    public abstract IoBuffer put(final ByteBuffer p0);
    
    public abstract IoBuffer put(final IoBuffer p0);
    
    public abstract IoBuffer put(final byte[] p0, final int p1, final int p2);
    
    public abstract IoBuffer put(final byte[] p0);
    
    public abstract IoBuffer compact();
    
    public abstract ByteOrder order();
    
    public abstract IoBuffer order(final ByteOrder p0);
    
    public abstract char getChar();
    
    public abstract IoBuffer putChar(final char p0);
    
    public abstract char getChar(final int p0);
    
    public abstract IoBuffer putChar(final int p0, final char p1);
    
    public abstract CharBuffer asCharBuffer();
    
    public abstract short getShort();
    
    public abstract int getUnsignedShort();
    
    public abstract IoBuffer putShort(final short p0);
    
    public abstract short getShort(final int p0);
    
    public abstract int getUnsignedShort(final int p0);
    
    public abstract IoBuffer putShort(final int p0, final short p1);
    
    public abstract ShortBuffer asShortBuffer();
    
    public abstract int getInt();
    
    public abstract long getUnsignedInt();
    
    public abstract int getMediumInt();
    
    public abstract int getUnsignedMediumInt();
    
    public abstract int getMediumInt(final int p0);
    
    public abstract int getUnsignedMediumInt(final int p0);
    
    public abstract IoBuffer putMediumInt(final int p0);
    
    public abstract IoBuffer putMediumInt(final int p0, final int p1);
    
    public abstract IoBuffer putInt(final int p0);
    
    public abstract IoBuffer putUnsigned(final byte p0);
    
    public abstract IoBuffer putUnsigned(final int p0, final byte p1);
    
    public abstract IoBuffer putUnsigned(final short p0);
    
    public abstract IoBuffer putUnsigned(final int p0, final short p1);
    
    public abstract IoBuffer putUnsigned(final int p0);
    
    public abstract IoBuffer putUnsigned(final int p0, final int p1);
    
    public abstract IoBuffer putUnsigned(final long p0);
    
    public abstract IoBuffer putUnsigned(final int p0, final long p1);
    
    public abstract IoBuffer putUnsignedInt(final byte p0);
    
    public abstract IoBuffer putUnsignedInt(final int p0, final byte p1);
    
    public abstract IoBuffer putUnsignedInt(final short p0);
    
    public abstract IoBuffer putUnsignedInt(final int p0, final short p1);
    
    public abstract IoBuffer putUnsignedInt(final int p0);
    
    public abstract IoBuffer putUnsignedInt(final int p0, final int p1);
    
    public abstract IoBuffer putUnsignedInt(final long p0);
    
    public abstract IoBuffer putUnsignedInt(final int p0, final long p1);
    
    public abstract IoBuffer putUnsignedShort(final byte p0);
    
    public abstract IoBuffer putUnsignedShort(final int p0, final byte p1);
    
    public abstract IoBuffer putUnsignedShort(final short p0);
    
    public abstract IoBuffer putUnsignedShort(final int p0, final short p1);
    
    public abstract IoBuffer putUnsignedShort(final int p0);
    
    public abstract IoBuffer putUnsignedShort(final int p0, final int p1);
    
    public abstract IoBuffer putUnsignedShort(final long p0);
    
    public abstract IoBuffer putUnsignedShort(final int p0, final long p1);
    
    public abstract int getInt(final int p0);
    
    public abstract long getUnsignedInt(final int p0);
    
    public abstract IoBuffer putInt(final int p0, final int p1);
    
    public abstract IntBuffer asIntBuffer();
    
    public abstract long getLong();
    
    public abstract IoBuffer putLong(final long p0);
    
    public abstract long getLong(final int p0);
    
    public abstract IoBuffer putLong(final int p0, final long p1);
    
    public abstract LongBuffer asLongBuffer();
    
    public abstract float getFloat();
    
    public abstract IoBuffer putFloat(final float p0);
    
    public abstract float getFloat(final int p0);
    
    public abstract IoBuffer putFloat(final int p0, final float p1);
    
    public abstract FloatBuffer asFloatBuffer();
    
    public abstract double getDouble();
    
    public abstract IoBuffer putDouble(final double p0);
    
    public abstract double getDouble(final int p0);
    
    public abstract IoBuffer putDouble(final int p0, final double p1);
    
    public abstract DoubleBuffer asDoubleBuffer();
    
    public abstract InputStream asInputStream();
    
    public abstract OutputStream asOutputStream();
    
    public abstract String getHexDump();
    
    public abstract String getHexDump(final int p0);
    
    public abstract String getString(final CharsetDecoder p0) throws CharacterCodingException;
    
    public abstract String getString(final int p0, final CharsetDecoder p1) throws CharacterCodingException;
    
    public abstract IoBuffer putString(final CharSequence p0, final CharsetEncoder p1) throws CharacterCodingException;
    
    public abstract IoBuffer putString(final CharSequence p0, final int p1, final CharsetEncoder p2) throws CharacterCodingException;
    
    public abstract String getPrefixedString(final CharsetDecoder p0) throws CharacterCodingException;
    
    public abstract String getPrefixedString(final int p0, final CharsetDecoder p1) throws CharacterCodingException;
    
    public abstract IoBuffer putPrefixedString(final CharSequence p0, final CharsetEncoder p1) throws CharacterCodingException;
    
    public abstract IoBuffer putPrefixedString(final CharSequence p0, final int p1, final CharsetEncoder p2) throws CharacterCodingException;
    
    public abstract IoBuffer putPrefixedString(final CharSequence p0, final int p1, final int p2, final CharsetEncoder p3) throws CharacterCodingException;
    
    public abstract IoBuffer putPrefixedString(final CharSequence p0, final int p1, final int p2, final byte p3, final CharsetEncoder p4) throws CharacterCodingException;
    
    public abstract Object getObject() throws ClassNotFoundException;
    
    public abstract Object getObject(final ClassLoader p0) throws ClassNotFoundException;
    
    public abstract IoBuffer putObject(final Object p0);
    
    public abstract boolean prefixedDataAvailable(final int p0);
    
    public abstract boolean prefixedDataAvailable(final int p0, final int p1);
    
    public abstract int indexOf(final byte p0);
    
    public abstract IoBuffer skip(final int p0);
    
    public abstract IoBuffer fill(final byte p0, final int p1);
    
    public abstract IoBuffer fillAndReset(final byte p0, final int p1);
    
    public abstract IoBuffer fill(final int p0);
    
    public abstract IoBuffer fillAndReset(final int p0);
    
    public abstract <E extends Enum<E>> E getEnum(final Class<E> p0);
    
    public abstract <E extends Enum<E>> E getEnum(final int p0, final Class<E> p1);
    
    public abstract <E extends Enum<E>> E getEnumShort(final Class<E> p0);
    
    public abstract <E extends Enum<E>> E getEnumShort(final int p0, final Class<E> p1);
    
    public abstract <E extends Enum<E>> E getEnumInt(final Class<E> p0);
    
    public abstract <E extends Enum<E>> E getEnumInt(final int p0, final Class<E> p1);
    
    public abstract IoBuffer putEnum(final Enum<?> p0);
    
    public abstract IoBuffer putEnum(final int p0, final Enum<?> p1);
    
    public abstract IoBuffer putEnumShort(final Enum<?> p0);
    
    public abstract IoBuffer putEnumShort(final int p0, final Enum<?> p1);
    
    public abstract IoBuffer putEnumInt(final Enum<?> p0);
    
    public abstract IoBuffer putEnumInt(final int p0, final Enum<?> p1);
    
    public abstract <E extends Enum<E>> EnumSet<E> getEnumSet(final Class<E> p0);
    
    public abstract <E extends Enum<E>> EnumSet<E> getEnumSet(final int p0, final Class<E> p1);
    
    public abstract <E extends Enum<E>> EnumSet<E> getEnumSetShort(final Class<E> p0);
    
    public abstract <E extends Enum<E>> EnumSet<E> getEnumSetShort(final int p0, final Class<E> p1);
    
    public abstract <E extends Enum<E>> EnumSet<E> getEnumSetInt(final Class<E> p0);
    
    public abstract <E extends Enum<E>> EnumSet<E> getEnumSetInt(final int p0, final Class<E> p1);
    
    public abstract <E extends Enum<E>> EnumSet<E> getEnumSetLong(final Class<E> p0);
    
    public abstract <E extends Enum<E>> EnumSet<E> getEnumSetLong(final int p0, final Class<E> p1);
    
    public abstract <E extends Enum<E>> IoBuffer putEnumSet(final Set<E> p0);
    
    public abstract <E extends Enum<E>> IoBuffer putEnumSet(final int p0, final Set<E> p1);
    
    public abstract <E extends Enum<E>> IoBuffer putEnumSetShort(final Set<E> p0);
    
    public abstract <E extends Enum<E>> IoBuffer putEnumSetShort(final int p0, final Set<E> p1);
    
    public abstract <E extends Enum<E>> IoBuffer putEnumSetInt(final Set<E> p0);
    
    public abstract <E extends Enum<E>> IoBuffer putEnumSetInt(final int p0, final Set<E> p1);
    
    public abstract <E extends Enum<E>> IoBuffer putEnumSetLong(final Set<E> p0);
    
    public abstract <E extends Enum<E>> IoBuffer putEnumSetLong(final int p0, final Set<E> p1);
    
    static {
        IoBuffer.allocator = new SimpleBufferAllocator();
        IoBuffer.useDirectBuffer = false;
    }
}
