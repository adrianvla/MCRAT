// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.executor;

import org.apache.mina.core.session.IoEvent;
import java.util.EventListener;

public interface IoEventQueueHandler extends EventListener
{
    public static final IoEventQueueHandler NOOP = new IoEventQueueHandler() {
        @Override
        public boolean accept(final Object source, final IoEvent event) {
            return true;
        }
        
        @Override
        public void offered(final Object source, final IoEvent event) {
        }
        
        @Override
        public void polled(final Object source, final IoEvent event) {
        }
    };
    
    boolean accept(final Object p0, final IoEvent p1);
    
    void offered(final Object p0, final IoEvent p1);
    
    void polled(final Object p0, final IoEvent p1);
}
