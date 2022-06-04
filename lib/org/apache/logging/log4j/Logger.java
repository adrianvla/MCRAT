// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j;

import org.apache.logging.log4j.message.EntryMessage;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.util.Supplier;
import org.apache.logging.log4j.util.MessageSupplier;
import org.apache.logging.log4j.message.Message;

public interface Logger
{
    void catching(final Level p0, final Throwable p1);
    
    void catching(final Throwable p0);
    
    void debug(final Marker p0, final Message p1);
    
    void debug(final Marker p0, final Message p1, final Throwable p2);
    
    void debug(final Marker p0, final MessageSupplier p1);
    
    void debug(final Marker p0, final MessageSupplier p1, final Throwable p2);
    
    void debug(final Marker p0, final CharSequence p1);
    
    void debug(final Marker p0, final CharSequence p1, final Throwable p2);
    
    void debug(final Marker p0, final Object p1);
    
    void debug(final Marker p0, final Object p1, final Throwable p2);
    
    void debug(final Marker p0, final String p1);
    
    void debug(final Marker p0, final String p1, final Object... p2);
    
    void debug(final Marker p0, final String p1, final Supplier<?>... p2);
    
    void debug(final Marker p0, final String p1, final Throwable p2);
    
    void debug(final Marker p0, final Supplier<?> p1);
    
    void debug(final Marker p0, final Supplier<?> p1, final Throwable p2);
    
    void debug(final Message p0);
    
    void debug(final Message p0, final Throwable p1);
    
    void debug(final MessageSupplier p0);
    
    void debug(final MessageSupplier p0, final Throwable p1);
    
    void debug(final CharSequence p0);
    
    void debug(final CharSequence p0, final Throwable p1);
    
    void debug(final Object p0);
    
    void debug(final Object p0, final Throwable p1);
    
    void debug(final String p0);
    
    void debug(final String p0, final Object... p1);
    
    void debug(final String p0, final Supplier<?>... p1);
    
    void debug(final String p0, final Throwable p1);
    
    void debug(final Supplier<?> p0);
    
    void debug(final Supplier<?> p0, final Throwable p1);
    
    void debug(final Marker p0, final String p1, final Object p2);
    
    void debug(final Marker p0, final String p1, final Object p2, final Object p3);
    
    void debug(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4);
    
    void debug(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5);
    
    void debug(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6);
    
    void debug(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7);
    
    void debug(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8);
    
    void debug(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9);
    
    void debug(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10);
    
    void debug(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10, final Object p11);
    
    void debug(final String p0, final Object p1);
    
    void debug(final String p0, final Object p1, final Object p2);
    
    void debug(final String p0, final Object p1, final Object p2, final Object p3);
    
    void debug(final String p0, final Object p1, final Object p2, final Object p3, final Object p4);
    
    void debug(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5);
    
    void debug(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6);
    
    void debug(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7);
    
    void debug(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8);
    
    void debug(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9);
    
    void debug(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10);
    
    @Deprecated
    void entry();
    
    @Deprecated
    void entry(final Object... p0);
    
    void error(final Marker p0, final Message p1);
    
    void error(final Marker p0, final Message p1, final Throwable p2);
    
    void error(final Marker p0, final MessageSupplier p1);
    
    void error(final Marker p0, final MessageSupplier p1, final Throwable p2);
    
    void error(final Marker p0, final CharSequence p1);
    
    void error(final Marker p0, final CharSequence p1, final Throwable p2);
    
    void error(final Marker p0, final Object p1);
    
    void error(final Marker p0, final Object p1, final Throwable p2);
    
    void error(final Marker p0, final String p1);
    
    void error(final Marker p0, final String p1, final Object... p2);
    
    void error(final Marker p0, final String p1, final Supplier<?>... p2);
    
    void error(final Marker p0, final String p1, final Throwable p2);
    
    void error(final Marker p0, final Supplier<?> p1);
    
    void error(final Marker p0, final Supplier<?> p1, final Throwable p2);
    
    void error(final Message p0);
    
    void error(final Message p0, final Throwable p1);
    
    void error(final MessageSupplier p0);
    
    void error(final MessageSupplier p0, final Throwable p1);
    
    void error(final CharSequence p0);
    
    void error(final CharSequence p0, final Throwable p1);
    
    void error(final Object p0);
    
