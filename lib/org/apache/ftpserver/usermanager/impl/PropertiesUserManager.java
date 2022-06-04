// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.usermanager.impl;

import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.Authority;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.ArrayList;
import java.io.OutputStream;
import java.io.FileOutputStream;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.User;
import java.io.IOException;
import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.util.IoUtils;
import java.io.InputStream;
import java.io.FileInputStream;
import org.slf4j.LoggerFactory;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import java.net.URL;
import java.io.File;
import org.apache.ftpserver.util.BaseProperties;
import org.slf4j.Logger;

public class PropertiesUserManager extends AbstractUserManager
{
    private final Logger LOG;
    private static final String PREFIX = "ftpserver.user.";
    private BaseProperties userDataProp;
    private File userDataFile;
    private URL userUrl;
    
    public PropertiesUserManager(final PasswordEncryptor passwordEncryptor, final File userDataFile, final String adminName) {
        super(adminName, passwordEncryptor);
        this.LOG = LoggerFactory.getLogger(PropertiesUserManager.class);
        this.loadFromFile(userDataFile);
    }
    
    public PropertiesUserManager(final PasswordEncryptor passwordEncryptor, final URL userDataPath, final String adminName) {
        super(adminName, passwordEncryptor);
        this.LOG = LoggerFactory.getLogger(PropertiesUserManager.class);
        this.loadFromUrl(userDataPath);
    }
    
