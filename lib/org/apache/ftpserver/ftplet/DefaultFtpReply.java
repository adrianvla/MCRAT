// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

public class DefaultFtpReply implements FtpReply
{
    private int code;
    private String message;
    private long sentTime;
    private static final String CRLF = "\r\n";
    
    public DefaultFtpReply(final int code, final String message) {
        this.sentTime = 0L;
        this.code = code;
        this.message = message;
        this.sentTime = System.currentTimeMillis();
    }
    
    public DefaultFtpReply(final int code, final String[] message) {
        this.sentTime = 0L;
        this.code = code;
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < message.length; ++i) {
            sb.append(message[i]);
            sb.append('\n');
        }
        this.message = sb.toString();
        this.sentTime = System.currentTimeMillis();
    }
    
    @Override
    public int getCode() {
        return this.code;
    }
    
    @Override
    public String getMessage() {
        return this.message;
    }
    
    @Override
    public long getSentTime() {
        return this.sentTime;
    }
    
    @Override
    public boolean isPositive() {
        return this.code < 400;
    }
    
    private boolean isDigit(final char c) {
        return c >= '0' && c <= '9';
    }
    
    @Override
    public String toString() {
        final int code = this.getCode();
        String notNullMessage = this.getMessage();
        if (notNullMessage == null) {
            notNullMessage = "";
        }
        final StringBuilder sb = new StringBuilder();
        notNullMessage = notNullMessage.replace("\r", "");
        if (notNullMessage.endsWith("\n")) {
            notNullMessage = notNullMessage.substring(0, notNullMessage.length() - 1);
        }
        final String[] lines = notNullMessage.split("\n");
        if (lines.length == 1) {
            sb.append(code);
            sb.append(" ");
            sb.append(notNullMessage);
            sb.append("\r\n");
        }
        else {
            sb.append(code);
            sb.append("-");
            for (int i = 0; i < lines.length; ++i) {
                final String line = lines[i];
                if (i + 1 == lines.length) {
                    sb.append(code);
                    sb.append(" ");
                }
                if (i > 0 && i + 1 < lines.length && line.length() > 2 && this.isDigit(line.charAt(0)) && this.isDigit(line.charAt(1)) && this.isDigit(line.charAt(2))) {
                    sb.append("  ");
                }
                sb.append(line);
                sb.append("\r\n");
            }
        }
        return sb.toString();
    }
}
