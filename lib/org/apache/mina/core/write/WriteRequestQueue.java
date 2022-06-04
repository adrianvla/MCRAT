// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.write;

import org.apache.mina.core.session.IoSession;

public interface WriteRequestQueue
{
    WriteRequest poll(final IoSession p0);
    
    void offer(final IoSession p0, final WriteRequest p1);
    
    boolean isEmpty(final IoSession p0);
    
    void clear(final IoSession p0);
    
    void dispose(final IoSession p0);
    
    int size();
}