    private void loadFromFile(final File userDataFile) {
        try {
            this.userDataProp = new BaseProperties();
            if (userDataFile != null) {
                this.LOG.debug("File configured, will try loading");
                if (userDataFile.exists()) {
                    this.userDataFile = userDataFile;
                    this.LOG.debug("File found on file system");
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(userDataFile);
                        this.userDataProp.load(fis);
                    }
                    finally {
                        IoUtils.close(fis);
                    }
                }
                else {
                    this.LOG.debug("File not found on file system, try loading from classpath");
                    final InputStream is = this.getClass().getClassLoader().getResourceAsStream(userDataFile.getPath());
                    if (is == null) {
                        throw new FtpServerConfigurationException("User data file specified but could not be located, neither on the file system or in the classpath: " + userDataFile.getPath());
                    }
                    try {
                        this.userDataProp.load(is);
                    }
                    finally {
                        IoUtils.close(is);
                    }
                }
            }
        }
        catch (IOException e) {
            throw new FtpServerConfigurationException("Error loading user data file : " + userDataFile, e);
        }
    }
    
    private void loadFromUrl(final URL userDataPath) {
        try {
            this.userDataProp = new BaseProperties();
            if (userDataPath != null) {
                this.LOG.debug("URL configured, will try loading");
                this.userUrl = userDataPath;
                InputStream is = null;
                is = userDataPath.openStream();
                try {
                    this.userDataProp.load(is);
                }
                finally {
                    IoUtils.close(is);
                }
            }
        }
        catch (IOException e) {
            throw new FtpServerConfigurationException("Error loading user data resource : " + userDataPath, e);
        }
    }
    
    public void refresh() {
        synchronized (this.userDataProp) {
            if (this.userDataFile != null) {
                this.LOG.debug("Refreshing user manager using file: " + this.userDataFile.getAbsolutePath());
                this.loadFromFile(this.userDataFile);
            }
            else {
                this.LOG.debug("Refreshing user manager using URL: " + this.userUrl.toString());
                this.loadFromUrl(this.userUrl);
            }
        }
    }
    
    public File getFile() {
        return this.userDataFile;
    }
    
    @Override
    public synchronized void save(final User usr) throws FtpException {
        if (usr.getName() == null) {
            throw new NullPointerException("User name is null.");
        }
        final String thisPrefix = "ftpserver.user." + usr.getName() + '.';
        this.userDataProp.setProperty(thisPrefix + "userpassword", this.getPassword(usr));
        String home = usr.getHomeDirectory();
        if (home == null) {
            home = "/";
        }
        this.userDataProp.setProperty(thisPrefix + "homedirectory", home);
        this.userDataProp.setProperty(thisPrefix + "enableflag", usr.getEnabled());
        this.userDataProp.setProperty(thisPrefix + "writepermission", usr.authorize(new WriteRequest()) != null);
        this.userDataProp.setProperty(thisPrefix + "idletime", usr.getMaxIdleTime());
        TransferRateRequest transferRateRequest = new TransferRateRequest();
        transferRateRequest = (TransferRateRequest)usr.authorize(transferRateRequest);
        if (transferRateRequest != null) {
            this.userDataProp.setProperty(thisPrefix + "uploadrate", transferRateRequest.getMaxUploadRate());
            this.userDataProp.setProperty(thisPrefix + "downloadrate", transferRateRequest.getMaxDownloadRate());
        }
        else {
            this.userDataProp.remove(thisPrefix + "uploadrate");
            this.userDataProp.remove(thisPrefix + "downloadrate");
        }
        ConcurrentLoginRequest concurrentLoginRequest = new ConcurrentLoginRequest(0, 0);
        concurrentLoginRequest = (ConcurrentLoginRequest)usr.authorize(concurrentLoginRequest);
        if (concurrentLoginRequest != null) {
            this.userDataProp.setProperty(thisPrefix + "maxloginnumber", concurrentLoginRequest.getMaxConcurrentLogins());
            this.userDataProp.setProperty(thisPrefix + "maxloginperip", concurrentLoginRequest.getMaxConcurrentLoginsPerIP());
        }
        else {
            this.userDataProp.remove(thisPrefix + "maxloginnumber");
            this.userDataProp.remove(thisPrefix + "maxloginperip");
        }
        this.saveUserData();
    }
    
    private void saveUserData() throws FtpException {
        if (this.userDataFile == null) {
            return;
        }
        final File dir = this.userDataFile.getAbsoluteFile().getParentFile();
        if (dir != null && !dir.exists() && !dir.mkdirs()) {
            final String dirName = dir.getAbsolutePath();
            throw new FtpServerConfigurationException("Cannot create directory for user data file : " + dirName);
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(this.userDataFile);
            this.userDataProp.store(fos, "Generated file - don't edit (please)");
        }
        catch (IOException ex) {
            this.LOG.error("Failed saving user data", ex);
            throw new FtpException("Failed saving user data", ex);
        }
        finally {
            IoUtils.close(fos);
        }
    }
    
    @Override
    public void delete(final String usrName) throws FtpException {
        final String thisPrefix = "ftpserver.user." + usrName + '.';
        final Enumeration<?> propNames = this.userDataProp.propertyNames();
        final ArrayList<String> remKeys = new ArrayList<String>();
        while (propNames.hasMoreElements()) {
            final String thisKey = propNames.nextElement().toString();
            if (thisKey.startsWith(thisPrefix)) {
                remKeys.add(thisKey);
            }
        }
        final Iterator<String> remKeysIt = remKeys.iterator();
        while (remKeysIt.hasNext()) {
            this.userDataProp.remove(remKeysIt.next());
        }
        this.saveUserData();
    }
    
    private String getPassword(final User usr) {
        final String name = usr.getName();
        String password = usr.getPassword();
        if (password != null) {
            password = this.getPasswordEncryptor().encrypt(password);
        }
        else {
            final String blankPassword = this.getPasswordEncryptor().encrypt("");
            if (this.doesExist(name)) {
                final String key = "ftpserver.user." + name + '.' + "userpassword";
                password = this.userDataProp.getProperty(key, blankPassword);
            }
            else {
                password = blankPassword;
            }
        }
        return password;
    }
    
    @Override
    public String[] getAllUserNames() {
        final String suffix = ".homedirectory";
        final ArrayList<String> ulst = new ArrayList<String>();
        final Enumeration<?> allKeys = this.userDataProp.propertyNames();
        final int prefixlen = "ftpserver.user.".length();
        final int suffixlen = suffix.length();
        while (allKeys.hasMoreElements()) {
            final String key = (String)allKeys.nextElement();
            if (key.endsWith(suffix)) {
                String name = key.substring(prefixlen);
                final int endIndex = name.length() - suffixlen;
                name = name.substring(0, endIndex);
                ulst.add(name);
            }
        }
        Collections.sort(ulst);
        return ulst.toArray(new String[0]);
    }
    
    @Override
    public User getUserByName(final String userName) {
        if (!this.doesExist(userName)) {
            return null;
        }
        final String baseKey = "ftpserver.user." + userName + '.';
        final BaseUser user = new BaseUser();
        user.setName(userName);
        user.setEnabled(this.userDataProp.getBoolean(baseKey + "enableflag", true));
        user.setHomeDirectory(this.userDataProp.getProperty(baseKey + "homedirectory", "/"));
        final List<Authority> authorities = new ArrayList<Authority>();
        if (this.userDataProp.getBoolean(baseKey + "writepermission", false)) {
            authorities.add(new WritePermission());
        }
        final int maxLogin = this.userDataProp.getInteger(baseKey + "maxloginnumber", 0);
        final int maxLoginPerIP = this.userDataProp.getInteger(baseKey + "maxloginperip", 0);
        authorities.add(new ConcurrentLoginPermission(maxLogin, maxLoginPerIP));
        final int uploadRate = this.userDataProp.getInteger(baseKey + "uploadrate", 0);
        final int downloadRate = this.userDataProp.getInteger(baseKey + "downloadrate", 0);
        authorities.add(new TransferRatePermission(downloadRate, uploadRate));
        user.setAuthorities(authorities);
        user.setMaxIdleTime(this.userDataProp.getInteger(baseKey + "idletime", 0));
        return user;
    }
    
    @Override
    public boolean doesExist(final String name) {
        final String key = "ftpserver.user." + name + '.' + "homedirectory";
        return this.userDataProp.containsKey(key);
    }
    
    @Override
    public User authenticate(final Authentication authentication) throws AuthenticationFailedException {
        if (authentication instanceof UsernamePasswordAuthentication) {
            final UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication)authentication;
            final String user = upauth.getUsername();
            String password = upauth.getPassword();
            if (user == null) {
                throw new AuthenticationFailedException("Authentication failed");
            }
            if (password == null) {
                password = "";
            }
            final String storedPassword = this.userDataProp.getProperty("ftpserver.user." + user + '.' + "userpassword");
            if (storedPassword == null) {
                throw new AuthenticationFailedException("Authentication failed");
            }
            if (this.getPasswordEncryptor().matches(password, storedPassword)) {
                return this.getUserByName(user);
            }
            throw new AuthenticationFailedException("Authentication failed");
        }
        else {
            if (!(authentication instanceof AnonymousAuthentication)) {
                throw new IllegalArgumentException("Authentication not supported by this user manager");
            }
            if (this.doesExist("anonymous")) {
                return this.getUserByName("anonymous");
            }
            throw new AuthenticationFailedException("Authentication failed");
        }
    }
    
    public synchronized void dispose() {
        if (this.userDataProp != null) {
            this.userDataProp.clear();
            this.userDataProp = null;
        }
    }
}
