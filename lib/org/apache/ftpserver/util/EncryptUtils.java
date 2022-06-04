// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.util;

import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;

public class EncryptUtils
{
    public static final byte[] encrypt(final byte[] source, final String algorithm) throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance(algorithm);
        md.reset();
        md.update(source);
        return md.digest();
    }
    
    public static final String encrypt(final String source, final String algorithm) throws NoSuchAlgorithmException {
        final byte[] resByteArray = encrypt(source.getBytes(), algorithm);
        return StringUtils.toHexString(resByteArray);
    }
    
    public static final String encryptMD5(String source) {
        if (source == null) {
            source = "";
        }
        String result = "";
        try {
            result = encrypt(source, "MD5");
        }
        catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }
    
    public static final String encryptSHA(String source) {
        if (source == null) {
            source = "";
        }
        String result = "";
        try {
            result = encrypt(source, "SHA");
        }
        catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }
}
