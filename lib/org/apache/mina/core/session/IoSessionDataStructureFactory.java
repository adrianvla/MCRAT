// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.session;

import org.apache.mina.core.write.WriteRequestQueue;

public interface IoSessionDataStructureFactory
{
    IoSessionAttributeMap getAttributeMap(final IoSession p0) throws Exception;
    
    WriteRequestQueue getWriteRequestQueue(final IoSession p0) throws Exception;
}
