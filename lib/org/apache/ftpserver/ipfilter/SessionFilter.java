// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ipfilter;

import org.apache.mina.core.session.IoSession;

public interface SessionFilter
{
    boolean accept(final IoSession p0);
}
