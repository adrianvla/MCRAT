// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.util;

import java.text.ParseException;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.io.File;
import java.net.UnknownHostException;
import java.net.InetAddress;
import org.apache.ftpserver.ftplet.FtpException;
import java.util.Properties;

public class BaseProperties extends Properties
{
    private static final long serialVersionUID = 5572645129592131953L;
    
    public BaseProperties() {
    }
    
    public BaseProperties(final Properties prop) {
        super(prop);
    }
    
    public boolean getBoolean(final String str) throws FtpException {
        final String prop = this.getProperty(str);
        if (prop == null) {
            throw new FtpException(str + " not found");
        }
        return prop.toLowerCase().equals("true");
    }
    
    public boolean getBoolean(final String str, final boolean bol) {
        try {
            return this.getBoolean(str);
        }
        catch (FtpException ex) {
            return bol;
        }
    }
    
    public int getInteger(final String str) throws FtpException {
        final String value = this.getProperty(str);
        if (value == null) {
            throw new FtpException(str + " not found");
        }
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException ex) {
            throw new FtpException("BaseProperties.getInteger()", ex);
        }
    }
    
    public int getInteger(final String str, final int intVal) {
        try {
            return this.getInteger(str);
        }
        catch (FtpException ex) {
            return intVal;
        }
    }
    
    public long getLong(final String str) throws FtpException {
        final String value = this.getProperty(str);
        if (value == null) {
            throw new FtpException(str + " not found");
        }
        try {
            return Long.parseLong(value);
        }
        catch (NumberFormatException ex) {
            throw new FtpException("BaseProperties.getLong()", ex);
        }
    }
    
    public long getLong(final String str, final long val) {
        try {
            return this.getLong(str);
        }
        catch (FtpException ex) {
            return val;
        }
    }
    
    public double getDouble(final String str) throws FtpException {
        final String value = this.getProperty(str);
        if (value == null) {
            throw new FtpException(str + " not found");
        }
        try {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException ex) {
            throw new FtpException("BaseProperties.getDouble()", ex);
        }
    }
    
    public double getDouble(final String str, final double doubleVal) {
        try {
            return this.getDouble(str);
        }
        catch (FtpException ex) {
            return doubleVal;
        }
    }
    
    public InetAddress getInetAddress(final String str) throws FtpException {
        final String value = this.getProperty(str);
        if (value == null) {
            throw new FtpException(str + " not found");
        }
        try {
            return InetAddress.getByName(value);
        }
        catch (UnknownHostException ex) {
            throw new FtpException("Host " + value + " not found");
        }
    }
    
    public InetAddress getInetAddress(final String str, final InetAddress addr) {
        try {
            return this.getInetAddress(str);
        }
        catch (FtpException ex) {
            return addr;
        }
    }
    
    public String getString(final String str) throws FtpException {
        final String value = this.getProperty(str);
        if (value == null) {
            throw new FtpException(str + " not found");
        }
        return value;
    }
    
    public String getString(final String str, final String s) {
        try {
            return this.getString(str);
        }
        catch (FtpException ex) {
            return s;
        }
    }
    
    public File getFile(final String str) throws FtpException {
        final String value = this.getProperty(str);
        if (value == null) {
            throw new FtpException(str + " not found");
        }
        return new File(value);
    }
    
    public File getFile(final String str, final File fl) {
        try {
            return this.getFile(str);
        }
        catch (FtpException ex) {
            return fl;
        }
    }
    
    public Class<?> getClass(final String str) throws FtpException {
        final String value = this.getProperty(str);
        if (value == null) {
            throw new FtpException(str + " not found");
        }
        try {
            return Class.forName(value);
        }
        catch (ClassNotFoundException ex) {
            throw new FtpException("BaseProperties.getClass()", ex);
        }
    }
    
    public Class<?> getClass(final String str, final Class<?> cls) {
        try {
            return this.getClass(str);
        }
        catch (FtpException ex) {
            return cls;
        }
    }
    
    public TimeZone getTimeZone(final String str) throws FtpException {
        final String value = this.getProperty(str);
        if (value == null) {
            throw new FtpException(str + " not found");
        }
        return TimeZone.getTimeZone(value);
    }
    
    public TimeZone getTimeZone(final String str, final TimeZone tz) {
        try {
            return this.getTimeZone(str);
        }
        catch (FtpException ex) {
            return tz;
        }
    }
    
    public SimpleDateFormat getDateFormat(final String str) throws FtpException {
        final String value = this.getProperty(str);
        if (value == null) {
            throw new FtpException(str + " not found");
        }
        try {
            return new SimpleDateFormat(value);
        }
        catch (IllegalArgumentException e) {
            throw new FtpException("Date format was incorrect: " + value, e);
        }
    }
    
    public SimpleDateFormat getDateFormat(final String str, final SimpleDateFormat fmt) {
        try {
            return this.getDateFormat(str);
        }
        catch (FtpException ex) {
            return fmt;
        }
    }
    
    public Date getDate(final String str, final DateFormat fmt) throws FtpException {
        final String value = this.getProperty(str);
        if (value == null) {
            throw new FtpException(str + " not found");
        }
        try {
            return fmt.parse(value);
        }
        catch (ParseException ex) {
            throw new FtpException("BaseProperties.getdate()", ex);
        }
    }
    
    public Date getDate(final String str, final DateFormat fmt, final Date dt) {
        try {
            return this.getDate(str, fmt);
        }
        catch (FtpException ex) {
            return dt;
        }
    }
    
    public void setProperty(final String key, final boolean val) {
        this.setProperty(key, String.valueOf(val));
    }
    
    public void setProperty(final String key, final int val) {
        this.setProperty(key, String.valueOf(val));
    }
    
    public void setProperty(final String key, final double val) {
        this.setProperty(key, String.valueOf(val));
    }
    
    public void setProperty(final String key, final float val) {
        this.setProperty(key, String.valueOf(val));
    }
    
    public void setProperty(final String key, final long val) {
        this.setProperty(key, String.valueOf(val));
    }
    
    public void setInetAddress(final String key, final InetAddress val) {
        this.setProperty(key, val.getHostAddress());
    }
    
    public void setProperty(final String key, final File val) {
        this.setProperty(key, val.getAbsolutePath());
    }
    
    public void setProperty(final String key, final SimpleDateFormat val) {
        this.setProperty(key, val.toPattern());
    }
    
    public void setProperty(final String key, final TimeZone val) {
        this.setProperty(key, val.getID());
    }
    
    public void setProperty(final String key, final Date val, final DateFormat fmt) {
        this.setProperty(key, fmt.format(val));
    }
    
    public void setProperty(final String key, final Class<?> val) {
        this.setProperty(key, val.getName());
    }
}
