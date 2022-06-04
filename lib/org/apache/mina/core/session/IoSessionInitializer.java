// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.session;

import org.apache.mina.core.future.IoFuture;

public interface IoSessionInitializer<T extends IoFuture>
{
    void initializeSession(final IoSession p0, final T p1);
}
