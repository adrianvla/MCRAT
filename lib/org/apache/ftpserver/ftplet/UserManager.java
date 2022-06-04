// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

public interface UserManager
{
    User getUserByName(final String p0) throws FtpException;
    
    String[] getAllUserNames() throws FtpException;
    
    void delete(final String p0) throws FtpException;
    
    void save(final User p0) throws FtpException;
    
    boolean doesExist(final String p0) throws FtpException;
    
    User authenticate(final Authentication p0) throws AuthenticationFailedException;
    
    String getAdminName() throws FtpException;
    
    boolean isAdmin(final String p0) throws FtpException;
}
