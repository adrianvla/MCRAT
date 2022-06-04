// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.buffer;

class IoBufferHexDumper
{
    private static final byte[] highDigits;
    private static final byte[] lowDigits;
    
    public static String getHexdump(final IoBuffer in, final int lengthLimit) {
        if (lengthLimit == 0) {
            throw new IllegalArgumentException("lengthLimit: " + lengthLimit + " (expected: 1+)");
        }
        final boolean truncate = in.remaining() > lengthLimit;
        int size;
        if (truncate) {
            size = lengthLimit;
        }
        else {
            size = in.remaining();
        }
        if (size == 0) {
            return "empty";
        }
        final StringBuilder out = new StringBuilder(size * 3 + 3);
        final int mark = in.position();
        int byteValue = in.get() & 0xFF;
        out.append((char)IoBufferHexDumper.highDigits[byteValue]);
        out.append((char)IoBufferHexDumper.lowDigits[byteValue]);
        --size;
        while (size > 0) {
            out.append(' ');
            byteValue = (in.get() & 0xFF);
            out.append((char)IoBufferHexDumper.highDigits[byteValue]);
            out.append((char)IoBufferHexDumper.lowDigits[byteValue]);
            --size;
        }
        in.position(mark);
        if (truncate) {
            out.append("...");
        }
        return out.toString();
    }
    
    static {
        final byte[] digits = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70 };
        final byte[] high = new byte[256];
        final byte[] low = new byte[256];
        for (int i = 0; i < 256; ++i) {
            high[i] = digits[i >>> 4];
            low[i] = digits[i & 0xF];
        }
        highDigits = high;
        lowDigits = low;
    }
}
