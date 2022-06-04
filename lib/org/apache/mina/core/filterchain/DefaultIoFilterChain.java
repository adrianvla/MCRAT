// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core.filterchain;

import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.write.WriteRequestQueue;
import org.slf4j.LoggerFactory;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IdleStatus;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.mina.core.session.IoSession;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import java.util.Map;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.AttributeKey;

public class DefaultIoFilterChain implements IoFilterChain
{
    public static final AttributeKey SESSION_CREATED_FUTURE;
    private final AbstractIoSession session;
    private final Map<String, Entry> name2entry;
    private final EntryImpl head;
    private final EntryImpl tail;
    private static final Logger LOGGER;
    
    public DefaultIoFilterChain(final AbstractIoSession session) {
        this.name2entry = new ConcurrentHashMap<String, Entry>();
        if (session == null) {
            throw new IllegalArgumentException("session");
        }
        this.session = session;
        this.head = new EntryImpl((EntryImpl)null, (EntryImpl)null, "head", (IoFilter)new HeadFilter());
        this.tail = new EntryImpl(this.head, (EntryImpl)null, "tail", (IoFilter)new TailFilter());
        this.head.nextEntry = this.tail;
    }
    
    @Override
    public IoSession getSession() {
        return this.session;
    }
    
    @Override
    public Entry getEntry(final String name) {
        final Entry e = this.name2entry.get(name);
        if (e == null) {
            return null;
        }
        return e;
    }
    
    @Override
    public Entry getEntry(final IoFilter filter) {
        for (EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            if (e.getFilter() == filter) {
                return e;
            }
        }
        return null;
    }
    
    @Override
    public Entry getEntry(final Class<? extends IoFilter> filterType) {
        for (EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            if (filterType.isAssignableFrom(e.getFilter().getClass())) {
                return e;
            }
        }
        return null;
    }
    
    @Override
    public IoFilter get(final String name) {
        final Entry e = this.getEntry(name);
        if (e == null) {
            return null;
        }
        return e.getFilter();
    }
    
    @Override
    public IoFilter get(final Class<? extends IoFilter> filterType) {
        final Entry e = this.getEntry(filterType);
        if (e == null) {
            return null;
        }
        return e.getFilter();
    }
    
    @Override
    public IoFilter.NextFilter getNextFilter(final String name) {
        final Entry e = this.getEntry(name);
        if (e == null) {
            return null;
        }
        return e.getNextFilter();
    }
    
    @Override
    public IoFilter.NextFilter getNextFilter(final IoFilter filter) {
        final Entry e = this.getEntry(filter);
        if (e == null) {
            return null;
        }
        return e.getNextFilter();
    }
    
    @Override
    public IoFilter.NextFilter getNextFilter(final Class<? extends IoFilter> filterType) {
        final Entry e = this.getEntry(filterType);
        if (e == null) {
            return null;
        }
        return e.getNextFilter();
    }
    
    @Override
    public synchronized void addFirst(final String name, final IoFilter filter) {
        this.checkAddable(name);
        this.register(this.head, name, filter);
    }
    
    @Override
    public synchronized void addLast(final String name, final IoFilter filter) {
        this.checkAddable(name);
        this.register(this.tail.prevEntry, name, filter);
    }
    
    @Override
    public synchronized void addBefore(final String baseName, final String name, final IoFilter filter) {
        final EntryImpl baseEntry = this.checkOldName(baseName);
        this.checkAddable(name);
        this.register(baseEntry.prevEntry, name, filter);
    }
    
    @Override
    public synchronized void addAfter(final String baseName, final String name, final IoFilter filter) {
        final EntryImpl baseEntry = this.checkOldName(baseName);
        this.checkAddable(name);
        this.register(baseEntry, name, filter);
    }
    
    @Override
    public synchronized IoFilter remove(final String name) {
        final EntryImpl entry = this.checkOldName(name);
        this.deregister(entry);
        return entry.getFilter();
    }
    
