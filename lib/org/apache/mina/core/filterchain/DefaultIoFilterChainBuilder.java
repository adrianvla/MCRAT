// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.filterchain;

import org.slf4j.LoggerFactory;
import java.util.Random;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ListIterator;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import org.slf4j.Logger;

public class DefaultIoFilterChainBuilder implements IoFilterChainBuilder
{
    private static final Logger LOGGER;
    private final List<IoFilterChain.Entry> entries;
    
    public DefaultIoFilterChainBuilder() {
        this.entries = new CopyOnWriteArrayList<IoFilterChain.Entry>();
    }
    
    public DefaultIoFilterChainBuilder(final DefaultIoFilterChainBuilder filterChain) {
        if (filterChain == null) {
            throw new IllegalArgumentException("filterChain");
        }
        this.entries = new CopyOnWriteArrayList<IoFilterChain.Entry>(filterChain.entries);
    }
    
    public IoFilterChain.Entry getEntry(final String name) {
        for (final IoFilterChain.Entry e : this.entries) {
            if (e.getName().equals(name)) {
                return e;
            }
        }
        return null;
    }
    
    public IoFilterChain.Entry getEntry(final IoFilter filter) {
        for (final IoFilterChain.Entry e : this.entries) {
            if (e.getFilter() == filter) {
                return e;
            }
        }
        return null;
    }
    
    public IoFilterChain.Entry getEntry(final Class<? extends IoFilter> filterType) {
        for (final IoFilterChain.Entry e : this.entries) {
            if (filterType.isAssignableFrom(e.getFilter().getClass())) {
                return e;
            }
        }
        return null;
    }
    
    public IoFilter get(final String name) {
        final IoFilterChain.Entry e = this.getEntry(name);
        if (e == null) {
            return null;
        }
        return e.getFilter();
    }
    
    public IoFilter get(final Class<? extends IoFilter> filterType) {
        final IoFilterChain.Entry e = this.getEntry(filterType);
        if (e == null) {
            return null;
        }
        return e.getFilter();
    }
    
    public List<IoFilterChain.Entry> getAll() {
        return new ArrayList<IoFilterChain.Entry>(this.entries);
    }
    
    public List<IoFilterChain.Entry> getAllReversed() {
        final List<IoFilterChain.Entry> result = this.getAll();
        Collections.reverse(result);
        return result;
    }
    
    public boolean contains(final String name) {
        return this.getEntry(name) != null;
    }
    
    public boolean contains(final IoFilter filter) {
        return this.getEntry(filter) != null;
    }
    
    public boolean contains(final Class<? extends IoFilter> filterType) {
        return this.getEntry(filterType) != null;
    }
    
    public synchronized void addFirst(final String name, final IoFilter filter) {
        this.register(0, new EntryImpl(name, filter));
    }
    
    public synchronized void addLast(final String name, final IoFilter filter) {
        this.register(this.entries.size(), new EntryImpl(name, filter));
    }
    
    public synchronized void addBefore(final String baseName, final String name, final IoFilter filter) {
        this.checkBaseName(baseName);
        final ListIterator<IoFilterChain.Entry> i = this.entries.listIterator();
        while (i.hasNext()) {
            final IoFilterChain.Entry base = i.next();
            if (base.getName().equals(baseName)) {
                this.register(i.previousIndex(), new EntryImpl(name, filter));
                break;
            }
        }
    }
    
    public synchronized void addAfter(final String baseName, final String name, final IoFilter filter) {
        this.checkBaseName(baseName);
        final ListIterator<IoFilterChain.Entry> i = this.entries.listIterator();
        while (i.hasNext()) {
            final IoFilterChain.Entry base = i.next();
            if (base.getName().equals(baseName)) {
                this.register(i.nextIndex(), new EntryImpl(name, filter));
                break;
            }
        }
    }
    
