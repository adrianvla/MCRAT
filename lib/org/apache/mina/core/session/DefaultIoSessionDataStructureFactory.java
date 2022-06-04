// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.session;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.mina.core.write.WriteRequest;
import java.util.Queue;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.core.write.WriteRequestQueue;

public class DefaultIoSessionDataStructureFactory implements IoSessionDataStructureFactory
{
    @Override
    public IoSessionAttributeMap getAttributeMap(final IoSession session) throws Exception {
        return new DefaultIoSessionAttributeMap();
    }
    
    @Override
    public WriteRequestQueue getWriteRequestQueue(final IoSession session) throws Exception {
        return new DefaultWriteRequestQueue();
    }
    
    private static class DefaultIoSessionAttributeMap implements IoSessionAttributeMap
    {
        private final ConcurrentHashMap<Object, Object> attributes;
        
        public DefaultIoSessionAttributeMap() {
            this.attributes = new ConcurrentHashMap<Object, Object>(4);
        }
        
        @Override
        public Object getAttribute(final IoSession session, final Object key, final Object defaultValue) {
            if (key == null) {
                throw new IllegalArgumentException("key");
            }
            if (defaultValue == null) {
                return this.attributes.get(key);
            }
            final Object object = this.attributes.putIfAbsent(key, defaultValue);
            if (object == null) {
                return defaultValue;
            }
            return object;
        }
        
        @Override
        public Object setAttribute(final IoSession session, final Object key, final Object value) {
            if (key == null) {
                throw new IllegalArgumentException("key");
            }
            if (value == null) {
                return this.attributes.remove(key);
            }
            return this.attributes.put(key, value);
        }
        
        @Override
        public Object setAttributeIfAbsent(final IoSession session, final Object key, final Object value) {
            if (key == null) {
                throw new IllegalArgumentException("key");
            }
            if (value == null) {
                return null;
            }
            return this.attributes.putIfAbsent(key, value);
        }
        
        @Override
        public Object removeAttribute(final IoSession session, final Object key) {
            if (key == null) {
                throw new IllegalArgumentException("key");
            }
            return this.attributes.remove(key);
        }
        
        @Override
        public boolean removeAttribute(final IoSession session, final Object key, final Object value) {
            if (key == null) {
                throw new IllegalArgumentException("key");
            }
            if (value == null) {
                return false;
            }
            try {
                return this.attributes.remove(key, value);
            }
            catch (NullPointerException e) {
                return false;
            }
        }
        
        @Override
        public boolean replaceAttribute(final IoSession session, final Object key, final Object oldValue, final Object newValue) {
            try {
                return this.attributes.replace(key, oldValue, newValue);
            }
            catch (NullPointerException ex) {
                return false;
            }
        }
        
        @Override
        public boolean containsAttribute(final IoSession session, final Object key) {
            return this.attributes.containsKey(key);
        }
        
        @Override
        public Set<Object> getAttributeKeys(final IoSession session) {
            synchronized (this.attributes) {
                return new HashSet<Object>(this.attributes.keySet());
            }
        }
        
        @Override
        public void dispose(final IoSession session) throws Exception {
        }
    }
    
    private static class DefaultWriteRequestQueue implements WriteRequestQueue
    {
        private final Queue<WriteRequest> q;
        
        public DefaultWriteRequestQueue() {
            this.q = new ConcurrentLinkedQueue<WriteRequest>();
        }
        
        @Override
        public void dispose(final IoSession session) {
        }
        
        @Override
        public void clear(final IoSession session) {
            this.q.clear();
        }
        
        @Override
        public boolean isEmpty(final IoSession session) {
            return this.q.isEmpty();
        }
        
        @Override
        public void offer(final IoSession session, final WriteRequest writeRequest) {
            this.q.offer(writeRequest);
        }
        
        @Override
        public WriteRequest poll(final IoSession session) {
            WriteRequest answer = this.q.poll();
            if (answer == AbstractIoSession.CLOSE_REQUEST) {
                session.closeNow();
                this.dispose(session);
                answer = null;
            }
            return answer;
        }
        
        @Override
        public String toString() {
            return this.q.toString();
        }
        
        @Override
        public int size() {
            return this.q.size();
        }
    }
}