    @Override
    public synchronized void remove(final IoFilter filter) {
        for (EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            if (e.getFilter() == filter) {
                this.deregister(e);
                return;
            }
        }
        throw new IllegalArgumentException("Filter not found: " + filter.getClass().getName());
    }
    
    @Override
    public synchronized IoFilter remove(final Class<? extends IoFilter> filterType) {
        for (EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            if (filterType.isAssignableFrom(e.getFilter().getClass())) {
                final IoFilter oldFilter = e.getFilter();
                this.deregister(e);
                return oldFilter;
            }
        }
        throw new IllegalArgumentException("Filter not found: " + filterType.getName());
    }
    
    @Override
    public synchronized IoFilter replace(final String name, final IoFilter newFilter) {
        final EntryImpl entry = this.checkOldName(name);
        final IoFilter oldFilter = entry.getFilter();
        try {
            newFilter.onPreAdd(this, name, entry.getNextFilter());
        }
        catch (Exception e) {
            throw new IoFilterLifeCycleException("onPreAdd(): " + name + ':' + newFilter + " in " + this.getSession(), e);
        }
        entry.setFilter(newFilter);
        try {
            newFilter.onPostAdd(this, name, entry.getNextFilter());
        }
        catch (Exception e) {
            entry.setFilter(oldFilter);
            throw new IoFilterLifeCycleException("onPostAdd(): " + name + ':' + newFilter + " in " + this.getSession(), e);
        }
        return oldFilter;
    }
    
    @Override
    public synchronized void replace(final IoFilter oldFilter, final IoFilter newFilter) {
        for (EntryImpl entry = this.head.nextEntry; entry != this.tail; entry = entry.nextEntry) {
            if (entry.getFilter() == oldFilter) {
                String oldFilterName = null;
                for (final Map.Entry<String, Entry> mapping : this.name2entry.entrySet()) {
                    if (entry == mapping.getValue()) {
                        oldFilterName = mapping.getKey();
                        break;
                    }
                }
                try {
                    newFilter.onPreAdd(this, oldFilterName, entry.getNextFilter());
                }
                catch (Exception e) {
                    throw new IoFilterLifeCycleException("onPreAdd(): " + oldFilterName + ':' + newFilter + " in " + this.getSession(), e);
                }
                entry.setFilter(newFilter);
                try {
                    newFilter.onPostAdd(this, oldFilterName, entry.getNextFilter());
                }
                catch (Exception e) {
                    entry.setFilter(oldFilter);
                    throw new IoFilterLifeCycleException("onPostAdd(): " + oldFilterName + ':' + newFilter + " in " + this.getSession(), e);
                }
                return;
            }
        }
        throw new IllegalArgumentException("Filter not found: " + oldFilter.getClass().getName());
    }
    
    @Override
    public synchronized IoFilter replace(final Class<? extends IoFilter> oldFilterType, final IoFilter newFilter) {
        for (EntryImpl entry = this.head.nextEntry; entry != this.tail; entry = entry.nextEntry) {
            if (oldFilterType.isAssignableFrom(entry.getFilter().getClass())) {
                final IoFilter oldFilter = entry.getFilter();
                String oldFilterName = null;
                for (final Map.Entry<String, Entry> mapping : this.name2entry.entrySet()) {
                    if (entry == mapping.getValue()) {
                        oldFilterName = mapping.getKey();
                        break;
                    }
                }
                try {
                    newFilter.onPreAdd(this, oldFilterName, entry.getNextFilter());
                }
                catch (Exception e) {
                    throw new IoFilterLifeCycleException("onPreAdd(): " + oldFilterName + ':' + newFilter + " in " + this.getSession(), e);
                }
                entry.setFilter(newFilter);
                try {
                    newFilter.onPostAdd(this, oldFilterName, entry.getNextFilter());
                }
                catch (Exception e) {
                    entry.setFilter(oldFilter);
                    throw new IoFilterLifeCycleException("onPostAdd(): " + oldFilterName + ':' + newFilter + " in " + this.getSession(), e);
                }
                return oldFilter;
            }
        }
        throw new IllegalArgumentException("Filter not found: " + oldFilterType.getName());
    }
    
