// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.codec;

import org.apache.mina.core.session.IoSession;

public interface ProtocolCodecFactory
{
    ProtocolEncoder getEncoder(final IoSession p0) throws Exception;
    
    ProtocolDecoder getDecoder(final IoSession p0) throws Exception;
}
