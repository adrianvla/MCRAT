// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.codec.textline;

import org.apache.mina.filter.codec.ProtocolDecoderException;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.ByteBuffer;
import org.apache.mina.filter.codec.RecoverableProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.core.session.IoSession;
import java.nio.charset.CharacterCodingException;
import org.apache.mina.core.buffer.IoBuffer;
import java.nio.charset.Charset;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.filter.codec.ProtocolDecoder;

public class TextLineDecoder implements ProtocolDecoder
{
    private final AttributeKey CONTEXT;
    private final Charset charset;
    private final LineDelimiter delimiter;
    private IoBuffer delimBuf;
    private int maxLineLength;
    private int bufferLength;
    
    public TextLineDecoder() {
        this(LineDelimiter.AUTO);
    }
    
    public TextLineDecoder(final String delimiter) {
        this(new LineDelimiter(delimiter));
    }
    
    public TextLineDecoder(final LineDelimiter delimiter) {
        this(Charset.defaultCharset(), delimiter);
    }
    
    public TextLineDecoder(final Charset charset) {
        this(charset, LineDelimiter.AUTO);
    }
    
    public TextLineDecoder(final Charset charset, final String delimiter) {
        this(charset, new LineDelimiter(delimiter));
    }
    
    public TextLineDecoder(final Charset charset, final LineDelimiter delimiter) {
        this.CONTEXT = new AttributeKey(this.getClass(), "context");
        this.maxLineLength = 1024;
        this.bufferLength = 128;
        if (charset == null) {
            throw new IllegalArgumentException("charset parameter shuld not be null");
        }
        if (delimiter == null) {
            throw new IllegalArgumentException("delimiter parameter should not be null");
        }
        this.charset = charset;
        this.delimiter = delimiter;
        if (this.delimBuf == null) {
            final IoBuffer tmp = IoBuffer.allocate(2).setAutoExpand(true);
            try {
                tmp.putString(delimiter.getValue(), charset.newEncoder());
            }
            catch (CharacterCodingException ex) {}
            tmp.flip();
            this.delimBuf = tmp;
        }
    }
    
    public int getMaxLineLength() {
        return this.maxLineLength;
    }
    
    public void setMaxLineLength(final int maxLineLength) {
        if (maxLineLength <= 0) {
            throw new IllegalArgumentException("maxLineLength (" + maxLineLength + ") should be a positive value");
        }
        this.maxLineLength = maxLineLength;
    }
    
    public void setBufferLength(final int bufferLength) {
        if (bufferLength <= 0) {
            throw new IllegalArgumentException("bufferLength (" + this.maxLineLength + ") should be a positive value");
        }
        this.bufferLength = bufferLength;
    }
    
    public int getBufferLength() {
        return this.bufferLength;
    }
    
    @Override
    public void decode(final IoSession session, final IoBuffer in, final ProtocolDecoderOutput out) throws Exception {
        final Context ctx = this.getContext(session);
        if (LineDelimiter.AUTO.equals(this.delimiter)) {
            this.decodeAuto(ctx, session, in, out);
        }
        else {
            this.decodeNormal(ctx, session, in, out);
        }
    }
    
    private Context getContext(final IoSession session) {
        Context ctx = (Context)session.getAttribute(this.CONTEXT);
        if (ctx == null) {
            ctx = new Context(this.bufferLength);
            session.setAttribute(this.CONTEXT, ctx);
        }
        return ctx;
    }
    
    @Override
    public void finishDecode(final IoSession session, final ProtocolDecoderOutput out) throws Exception {
    }
    
    @Override
    public void dispose(final IoSession session) throws Exception {
        final Context ctx = (Context)session.getAttribute(this.CONTEXT);
        if (ctx != null) {
            session.removeAttribute(this.CONTEXT);
        }
    }
    
