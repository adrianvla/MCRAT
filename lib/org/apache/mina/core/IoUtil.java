// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.mina.core;

import java.util.concurrent.TimeUnit;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.buffer.IoBuffer;
import java.util.Iterator;
import java.util.ArrayList;
import org.apache.mina.core.future.WriteFuture;
import java.util.List;
import java.util.Collection;
import org.apache.mina.core.session.IoSession;

public final class IoUtil
{
    private static final IoSession[] EMPTY_SESSIONS;
    
    public static List<WriteFuture> broadcast(final Object message, final Collection<IoSession> sessions) {
        final List<WriteFuture> answer = new ArrayList<WriteFuture>(sessions.size());
        broadcast(message, sessions.iterator(), answer);
        return answer;
    }
    
    public static List<WriteFuture> broadcast(final Object message, final Iterable<IoSession> sessions) {
        final List<WriteFuture> answer = new ArrayList<WriteFuture>();
        broadcast(message, sessions.iterator(), answer);
        return answer;
    }
    
    public static List<WriteFuture> broadcast(final Object message, final Iterator<IoSession> sessions) {
        final List<WriteFuture> answer = new ArrayList<WriteFuture>();
        broadcast(message, sessions, answer);
        return answer;
    }
    
    public static List<WriteFuture> broadcast(final Object message, IoSession... sessions) {
        if (sessions == null) {
            sessions = IoUtil.EMPTY_SESSIONS;
        }
        final List<WriteFuture> answer = new ArrayList<WriteFuture>(sessions.length);
        if (message instanceof IoBuffer) {
            for (final IoSession s : sessions) {
                answer.add(s.write(((IoBuffer)message).duplicate()));
            }
        }
        else {
            for (final IoSession s : sessions) {
                answer.add(s.write(message));
            }
        }
        return answer;
    }
    
    private static void broadcast(final Object message, final Iterator<IoSession> sessions, final Collection<WriteFuture> answer) {
        if (message instanceof IoBuffer) {
            while (sessions.hasNext()) {
                final IoSession s = sessions.next();
                answer.add(s.write(((IoBuffer)message).duplicate()));
            }
        }
        else {
            while (sessions.hasNext()) {
                final IoSession s = sessions.next();
                answer.add(s.write(message));
            }
        }
    }
    
    public static void await(final Iterable<? extends IoFuture> futures) throws InterruptedException {
        for (final IoFuture f : futures) {
            f.await();
        }
    }
    
    public static void awaitUninterruptably(final Iterable<? extends IoFuture> futures) {
        for (final IoFuture f : futures) {
            f.awaitUninterruptibly();
        }
    }
    
    public static boolean await(final Iterable<? extends IoFuture> futures, final long timeout, final TimeUnit unit) throws InterruptedException {
        return await(futures, unit.toMillis(timeout));
    }
    
    public static boolean await(final Iterable<? extends IoFuture> futures, final long timeoutMillis) throws InterruptedException {
        return await0(futures, timeoutMillis, true);
    }
    
    public static boolean awaitUninterruptibly(final Iterable<? extends IoFuture> futures, final long timeout, final TimeUnit unit) {
        return awaitUninterruptibly(futures, unit.toMillis(timeout));
    }
    
    public static boolean awaitUninterruptibly(final Iterable<? extends IoFuture> futures, final long timeoutMillis) {
        try {
            return await0(futures, timeoutMillis, false);
        }
        catch (InterruptedException e) {
            throw new InternalError();
        }
    }
    
    private static boolean await0(final Iterable<? extends IoFuture> futures, final long timeoutMillis, final boolean interruptable) throws InterruptedException {
        final long startTime = (timeoutMillis <= 0L) ? 0L : System.currentTimeMillis();
        long waitTime = timeoutMillis;
        boolean lastComplete = true;
        final Iterator<? extends IoFuture> i = futures.iterator();
        while (i.hasNext()) {
            final IoFuture f = (IoFuture)i.next();
            do {
                if (interruptable) {
                    lastComplete = f.await(waitTime);
                }
                else {
                    lastComplete = f.awaitUninterruptibly(waitTime);
                }
                waitTime = timeoutMillis - (System.currentTimeMillis() - startTime);
                if (lastComplete) {
                    break;
                }
                if (waitTime <= 0L) {
                    break;
                }
            } while (!lastComplete);
            if (waitTime <= 0L) {
                break;
            }
        }
        return lastComplete && !i.hasNext();
    }
    
    private IoUtil() {
    }
    
    static {
        EMPTY_SESSIONS = new IoSession[0];
    }
}
