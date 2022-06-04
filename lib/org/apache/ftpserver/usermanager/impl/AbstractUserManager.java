// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.usermanager.impl;

import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.usermanager.Md5PasswordEncryptor;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.ftplet.UserManager;

public abstract class AbstractUserManager implements UserManager
{
    public static final String ATTR_LOGIN = "userid";
    public static final String ATTR_PASSWORD = "userpassword";
    public static final String ATTR_HOME = "homedirectory";
    public static final String ATTR_WRITE_PERM = "writepermission";
    public static final String ATTR_ENABLE = "enableflag";
    public static final String ATTR_MAX_IDLE_TIME = "idletime";
    public static final String ATTR_MAX_UPLOAD_RATE = "uploadrate";
    public static final String ATTR_MAX_DOWNLOAD_RATE = "downloadrate";
    public static final String ATTR_MAX_LOGIN_NUMBER = "maxloginnumber";
    public static final String ATTR_MAX_LOGIN_PER_IP = "maxloginperip";
    private final String adminName;
    private final PasswordEncryptor passwordEncryptor;
    
    public AbstractUserManager() {
        this(null, new Md5PasswordEncryptor());
    }
    
    public AbstractUserManager(final String adminName, final PasswordEncryptor passwordEncryptor) {
        this.adminName = adminName;
        this.passwordEncryptor = passwordEncryptor;
    }
    
    @Override
    public String getAdminName() {
        return this.adminName;
    }
    
    @Override
    public boolean isAdmin(final String login) throws FtpException {
        return this.adminName.equals(login);
    }
    
    public PasswordEncryptor getPasswordEncryptor() {
        return this.passwordEncryptor;
    }
}