    private void decodeAuto(final Context ctx, final IoSession session, final IoBuffer in, final ProtocolDecoderOutput out) throws CharacterCodingException, ProtocolDecoderException {
        int matchCount = ctx.getMatchCount();
        int oldPos = in.position();
        final int oldLimit = in.limit();
        while (in.hasRemaining()) {
            final byte b = in.get();
            boolean matched = false;
            switch (b) {
                case 13: {
                    ++matchCount;
                    break;
                }
                case 10: {
                    ++matchCount;
                    matched = true;
                    break;
                }
                default: {
                    matchCount = 0;
                    break;
                }
            }
            if (matched) {
                final int pos = in.position();
                in.limit(pos);
                in.position(oldPos);
                ctx.append(in);
                in.limit(oldLimit);
                in.position(pos);
                if (ctx.getOverflowPosition() != 0) {
                    final int overflowPosition = ctx.getOverflowPosition();
                    ctx.reset();
                    throw new RecoverableProtocolDecoderException("Line is too long: " + overflowPosition);
                }
                final IoBuffer buf = ctx.getBuffer();
                buf.flip();
                buf.limit(buf.limit() - matchCount);
                try {
                    final byte[] data = new byte[buf.limit()];
                    buf.get(data);
                    final CharsetDecoder decoder = ctx.getDecoder();
                    final CharBuffer buffer = decoder.decode(ByteBuffer.wrap(data));
                    final String str = buffer.toString();
                    this.writeText(session, str, out);
                }
                finally {
                    buf.clear();
                }
                oldPos = pos;
                matchCount = 0;
            }
        }
        in.position(oldPos);
        ctx.append(in);
        ctx.setMatchCount(matchCount);
    }
    
    private void decodeNormal(final Context ctx, final IoSession session, final IoBuffer in, final ProtocolDecoderOutput out) throws CharacterCodingException, ProtocolDecoderException {
        int matchCount = ctx.getMatchCount();
        int oldPos = in.position();
        final int oldLimit = in.limit();
        while (in.hasRemaining()) {
            final byte b = in.get();
            if (this.delimBuf.get(matchCount) == b) {
                if (++matchCount != this.delimBuf.limit()) {
                    continue;
                }
                final int pos = in.position();
                in.limit(pos);
                in.position(oldPos);
                ctx.append(in);
                in.limit(oldLimit);
                in.position(pos);
                if (ctx.getOverflowPosition() != 0) {
                    final int overflowPosition = ctx.getOverflowPosition();
                    ctx.reset();
                    throw new RecoverableProtocolDecoderException("Line is too long: " + overflowPosition);
                }
                final IoBuffer buf = ctx.getBuffer();
                buf.flip();
                buf.limit(buf.limit() - matchCount);
                try {
                    this.writeText(session, buf.getString(ctx.getDecoder()), out);
                }
                finally {
                    buf.clear();
                }
                oldPos = pos;
                matchCount = 0;
            }
            else {
                in.position(Math.max(0, in.position() - matchCount));
                matchCount = 0;
            }
        }
        in.position(oldPos);
        ctx.append(in);
        ctx.setMatchCount(matchCount);
    }
    
    protected void writeText(final IoSession session, final String text, final ProtocolDecoderOutput out) {
        out.write(text);
    }
    
    private class Context
    {
        private final CharsetDecoder decoder;
        private final IoBuffer buf;
        private int matchCount;
        private int overflowPosition;
        
        private Context(final int bufferLength) {
            this.matchCount = 0;
            this.overflowPosition = 0;
            this.decoder = TextLineDecoder.this.charset.newDecoder();
            this.buf = IoBuffer.allocate(bufferLength).setAutoExpand(true);
        }
        
        public CharsetDecoder getDecoder() {
            return this.decoder;
        }
        
        public IoBuffer getBuffer() {
            return this.buf;
        }
        
        public int getOverflowPosition() {
            return this.overflowPosition;
        }
        
        public int getMatchCount() {
            return this.matchCount;
        }
        
        public void setMatchCount(final int matchCount) {
            this.matchCount = matchCount;
        }
        
        public void reset() {
            this.overflowPosition = 0;
            this.matchCount = 0;
            this.decoder.reset();
        }
        
        public void append(final IoBuffer in) {
            if (this.overflowPosition != 0) {
                this.discard(in);
            }
            else if (this.buf.position() > TextLineDecoder.this.maxLineLength - in.remaining()) {
                this.overflowPosition = this.buf.position();
                this.buf.clear();
                this.discard(in);
            }
            else {
                this.getBuffer().put(in);
            }
        }
        
        private void discard(final IoBuffer in) {
            if (Integer.MAX_VALUE - in.remaining() < this.overflowPosition) {
                this.overflowPosition = Integer.MAX_VALUE;
            }
            else {
                this.overflowPosition += in.remaining();
            }
            in.position(in.limit());
        }
    }
}
