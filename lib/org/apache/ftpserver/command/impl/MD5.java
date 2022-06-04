// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl;

import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.io.IOException;
import java.io.InputStream;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.util.IoUtils;
import java.security.NoSuchAlgorithmException;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.FtpIoSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.ftpserver.command.AbstractCommand;

public class MD5 extends AbstractCommand
{
    private final Logger LOG;
    private static final char[] DIGITS;
    
    public MD5() {
        this.LOG = LoggerFactory.getLogger(MD5.class);
    }
    
    @Override
    public void execute(final FtpIoSession session, final FtpServerContext context, final FtpRequest request) throws IOException {
        session.resetState();
        boolean isMMD5 = false;
        if ("MMD5".equals(request.getCommand())) {
            isMMD5 = true;
        }
        final String argument = request.getArgument();
        if (argument == null || argument.trim().length() == 0) {
            session.write(LocalizedFtpReply.translate(session, request, context, 504, "MD5.invalid", null));
            return;
        }
        String[] fileNames = null;
        if (isMMD5) {
            fileNames = argument.split(",");
        }
        else {
            fileNames = new String[] { argument };
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fileNames.length; ++i) {
            final String fileName = fileNames[i].trim();
            FtpFile file = null;
            try {
                file = session.getFileSystemView().getFile(fileName);
            }
            catch (Exception ex) {
                this.LOG.debug("Exception getting the file object: " + fileName, ex);
            }
            if (file == null) {
                session.write(LocalizedFtpReply.translate(session, request, context, 504, "MD5.invalid", fileName));
                return;
            }
            if (!file.isFile()) {
                session.write(LocalizedFtpReply.translate(session, request, context, 504, "MD5.invalid", fileName));
                return;
            }
            InputStream is = null;
            try {
                is = file.createInputStream(0L);
                final String md5Hash = this.md5(is);
                if (i > 0) {
                    sb.append(", ");
                }
                final boolean nameHasSpaces = fileName.indexOf(32) >= 0;
                if (nameHasSpaces) {
                    sb.append('\"');
                }
                sb.append(fileName);
                if (nameHasSpaces) {
                    sb.append('\"');
                }
                sb.append(' ');
                sb.append(md5Hash);
            }
            catch (NoSuchAlgorithmException e) {
                this.LOG.debug("MD5 algorithm not available", e);
                session.write(LocalizedFtpReply.translate(session, request, context, 502, "MD5.notimplemened", null));
            }
            finally {
                IoUtils.close(is);
            }
        }
        if (isMMD5) {
            session.write(LocalizedFtpReply.translate(session, request, context, 252, "MMD5", sb.toString()));
        }
        else {
            session.write(LocalizedFtpReply.translate(session, request, context, 251, "MD5", sb.toString()));
        }
    }
    
    private String md5(final InputStream is) throws IOException, NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("MD5");
        final DigestInputStream dis = new DigestInputStream(is, digest);
        final byte[] buffer = new byte[1024];
        for (int read = dis.read(buffer); read > -1; read = dis.read(buffer)) {}
        return new String(encodeHex(dis.getMessageDigest().digest()));
    }
    
    public static char[] encodeHex(final byte[] data) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        int i = 0;
        int j = 0;
        while (i < l) {
            out[j++] = MD5.DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = MD5.DIGITS[0xF & data[i]];
            ++i;
        }
        return out;
    }
    
    static {
        DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    }
}