    void error(final Object p0, final Throwable p1);
    
    void error(final String p0);
    
    void error(final String p0, final Object... p1);
    
    void error(final String p0, final Supplier<?>... p1);
    
    void error(final String p0, final Throwable p1);
    
    void error(final Supplier<?> p0);
    
    void error(final Supplier<?> p0, final Throwable p1);
    
    void error(final Marker p0, final String p1, final Object p2);
    
    void error(final Marker p0, final String p1, final Object p2, final Object p3);
    
    void error(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4);
    
    void error(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5);
    
    void error(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6);
    
    void error(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7);
    
    void error(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8);
    
    void error(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9);
    
    void error(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10);
    
    void error(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10, final Object p11);
    
    void error(final String p0, final Object p1);
    
    void error(final String p0, final Object p1, final Object p2);
    
    void error(final String p0, final Object p1, final Object p2, final Object p3);
    
    void error(final String p0, final Object p1, final Object p2, final Object p3, final Object p4);
    
    void error(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5);
    
    void error(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6);
    
    void error(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7);
    
    void error(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8);
    
    void error(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9);
    
    void error(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10);
    
    @Deprecated
    void exit();
    
    @Deprecated
     <R> R exit(final R p0);
    
    void fatal(final Marker p0, final Message p1);
    
    void fatal(final Marker p0, final Message p1, final Throwable p2);
    
    void fatal(final Marker p0, final MessageSupplier p1);
    
    void fatal(final Marker p0, final MessageSupplier p1, final Throwable p2);
    
    void fatal(final Marker p0, final CharSequence p1);
    
    void fatal(final Marker p0, final CharSequence p1, final Throwable p2);
    
    void fatal(final Marker p0, final Object p1);
    
    void fatal(final Marker p0, final Object p1, final Throwable p2);
    
    void fatal(final Marker p0, final String p1);
    
    void fatal(final Marker p0, final String p1, final Object... p2);
    
    void fatal(final Marker p0, final String p1, final Supplier<?>... p2);
    
    void fatal(final Marker p0, final String p1, final Throwable p2);
    
    void fatal(final Marker p0, final Supplier<?> p1);
    
    void fatal(final Marker p0, final Supplier<?> p1, final Throwable p2);
    
    void fatal(final Message p0);
    
    void fatal(final Message p0, final Throwable p1);
    
    void fatal(final MessageSupplier p0);
    
    void fatal(final MessageSupplier p0, final Throwable p1);
    
    void fatal(final CharSequence p0);
    
    void fatal(final CharSequence p0, final Throwable p1);
    
    void fatal(final Object p0);
    
    void fatal(final Object p0, final Throwable p1);
    
    void fatal(final String p0);
    
    void fatal(final String p0, final Object... p1);
    
    void fatal(final String p0, final Supplier<?>... p1);
    
    void fatal(final String p0, final Throwable p1);
    
    void fatal(final Supplier<?> p0);
    
    void fatal(final Supplier<?> p0, final Throwable p1);
    
    void fatal(final Marker p0, final String p1, final Object p2);
    
    void fatal(final Marker p0, final String p1, final Object p2, final Object p3);
    
    void fatal(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4);
    
    void fatal(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5);
    
    void fatal(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6);
    
    void fatal(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7);
    
    void fatal(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8);
    
    void fatal(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9);
    
    void fatal(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10);
    
    void fatal(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10, final Object p11);
    
    void fatal(final String p0, final Object p1);
    
    void fatal(final String p0, final Object p1, final Object p2);
    
    void fatal(final String p0, final Object p1, final Object p2, final Object p3);
    
    void fatal(final String p0, final Object p1, final Object p2, final Object p3, final Object p4);
    
    void fatal(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5);
    
    void fatal(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6);
    
    void fatal(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7);
    
    void fatal(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8);
    
    void fatal(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9);
    
    void fatal(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10);
    
    Level getLevel();
    
     <MF extends MessageFactory> MF getMessageFactory();
    
    String getName();
    
    void info(final Marker p0, final Message p1);
    
    void info(final Marker p0, final Message p1, final Throwable p2);
    
    void info(final Marker p0, final MessageSupplier p1);
    
    void info(final Marker p0, final MessageSupplier p1, final Throwable p2);
    
    void info(final Marker p0, final CharSequence p1);
    
