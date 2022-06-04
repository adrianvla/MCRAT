// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.listener.nio;

import java.nio.charset.Charset;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.core.session.IoSession;
import java.nio.charset.CharsetEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;

public class FtpResponseEncoder extends ProtocolEncoderAdapter
{
    private static final CharsetEncoder ENCODER;
    
    @Override
    public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
        final String value = message.toString();
        final IoBuffer buf = IoBuffer.allocate(value.length()).setAutoExpand(true);
        buf.putString(value, FtpResponseEncoder.ENCODER);
        buf.flip();
        out.write(buf);
    }
    
    static {
        ENCODER = Charset.forName("UTF-8").newEncoder();
    }
}