    public synchronized IoFilter remove(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("name");
        }
        final ListIterator<IoFilterChain.Entry> i = this.entries.listIterator();
        while (i.hasNext()) {
            final IoFilterChain.Entry e = i.next();
            if (e.getName().equals(name)) {
                this.entries.remove(i.previousIndex());
                return e.getFilter();
            }
        }
        throw new IllegalArgumentException("Unknown filter name: " + name);
    }
    
    public synchronized IoFilter remove(final IoFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("filter");
        }
        final ListIterator<IoFilterChain.Entry> i = this.entries.listIterator();
        while (i.hasNext()) {
            final IoFilterChain.Entry e = i.next();
            if (e.getFilter() == filter) {
                this.entries.remove(i.previousIndex());
                return e.getFilter();
            }
        }
        throw new IllegalArgumentException("Filter not found: " + filter.getClass().getName());
    }
    
    public synchronized IoFilter remove(final Class<? extends IoFilter> filterType) {
        if (filterType == null) {
            throw new IllegalArgumentException("filterType");
        }
        final ListIterator<IoFilterChain.Entry> i = this.entries.listIterator();
        while (i.hasNext()) {
            final IoFilterChain.Entry e = i.next();
            if (filterType.isAssignableFrom(e.getFilter().getClass())) {
                this.entries.remove(i.previousIndex());
                return e.getFilter();
            }
        }
        throw new IllegalArgumentException("Filter not found: " + filterType.getName());
    }
    
    public synchronized IoFilter replace(final String name, final IoFilter newFilter) {
        this.checkBaseName(name);
        final EntryImpl e = (EntryImpl)this.getEntry(name);
        final IoFilter oldFilter = e.getFilter();
        e.setFilter(newFilter);
        return oldFilter;
    }
    
    public synchronized void replace(final IoFilter oldFilter, final IoFilter newFilter) {
        for (final IoFilterChain.Entry e : this.entries) {
            if (e.getFilter() == oldFilter) {
                ((EntryImpl)e).setFilter(newFilter);
                return;
            }
        }
        throw new IllegalArgumentException("Filter not found: " + oldFilter.getClass().getName());
    }
    
    public synchronized void replace(final Class<? extends IoFilter> oldFilterType, final IoFilter newFilter) {
        for (final IoFilterChain.Entry e : this.entries) {
            if (oldFilterType.isAssignableFrom(e.getFilter().getClass())) {
                ((EntryImpl)e).setFilter(newFilter);
                return;
            }
        }
        throw new IllegalArgumentException("Filter not found: " + oldFilterType.getName());
    }
    
    public synchronized void clear() {
        this.entries.clear();
    }
    
    public void setFilters(Map<String, ? extends IoFilter> filters) {
        if (filters == null) {
            throw new IllegalArgumentException("filters");
        }
        if (!this.isOrderedMap(filters)) {
            throw new IllegalArgumentException("filters is not an ordered map. Please try " + LinkedHashMap.class.getName() + ".");
        }
        filters = new LinkedHashMap<String, IoFilter>(filters);
        for (final Map.Entry<String, ? extends IoFilter> e : filters.entrySet()) {
            if (e.getKey() == null) {
                throw new IllegalArgumentException("filters contains a null key.");
            }
            if (e.getValue() == null) {
                throw new IllegalArgumentException("filters contains a null value.");
            }
        }
        synchronized (this) {
            this.clear();
            for (final Map.Entry<String, ? extends IoFilter> e2 : filters.entrySet()) {
                this.addLast(e2.getKey(), (IoFilter)e2.getValue());
            }
        }
    }
    
    private boolean isOrderedMap(final Map<String, ? extends IoFilter> map) {
        final Class<?> mapType = map.getClass();
        if (LinkedHashMap.class.isAssignableFrom(mapType)) {
            if (DefaultIoFilterChainBuilder.LOGGER.isDebugEnabled()) {
                DefaultIoFilterChainBuilder.LOGGER.debug("{} is an ordered map.", mapType.getSimpleName());
            }
            return true;
        }
        if (DefaultIoFilterChainBuilder.LOGGER.isDebugEnabled()) {
            DefaultIoFilterChainBuilder.LOGGER.debug("{} is not a {}", mapType.getName(), LinkedHashMap.class.getSimpleName());
        }
        for (Class<?> type = mapType; type != null; type = type.getSuperclass()) {
            for (final Class<?> i : type.getInterfaces()) {
                if (i.getName().endsWith("OrderedMap")) {
                    if (DefaultIoFilterChainBuilder.LOGGER.isDebugEnabled()) {
                        DefaultIoFilterChainBuilder.LOGGER.debug("{} is an ordered map (guessed from that it implements OrderedMap interface.)", mapType.getSimpleName());
                    }
                    return true;
                }
            }
        }
        if (DefaultIoFilterChainBuilder.LOGGER.isDebugEnabled()) {
            DefaultIoFilterChainBuilder.LOGGER.debug("{} doesn't implement OrderedMap interface.", mapType.getName());
        }
        DefaultIoFilterChainBuilder.LOGGER.debug("Last resort; trying to create a new map instance with a default constructor and test if insertion order is maintained.");
        Map<String, IoFilter> newMap;
        try {
            newMap = (Map<String, IoFilter>)mapType.newInstance();
        }
        catch (Exception e) {
            if (DefaultIoFilterChainBuilder.LOGGER.isDebugEnabled()) {
                DefaultIoFilterChainBuilder.LOGGER.debug("Failed to create a new map instance of '{}'.", mapType.getName(), e);
            }
            return false;
        }
        final Random rand = new Random();
        final List<String> expectedNames = new ArrayList<String>();
        final IoFilter dummyFilter = new IoFilterAdapter();
        for (int j = 0; j < 65536; ++j) {
            String filterName;
            do {
                filterName = String.valueOf(rand.nextInt());
            } while (newMap.containsKey(filterName));
            newMap.put(filterName, dummyFilter);
            expectedNames.add(filterName);
            final Iterator<String> it = expectedNames.iterator();
            for (final Object key : newMap.keySet()) {
                if (!it.next().equals(key)) {
                    if (DefaultIoFilterChainBuilder.LOGGER.isDebugEnabled()) {
                        DefaultIoFilterChainBuilder.LOGGER.debug("The specified map didn't pass the insertion order test after {} tries.", (Object)(j + 1));
                    }
                    return false;
                }
            }
        }
        DefaultIoFilterChainBuilder.LOGGER.debug("The specified map passed the insertion order test.");
        return true;
    }
    
    @Override
    public void buildFilterChain(final IoFilterChain chain) throws Exception {
        for (final IoFilterChain.Entry e : this.entries) {
            chain.addLast(e.getName(), e.getFilter());
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("{ ");
        boolean empty = true;
        for (final IoFilterChain.Entry e : this.entries) {
            if (!empty) {
                buf.append(", ");
            }
            else {
                empty = false;
            }
            buf.append('(');
            buf.append(e.getName());
            buf.append(':');
            buf.append(e.getFilter());
            buf.append(')');
        }
        if (empty) {
            buf.append("empty");
        }
        buf.append(" }");
        return buf.toString();
    }
    
    private void checkBaseName(final String baseName) {
        if (baseName == null) {
            throw new IllegalArgumentException("baseName");
        }
        if (!this.contains(baseName)) {
            throw new IllegalArgumentException("Unknown filter name: " + baseName);
        }
    }
    
    private void register(final int index, final IoFilterChain.Entry e) {
        if (this.contains(e.getName())) {
            throw new IllegalArgumentException("Other filter is using the same name: " + e.getName());
        }
        this.entries.add(index, e);
    }
    
    static {
        LOGGER = LoggerFactory.getLogger(DefaultIoFilterChainBuilder.class);
    }
    
    private final class EntryImpl implements IoFilterChain.Entry
    {
        private final String name;
        private volatile IoFilter filter;
        
        private EntryImpl(final String name, final IoFilter filter) {
            if (name == null) {
                throw new IllegalArgumentException("name");
            }
            if (filter == null) {
                throw new IllegalArgumentException("filter");
            }
            this.name = name;
            this.filter = filter;
        }
        
        @Override
        public String getName() {
            return this.name;
        }
        
        @Override
        public IoFilter getFilter() {
            return this.filter;
        }
        
        private void setFilter(final IoFilter filter) {
            this.filter = filter;
        }
        
        @Override
        public IoFilter.NextFilter getNextFilter() {
            throw new IllegalStateException();
        }
        
        @Override
        public String toString() {
            return "(" + this.getName() + ':' + this.filter + ')';
        }
        
        @Override
        public void addAfter(final String name, final IoFilter filter) {
            DefaultIoFilterChainBuilder.this.addAfter(this.getName(), name, filter);
        }
        
        @Override
        public void addBefore(final String name, final IoFilter filter) {
            DefaultIoFilterChainBuilder.this.addBefore(this.getName(), name, filter);
        }
        
        @Override
        public void remove() {
            DefaultIoFilterChainBuilder.this.remove(this.getName());
        }
        
        @Override
        public void replace(final IoFilter newFilter) {
            DefaultIoFilterChainBuilder.this.replace(this.getName(), newFilter);
        }
    }
}
