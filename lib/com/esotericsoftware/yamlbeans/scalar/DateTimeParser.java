// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.scalar;

import java.text.FieldPosition;
import java.util.Iterator;
import java.text.ParsePosition;
import java.text.ParseException;
import java.util.Date;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

class DateTimeParser extends DateFormat
{
    private static final String DATEFORMAT_YAML = "yyyy-MM-dd HH:mm:ss";
    private static final int FORMAT_NONE = -1;
    private SimpleDateFormat outputFormat;
    private ArrayList<Parser> parsers;
    
    public DateTimeParser() {
        this.parsers = new ArrayList<Parser>();
        this.outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.parsers.add(new SimpleParser(this.outputFormat));
        this.parsers.add(new Parser() {
            public Date parse(final String s) throws ParseException {
                try {
                    final long val = Long.parseLong(s);
                    return new Date(val);
                }
                catch (NumberFormatException e) {
                    throw new ParseException("Error parsing value", -1);
                }
            }
        });
        this.parsers.add(new SimpleParser("yyyy-MM-dd"));
        this.parsers.add(new SimpleParser(0, 0));
        this.parsers.add(new SimpleParser(1, 1));
        this.parsers.add(new SimpleParser(2, 2));
        this.parsers.add(new SimpleParser(3, 3));
        this.parsers.add(new SimpleParser(0, -1));
        this.parsers.add(new SimpleParser(1, -1));
        this.parsers.add(new SimpleParser(2, -1));
        this.parsers.add(new SimpleParser(3, -1));
        this.parsers.add(new SimpleParser(-1, 0));
        this.parsers.add(new SimpleParser(-1, 1));
        this.parsers.add(new SimpleParser(-1, 2));
        this.parsers.add(new SimpleParser(-1, 3));
    }
    
    @Override
    public Date parse(final String text, final ParsePosition pos) {
        final String s = text.substring(pos.getIndex());
        Date date = null;
        for (final Parser parser : this.parsers) {
            try {
                date = parser.parse(s);
            }
            catch (ParseException ex) {
                continue;
            }
            break;
        }
        if (date == null) {
            pos.setIndex(pos.getIndex());
            pos.setErrorIndex(pos.getIndex());
        }
        else {
            pos.setIndex(s.length());
        }
        return date;
    }
    
    @Override
    public StringBuffer format(final Date date, final StringBuffer buf, final FieldPosition pos) {
        return this.outputFormat.format(date, buf, pos);
    }
    
    protected static class SimpleParser implements Parser
    {
        private DateFormat format;
        
        public SimpleParser(final String format) {
            this.format = new SimpleDateFormat(format);
        }
        
        public SimpleParser(final DateFormat format) {
            this.format = format;
        }
        
        public SimpleParser(final int dateType, final int timeType) {
            if (timeType < 0) {
                this.format = DateFormat.getDateInstance(dateType);
            }
            else if (dateType < 0) {
                this.format = DateFormat.getTimeInstance(timeType);
            }
            else {
                this.format = DateFormat.getDateTimeInstance(dateType, timeType);
            }
        }
        
        public Date parse(final String s) throws ParseException {
            return this.format.parse(s);
        }
    }
    
    protected interface Parser
    {
        Date parse(final String p0) throws ParseException;
    }
}
