// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.impl;

import java.util.Iterator;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import java.util.StringTokenizer;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.List;
import org.slf4j.Logger;

public class PassivePorts
{
    private Logger log;
    private static final int MAX_PORT = 65535;
    private static final Integer MAX_PORT_INTEGER;
    private List<Integer> freeList;
    private Set<Integer> usedList;
    private Random r;
    private String passivePortsString;
    private boolean checkIfBound;
    
    private static Set<Integer> parse(final String portsString) {
        final Set<Integer> passivePortsList = new HashSet<Integer>();
        boolean inRange = false;
        Integer lastPort = 1;
        final StringTokenizer st = new StringTokenizer(portsString, ",;-", true);
        while (st.hasMoreTokens()) {
            final String token = st.nextToken().trim();
            if (",".equals(token) || ";".equals(token)) {
                if (inRange) {
                    fillRange(passivePortsList, lastPort, PassivePorts.MAX_PORT_INTEGER);
                }
                lastPort = 1;
                inRange = false;
            }
            else if ("-".equals(token)) {
                inRange = true;
            }
            else {
                if (token.length() == 0) {
                    continue;
                }
                final Integer port = Integer.valueOf(token);
                verifyPort(port);
                if (inRange) {
                    fillRange(passivePortsList, lastPort, port);
                    inRange = false;
                }
                addPort(passivePortsList, port);
                lastPort = port;
            }
        }
        if (inRange) {
            fillRange(passivePortsList, lastPort, PassivePorts.MAX_PORT_INTEGER);
        }
        return passivePortsList;
    }
    
    private static void fillRange(final Set<Integer> passivePortsList, final Integer beginPort, final Integer endPort) {
        for (int i = beginPort; i <= endPort; ++i) {
            addPort(passivePortsList, i);
        }
    }
    
    private static void addPort(final Set<Integer> passivePortsList, final Integer port) {
        passivePortsList.add(port);
    }
    
    private static void verifyPort(final int port) {
        if (port < 0) {
            throw new IllegalArgumentException("Port can not be negative: " + port);
        }
        if (port > 65535) {
            throw new IllegalArgumentException("Port too large: " + port);
        }
    }
    
    public PassivePorts(final String passivePorts, final boolean checkIfBound) {
        this(parse(passivePorts), checkIfBound);
        this.passivePortsString = passivePorts;
    }
    
    public PassivePorts(Set<Integer> passivePorts, final boolean checkIfBound) {
        this.log = LoggerFactory.getLogger(PassivePorts.class);
        this.r = new Random();
        if (passivePorts == null) {
            throw new NullPointerException("passivePorts can not be null");
        }
        if (passivePorts.isEmpty()) {
            passivePorts = new HashSet<Integer>();
            passivePorts.add(0);
        }
        this.freeList = new ArrayList<Integer>(passivePorts);
        this.usedList = new HashSet<Integer>(passivePorts.size());
        this.checkIfBound = checkIfBound;
    }
    
    private boolean checkPortUnbound(final int port) {
        if (!this.checkIfBound) {
            return true;
        }
        if (port == 0) {
            return true;
        }
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            return true;
        }
        catch (IOException e) {
            return false;
        }
        finally {
            if (ss != null) {
                try {
                    ss.close();
                }
                catch (IOException e2) {
                    return false;
                }
            }
        }
    }
    
    public synchronized int reserveNextPort() {
        final List<Integer> freeCopy = new ArrayList<Integer>(this.freeList);
        while (freeCopy.size() > 0) {
            final int i = this.r.nextInt(freeCopy.size());
            final Integer ret = freeCopy.get(i);
            if (ret == 0) {
                return 0;
            }
            if (this.checkPortUnbound(ret)) {
                this.freeList.remove(ret);
                this.usedList.add(ret);
                return ret;
            }
            freeCopy.remove(i);
            this.log.warn("Passive port in use by another process: " + ret);
        }
        return -1;
    }
    
    public synchronized void releasePort(final int port) {
        if (port != 0) {
            if (this.usedList.remove(port)) {
                this.freeList.add(port);
            }
            else {
                this.log.warn("Releasing unreserved passive port: " + port);
            }
        }
    }
    
    @Override
    public String toString() {
        if (this.passivePortsString != null) {
            return this.passivePortsString;
        }
        final StringBuilder sb = new StringBuilder();
        for (final Integer port : this.freeList) {
            sb.append(port);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    
    static {
        MAX_PORT_INTEGER = 65535;
    }
}
