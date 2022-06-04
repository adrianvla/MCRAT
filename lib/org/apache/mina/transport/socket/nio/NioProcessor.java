// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.transport.socket.nio;

import org.apache.mina.core.session.AbstractIoSession;
import java.nio.channels.WritableByteChannel;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.SessionState;
import java.nio.channels.SocketChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;
import java.util.Set;
import java.util.Iterator;
import java.io.IOException;
import org.apache.mina.core.RuntimeIoException;
import java.util.concurrent.Executor;
import java.nio.channels.spi.SelectorProvider;
import java.nio.channels.Selector;
import org.apache.mina.core.polling.AbstractPollingIoProcessor;

public final class NioProcessor extends AbstractPollingIoProcessor<NioSession>
{
    private Selector selector;
    private SelectorProvider selectorProvider;
    
    public NioProcessor(final Executor executor) {
        super(executor);
        this.selectorProvider = null;
        try {
            this.selector = Selector.open();
        }
        catch (IOException e) {
            throw new RuntimeIoException("Failed to open a selector.", e);
        }
    }
    
    public NioProcessor(final Executor executor, final SelectorProvider selectorProvider) {
        super(executor);
        this.selectorProvider = null;
        try {
            if (selectorProvider == null) {
                this.selector = Selector.open();
            }
            else {
                this.selector = selectorProvider.openSelector();
            }
        }
        catch (IOException e) {
            throw new RuntimeIoException("Failed to open a selector.", e);
        }
    }
    
    @Override
    protected void doDispose() throws Exception {
        this.selector.close();
    }
    
    @Override
    protected int select(final long timeout) throws Exception {
        return this.selector.select(timeout);
    }
    
    @Override
    protected int select() throws Exception {
        return this.selector.select();
    }
    
    @Override
    protected boolean isSelectorEmpty() {
        return this.selector.keys().isEmpty();
    }
    
    @Override
    protected void wakeup() {
        this.wakeupCalled.getAndSet(true);
        this.selector.wakeup();
    }
    
    @Override
    protected Iterator<NioSession> allSessions() {
        return new IoSessionIterator<NioSession>((Set)this.selector.keys());
    }
    
    @Override
    protected Iterator<NioSession> selectedSessions() {
        return new IoSessionIterator<NioSession>((Set)this.selector.selectedKeys());
    }
    
    @Override
    protected void init(final NioSession session) throws Exception {
        final SelectableChannel ch = (SelectableChannel)session.getChannel();
        ch.configureBlocking(false);
        session.setSelectionKey(ch.register(this.selector, 1, session));
    }
    
    @Override
    protected void destroy(final NioSession session) throws Exception {
        final ByteChannel ch = session.getChannel();
        final SelectionKey key = session.getSelectionKey();
        if (key != null) {
            key.cancel();
        }
        if (ch.isOpen()) {
            ch.close();
        }
    }
    
    @Override
    protected void registerNewSelector() throws IOException {
        synchronized (this.selector) {
            final Set<SelectionKey> keys = this.selector.keys();
            Selector newSelector = null;
            if (this.selectorProvider == null) {
                newSelector = Selector.open();
            }
            else {
                newSelector = this.selectorProvider.openSelector();
            }
            for (final SelectionKey key : keys) {
                final SelectableChannel ch = key.channel();
                final NioSession session = (NioSession)key.attachment();
                final SelectionKey newKey = ch.register(newSelector, key.interestOps(), session);
                session.setSelectionKey(newKey);
            }
            this.selector.close();
            this.selector = newSelector;
        }
    }
    
    @Override
    protected boolean isBrokenConnection() throws IOException {
        boolean brokenSession = false;
        synchronized (this.selector) {
            final Set<SelectionKey> keys = this.selector.keys();
            for (final SelectionKey key : keys) {
                final SelectableChannel channel = key.channel();
                if ((channel instanceof DatagramChannel && !((DatagramChannel)channel).isConnected()) || (channel instanceof SocketChannel && !((SocketChannel)channel).isConnected())) {
                    key.cancel();
                    brokenSession = true;
                }
            }
        }
        return brokenSession;
    }
    
