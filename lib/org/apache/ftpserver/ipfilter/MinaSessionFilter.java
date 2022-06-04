// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ipfilter;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;

public class MinaSessionFilter extends IoFilterAdapter
{
    private final SessionFilter filter;
    
    public MinaSessionFilter(final SessionFilter filter) {
        this.filter = filter;
    }
    
    @Override
    public void sessionCreated(final IoFilter.NextFilter nextFilter, final IoSession session) {
        if (!this.filter.accept(session)) {
            session.close(true);
        }
        else {
            nextFilter.sessionCreated(session);
        }
    }
}
