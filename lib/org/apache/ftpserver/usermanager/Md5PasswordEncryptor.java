// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.usermanager;

import org.apache.ftpserver.util.EncryptUtils;

public class Md5PasswordEncryptor implements PasswordEncryptor
{
    @Override
    public String encrypt(final String password) {
        return EncryptUtils.encryptMD5(password);
    }
    
    @Override
    public boolean matches(final String passwordToCheck, final String storedPassword) {
        if (storedPassword == null) {
            throw new NullPointerException("storedPassword can not be null");
        }
        if (passwordToCheck == null) {
            throw new NullPointerException("passwordToCheck can not be null");
        }
        return this.encrypt(passwordToCheck).equalsIgnoreCase(storedPassword);
    }
}