    @Override
    public synchronized void clear() throws Exception {
        final List<Entry> l = new ArrayList<Entry>(this.name2entry.values());
        for (final Entry entry : l) {
            try {
                this.deregister((EntryImpl)entry);
            }
            catch (Exception e) {
                throw new IoFilterLifeCycleException("clear(): " + entry.getName() + " in " + this.getSession(), e);
            }
        }
    }
    
    private void register(final EntryImpl prevEntry, final String name, final IoFilter filter) {
        final EntryImpl newEntry = new EntryImpl(prevEntry, prevEntry.nextEntry, name, filter);
        try {
            filter.onPreAdd(this, name, newEntry.getNextFilter());
        }
        catch (Exception e) {
            throw new IoFilterLifeCycleException("onPreAdd(): " + name + ':' + filter + " in " + this.getSession(), e);
        }
        prevEntry.nextEntry.prevEntry = newEntry;
        prevEntry.nextEntry = newEntry;
        this.name2entry.put(name, newEntry);
        try {
            filter.onPostAdd(this, name, newEntry.getNextFilter());
        }
        catch (Exception e) {
            this.deregister0(newEntry);
            throw new IoFilterLifeCycleException("onPostAdd(): " + name + ':' + filter + " in " + this.getSession(), e);
        }
    }
    
    private void deregister(final EntryImpl entry) {
        final IoFilter filter = entry.getFilter();
        try {
            filter.onPreRemove(this, entry.getName(), entry.getNextFilter());
        }
        catch (Exception e) {
            throw new IoFilterLifeCycleException("onPreRemove(): " + entry.getName() + ':' + filter + " in " + this.getSession(), e);
        }
        this.deregister0(entry);
        try {
            filter.onPostRemove(this, entry.getName(), entry.getNextFilter());
        }
        catch (Exception e) {
            throw new IoFilterLifeCycleException("onPostRemove(): " + entry.getName() + ':' + filter + " in " + this.getSession(), e);
        }
    }
    
    private void deregister0(final EntryImpl entry) {
        final EntryImpl prevEntry = entry.prevEntry;
        final EntryImpl nextEntry = entry.nextEntry;
        prevEntry.nextEntry = nextEntry;
        nextEntry.prevEntry = prevEntry;
        this.name2entry.remove(entry.name);
    }
    
    private EntryImpl checkOldName(final String baseName) {
        final EntryImpl e = this.name2entry.get(baseName);
        if (e == null) {
            throw new IllegalArgumentException("Filter not found:" + baseName);
        }
        return e;
    }
    
    private void checkAddable(final String name) {
        if (this.name2entry.containsKey(name)) {
            throw new IllegalArgumentException("Other filter is using the same name '" + name + "'");
        }
    }
    
    @Override
    public void fireSessionCreated() {
        this.callNextSessionCreated(this.head, this.session);
    }
    
    private void callNextSessionCreated(final Entry entry, final IoSession session) {
        try {
            final IoFilter filter = entry.getFilter();
            final IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.sessionCreated(nextFilter, session);
        }
        catch (Exception e) {
            this.fireExceptionCaught(e);
        }
        catch (Error e2) {
            this.fireExceptionCaught(e2);
            throw e2;
        }
    }
    
    @Override
    public void fireSessionOpened() {
        this.callNextSessionOpened(this.head, this.session);
    }
    
    private void callNextSessionOpened(final Entry entry, final IoSession session) {
        try {
            final IoFilter filter = entry.getFilter();
            final IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.sessionOpened(nextFilter, session);
        }
        catch (Exception e) {
            this.fireExceptionCaught(e);
        }
        catch (Error e2) {
            this.fireExceptionCaught(e2);
            throw e2;
        }
    }
    
