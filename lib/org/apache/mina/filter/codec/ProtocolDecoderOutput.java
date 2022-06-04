// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.filterchain.IoFilter;

public interface ProtocolDecoderOutput
{
    void write(final Object p0);
    
    void flush(final IoFilter.NextFilter p0, final IoSession p1);
}
