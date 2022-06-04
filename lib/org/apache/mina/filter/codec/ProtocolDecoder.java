// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

public interface ProtocolDecoder
{
    void decode(final IoSession p0, final IoBuffer p1, final ProtocolDecoderOutput p2) throws Exception;
    
    void finishDecode(final IoSession p0, final ProtocolDecoderOutput p1) throws Exception;
    
    void dispose(final IoSession p0) throws Exception;
}
