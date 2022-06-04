// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.usermanager;

import org.apache.ftpserver.usermanager.impl.PropertiesUserManager;
import org.apache.ftpserver.ftplet.UserManager;
import java.net.URL;
import java.io.File;

public class PropertiesUserManagerFactory implements UserManagerFactory
{
    private String adminName;
    private File userDataFile;
    private URL userDataURL;
    private PasswordEncryptor passwordEncryptor;
    
    public PropertiesUserManagerFactory() {
        this.adminName = "admin";
        this.passwordEncryptor = new Md5PasswordEncryptor();
    }
    
    @Override
    public UserManager createUserManager() {
        if (this.userDataURL != null) {
            return new PropertiesUserManager(this.passwordEncryptor, this.userDataURL, this.adminName);
        }
        return new PropertiesUserManager(this.passwordEncryptor, this.userDataFile, this.adminName);
    }
    
    public String getAdminName() {
        return this.adminName;
    }
    
    public void setAdminName(final String adminName) {
        this.adminName = adminName;
    }
    
    public File getFile() {
        return this.userDataFile;
    }
    
    public void setFile(final File propFile) {
        this.userDataFile = propFile;
    }
    
    public URL getUrl() {
        return this.userDataURL;
    }
    
    public void setUrl(final URL userDataURL) {
        this.userDataURL = userDataURL;
    }
    
    public PasswordEncryptor getPasswordEncryptor() {
        return this.passwordEncryptor;
    }
    
    public void setPasswordEncryptor(final PasswordEncryptor passwordEncryptor) {
        this.passwordEncryptor = passwordEncryptor;
    }
}
