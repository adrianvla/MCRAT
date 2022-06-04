// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.usermanager.impl;

import java.util.Iterator;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import java.util.Collections;
import java.util.ArrayList;
import org.apache.ftpserver.ftplet.Authority;
import java.util.List;
import org.apache.ftpserver.ftplet.User;

public class BaseUser implements User
{
    private String name;
    private String password;
    private int maxIdleTimeSec;
    private String homeDir;
    private boolean isEnabled;
    private List<? extends Authority> authorities;
    
    public BaseUser() {
        this.name = null;
        this.password = null;
        this.maxIdleTimeSec = 0;
        this.homeDir = null;
        this.isEnabled = true;
        this.authorities = new ArrayList<Authority>();
    }
    
    public BaseUser(final User user) {
        this.name = null;
        this.password = null;
        this.maxIdleTimeSec = 0;
        this.homeDir = null;
        this.isEnabled = true;
        this.authorities = new ArrayList<Authority>();
        this.name = user.getName();
        this.password = user.getPassword();
        this.authorities = user.getAuthorities();
        this.maxIdleTimeSec = user.getMaxIdleTime();
        this.homeDir = user.getHomeDirectory();
        this.isEnabled = user.getEnabled();
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    @Override
    public String getPassword() {
        return this.password;
    }
    
    public void setPassword(final String pass) {
        this.password = pass;
    }
    
    @Override
    public List<Authority> getAuthorities() {
        if (this.authorities != null) {
            return Collections.unmodifiableList(this.authorities);
        }
        return null;
    }
    
    public void setAuthorities(final List<Authority> authorities) {
        if (authorities != null) {
            this.authorities = Collections.unmodifiableList((List<? extends Authority>)authorities);
        }
        else {
            this.authorities = null;
        }
    }
    
    @Override
    public int getMaxIdleTime() {
        return this.maxIdleTimeSec;
    }
    
    public void setMaxIdleTime(final int idleSec) {
        this.maxIdleTimeSec = idleSec;
        if (this.maxIdleTimeSec < 0) {
            this.maxIdleTimeSec = 0;
        }
    }
    
    @Override
    public boolean getEnabled() {
        return this.isEnabled;
    }
    
    public void setEnabled(final boolean enb) {
        this.isEnabled = enb;
    }
    
    @Override
    public String getHomeDirectory() {
        return this.homeDir;
    }
    
    public void setHomeDirectory(final String home) {
        this.homeDir = home;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    @Override
    public AuthorizationRequest authorize(AuthorizationRequest request) {
        if (this.authorities == null) {
            return null;
        }
        boolean someoneCouldAuthorize = false;
        for (final Authority authority : this.authorities) {
            if (authority.canAuthorize(request)) {
                someoneCouldAuthorize = true;
                request = authority.authorize(request);
                if (request == null) {
                    return null;
                }
                continue;
            }
        }
        if (someoneCouldAuthorize) {
            return request;
        }
        return null;
    }
    
    @Override
    public List<Authority> getAuthorities(final Class<? extends Authority> clazz) {
        final List<Authority> selected = new ArrayList<Authority>();
        for (final Authority authority : this.authorities) {
            if (authority.getClass().equals(clazz)) {
                selected.add(authority);
            }
        }
        return selected;
    }
}
