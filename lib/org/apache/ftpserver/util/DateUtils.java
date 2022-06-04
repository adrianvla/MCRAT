// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.util;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.DateFormat;
import java.util.TimeZone;

public class DateUtils
{
    private static final TimeZone TIME_ZONE_UTC;
    private static final String[] MONTHS;
    private static final ThreadLocal<DateFormat> FTP_DATE_FORMAT;
    
    public static final String getUnixDate(final long millis) {
        if (millis < 0L) {
            return "------------";
        }
        final StringBuilder sb = new StringBuilder(16);
        final Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(millis);
        sb.append(DateUtils.MONTHS[cal.get(2)]);
        sb.append(' ');
        final int day = cal.get(5);
        if (day < 10) {
            sb.append(' ');
        }
        sb.append(day);
        sb.append(' ');
        final long sixMonth = 15811200000L;
        final long nowTime = System.currentTimeMillis();
        if (Math.abs(nowTime - millis) > sixMonth) {
            final int year = cal.get(1);
            sb.append(' ');
            sb.append(year);
        }
        else {
            final int hh = cal.get(11);
            if (hh < 10) {
                sb.append('0');
            }
            sb.append(hh);
            sb.append(':');
            final int mm = cal.get(12);
            if (mm < 10) {
                sb.append('0');
            }
            sb.append(mm);
        }
        return sb.toString();
    }
    
    public static final String getISO8601Date(final long millis) {
        final StringBuilder sb = new StringBuilder(19);
        final Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(millis);
        sb.append(cal.get(1));
        sb.append('-');
        final int month = cal.get(2) + 1;
        if (month < 10) {
            sb.append('0');
        }
        sb.append(month);
        sb.append('-');
        final int date = cal.get(5);
        if (date < 10) {
            sb.append('0');
        }
        sb.append(date);
        sb.append('T');
        final int hour = cal.get(11);
        if (hour < 10) {
            sb.append('0');
        }
        sb.append(hour);
        sb.append(':');
        final int min = cal.get(12);
        if (min < 10) {
            sb.append('0');
        }
        sb.append(min);
        sb.append(':');
        final int sec = cal.get(13);
        if (sec < 10) {
            sb.append('0');
        }
        sb.append(sec);
        return sb.toString();
    }
    
    public static final String getFtpDate(final long millis) {
        final StringBuilder sb = new StringBuilder(20);
        final Calendar cal = new GregorianCalendar(DateUtils.TIME_ZONE_UTC);
        cal.setTimeInMillis(millis);
        sb.append(cal.get(1));
        final int month = cal.get(2) + 1;
        if (month < 10) {
            sb.append('0');
        }
        sb.append(month);
        final int date = cal.get(5);
        if (date < 10) {
            sb.append('0');
        }
        sb.append(date);
        final int hour = cal.get(11);
        if (hour < 10) {
            sb.append('0');
        }
        sb.append(hour);
        final int min = cal.get(12);
        if (min < 10) {
            sb.append('0');
        }
        sb.append(min);
        final int sec = cal.get(13);
        if (sec < 10) {
            sb.append('0');
        }
        sb.append(sec);
        sb.append('.');
        final int milli = cal.get(14);
        if (milli < 100) {
            sb.append('0');
        }
        if (milli < 10) {
            sb.append('0');
        }
        sb.append(milli);
        return sb.toString();
    }
    
    public static final Date parseFTPDate(final String dateStr) throws ParseException {
        return DateUtils.FTP_DATE_FORMAT.get().parse(dateStr);
    }
    
    static {
        TIME_ZONE_UTC = TimeZone.getTimeZone("UTC");
        MONTHS = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        FTP_DATE_FORMAT = new ThreadLocal<DateFormat>() {
            @Override
            protected DateFormat initialValue() {
                final DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                df.setLenient(false);
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                return df;
            }
        };
    }
}
