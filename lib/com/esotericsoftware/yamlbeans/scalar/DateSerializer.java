// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.scalar;

import java.text.ParseException;
import com.esotericsoftware.yamlbeans.YamlException;
import java.util.Date;

public class DateSerializer implements ScalarSerializer<Date>
{
    private DateTimeParser dateParser;
    
    public DateSerializer() {
        this.dateParser = new DateTimeParser();
    }
    
    public Date read(final String value) throws YamlException {
        try {
            return this.dateParser.parse(value);
        }
        catch (ParseException ex) {
            throw new YamlException("Invalid date: " + value, ex);
        }
    }
    
    public String write(final Date object) throws YamlException {
        return this.dateParser.format(object);
    }
}