    @Override
    public void fireSessionClosed() {
        try {
            this.session.getCloseFuture().setClosed();
        }
        catch (Exception e) {
            this.fireExceptionCaught(e);
        }
        catch (Error e2) {
            this.fireExceptionCaught(e2);
            throw e2;
        }
        this.callNextSessionClosed(this.head, this.session);
    }
    
    private void callNextSessionClosed(final Entry entry, final IoSession session) {
        try {
            final IoFilter filter = entry.getFilter();
            final IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.sessionClosed(nextFilter, session);
        }
        catch (Exception e) {
            this.fireExceptionCaught(e);
        }
        catch (Error e2) {
            this.fireExceptionCaught(e2);
        }
    }
    
    @Override
    public void fireSessionIdle(final IdleStatus status) {
        this.session.increaseIdleCount(status, System.currentTimeMillis());
        this.callNextSessionIdle(this.head, this.session, status);
    }
    
    private void callNextSessionIdle(final Entry entry, final IoSession session, final IdleStatus status) {
        try {
            final IoFilter filter = entry.getFilter();
            final IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.sessionIdle(nextFilter, session, status);
        }
        catch (Exception e) {
            this.fireExceptionCaught(e);
        }
        catch (Error e2) {
            this.fireExceptionCaught(e2);
            throw e2;
        }
    }
    
    @Override
    public void fireMessageReceived(final Object message) {
        if (message instanceof IoBuffer) {
            this.session.increaseReadBytes(((IoBuffer)message).remaining(), System.currentTimeMillis());
        }
        this.callNextMessageReceived(this.head, this.session, message);
    }
    
    private void callNextMessageReceived(final Entry entry, final IoSession session, final Object message) {
        try {
            final IoFilter filter = entry.getFilter();
            final IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.messageReceived(nextFilter, session, message);
        }
        catch (Exception e) {
            this.fireExceptionCaught(e);
        }
        catch (Error e2) {
            this.fireExceptionCaught(e2);
            throw e2;
        }
    }
    
    @Override
    public void fireMessageSent(final WriteRequest request) {
        try {
            request.getFuture().setWritten();
        }
        catch (Exception e) {
            this.fireExceptionCaught(e);
        }
        catch (Error e2) {
            this.fireExceptionCaught(e2);
            throw e2;
        }
        if (!request.isEncoded()) {
            this.callNextMessageSent(this.head, this.session, request);
        }
    }
    
    private void callNextMessageSent(final Entry entry, final IoSession session, final WriteRequest writeRequest) {
        try {
            final IoFilter filter = entry.getFilter();
            final IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.messageSent(nextFilter, session, writeRequest);
        }
        catch (Exception e) {
            this.fireExceptionCaught(e);
        }
        catch (Error e2) {
            this.fireExceptionCaught(e2);
            throw e2;
        }
    }
    
    @Override
    public void fireExceptionCaught(final Throwable cause) {
        this.callNextExceptionCaught(this.head, this.session, cause);
    }
    
    private void callNextExceptionCaught(final Entry entry, final IoSession session, final Throwable cause) {
        final ConnectFuture future = (ConnectFuture)session.removeAttribute(DefaultIoFilterChain.SESSION_CREATED_FUTURE);
        if (future == null) {
            try {
                final IoFilter filter = entry.getFilter();
                final IoFilter.NextFilter nextFilter = entry.getNextFilter();
                filter.exceptionCaught(nextFilter, session, cause);
            }
            catch (Throwable e) {
                DefaultIoFilterChain.LOGGER.warn("Unexpected exception from exceptionCaught handler.", e);
            }
        }
        else {
            if (!session.isClosing()) {
                session.closeNow();
            }
            future.setException(cause);
        }
    }
    
    @Override
    public void fireInputClosed() {
        final Entry head = this.head;
        this.callNextInputClosed(head, this.session);
    }
    
    private void callNextInputClosed(final Entry entry, final IoSession session) {
        try {
            final IoFilter filter = entry.getFilter();
            final IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.inputClosed(nextFilter, session);
        }
        catch (Throwable e) {
            this.fireExceptionCaught(e);
        }
    }
    
