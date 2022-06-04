// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.util;

import java.util.Collection;
import java.util.Map;
import java.util.IdentityHashMap;

public class IdentityHashSet<E> extends MapBackedSet<E>
{
    private static final long serialVersionUID = 6948202189467167147L;
    
    public IdentityHashSet() {
        super(new IdentityHashMap());
    }
    
    public IdentityHashSet(final int expectedMaxSize) {
        super(new IdentityHashMap(expectedMaxSize));
    }
    
    public IdentityHashSet(final Collection<E> c) {
        super(new IdentityHashMap<E, Boolean>(), c);
    }
}
