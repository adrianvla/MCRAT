// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.codec.textline;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;

public class LineDelimiter
{
    public static final LineDelimiter DEFAULT;
    public static final LineDelimiter AUTO;
    public static final LineDelimiter CRLF;
    public static final LineDelimiter UNIX;
    public static final LineDelimiter WINDOWS;
    public static final LineDelimiter MAC;
    public static final LineDelimiter NUL;
    private final String value;
    
    public LineDelimiter(final String value) {
        if (value == null) {
            throw new IllegalArgumentException("delimiter");
        }
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
    
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LineDelimiter)) {
            return false;
        }
        final LineDelimiter that = (LineDelimiter)o;
        return this.value.equals(that.value);
    }
    
    @Override
    public String toString() {
        if (this.value.length() == 0) {
            return "delimiter: auto";
        }
        final StringBuilder buf = new StringBuilder();
        buf.append("delimiter:");
        for (int i = 0; i < this.value.length(); ++i) {
            buf.append(" 0x");
            buf.append(Integer.toHexString(this.value.charAt(i)));
        }
        return buf.toString();
    }
    
    static {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final PrintWriter out = new PrintWriter(bout, true);
        out.println();
        DEFAULT = new LineDelimiter(new String(bout.toByteArray()));
        AUTO = new LineDelimiter("");
        CRLF = new LineDelimiter("\r\n");
        UNIX = new LineDelimiter("\n");
        WINDOWS = LineDelimiter.CRLF;
        MAC = new LineDelimiter("\r");
        NUL = new LineDelimiter("\u0000");
    }
}