    @Override
    public void fireFilterWrite(final WriteRequest writeRequest) {
        this.callPreviousFilterWrite(this.tail, this.session, writeRequest);
    }
    
    private void callPreviousFilterWrite(final Entry entry, final IoSession session, final WriteRequest writeRequest) {
        try {
            final IoFilter filter = entry.getFilter();
            final IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.filterWrite(nextFilter, session, writeRequest);
        }
        catch (Exception e) {
            writeRequest.getFuture().setException(e);
            this.fireExceptionCaught(e);
        }
        catch (Error e2) {
            writeRequest.getFuture().setException(e2);
            this.fireExceptionCaught(e2);
            throw e2;
        }
    }
    
    @Override
    public void fireFilterClose() {
        this.callPreviousFilterClose(this.tail, this.session);
    }
    
    private void callPreviousFilterClose(final Entry entry, final IoSession session) {
        try {
            final IoFilter filter = entry.getFilter();
            final IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.filterClose(nextFilter, session);
        }
        catch (Exception e) {
            this.fireExceptionCaught(e);
        }
        catch (Error e2) {
            this.fireExceptionCaught(e2);
            throw e2;
        }
    }
    
    @Override
    public List<Entry> getAll() {
        final List<Entry> list = new ArrayList<Entry>();
        for (EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            list.add(e);
        }
        return list;
    }
    
    @Override
    public List<Entry> getAllReversed() {
        final List<Entry> list = new ArrayList<Entry>();
        for (EntryImpl e = this.tail.prevEntry; e != this.head; e = e.prevEntry) {
            list.add(e);
        }
        return list;
    }
    
    @Override
    public boolean contains(final String name) {
        return this.getEntry(name) != null;
    }
    
    @Override
    public boolean contains(final IoFilter filter) {
        return this.getEntry(filter) != null;
    }
    
