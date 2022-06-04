// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import java.net.InetSocketAddress;
import org.apache.ftpserver.ftplet.FtpFile;
import java.net.InetAddress;
import org.apache.ftpserver.ftplet.User;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Date;

public class DefaultFtpStatistics implements ServerFtpStatistics
{
    private StatisticsObserver observer;
    private FileObserver fileObserver;
    private Date startTime;
    private AtomicInteger uploadCount;
    private AtomicInteger downloadCount;
    private AtomicInteger deleteCount;
    private AtomicInteger mkdirCount;
    private AtomicInteger rmdirCount;
    private AtomicInteger currLogins;
    private AtomicInteger totalLogins;
    private AtomicInteger totalFailedLogins;
    private AtomicInteger currAnonLogins;
    private AtomicInteger totalAnonLogins;
    private AtomicInteger currConnections;
    private AtomicInteger totalConnections;
    private AtomicLong bytesUpload;
    private AtomicLong bytesDownload;
    private Map<String, UserLogins> userLoginTable;
    public static final String LOGIN_NUMBER = "login_number";
    
    public DefaultFtpStatistics() {
        this.observer = null;
        this.fileObserver = null;
        this.startTime = new Date();
        this.uploadCount = new AtomicInteger(0);
        this.downloadCount = new AtomicInteger(0);
        this.deleteCount = new AtomicInteger(0);
        this.mkdirCount = new AtomicInteger(0);
        this.rmdirCount = new AtomicInteger(0);
        this.currLogins = new AtomicInteger(0);
        this.totalLogins = new AtomicInteger(0);
        this.totalFailedLogins = new AtomicInteger(0);
        this.currAnonLogins = new AtomicInteger(0);
        this.totalAnonLogins = new AtomicInteger(0);
        this.currConnections = new AtomicInteger(0);
        this.totalConnections = new AtomicInteger(0);
        this.bytesUpload = new AtomicLong(0L);
        this.bytesDownload = new AtomicLong(0L);
        this.userLoginTable = new ConcurrentHashMap<String, UserLogins>();
    }
    
    @Override
    public void setObserver(final StatisticsObserver observer) {
        this.observer = observer;
    }
    
    @Override
    public void setFileObserver(final FileObserver observer) {
        this.fileObserver = observer;
    }
    
    @Override
    public Date getStartTime() {
        if (this.startTime != null) {
            return (Date)this.startTime.clone();
        }
        return null;
    }
    
    @Override
    public int getTotalUploadNumber() {
        return this.uploadCount.get();
    }
    
    @Override
    public int getTotalDownloadNumber() {
        return this.downloadCount.get();
    }
    
    @Override
    public int getTotalDeleteNumber() {
        return this.deleteCount.get();
    }
    
    @Override
    public long getTotalUploadSize() {
        return this.bytesUpload.get();
    }
    
    @Override
    public long getTotalDownloadSize() {
        return this.bytesDownload.get();
    }
    
    @Override
    public int getTotalDirectoryCreated() {
        return this.mkdirCount.get();
    }
    
    @Override
    public int getTotalDirectoryRemoved() {
        return this.rmdirCount.get();
    }
    
    @Override
    public int getTotalConnectionNumber() {
        return this.totalConnections.get();
    }
    
    @Override
    public int getCurrentConnectionNumber() {
        return this.currConnections.get();
    }
    
    @Override
    public int getTotalLoginNumber() {
        return this.totalLogins.get();
    }
    
    @Override
    public int getTotalFailedLoginNumber() {
        return this.totalFailedLogins.get();
    }
    
    @Override
    public int getCurrentLoginNumber() {
        return this.currLogins.get();
    }
    
    @Override
    public int getTotalAnonymousLoginNumber() {
        return this.totalAnonLogins.get();
    }
    
    @Override
    public int getCurrentAnonymousLoginNumber() {
        return this.currAnonLogins.get();
    }
    
    @Override
    public synchronized int getCurrentUserLoginNumber(final User user) {
        final UserLogins userLogins = this.userLoginTable.get(user.getName());
        if (userLogins == null) {
            return 0;
        }
        return userLogins.totalLogins.get();
    }
    
