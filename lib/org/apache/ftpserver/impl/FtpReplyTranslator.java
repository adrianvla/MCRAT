// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import org.apache.ftpserver.ftplet.FtpStatistics;
import java.net.InetAddress;
import java.net.SocketAddress;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.util.DateUtils;
import java.net.InetSocketAddress;
import org.apache.ftpserver.message.MessageResource;
import org.apache.ftpserver.ftplet.FtpRequest;

public class FtpReplyTranslator
{
    public static final String CLIENT_ACCESS_TIME = "client.access.time";
    public static final String CLIENT_CON_TIME = "client.con.time";
    public static final String CLIENT_DIR = "client.dir";
    public static final String CLIENT_HOME = "client.home";
    public static final String CLIENT_IP = "client.ip";
    public static final String CLIENT_LOGIN_NAME = "client.login.name";
    public static final String CLIENT_LOGIN_TIME = "client.login.time";
    public static final String OUTPUT_CODE = "output.code";
    public static final String OUTPUT_MSG = "output.msg";
    public static final String REQUEST_ARG = "request.arg";
    public static final String REQUEST_CMD = "request.cmd";
    public static final String REQUEST_LINE = "request.line";
    public static final String SERVER_IP = "server.ip";
    public static final String SERVER_PORT = "server.port";
    public static final String STAT_CON_CURR = "stat.con.curr";
    public static final String STAT_CON_TOTAL = "stat.con.total";
    public static final String STAT_DIR_CREATE_COUNT = "stat.dir.create.count";
    public static final String STAT_DIR_DELETE_COUNT = "stat.dir.delete.count";
    public static final String STAT_FILE_DELETE_COUNT = "stat.file.delete.count";
    public static final String STAT_FILE_DOWNLOAD_BYTES = "stat.file.download.bytes";
    public static final String STAT_FILE_DOWNLOAD_COUNT = "stat.file.download.count";
    public static final String STAT_FILE_UPLOAD_BYTES = "stat.file.upload.bytes";
    public static final String STAT_FILE_UPLOAD_COUNT = "stat.file.upload.count";
    public static final String STAT_LOGIN_ANON_CURR = "stat.login.anon.curr";
    public static final String STAT_LOGIN_ANON_TOTAL = "stat.login.anon.total";
    public static final String STAT_LOGIN_CURR = "stat.login.curr";
    public static final String STAT_LOGIN_TOTAL = "stat.login.total";
    public static final String STAT_START_TIME = "stat.start.time";
    
    public static String translateMessage(final FtpIoSession session, final FtpRequest request, final FtpServerContext context, final int code, final String subId, final String basicMsg) {
        final MessageResource resource = context.getMessageResource();
        final String lang = session.getLanguage();
        String msg = null;
        if (resource != null) {
            msg = resource.getMessage(code, subId, lang);
        }
        if (msg == null) {
            msg = "";
        }
        msg = replaceVariables(session, request, context, code, basicMsg, msg);
        return msg;
    }
    
    private static String replaceVariables(final FtpIoSession session, final FtpRequest request, final FtpServerContext context, final int code, final String basicMsg, final String str) {
        int startIndex = 0;
        int openIndex = str.indexOf(123, startIndex);
        if (openIndex == -1) {
            return str;
        }
        int closeIndex = str.indexOf(125, startIndex);
        if (closeIndex == -1 || openIndex > closeIndex) {
            return str;
        }
        final StringBuilder sb = new StringBuilder(128);
        sb.append(str.substring(startIndex, openIndex));
        while (true) {
            final String varName = str.substring(openIndex + 1, closeIndex);
            sb.append(getVariableValue(session, request, context, code, basicMsg, varName));
            startIndex = closeIndex + 1;
            openIndex = str.indexOf(123, startIndex);
            if (openIndex == -1) {
                sb.append(str.substring(startIndex));
                break;
            }
            closeIndex = str.indexOf(125, startIndex);
            if (closeIndex == -1 || openIndex > closeIndex) {
                sb.append(str.substring(startIndex));
                break;
            }
            sb.append(str.substring(startIndex, openIndex));
        }
        return sb.toString();
    }
    
    private static String getVariableValue(final FtpIoSession session, final FtpRequest request, final FtpServerContext context, final int code, final String basicMsg, final String varName) {
        String varVal = null;
        if (varName.startsWith("output.")) {
            varVal = getOutputVariableValue(session, code, basicMsg, varName);
        }
        else if (varName.startsWith("server.")) {
            varVal = getServerVariableValue(session, varName);
        }
        else if (varName.startsWith("request.")) {
            varVal = getRequestVariableValue(session, request, varName);
        }
        else if (varName.startsWith("stat.")) {
            varVal = getStatisticalVariableValue(session, context, varName);
        }
        else if (varName.startsWith("client.")) {
            varVal = getClientVariableValue(session, varName);
        }
        if (varVal == null) {
            varVal = "";
        }
        return varVal;
    }
    
    private static String getClientVariableValue(final FtpIoSession session, final String varName) {
        String varVal = null;
        if (varName.equals("client.ip")) {
            if (session.getRemoteAddress() instanceof InetSocketAddress) {
                final InetSocketAddress remoteSocketAddress = (InetSocketAddress)session.getRemoteAddress();
                varVal = remoteSocketAddress.getAddress().getHostAddress();
            }
        }
        else if (varName.equals("client.con.time")) {
            varVal = DateUtils.getISO8601Date(session.getCreationTime());
        }
        else if (varName.equals("client.login.name")) {
            if (session.getUser() != null) {
                varVal = session.getUser().getName();
            }
        }
        else if (varName.equals("client.login.time")) {
            varVal = DateUtils.getISO8601Date(session.getLoginTime().getTime());
        }
        else if (varName.equals("client.access.time")) {
            varVal = DateUtils.getISO8601Date(session.getLastAccessTime().getTime());
        }
        else if (varName.equals("client.home")) {
            varVal = session.getUser().getHomeDirectory();
        }
        else if (varName.equals("client.dir")) {
            final FileSystemView fsView = session.getFileSystemView();
            if (fsView != null) {
                try {
                    varVal = fsView.getWorkingDirectory().getAbsolutePath();
                }
                catch (Exception ex) {
                    varVal = "";
                }
            }
        }
        return varVal;
    }
    
