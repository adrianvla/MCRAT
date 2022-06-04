// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.future;

import java.util.EventListener;

public interface IoFutureListener<F extends IoFuture> extends EventListener
{
    public static final IoFutureListener<IoFuture> CLOSE = new IoFutureListener<IoFuture>() {
        @Override
        public void operationComplete(final IoFuture future) {
            future.getSession().closeNow();
        }
    };
    
    void operationComplete(final F p0);
}
