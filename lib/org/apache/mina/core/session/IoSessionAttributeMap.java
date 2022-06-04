// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.session;

import java.util.Set;

public interface IoSessionAttributeMap
{
    Object getAttribute(final IoSession p0, final Object p1, final Object p2);
    
    Object setAttribute(final IoSession p0, final Object p1, final Object p2);
    
    Object setAttributeIfAbsent(final IoSession p0, final Object p1, final Object p2);
    
    Object removeAttribute(final IoSession p0, final Object p1);
    
    boolean removeAttribute(final IoSession p0, final Object p1, final Object p2);
    
    boolean replaceAttribute(final IoSession p0, final Object p1, final Object p2, final Object p3);
    
    boolean containsAttribute(final IoSession p0, final Object p1);
    
    Set<Object> getAttributeKeys(final IoSession p0);
    
    void dispose(final IoSession p0) throws Exception;
}
