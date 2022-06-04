// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;

public final class URLPathEncoder
{
    private static boolean[] safe;
    private static final char[] HEXADECIMAL;
    private static final String[] SCHEME_PREFIXES;
    
    private URLPathEncoder() {
    }
    
    private static void unescapeChars(final String str, final boolean isQuery, final ByteArrayOutputStream buf) {
        if (str.isEmpty()) {
            return;
        }
        for (int chrIdx = 0, len = str.length(); chrIdx < len; ++chrIdx) {
            final char c = str.charAt(chrIdx);
            if (c == '%') {
                if (chrIdx <= len - 3) {
                    final char c2 = str.charAt(++chrIdx);
                    final int digit1 = (c2 >= '0' && c2 <= '9') ? (c2 - '0') : ((c2 >= 'a' && c2 <= 'f') ? (c2 - 'a' + 10) : ((c2 >= 'A' && c2 <= 'F') ? (c2 - 'A' + 10) : -1));
                    final char c3 = str.charAt(++chrIdx);
                    final int digit2 = (c3 >= '0' && c3 <= '9') ? (c3 - '0') : ((c3 >= 'a' && c3 <= 'f') ? (c3 - 'a' + 10) : ((c3 >= 'A' && c3 <= 'F') ? (c3 - 'A' + 10) : -1));
                    Label_0267: {
                        if (digit1 >= 0) {
                            if (digit2 >= 0) {
                                buf.write((byte)(digit1 << 4 | digit2));
                                break Label_0267;
                            }
                        }
                        try {
                            buf.write(str.substring(chrIdx - 2, chrIdx + 1).getBytes(StandardCharsets.UTF_8));
                        }
                        catch (IOException ex) {}
                    }
                }
            }
            else if (isQuery && c == '+') {
                buf.write(32);
            }
            else if (c <= '\u007f') {
                buf.write((byte)c);
            }
            else {
                try {
                    buf.write(Character.toString(c).getBytes(StandardCharsets.UTF_8));
                }
                catch (IOException ex2) {}
            }
        }
    }
    
    public static String decodePath(final String str) {
        final int queryIdx = str.indexOf(63);
        final String partBeforeQuery = (queryIdx < 0) ? str : str.substring(0, queryIdx);
        final String partFromQuery = (queryIdx < 0) ? "" : str.substring(queryIdx);
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        unescapeChars(partBeforeQuery, false, buf);
        unescapeChars(partFromQuery, true, buf);
        return new String(buf.toByteArray(), StandardCharsets.UTF_8);
    }
    
    public static String encodePath(final String path) {
        int validColonPrefixLen = 0;
        for (final String scheme : URLPathEncoder.SCHEME_PREFIXES) {
            if (path.startsWith(scheme)) {
                validColonPrefixLen = scheme.length();
                break;
            }
        }
        if (VersionFinder.OS == VersionFinder.OperatingSystem.Windows) {
            int i = validColonPrefixLen;
            if (i < path.length() && path.charAt(i) == '/') {
                ++i;
            }
            if (i < path.length() - 1 && Character.isLetter(path.charAt(i)) && path.charAt(i + 1) == ':') {
                validColonPrefixLen = i + 2;
            }
        }
        final byte[] pathBytes = path.getBytes(StandardCharsets.UTF_8);
        final StringBuilder encodedPath = new StringBuilder(pathBytes.length * 3);
        for (int j = 0; j < pathBytes.length; ++j) {
            final byte pathByte = pathBytes[j];
            final int b = pathByte & 0xFF;
            if (URLPathEncoder.safe[b] || (b == 58 && j < validColonPrefixLen)) {
                encodedPath.append((char)b);
            }
            else {
                encodedPath.append('%');
                encodedPath.append(URLPathEncoder.HEXADECIMAL[(b & 0xF0) >> 4]);
                encodedPath.append(URLPathEncoder.HEXADECIMAL[b & 0xF]);
            }
        }
        return encodedPath.toString();
    }
    
