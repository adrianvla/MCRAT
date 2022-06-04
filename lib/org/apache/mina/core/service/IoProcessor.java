// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.service;

import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.session.IoSession;

public interface IoProcessor<S extends IoSession>
{
    boolean isDisposing();
    
    boolean isDisposed();
    
    void dispose();
    
    void add(final S p0);
    
    void flush(final S p0);
    
    void write(final S p0, final WriteRequest p1);
    
    void updateTrafficControl(final S p0);
    
    void remove(final S p0);
}