    @Override
    public synchronized int getCurrentUserLoginNumber(final User user, final InetAddress ipAddress) {
        final UserLogins userLogins = this.userLoginTable.get(user.getName());
        if (userLogins == null) {
            return 0;
        }
        return userLogins.loginsFromInetAddress(ipAddress).get();
    }
    
    @Override
    public synchronized void setUpload(final FtpIoSession session, final FtpFile file, final long size) {
        this.uploadCount.incrementAndGet();
        this.bytesUpload.addAndGet(size);
        this.notifyUpload(session, file, size);
    }
    
    @Override
    public synchronized void setDownload(final FtpIoSession session, final FtpFile file, final long size) {
        this.downloadCount.incrementAndGet();
        this.bytesDownload.addAndGet(size);
        this.notifyDownload(session, file, size);
    }
    
    @Override
    public synchronized void setDelete(final FtpIoSession session, final FtpFile file) {
        this.deleteCount.incrementAndGet();
        this.notifyDelete(session, file);
    }
    
    @Override
    public synchronized void setMkdir(final FtpIoSession session, final FtpFile file) {
        this.mkdirCount.incrementAndGet();
        this.notifyMkdir(session, file);
    }
    
    @Override
    public synchronized void setRmdir(final FtpIoSession session, final FtpFile file) {
        this.rmdirCount.incrementAndGet();
        this.notifyRmdir(session, file);
    }
    
    @Override
    public synchronized void setOpenConnection(final FtpIoSession session) {
        this.currConnections.incrementAndGet();
        this.totalConnections.incrementAndGet();
        this.notifyOpenConnection(session);
    }
    
    @Override
    public synchronized void setCloseConnection(final FtpIoSession session) {
        if (this.currConnections.get() > 0) {
            this.currConnections.decrementAndGet();
        }
        this.notifyCloseConnection(session);
    }
    
    @Override
    public synchronized void setLogin(final FtpIoSession session) {
        this.currLogins.incrementAndGet();
        this.totalLogins.incrementAndGet();
        final User user = session.getUser();
        if ("anonymous".equals(user.getName())) {
            this.currAnonLogins.incrementAndGet();
            this.totalAnonLogins.incrementAndGet();
        }
        synchronized (user) {
            UserLogins statisticsTable = this.userLoginTable.get(user.getName());
            if (statisticsTable == null) {
                InetAddress address = null;
                if (session.getRemoteAddress() instanceof InetSocketAddress) {
                    address = ((InetSocketAddress)session.getRemoteAddress()).getAddress();
                }
                statisticsTable = new UserLogins(address);
                this.userLoginTable.put(user.getName(), statisticsTable);
            }
            else {
                statisticsTable.totalLogins.incrementAndGet();
                if (session.getRemoteAddress() instanceof InetSocketAddress) {
                    final InetAddress address = ((InetSocketAddress)session.getRemoteAddress()).getAddress();
                    statisticsTable.loginsFromInetAddress(address).incrementAndGet();
                }
            }
        }
        this.notifyLogin(session);
    }
    
    @Override
    public synchronized void setLoginFail(final FtpIoSession session) {
        this.totalFailedLogins.incrementAndGet();
        this.notifyLoginFail(session);
    }
    
    @Override
    public synchronized void setLogout(final FtpIoSession session) {
        final User user = session.getUser();
        if (user == null) {
            return;
        }
        this.currLogins.decrementAndGet();
        if ("anonymous".equals(user.getName())) {
            this.currAnonLogins.decrementAndGet();
        }
        synchronized (user) {
            final UserLogins userLogins = this.userLoginTable.get(user.getName());
            if (userLogins != null) {
                userLogins.totalLogins.decrementAndGet();
                if (session.getRemoteAddress() instanceof InetSocketAddress) {
                    final InetAddress address = ((InetSocketAddress)session.getRemoteAddress()).getAddress();
                    userLogins.loginsFromInetAddress(address).decrementAndGet();
                }
            }
        }
        this.notifyLogout(session);
    }
    
    private void notifyUpload(final FtpIoSession session, final FtpFile file, final long size) {
        final StatisticsObserver observer = this.observer;
        if (observer != null) {
            observer.notifyUpload();
        }
        final FileObserver fileObserver = this.fileObserver;
        if (fileObserver != null) {
            fileObserver.notifyUpload(session, file, size);
        }
    }
    
