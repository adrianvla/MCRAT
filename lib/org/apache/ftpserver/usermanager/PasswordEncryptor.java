// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.usermanager;

public interface PasswordEncryptor
{
    String encrypt(final String p0);
    
    boolean matches(final String p0, final String p1);
}