    void info(final Marker p0, final CharSequence p1, final Throwable p2);
    
    void info(final Marker p0, final Object p1);
    
    void info(final Marker p0, final Object p1, final Throwable p2);
    
    void info(final Marker p0, final String p1);
    
    void info(final Marker p0, final String p1, final Object... p2);
    
    void info(final Marker p0, final String p1, final Supplier<?>... p2);
    
    void info(final Marker p0, final String p1, final Throwable p2);
    
    void info(final Marker p0, final Supplier<?> p1);
    
    void info(final Marker p0, final Supplier<?> p1, final Throwable p2);
    
    void info(final Message p0);
    
    void info(final Message p0, final Throwable p1);
    
    void info(final MessageSupplier p0);
    
    void info(final MessageSupplier p0, final Throwable p1);
    
    void info(final CharSequence p0);
    
    void info(final CharSequence p0, final Throwable p1);
    
    void info(final Object p0);
    
    void info(final Object p0, final Throwable p1);
    
    void info(final String p0);
    
    void info(final String p0, final Object... p1);
    
    void info(final String p0, final Supplier<?>... p1);
    
    void info(final String p0, final Throwable p1);
    
    void info(final Supplier<?> p0);
    
    void info(final Supplier<?> p0, final Throwable p1);
    
    void info(final Marker p0, final String p1, final Object p2);
    
    void info(final Marker p0, final String p1, final Object p2, final Object p3);
    
    void info(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4);
    
    void info(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5);
    
    void info(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6);
    
    void info(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7);
    
    void info(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8);
    
    void info(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9);
    
    void info(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10);
    
    void info(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10, final Object p11);
    
    void info(final String p0, final Object p1);
    
    void info(final String p0, final Object p1, final Object p2);
    
    void info(final String p0, final Object p1, final Object p2, final Object p3);
    
    void info(final String p0, final Object p1, final Object p2, final Object p3, final Object p4);
    
    void info(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5);
    
    void info(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6);
    
    void info(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7);
    
    void info(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8);
    
    void info(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9);
    
    void info(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10);
    
    boolean isDebugEnabled();
    
    boolean isDebugEnabled(final Marker p0);
    
    boolean isEnabled(final Level p0);
    
    boolean isEnabled(final Level p0, final Marker p1);
    
    boolean isErrorEnabled();
    
    boolean isErrorEnabled(final Marker p0);
    
    boolean isFatalEnabled();
    
    boolean isFatalEnabled(final Marker p0);
    
    boolean isInfoEnabled();
    
    boolean isInfoEnabled(final Marker p0);
    
    boolean isTraceEnabled();
    
    boolean isTraceEnabled(final Marker p0);
    
    boolean isWarnEnabled();
    
    boolean isWarnEnabled(final Marker p0);
    
    void log(final Level p0, final Marker p1, final Message p2);
    
    void log(final Level p0, final Marker p1, final Message p2, final Throwable p3);
    
    void log(final Level p0, final Marker p1, final MessageSupplier p2);
    
    void log(final Level p0, final Marker p1, final MessageSupplier p2, final Throwable p3);
    
    void log(final Level p0, final Marker p1, final CharSequence p2);
    
    void log(final Level p0, final Marker p1, final CharSequence p2, final Throwable p3);
    
    void log(final Level p0, final Marker p1, final Object p2);
    
    void log(final Level p0, final Marker p1, final Object p2, final Throwable p3);
    
    void log(final Level p0, final Marker p1, final String p2);
    
    void log(final Level p0, final Marker p1, final String p2, final Object... p3);
    
    void log(final Level p0, final Marker p1, final String p2, final Supplier<?>... p3);
    
    void log(final Level p0, final Marker p1, final String p2, final Throwable p3);
    
    void log(final Level p0, final Marker p1, final Supplier<?> p2);
    
    void log(final Level p0, final Marker p1, final Supplier<?> p2, final Throwable p3);
    
    void log(final Level p0, final Message p1);
    
    void log(final Level p0, final Message p1, final Throwable p2);
    
    void log(final Level p0, final MessageSupplier p1);
    
    void log(final Level p0, final MessageSupplier p1, final Throwable p2);
    
    void log(final Level p0, final CharSequence p1);
    
    void log(final Level p0, final CharSequence p1, final Throwable p2);
    
    void log(final Level p0, final Object p1);
    