    private static String getOutputVariableValue(final FtpIoSession session, final int code, final String basicMsg, final String varName) {
        String varVal = null;
        if (varName.equals("output.code")) {
            varVal = String.valueOf(code);
        }
        else if (varName.equals("output.msg")) {
            varVal = basicMsg;
        }
        return varVal;
    }
    
    private static String getRequestVariableValue(final FtpIoSession session, final FtpRequest request, final String varName) {
        String varVal = null;
        if (request == null) {
            return "";
        }
        if (varName.equals("request.line")) {
            varVal = request.getRequestLine();
        }
        else if (varName.equals("request.cmd")) {
            varVal = request.getCommand();
        }
        else if (varName.equals("request.arg")) {
            varVal = request.getArgument();
        }
        return varVal;
    }
    
    private static String getServerVariableValue(final FtpIoSession session, final String varName) {
        String varVal = null;
        final SocketAddress localSocketAddress = session.getLocalAddress();
        if (localSocketAddress instanceof InetSocketAddress) {
            final InetSocketAddress localInetSocketAddress = (InetSocketAddress)localSocketAddress;
            if (varName.equals("server.ip")) {
                final InetAddress addr = localInetSocketAddress.getAddress();
                if (addr != null) {
                    varVal = addr.getHostAddress();
                }
            }
            else if (varName.equals("server.port")) {
                varVal = String.valueOf(localInetSocketAddress.getPort());
            }
        }
        return varVal;
    }
    
    private static String getStatisticalConnectionVariableValue(final FtpIoSession session, final FtpServerContext context, final String varName) {
        String varVal = null;
        final FtpStatistics stat = context.getFtpStatistics();
        if (varName.equals("stat.con.total")) {
            varVal = String.valueOf(stat.getTotalConnectionNumber());
        }
        else if (varName.equals("stat.con.curr")) {
            varVal = String.valueOf(stat.getCurrentConnectionNumber());
        }
        return varVal;
    }
    
    private static String getStatisticalDirectoryVariableValue(final FtpIoSession session, final FtpServerContext context, final String varName) {
        String varVal = null;
        final FtpStatistics stat = context.getFtpStatistics();
        if (varName.equals("stat.dir.create.count")) {
            varVal = String.valueOf(stat.getTotalDirectoryCreated());
        }
        else if (varName.equals("stat.dir.delete.count")) {
            varVal = String.valueOf(stat.getTotalDirectoryRemoved());
        }
        return varVal;
    }
    
    private static String getStatisticalFileVariableValue(final FtpIoSession session, final FtpServerContext context, final String varName) {
        String varVal = null;
        final FtpStatistics stat = context.getFtpStatistics();
        if (varName.equals("stat.file.upload.count")) {
            varVal = String.valueOf(stat.getTotalUploadNumber());
        }
        else if (varName.equals("stat.file.upload.bytes")) {
            varVal = String.valueOf(stat.getTotalUploadSize());
        }
        else if (varName.equals("stat.file.download.count")) {
            varVal = String.valueOf(stat.getTotalDownloadNumber());
        }
        else if (varName.equals("stat.file.download.bytes")) {
            varVal = String.valueOf(stat.getTotalDownloadSize());
        }
        else if (varName.equals("stat.file.delete.count")) {
            varVal = String.valueOf(stat.getTotalDeleteNumber());
        }
        return varVal;
    }
    
    private static String getStatisticalLoginVariableValue(final FtpIoSession session, final FtpServerContext context, final String varName) {
        String varVal = null;
        final FtpStatistics stat = context.getFtpStatistics();
        if (varName.equals("stat.login.total")) {
            varVal = String.valueOf(stat.getTotalLoginNumber());
        }
        else if (varName.equals("stat.login.curr")) {
            varVal = String.valueOf(stat.getCurrentLoginNumber());
        }
        else if (varName.equals("stat.login.anon.total")) {
            varVal = String.valueOf(stat.getTotalAnonymousLoginNumber());
        }
        else if (varName.equals("stat.login.anon.curr")) {
            varVal = String.valueOf(stat.getCurrentAnonymousLoginNumber());
        }
        return varVal;
    }
    
    private static String getStatisticalVariableValue(final FtpIoSession session, final FtpServerContext context, final String varName) {
        String varVal = null;
        final FtpStatistics stat = context.getFtpStatistics();
        if (varName.equals("stat.start.time")) {
            varVal = DateUtils.getISO8601Date(stat.getStartTime().getTime());
        }
        else if (varName.startsWith("stat.con")) {
            varVal = getStatisticalConnectionVariableValue(session, context, varName);
        }
        else if (varName.startsWith("stat.login.")) {
            varVal = getStatisticalLoginVariableValue(session, context, varName);
        }
        else if (varName.startsWith("stat.file")) {
            varVal = getStatisticalFileVariableValue(session, context, varName);
        }
        else if (varName.startsWith("stat.dir.")) {
            varVal = getStatisticalDirectoryVariableValue(session, context, varName);
        }
        return varVal;
    }
}
