// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import org.apache.ftpserver.ftplet.FtpRequest;

public class DefaultFtpRequest implements FtpRequest
{
    private final String line;
    private final String command;
    private final String argument;
    private final long receivedTime;
    
    public DefaultFtpRequest(final String requestLine) {
        this.receivedTime = System.currentTimeMillis();
        this.line = requestLine.trim();
        final int spInd = this.line.indexOf(32);
        this.command = this.parseCmd(this.line, spInd);
        this.argument = this.parseArg(this.line, spInd);
    }
    
    private String parseCmd(final String lineToParse, final int spInd) {
        String cmd = null;
        if (spInd != -1) {
            cmd = this.line.substring(0, spInd).toUpperCase();
        }
        else {
            cmd = this.line.toUpperCase();
        }
        if (cmd.length() > 0 && cmd.charAt(0) == 'X') {
            cmd = cmd.substring(1);
        }
        return cmd;
    }
    
    private String parseArg(final String lineToParse, final int spInd) {
        String arg = null;
        if (spInd != -1) {
            arg = this.line.substring(spInd + 1);
            if (arg.equals("")) {
                arg = null;
            }
        }
        return arg;
    }
    
    @Override
    public String getCommand() {
        return this.command;
    }
    
    @Override
    public String getArgument() {
        return this.argument;
    }
    
    @Override
    public String getRequestLine() {
        return this.line;
    }
    
    @Override
    public boolean hasArgument() {
        return this.getArgument() != null;
    }
    
    @Override
    public long getReceivedTime() {
        return this.receivedTime;
    }
    
    @Override
    public String toString() {
        return this.getRequestLine();
    }
}
