// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.util;

import java.util.Iterator;
import java.util.Collection;
import java.util.Map;
import java.io.Serializable;
import java.util.AbstractSet;

public class MapBackedSet<E> extends AbstractSet<E> implements Serializable
{
    private static final long serialVersionUID = -8347878570391674042L;
    protected final Map<E, Boolean> map;
    
    public MapBackedSet(final Map<E, Boolean> map) {
        this.map = map;
    }
    
    public MapBackedSet(final Map<E, Boolean> map, final Collection<E> c) {
        this.map = map;
        this.addAll((Collection<? extends E>)c);
    }
    
    @Override
    public int size() {
        return this.map.size();
    }
    
    @Override
    public boolean contains(final Object o) {
        return this.map.containsKey(o);
    }
    
    @Override
    public Iterator<E> iterator() {
        return this.map.keySet().iterator();
    }
    
    @Override
    public boolean add(final E o) {
        return this.map.put(o, Boolean.TRUE) == null;
    }
    
    @Override
    public boolean remove(final Object o) {
        return this.map.remove(o) != null;
    }
    
    @Override
    public void clear() {
        this.map.clear();
    }
}