    void log(final Level p0, final Object p1, final Throwable p2);
    
    void log(final Level p0, final String p1);
    
    void log(final Level p0, final String p1, final Object... p2);
    
    void log(final Level p0, final String p1, final Supplier<?>... p2);
    
    void log(final Level p0, final String p1, final Throwable p2);
    
    void log(final Level p0, final Supplier<?> p1);
    
    void log(final Level p0, final Supplier<?> p1, final Throwable p2);
    
    void log(final Level p0, final Marker p1, final String p2, final Object p3);
    
    void log(final Level p0, final Marker p1, final String p2, final Object p3, final Object p4);
    
    void log(final Level p0, final Marker p1, final String p2, final Object p3, final Object p4, final Object p5);
    
    void log(final Level p0, final Marker p1, final String p2, final Object p3, final Object p4, final Object p5, final Object p6);
    
    void log(final Level p0, final Marker p1, final String p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7);
    
    void log(final Level p0, final Marker p1, final String p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8);
    
    void log(final Level p0, final Marker p1, final String p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9);
    
    void log(final Level p0, final Marker p1, final String p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10);
    
    void log(final Level p0, final Marker p1, final String p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10, final Object p11);
    
    void log(final Level p0, final Marker p1, final String p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10, final Object p11, final Object p12);
    
    void log(final Level p0, final String p1, final Object p2);
    
    void log(final Level p0, final String p1, final Object p2, final Object p3);
    
    void log(final Level p0, final String p1, final Object p2, final Object p3, final Object p4);
    
    void log(final Level p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5);
    
    void log(final Level p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6);
    
    void log(final Level p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7);
    
    void log(final Level p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8);
    
    void log(final Level p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9);
    
    void log(final Level p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10);
    
    void log(final Level p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10, final Object p11);
    
    void printf(final Level p0, final Marker p1, final String p2, final Object... p3);
    
    void printf(final Level p0, final String p1, final Object... p2);
    
     <T extends Throwable> T throwing(final Level p0, final T p1);
    
     <T extends Throwable> T throwing(final T p0);
    
    void trace(final Marker p0, final Message p1);
    
    void trace(final Marker p0, final Message p1, final Throwable p2);
    
    void trace(final Marker p0, final MessageSupplier p1);
    
    void trace(final Marker p0, final MessageSupplier p1, final Throwable p2);
    
    void trace(final Marker p0, final CharSequence p1);
    
    void trace(final Marker p0, final CharSequence p1, final Throwable p2);
    
    void trace(final Marker p0, final Object p1);
    
    void trace(final Marker p0, final Object p1, final Throwable p2);
    
    void trace(final Marker p0, final String p1);
    
    void trace(final Marker p0, final String p1, final Object... p2);
    
    void trace(final Marker p0, final String p1, final Supplier<?>... p2);
    
    void trace(final Marker p0, final String p1, final Throwable p2);
    
    void trace(final Marker p0, final Supplier<?> p1);
    
    void trace(final Marker p0, final Supplier<?> p1, final Throwable p2);
    
    void trace(final Message p0);
    
    void trace(final Message p0, final Throwable p1);
    
    void trace(final MessageSupplier p0);
    
    void trace(final MessageSupplier p0, final Throwable p1);
    
    void trace(final CharSequence p0);
    
    void trace(final CharSequence p0, final Throwable p1);
    
    void trace(final Object p0);
    
    void trace(final Object p0, final Throwable p1);
    
    void trace(final String p0);
    
    void trace(final String p0, final Object... p1);
    
    void trace(final String p0, final Supplier<?>... p1);
    
    void trace(final String p0, final Throwable p1);
    
    void trace(final Supplier<?> p0);
    
    void trace(final Supplier<?> p0, final Throwable p1);
    
    void trace(final Marker p0, final String p1, final Object p2);
    
    void trace(final Marker p0, final String p1, final Object p2, final Object p3);
    
    void trace(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4);
    
    void trace(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5);
    
    void trace(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6);
    
    void trace(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7);
    
    void trace(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8);
    
    void trace(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9);
    
    void trace(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10);
    
    void trace(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10, final Object p11);
    
    void trace(final String p0, final Object p1);
    
    void trace(final String p0, final Object p1, final Object p2);
    
    void trace(final String p0, final Object p1, final Object p2, final Object p3);
    