    @Override
    protected SessionState getState(final NioSession session) {
        final SelectionKey key = session.getSelectionKey();
        if (key == null) {
            return SessionState.OPENING;
        }
        if (key.isValid()) {
            return SessionState.OPENED;
        }
        return SessionState.CLOSING;
    }
    
    @Override
    protected boolean isReadable(final NioSession session) {
        final SelectionKey key = session.getSelectionKey();
        return key != null && key.isValid() && key.isReadable();
    }
    
    @Override
    protected boolean isWritable(final NioSession session) {
        final SelectionKey key = session.getSelectionKey();
        return key != null && key.isValid() && key.isWritable();
    }
    
    @Override
    protected boolean isInterestedInRead(final NioSession session) {
        final SelectionKey key = session.getSelectionKey();
        return key != null && key.isValid() && (key.interestOps() & 0x1) != 0x0;
    }
    
    @Override
    protected boolean isInterestedInWrite(final NioSession session) {
        final SelectionKey key = session.getSelectionKey();
        return key != null && key.isValid() && (key.interestOps() & 0x4) != 0x0;
    }
    
    @Override
    protected void setInterestedInRead(final NioSession session, final boolean isInterested) throws Exception {
        final SelectionKey key = session.getSelectionKey();
        if (key == null || !key.isValid()) {
            return;
        }
        int newInterestOps;
        final int oldInterestOps = newInterestOps = key.interestOps();
        if (isInterested) {
            newInterestOps |= 0x1;
        }
        else {
            newInterestOps &= 0xFFFFFFFE;
        }
        if (oldInterestOps != newInterestOps) {
            key.interestOps(newInterestOps);
        }
    }
    
    @Override
    protected void setInterestedInWrite(final NioSession session, final boolean isInterested) throws Exception {
        final SelectionKey key = session.getSelectionKey();
        if (key == null || !key.isValid()) {
            return;
        }
        int newInterestOps = key.interestOps();
        if (isInterested) {
            newInterestOps |= 0x4;
        }
        else {
            newInterestOps &= 0xFFFFFFFB;
        }
        key.interestOps(newInterestOps);
    }
    
    @Override
    protected int read(final NioSession session, final IoBuffer buf) throws Exception {
        final ByteChannel channel = session.getChannel();
        return channel.read(buf.buf());
    }
    
    @Override
    protected int write(final NioSession session, final IoBuffer buf, final int length) throws IOException {
        if (buf.remaining() <= length) {
            return session.getChannel().write(buf.buf());
        }
        final int oldLimit = buf.limit();
        buf.limit(buf.position() + length);
        try {
            return session.getChannel().write(buf.buf());
        }
        finally {
            buf.limit(oldLimit);
        }
    }
    
    @Override
    protected int transferFile(final NioSession session, final FileRegion region, final int length) throws Exception {
        try {
            return (int)region.getFileChannel().transferTo(region.getPosition(), length, session.getChannel());
        }
        catch (IOException e) {
            final String message = e.getMessage();
            if (message != null && message.contains("temporarily unavailable")) {
                return 0;
            }
            throw e;
        }
    }
    
    protected static class IoSessionIterator<NioSession> implements Iterator<NioSession>
    {
        private final Iterator<SelectionKey> iterator;
        
        private IoSessionIterator(final Set<SelectionKey> keys) {
            this.iterator = keys.iterator();
        }
        
        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }
        
        @Override
        public NioSession next() {
            final SelectionKey key = this.iterator.next();
            final NioSession nioSession = (NioSession)key.attachment();
            return nioSession;
        }
        
        @Override
        public void remove() {
            this.iterator.remove();
        }
    }
}
