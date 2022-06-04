// 
// Decompiled by Procyon v0.5.36
// 

package org.slf4j.impl;

import org.slf4j.helpers.Util;
import org.apache.log4j.LogManager;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.ILoggerFactory;

public class Log4jLoggerFactory implements ILoggerFactory
{
    private static final String LOG4J_DELEGATION_LOOP_URL = "http://www.slf4j.org/codes.html#log4jDelegationLoop";
    ConcurrentMap<String, Logger> loggerMap;
    
    public Log4jLoggerFactory() {
        this.loggerMap = new ConcurrentHashMap<String, Logger>();
        LogManager.getRootLogger();
    }
    
    public Logger getLogger(final String name) {
        final Logger slf4jLogger = this.loggerMap.get(name);
        if (slf4jLogger != null) {
            return slf4jLogger;
        }
        org.apache.log4j.Logger log4jLogger;
        if (name.equalsIgnoreCase("ROOT")) {
            log4jLogger = LogManager.getRootLogger();
        }
        else {
            log4jLogger = LogManager.getLogger(name);
        }
        final Logger newInstance = new Log4jLoggerAdapter(log4jLogger);
        final Logger oldInstance = this.loggerMap.putIfAbsent(name, newInstance);
        return (oldInstance == null) ? newInstance : oldInstance;
    }
    
    static {
        try {
            Class.forName("org.apache.log4j.Log4jLoggerFactory");
            final String part1 = "Detected both log4j-over-slf4j.jar AND bound slf4j-log4j12.jar on the class path, preempting StackOverflowError. ";
            final String part2 = "See also http://www.slf4j.org/codes.html#log4jDelegationLoop for more details.";
            Util.report(part1);
            Util.report(part2);
            throw new IllegalStateException(part1 + part2);
        }
        catch (ClassNotFoundException e) {}
    }
}
