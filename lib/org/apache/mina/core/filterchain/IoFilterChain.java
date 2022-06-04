// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.filterchain;

import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.session.IdleStatus;
import java.util.List;
import org.apache.mina.core.session.IoSession;

public interface IoFilterChain
{
    IoSession getSession();
    
    Entry getEntry(final String p0);
    
    Entry getEntry(final IoFilter p0);
    
    Entry getEntry(final Class<? extends IoFilter> p0);
    
    IoFilter get(final String p0);
    
    IoFilter get(final Class<? extends IoFilter> p0);
    
    IoFilter.NextFilter getNextFilter(final String p0);
    
    IoFilter.NextFilter getNextFilter(final IoFilter p0);
    
    IoFilter.NextFilter getNextFilter(final Class<? extends IoFilter> p0);
    
    List<Entry> getAll();
    
    List<Entry> getAllReversed();
    
    boolean contains(final String p0);
    
    boolean contains(final IoFilter p0);
    
    boolean contains(final Class<? extends IoFilter> p0);
    
    void addFirst(final String p0, final IoFilter p1);
    
    void addLast(final String p0, final IoFilter p1);
    
    void addBefore(final String p0, final String p1, final IoFilter p2);
    
    void addAfter(final String p0, final String p1, final IoFilter p2);
    
    IoFilter replace(final String p0, final IoFilter p1);
    
    void replace(final IoFilter p0, final IoFilter p1);
    
    IoFilter replace(final Class<? extends IoFilter> p0, final IoFilter p1);
    
    IoFilter remove(final String p0);
    
    void remove(final IoFilter p0);
    
    IoFilter remove(final Class<? extends IoFilter> p0);
    
    void clear() throws Exception;
    
    void fireSessionCreated();
    
    void fireSessionOpened();
    
    void fireSessionClosed();
    
    void fireSessionIdle(final IdleStatus p0);
    
    void fireMessageReceived(final Object p0);
    
    void fireMessageSent(final WriteRequest p0);
    
    void fireExceptionCaught(final Throwable p0);
    
    void fireInputClosed();
    
    void fireFilterWrite(final WriteRequest p0);
    
    void fireFilterClose();
    
    public interface Entry
    {
        String getName();
        
        IoFilter getFilter();
        
        IoFilter.NextFilter getNextFilter();
        
        void addBefore(final String p0, final IoFilter p1);
        
        void addAfter(final String p0, final IoFilter p1);
        
        void replace(final IoFilter p0);
        
        void remove();
    }
}
