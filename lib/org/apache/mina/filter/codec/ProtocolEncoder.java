// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.codec;

import org.apache.mina.core.session.IoSession;

public interface ProtocolEncoder
{
    void encode(final IoSession p0, final Object p1, final ProtocolEncoderOutput p2) throws Exception;
    
    void dispose(final IoSession p0) throws Exception;
}