    private void notifyDownload(final FtpIoSession session, final FtpFile file, final long size) {
        final StatisticsObserver observer = this.observer;
        if (observer != null) {
            observer.notifyDownload();
        }
        final FileObserver fileObserver = this.fileObserver;
        if (fileObserver != null) {
            fileObserver.notifyDownload(session, file, size);
        }
    }
    
    private void notifyDelete(final FtpIoSession session, final FtpFile file) {
        final StatisticsObserver observer = this.observer;
        if (observer != null) {
            observer.notifyDelete();
        }
        final FileObserver fileObserver = this.fileObserver;
        if (fileObserver != null) {
            fileObserver.notifyDelete(session, file);
        }
    }
    
    private void notifyMkdir(final FtpIoSession session, final FtpFile file) {
        final StatisticsObserver observer = this.observer;
        if (observer != null) {
            observer.notifyMkdir();
        }
        final FileObserver fileObserver = this.fileObserver;
        if (fileObserver != null) {
            fileObserver.notifyMkdir(session, file);
        }
    }
    
    private void notifyRmdir(final FtpIoSession session, final FtpFile file) {
        final StatisticsObserver observer = this.observer;
        if (observer != null) {
            observer.notifyRmdir();
        }
        final FileObserver fileObserver = this.fileObserver;
        if (fileObserver != null) {
            fileObserver.notifyRmdir(session, file);
        }
    }
    
    private void notifyOpenConnection(final FtpIoSession session) {
        final StatisticsObserver observer = this.observer;
        if (observer != null) {
            observer.notifyOpenConnection();
        }
    }
    
    private void notifyCloseConnection(final FtpIoSession session) {
        final StatisticsObserver observer = this.observer;
        if (observer != null) {
            observer.notifyCloseConnection();
        }
    }
    
    private void notifyLogin(final FtpIoSession session) {
        final StatisticsObserver observer = this.observer;
        if (observer != null) {
            final User user = session.getUser();
            boolean anonymous = false;
            if (user != null) {
                final String login = user.getName();
                anonymous = (login != null && login.equals("anonymous"));
            }
            observer.notifyLogin(anonymous);
        }
    }
    
    private void notifyLoginFail(final FtpIoSession session) {
        final StatisticsObserver observer = this.observer;
        if (observer != null && session.getRemoteAddress() instanceof InetSocketAddress) {
            observer.notifyLoginFail(((InetSocketAddress)session.getRemoteAddress()).getAddress());
        }
    }
    
    private void notifyLogout(final FtpIoSession session) {
        final StatisticsObserver observer = this.observer;
        if (observer != null) {
            final User user = session.getUser();
            boolean anonymous = false;
            if (user != null) {
                final String login = user.getName();
                anonymous = (login != null && login.equals("anonymous"));
            }
            observer.notifyLogout(anonymous);
        }
    }
    
    @Override
    public synchronized void resetStatisticsCounters() {
        this.startTime = new Date();
        this.uploadCount.set(0);
        this.downloadCount.set(0);
        this.deleteCount.set(0);
        this.mkdirCount.set(0);
        this.rmdirCount.set(0);
        this.totalLogins.set(0);
        this.totalFailedLogins.set(0);
        this.totalAnonLogins.set(0);
        this.totalConnections.set(0);
        this.bytesUpload.set(0L);
        this.bytesDownload.set(0L);
    }
    
    private static class UserLogins
    {
        private Map<InetAddress, AtomicInteger> perAddress;
        public AtomicInteger totalLogins;
        
        public UserLogins(final InetAddress address) {
            this.perAddress = new ConcurrentHashMap<InetAddress, AtomicInteger>();
            this.totalLogins = new AtomicInteger(1);
            this.perAddress.put(address, new AtomicInteger(1));
        }
        
        public AtomicInteger loginsFromInetAddress(final InetAddress address) {
            AtomicInteger logins = this.perAddress.get(address);
            if (logins == null) {
                logins = new AtomicInteger(0);
                this.perAddress.put(address, logins);
            }
            return logins;
        }
    }
}
