// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.listener.nio;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.textline.TextLineDecoder;
import java.nio.charset.Charset;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolCodecFactory;

public class FtpServerProtocolCodecFactory implements ProtocolCodecFactory
{
    private final ProtocolDecoder decoder;
    private final ProtocolEncoder encoder;
    
    public FtpServerProtocolCodecFactory() {
        this.decoder = new TextLineDecoder(Charset.forName("UTF-8"));
        this.encoder = new FtpResponseEncoder();
    }
    
    @Override
    public ProtocolDecoder getDecoder(final IoSession session) throws Exception {
        return this.decoder;
    }
    
    @Override
    public ProtocolEncoder getEncoder(final IoSession session) throws Exception {
        return this.encoder;
    }
}