    @Override
    public boolean contains(final Class<? extends IoFilter> filterType) {
        return this.getEntry(filterType) != null;
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("{ ");
        boolean empty = true;
        for (EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
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
    
    static {
        SESSION_CREATED_FUTURE = new AttributeKey(DefaultIoFilterChain.class, "connectFuture");
        LOGGER = LoggerFactory.getLogger(DefaultIoFilterChain.class);
    }
    
    private class HeadFilter extends IoFilterAdapter
    {
        @Override
        public void filterWrite(final IoFilter.NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) throws Exception {
            final AbstractIoSession s = (AbstractIoSession)session;
            if (writeRequest.getMessage() instanceof IoBuffer) {
                final IoBuffer buffer = (IoBuffer)writeRequest.getMessage();
                buffer.mark();
                final int remaining = buffer.remaining();
                if (remaining > 0) {
                    s.increaseScheduledWriteBytes(remaining);
                }
            }
            else {
                s.increaseScheduledWriteMessages();
            }
            final WriteRequestQueue writeRequestQueue = s.getWriteRequestQueue();
            if (!s.isWriteSuspended()) {
                if (writeRequestQueue.isEmpty(session)) {
                    s.getProcessor().write(s, writeRequest);
                }
                else {
                    s.getWriteRequestQueue().offer(s, writeRequest);
                    s.getProcessor().flush(s);
                }
            }
            else {
                s.getWriteRequestQueue().offer(s, writeRequest);
            }
        }
        
        @Override
        public void filterClose(final IoFilter.NextFilter nextFilter, final IoSession session) throws Exception {
            ((AbstractIoSession)session).getProcessor().remove(session);
        }
    }
    
    private static class TailFilter extends IoFilterAdapter
    {
        @Override
        public void sessionCreated(final IoFilter.NextFilter nextFilter, final IoSession session) throws Exception {
            try {
                session.getHandler().sessionCreated(session);
            }
            finally {
                final ConnectFuture future = (ConnectFuture)session.removeAttribute(DefaultIoFilterChain.SESSION_CREATED_FUTURE);
                if (future != null) {
                    future.setSession(session);
                }
            }
        }
        
        @Override
        public void sessionOpened(final IoFilter.NextFilter nextFilter, final IoSession session) throws Exception {
            session.getHandler().sessionOpened(session);
        }
        
        @Override
        public void sessionClosed(final IoFilter.NextFilter nextFilter, final IoSession session) throws Exception {
            final AbstractIoSession s = (AbstractIoSession)session;
            try {
                s.getHandler().sessionClosed(session);
            }
            finally {
                try {
                    s.getWriteRequestQueue().dispose(session);
                }
                finally {
                    try {
                        s.getAttributeMap().dispose(session);
                    }
                    finally {
                        try {
                            session.getFilterChain().clear();
                        }
                        finally {
                            if (s.getConfig().isUseReadOperation()) {
                                s.offerClosedReadFuture();
                            }
                        }
                    }
                }
            }
        }
        
        @Override
        public void sessionIdle(final IoFilter.NextFilter nextFilter, final IoSession session, final IdleStatus status) throws Exception {
            session.getHandler().sessionIdle(session, status);
        }
        
        @Override
        public void exceptionCaught(final IoFilter.NextFilter nextFilter, final IoSession session, final Throwable cause) throws Exception {
            final AbstractIoSession s = (AbstractIoSession)session;
            try {
                s.getHandler().exceptionCaught(s, cause);
            }
            finally {
                if (s.getConfig().isUseReadOperation()) {
                    s.offerFailedReadFuture(cause);
                }
            }
        }
        
        @Override
        public void inputClosed(final IoFilter.NextFilter nextFilter, final IoSession session) throws Exception {
            session.getHandler().inputClosed(session);
        }
        
        @Override
        public void messageReceived(final IoFilter.NextFilter nextFilter, final IoSession session, final Object message) throws Exception {
            final AbstractIoSession s = (AbstractIoSession)session;
            if (!(message instanceof IoBuffer)) {
                s.increaseReadMessages(System.currentTimeMillis());
            }
            else if (!((IoBuffer)message).hasRemaining()) {
                s.increaseReadMessages(System.currentTimeMillis());
            }
            if (session.getService() instanceof AbstractIoService) {
                ((AbstractIoService)session.getService()).getStatistics().updateThroughput(System.currentTimeMillis());
            }
            try {
                session.getHandler().messageReceived(s, message);
            }
            finally {
                if (s.getConfig().isUseReadOperation()) {
                    s.offerReadFuture(message);
                }
            }
        }
        
        @Override
        public void messageSent(final IoFilter.NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) throws Exception {
            ((AbstractIoSession)session).increaseWrittenMessages(writeRequest, System.currentTimeMillis());
            if (session.getService() instanceof AbstractIoService) {
                ((AbstractIoService)session.getService()).getStatistics().updateThroughput(System.currentTimeMillis());
            }
            session.getHandler().messageSent(session, writeRequest.getMessage());
        }
        
        @Override
        public void filterWrite(final IoFilter.NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) throws Exception {
            nextFilter.filterWrite(session, writeRequest);
        }
        
        @Override
        public void filterClose(final IoFilter.NextFilter nextFilter, final IoSession session) throws Exception {
            nextFilter.filterClose(session);
        }
    }
    
    private final class EntryImpl implements Entry
    {
        private EntryImpl prevEntry;
        private EntryImpl nextEntry;
        private final String name;
        private IoFilter filter;
        private final IoFilter.NextFilter nextFilter;
        
        private EntryImpl(final EntryImpl prevEntry, final EntryImpl nextEntry, final String name, final IoFilter filter) {
            if (filter == null) {
                throw new IllegalArgumentException("filter");
            }
            if (name == null) {
                throw new IllegalArgumentException("name");
            }
            this.prevEntry = prevEntry;
            this.nextEntry = nextEntry;
            this.name = name;
            this.filter = filter;
            this.nextFilter = new IoFilter.NextFilter() {
                @Override
                public void sessionCreated(final IoSession session) {
                    final Entry nextEntry = EntryImpl.this.nextEntry;
                    DefaultIoFilterChain.this.callNextSessionCreated(nextEntry, session);
                }
                
                @Override
                public void sessionOpened(final IoSession session) {
                    final Entry nextEntry = EntryImpl.this.nextEntry;
                    DefaultIoFilterChain.this.callNextSessionOpened(nextEntry, session);
                }
                
                @Override
                public void sessionClosed(final IoSession session) {
                    final Entry nextEntry = EntryImpl.this.nextEntry;
                    DefaultIoFilterChain.this.callNextSessionClosed(nextEntry, session);
                }
                
                @Override
                public void sessionIdle(final IoSession session, final IdleStatus status) {
                    final Entry nextEntry = EntryImpl.this.nextEntry;
                    DefaultIoFilterChain.this.callNextSessionIdle(nextEntry, session, status);
                }
                
                @Override
                public void exceptionCaught(final IoSession session, final Throwable cause) {
                    final Entry nextEntry = EntryImpl.this.nextEntry;
                    DefaultIoFilterChain.this.callNextExceptionCaught(nextEntry, session, cause);
                }
                
                @Override
                public void inputClosed(final IoSession session) {
                    final Entry nextEntry = EntryImpl.this.nextEntry;
                    DefaultIoFilterChain.this.callNextInputClosed(nextEntry, session);
                }
                
                @Override
                public void messageReceived(final IoSession session, final Object message) {
                    final Entry nextEntry = EntryImpl.this.nextEntry;
                    DefaultIoFilterChain.this.callNextMessageReceived(nextEntry, session, message);
                }
                
                @Override
                public void messageSent(final IoSession session, final WriteRequest writeRequest) {
                    final Entry nextEntry = EntryImpl.this.nextEntry;
                    DefaultIoFilterChain.this.callNextMessageSent(nextEntry, session, writeRequest);
                }
                
                @Override
                public void filterWrite(final IoSession session, final WriteRequest writeRequest) {
                    final Entry nextEntry = EntryImpl.this.prevEntry;
                    DefaultIoFilterChain.this.callPreviousFilterWrite(nextEntry, session, writeRequest);
                }
                
                @Override
                public void filterClose(final IoSession session) {
                    final Entry nextEntry = EntryImpl.this.prevEntry;
                    DefaultIoFilterChain.this.callPreviousFilterClose(nextEntry, session);
                }
                
                @Override
                public String toString() {
                    return EntryImpl.this.nextEntry.name;
                }
            };
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
            if (filter == null) {
                throw new IllegalArgumentException("filter");
            }
            this.filter = filter;
        }
        
        @Override
        public IoFilter.NextFilter getNextFilter() {
            return this.nextFilter;
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("('").append(this.getName()).append('\'');
            sb.append(", prev: '");
            if (this.prevEntry != null) {
                sb.append(this.prevEntry.name);
                sb.append(':');
                sb.append(this.prevEntry.getFilter().getClass().getSimpleName());
            }
            else {
                sb.append("null");
            }
            sb.append("', next: '");
            if (this.nextEntry != null) {
                sb.append(this.nextEntry.name);
                sb.append(':');
                sb.append(this.nextEntry.getFilter().getClass().getSimpleName());
            }
            else {
                sb.append("null");
            }
            sb.append("')");
            return sb.toString();
        }
        
        @Override
        public void addAfter(final String name, final IoFilter filter) {
            DefaultIoFilterChain.this.addAfter(this.getName(), name, filter);
        }
        
        @Override
        public void addBefore(final String name, final IoFilter filter) {
            DefaultIoFilterChain.this.addBefore(this.getName(), name, filter);
        }
        
        @Override
        public void remove() {
            DefaultIoFilterChain.this.remove(this.getName());
        }
        
        @Override
        public void replace(final IoFilter newFilter) {
            DefaultIoFilterChain.this.replace(this.getName(), newFilter);
        }
    }
}