    void trace(final String p0, final Object p1, final Object p2, final Object p3, final Object p4);
    
    void trace(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5);
    
    void trace(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6);
    
    void trace(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7);
    
    void trace(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8);
    
    void trace(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9);
    
    void trace(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10);
    
    EntryMessage traceEntry();
    
    EntryMessage traceEntry(final String p0, final Object... p1);
    
    EntryMessage traceEntry(final Supplier<?>... p0);
    
    EntryMessage traceEntry(final String p0, final Supplier<?>... p1);
    
    EntryMessage traceEntry(final Message p0);
    
    void traceExit();
    
     <R> R traceExit(final R p0);
    
     <R> R traceExit(final String p0, final R p1);
    
    void traceExit(final EntryMessage p0);
    
     <R> R traceExit(final EntryMessage p0, final R p1);
    
     <R> R traceExit(final Message p0, final R p1);
    
    void warn(final Marker p0, final Message p1);
    
    void warn(final Marker p0, final Message p1, final Throwable p2);
    
    void warn(final Marker p0, final MessageSupplier p1);
    
    void warn(final Marker p0, final MessageSupplier p1, final Throwable p2);
    
    void warn(final Marker p0, final CharSequence p1);
    
    void warn(final Marker p0, final CharSequence p1, final Throwable p2);
    
    void warn(final Marker p0, final Object p1);
    
    void warn(final Marker p0, final Object p1, final Throwable p2);
    
    void warn(final Marker p0, final String p1);
    
    void warn(final Marker p0, final String p1, final Object... p2);
    
    void warn(final Marker p0, final String p1, final Supplier<?>... p2);
    
    void warn(final Marker p0, final String p1, final Throwable p2);
    
    void warn(final Marker p0, final Supplier<?> p1);
    
    void warn(final Marker p0, final Supplier<?> p1, final Throwable p2);
    
    void warn(final Message p0);
    
    void warn(final Message p0, final Throwable p1);
    
    void warn(final MessageSupplier p0);
    
    void warn(final MessageSupplier p0, final Throwable p1);
    
    void warn(final CharSequence p0);
    
    void warn(final CharSequence p0, final Throwable p1);
    
    void warn(final Object p0);
    
    void warn(final Object p0, final Throwable p1);
    
    void warn(final String p0);
    
    void warn(final String p0, final Object... p1);
    
    void warn(final String p0, final Supplier<?>... p1);
    
    void warn(final String p0, final Throwable p1);
    
    void warn(final Supplier<?> p0);
    
    void warn(final Supplier<?> p0, final Throwable p1);
    
    void warn(final Marker p0, final String p1, final Object p2);
    
    void warn(final Marker p0, final String p1, final Object p2, final Object p3);
    
    void warn(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4);
    
    void warn(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5);
    
    void warn(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6);
    
    void warn(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7);
    
    void warn(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8);
    
    void warn(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9);
    
    void warn(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10);
    
    void warn(final Marker p0, final String p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10, final Object p11);
    
    void warn(final String p0, final Object p1);
    
    void warn(final String p0, final Object p1, final Object p2);
    
    void warn(final String p0, final Object p1, final Object p2, final Object p3);
    
    void warn(final String p0, final Object p1, final Object p2, final Object p3, final Object p4);
    
    void warn(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5);
    
    void warn(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6);
    
    void warn(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7);
    
    void warn(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8);
    
    void warn(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9);
    
    void warn(final String p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9, final Object p10);
    
    default void logMessage(final Level level, final Marker marker, final String fqcn, final StackTraceElement location, final Message message, final Throwable throwable) {
    }
    
    default LogBuilder atTrace() {
        return LogBuilder.NOOP;
    }
    
    default LogBuilder atDebug() {
        return LogBuilder.NOOP;
    }
    
    default LogBuilder atInfo() {
        return LogBuilder.NOOP;
    }
    
    default LogBuilder atWarn() {
        return LogBuilder.NOOP;
    }
    
    default LogBuilder atError() {
        return LogBuilder.NOOP;
    }
    
    default LogBuilder atFatal() {
        return LogBuilder.NOOP;
    }
    
    default LogBuilder always() {
        return LogBuilder.NOOP;
    }
    
    default LogBuilder atLevel(final Level level) {
        return LogBuilder.NOOP;
    }
}