    public static String normalizeURLPath(final String urlPath) {
        String urlPathNormalized = urlPath;
        if (!urlPathNormalized.startsWith("jrt:") && !urlPathNormalized.startsWith("http://") && !urlPathNormalized.startsWith("https://")) {
            if (urlPathNormalized.startsWith("jar:")) {
                urlPathNormalized = urlPathNormalized.substring(4);
            }
            if (urlPathNormalized.startsWith("file:")) {
                urlPathNormalized = urlPathNormalized.substring(4);
            }
            String windowsDrivePrefix = "";
            if (VersionFinder.OS == VersionFinder.OperatingSystem.Windows) {
                if (urlPathNormalized.length() >= 2 && Character.isLetter(urlPathNormalized.charAt(0)) && urlPathNormalized.charAt(1) == ':') {
                    windowsDrivePrefix = urlPathNormalized.substring(0, 2);
                    urlPathNormalized = urlPathNormalized.substring(2);
                }
                else if (urlPathNormalized.length() >= 3 && urlPathNormalized.charAt(0) == '/' && Character.isLetter(urlPathNormalized.charAt(1)) && urlPathNormalized.charAt(2) == ':') {
                    windowsDrivePrefix = urlPathNormalized.substring(1, 3);
                    urlPathNormalized = urlPathNormalized.substring(3);
                }
            }
            urlPathNormalized = urlPathNormalized.replace("/!", "!").replace("!/", "!").replace("!", "!/");
            if (windowsDrivePrefix.isEmpty()) {
                urlPathNormalized = (urlPathNormalized.startsWith("/") ? ("file:" + urlPathNormalized) : ("file:/" + urlPathNormalized));
            }
            else {
                urlPathNormalized = "file:/" + windowsDrivePrefix + (urlPathNormalized.startsWith("/") ? urlPathNormalized : ("/" + urlPathNormalized));
            }
            if (urlPathNormalized.contains("!") && !urlPathNormalized.startsWith("jar:")) {
                urlPathNormalized = "jar:" + urlPathNormalized;
            }
        }
        return encodePath(urlPathNormalized);
    }
    
    static {
        URLPathEncoder.safe = new boolean[256];
        for (int i = 97; i <= 122; ++i) {
            URLPathEncoder.safe[i] = true;
        }
        for (int i = 65; i <= 90; ++i) {
            URLPathEncoder.safe[i] = true;
        }
        for (int i = 48; i <= 57; ++i) {
            URLPathEncoder.safe[i] = true;
        }
        final boolean[] safe = URLPathEncoder.safe;
        final int n = 36;
        final boolean[] safe2 = URLPathEncoder.safe;
        final int n2 = 45;
        final boolean[] safe3 = URLPathEncoder.safe;
        final int n3 = 95;
        final boolean[] safe4 = URLPathEncoder.safe;
        final int n4 = 46;
        final boolean[] safe5 = URLPathEncoder.safe;
        final int n5 = 43;
        final boolean b = true;
        safe5[n5] = b;
        safe3[n3] = (safe4[n4] = b);
        safe[n] = (safe2[n2] = b);
        final boolean[] safe6 = URLPathEncoder.safe;
        final int n6 = 33;
        final boolean[] safe7 = URLPathEncoder.safe;
        final int n7 = 42;
        final boolean[] safe8 = URLPathEncoder.safe;
        final int n8 = 39;
        final boolean[] safe9 = URLPathEncoder.safe;
        final int n9 = 40;
        final boolean[] safe10 = URLPathEncoder.safe;
        final int n10 = 41;
        final boolean[] safe11 = URLPathEncoder.safe;
        final int n11 = 44;
        final boolean b2 = true;
        safe10[n10] = (safe11[n11] = b2);
        safe8[n8] = (safe9[n9] = b2);
        safe6[n6] = (safe7[n7] = b2);
        URLPathEncoder.safe[47] = true;
        HEXADECIMAL = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        SCHEME_PREFIXES = new String[] { "jrt:", "file:", "jar:file:", "jar:", "http:", "https:" };
    }
}
