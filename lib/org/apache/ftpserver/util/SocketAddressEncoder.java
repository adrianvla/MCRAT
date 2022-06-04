// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.util;

import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.StringTokenizer;
import java.net.InetSocketAddress;

public class SocketAddressEncoder
{
    private static int convertAndValidateNumber(final String s) {
        final int i = Integer.parseInt(s);
        if (i < 0) {
            throw new IllegalArgumentException("Token can not be less than 0");
        }
        if (i > 255) {
            throw new IllegalArgumentException("Token can not be larger than 255");
        }
        return i;
    }
    
    public static InetSocketAddress decode(final String str) throws UnknownHostException {
        final StringTokenizer st = new StringTokenizer(str, ",");
        if (st.countTokens() != 6) {
            throw new IllegalInetAddressException("Illegal amount of tokens");
        }
        final StringBuilder sb = new StringBuilder();
        try {
            sb.append(convertAndValidateNumber(st.nextToken()));
            sb.append('.');
            sb.append(convertAndValidateNumber(st.nextToken()));
            sb.append('.');
            sb.append(convertAndValidateNumber(st.nextToken()));
            sb.append('.');
            sb.append(convertAndValidateNumber(st.nextToken()));
        }
        catch (IllegalArgumentException e) {
            throw new IllegalInetAddressException(e.getMessage());
        }
        final InetAddress dataAddr = InetAddress.getByName(sb.toString());
        int dataPort = 0;
        try {
            final int hi = convertAndValidateNumber(st.nextToken());
            final int lo = convertAndValidateNumber(st.nextToken());
            dataPort = (hi << 8 | lo);
        }
        catch (IllegalArgumentException ex) {
            throw new IllegalPortException("Invalid data port: " + str);
        }
        return new InetSocketAddress(dataAddr, dataPort);
    }
    
    public static String encode(final InetSocketAddress address) {
        final InetAddress servAddr = address.getAddress();
        final int servPort = address.getPort();
        return servAddr.getHostAddress().replace('.', ',') + ',' + (servPort >> 8) + ',' + (servPort & 0xFF);
    }
}
