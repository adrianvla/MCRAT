// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.filter.codec;

import org.apache.mina.core.write.NothingWrittenException;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.write.WriteRequestWrapper;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.slf4j.LoggerFactory;
import java.net.SocketAddress;
import java.util.Queue;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.apache.mina.core.filterchain.IoFilterAdapter;

public class ProtocolCodecFilter extends IoFilterAdapter
{
    private static final Logger LOGGER;
    private static final Class<?>[] EMPTY_PARAMS;
    private static final IoBuffer EMPTY_BUFFER;
    private static final AttributeKey ENCODER;
    private static final AttributeKey DECODER;
    private static final AttributeKey DECODER_OUT;
    private static final AttributeKey ENCODER_OUT;
    private final ProtocolCodecFactory factory;
    
    public ProtocolCodecFilter(final ProtocolCodecFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory");
        }
        this.factory = factory;
    }
    
    public ProtocolCodecFilter(final ProtocolEncoder encoder, final ProtocolDecoder decoder) {
        if (encoder == null) {
            throw new IllegalArgumentException("encoder");
        }
        if (decoder == null) {
            throw new IllegalArgumentException("decoder");
        }
        this.factory = new ProtocolCodecFactory() {
            @Override
            public ProtocolEncoder getEncoder(final IoSession session) {
                return encoder;
            }
            
            @Override
            public ProtocolDecoder getDecoder(final IoSession session) {
                return decoder;
            }
        };
    }
    
    public ProtocolCodecFilter(final Class<? extends ProtocolEncoder> encoderClass, final Class<? extends ProtocolDecoder> decoderClass) {
        if (encoderClass == null) {
            throw new IllegalArgumentException("encoderClass");
        }
        if (decoderClass == null) {
            throw new IllegalArgumentException("decoderClass");
        }
        if (!ProtocolEncoder.class.isAssignableFrom(encoderClass)) {
            throw new IllegalArgumentException("encoderClass: " + encoderClass.getName());
        }
        if (!ProtocolDecoder.class.isAssignableFrom(decoderClass)) {
            throw new IllegalArgumentException("decoderClass: " + decoderClass.getName());
        }
        try {
            encoderClass.getConstructor(ProtocolCodecFilter.EMPTY_PARAMS);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("encoderClass doesn't have a public default constructor.");
        }
        try {
            decoderClass.getConstructor(ProtocolCodecFilter.EMPTY_PARAMS);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("decoderClass doesn't have a public default constructor.");
        }
        ProtocolEncoder encoder;
        try {
            encoder = (ProtocolEncoder)encoderClass.newInstance();
        }
        catch (Exception e2) {
            throw new IllegalArgumentException("encoderClass cannot be initialized");
        }
        ProtocolDecoder decoder;
        try {
            decoder = (ProtocolDecoder)decoderClass.newInstance();
        }
        catch (Exception e3) {
            throw new IllegalArgumentException("decoderClass cannot be initialized");
        }
        this.factory = new ProtocolCodecFactory() {
            @Override
            public ProtocolEncoder getEncoder(final IoSession session) throws Exception {
                return encoder;
            }
            
            @Override
            public ProtocolDecoder getDecoder(final IoSession session) throws Exception {
                return decoder;
            }
        };
    }
    
    public ProtocolEncoder getEncoder(final IoSession session) {
        return (ProtocolEncoder)session.getAttribute(ProtocolCodecFilter.ENCODER);
    }
    
    @Override
    public void onPreAdd(final IoFilterChain parent, final String name, final IoFilter.NextFilter nextFilter) throws Exception {
        if (parent.contains(this)) {
            throw new IllegalArgumentException("You can't add the same filter instance more than once.  Create another instance and add it.");
        }
    }
    
    @Override
    public void onPostRemove(final IoFilterChain parent, final String name, final IoFilter.NextFilter nextFilter) throws Exception {
        this.disposeCodec(parent.getSession());
    }
    
    @Override
    public void messageReceived(final IoFilter.NextFilter nextFilter, final IoSession session, final Object message) throws Exception {
        ProtocolCodecFilter.LOGGER.debug("Processing a MESSAGE_RECEIVED for session {}", (Object)session.getId());
        if (!(message instanceof IoBuffer)) {
            nextFilter.messageReceived(session, message);
            return;
        }
        final IoBuffer in = (IoBuffer)message;
        final ProtocolDecoder decoder = this.factory.getDecoder(session);
        final ProtocolDecoderOutput decoderOut = this.getDecoderOut(session, nextFilter);
        while (in.hasRemaining()) {
            final int oldPos = in.position();
            try {
                synchronized (session) {
                    decoder.decode(session, in, decoderOut);
                }
                decoderOut.flush(nextFilter, session);
            }
            catch (Exception e) {
                ProtocolDecoderException pde;
                if (e instanceof ProtocolDecoderException) {
                    pde = (ProtocolDecoderException)e;
                }
                else {
                    pde = new ProtocolDecoderException(e);
                }
                if (pde.getHexdump() == null) {
                    final int curPos = in.position();
                    in.position(oldPos);
                    pde.setHexdump(in.getHexDump());
                    in.position(curPos);
                }
                decoderOut.flush(nextFilter, session);
                nextFilter.exceptionCaught(session, pde);
                if (!(e instanceof RecoverableProtocolDecoderException) || in.position() == oldPos) {
                    break;
                }
                continue;
            }
        }
    }
    
    @Override
    public void messageSent(final IoFilter.NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) throws Exception {
        if (writeRequest instanceof EncodedWriteRequest) {
            return;
        }
        if (writeRequest instanceof MessageWriteRequest) {
            final MessageWriteRequest wrappedRequest = (MessageWriteRequest)writeRequest;
            nextFilter.messageSent(session, wrappedRequest.getParentRequest());
        }
        else {
            nextFilter.messageSent(session, writeRequest);
        }
    }
    
    @Override
    public void filterWrite(final IoFilter.NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) throws Exception {
        final Object message = writeRequest.getMessage();
        if (message instanceof IoBuffer || message instanceof FileRegion) {
            nextFilter.filterWrite(session, writeRequest);
            return;
        }
        final ProtocolEncoder encoder = this.factory.getEncoder(session);
        final ProtocolEncoderOutput encoderOut = this.getEncoderOut(session, nextFilter, writeRequest);
        if (encoder == null) {
            throw new ProtocolEncoderException("The encoder is null for the session " + session);
        }
        try {
            encoder.encode(session, message, encoderOut);
            final Queue<Object> bufferQueue = ((AbstractProtocolEncoderOutput)encoderOut).getMessageQueue();
            while (!bufferQueue.isEmpty()) {
                final Object encodedMessage = bufferQueue.poll();
                if (encodedMessage == null) {
                    break;
                }
                if (encodedMessage instanceof IoBuffer && !((IoBuffer)encodedMessage).hasRemaining()) {
                    continue;
                }
                final SocketAddress destination = writeRequest.getDestination();
                final WriteRequest encodedWriteRequest = new EncodedWriteRequest(encodedMessage, null, destination);
                nextFilter.filterWrite(session, encodedWriteRequest);
            }
            nextFilter.filterWrite(session, new MessageWriteRequest(writeRequest));
        }
        catch (Exception e) {
            ProtocolEncoderException pee;
            if (e instanceof ProtocolEncoderException) {
                pee = (ProtocolEncoderException)e;
            }
            else {
                pee = new ProtocolEncoderException(e);
            }
            throw pee;
        }
    }
    
    @Override
    public void sessionClosed(final IoFilter.NextFilter nextFilter, final IoSession session) throws Exception {
        final ProtocolDecoder decoder = this.factory.getDecoder(session);
        final ProtocolDecoderOutput decoderOut = this.getDecoderOut(session, nextFilter);
        try {
            decoder.finishDecode(session, decoderOut);
        }
        catch (Exception e) {
            ProtocolDecoderException pde;
            if (e instanceof ProtocolDecoderException) {
                pde = (ProtocolDecoderException)e;
            }
            else {
                pde = new ProtocolDecoderException(e);
            }
            throw pde;
        }
        finally {
            this.disposeCodec(session);
            decoderOut.flush(nextFilter, session);
        }
        nextFilter.sessionClosed(session);
    }
    
    private void disposeCodec(final IoSession session) {
        this.disposeEncoder(session);
        this.disposeDecoder(session);
        this.disposeDecoderOut(session);
    }
    
    private void disposeEncoder(final IoSession session) {
        final ProtocolEncoder encoder = (ProtocolEncoder)session.removeAttribute(ProtocolCodecFilter.ENCODER);
        if (encoder == null) {
            return;
        }
        try {
            encoder.dispose(session);
        }
        catch (Exception e) {
            ProtocolCodecFilter.LOGGER.warn("Failed to dispose: " + encoder.getClass().getName() + " (" + encoder + ')');
        }
    }
    
    private void disposeDecoder(final IoSession session) {
        final ProtocolDecoder decoder = (ProtocolDecoder)session.removeAttribute(ProtocolCodecFilter.DECODER);
        if (decoder == null) {
            return;
        }
        try {
            decoder.dispose(session);
        }
        catch (Exception e) {
            ProtocolCodecFilter.LOGGER.warn("Failed to dispose: " + decoder.getClass().getName() + " (" + decoder + ')');
        }
    }
    
    private ProtocolDecoderOutput getDecoderOut(final IoSession session, final IoFilter.NextFilter nextFilter) {
        ProtocolDecoderOutput out = (ProtocolDecoderOutput)session.getAttribute(ProtocolCodecFilter.DECODER_OUT);
        if (out == null) {
            out = new ProtocolDecoderOutputImpl();
            session.setAttribute(ProtocolCodecFilter.DECODER_OUT, out);
        }
        return out;
    }
    
    private ProtocolEncoderOutput getEncoderOut(final IoSession session, final IoFilter.NextFilter nextFilter, final WriteRequest writeRequest) {
        ProtocolEncoderOutput out = (ProtocolEncoderOutput)session.getAttribute(ProtocolCodecFilter.ENCODER_OUT);
        if (out == null) {
            out = new ProtocolEncoderOutputImpl(session, nextFilter, writeRequest);
            session.setAttribute(ProtocolCodecFilter.ENCODER_OUT, out);
        }
        return out;
    }
    
    private void disposeDecoderOut(final IoSession session) {
        session.removeAttribute(ProtocolCodecFilter.DECODER_OUT);
    }
    
    static {
        LOGGER = LoggerFactory.getLogger(ProtocolCodecFilter.class);
        EMPTY_PARAMS = new Class[0];
        EMPTY_BUFFER = IoBuffer.wrap(new byte[0]);
        ENCODER = new AttributeKey(ProtocolCodecFilter.class, "encoder");
        DECODER = new AttributeKey(ProtocolCodecFilter.class, "decoder");
        DECODER_OUT = new AttributeKey(ProtocolCodecFilter.class, "decoderOut");
        ENCODER_OUT = new AttributeKey(ProtocolCodecFilter.class, "encoderOut");
    }
    
    private static class EncodedWriteRequest extends DefaultWriteRequest
    {
        public EncodedWriteRequest(final Object encodedMessage, final WriteFuture future, final SocketAddress destination) {
            super(encodedMessage, future, destination);
        }
        
        @Override
        public boolean isEncoded() {
            return true;
        }
    }
    
    private static class MessageWriteRequest extends WriteRequestWrapper
    {
        public MessageWriteRequest(final WriteRequest writeRequest) {
            super(writeRequest);
        }
        
        @Override
        public Object getMessage() {
            return ProtocolCodecFilter.EMPTY_BUFFER;
        }
        
        @Override
        public String toString() {
            return "MessageWriteRequest, parent : " + super.toString();
        }
    }
    
    private static class ProtocolDecoderOutputImpl extends AbstractProtocolDecoderOutput
    {
        public ProtocolDecoderOutputImpl() {
        }
        
        @Override
        public void flush(final IoFilter.NextFilter nextFilter, final IoSession session) {
            final Queue<Object> messageQueue = this.getMessageQueue();
            while (!messageQueue.isEmpty()) {
                nextFilter.messageReceived(session, messageQueue.poll());
            }
        }
    }
    
    private static class ProtocolEncoderOutputImpl extends AbstractProtocolEncoderOutput
    {
        private final IoSession session;
        private final IoFilter.NextFilter nextFilter;
        private final SocketAddress destination;
        
        public ProtocolEncoderOutputImpl(final IoSession session, final IoFilter.NextFilter nextFilter, final WriteRequest writeRequest) {
            this.session = session;
            this.nextFilter = nextFilter;
            this.destination = writeRequest.getDestination();
        }
        
        @Override
        public WriteFuture flush() {
            final Queue<Object> bufferQueue = this.getMessageQueue();
            WriteFuture future = null;
            while (!bufferQueue.isEmpty()) {
                final Object encodedMessage = bufferQueue.poll();
                if (encodedMessage == null) {
                    break;
                }
                if (encodedMessage instanceof IoBuffer && !((IoBuffer)encodedMessage).hasRemaining()) {
                    continue;
                }
                future = new DefaultWriteFuture(this.session);
                this.nextFilter.filterWrite(this.session, new EncodedWriteRequest(encodedMessage, future, this.destination));
            }
            if (future == null) {
                future = DefaultWriteFuture.newNotWrittenFuture(this.session, new NothingWrittenException(AbstractIoSession.MESSAGE_SENT_REQUEST));
            }
            return future;
        }
    }
}
