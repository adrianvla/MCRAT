// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.util;

import java.util.Map;

public class StringUtils
{
    public static final String replaceString(final String source, final String oldStr, final String newStr) {
        final StringBuilder sb = new StringBuilder(source.length());
        int sind = 0;
        for (int cind = 0; (cind = source.indexOf(oldStr, sind)) != -1; sind = cind + oldStr.length()) {
            sb.append(source.substring(sind, cind));
            sb.append(newStr);
        }
        sb.append(source.substring(sind));
        return sb.toString();
    }
    
    public static final String replaceString(final String source, final Object[] args) {
        int startIndex = 0;
        int openIndex = source.indexOf(123, startIndex);
        if (openIndex == -1) {
            return source;
        }
        int closeIndex = source.indexOf(125, startIndex);
        if (closeIndex == -1 || openIndex > closeIndex) {
            return source;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(source.substring(startIndex, openIndex));
        while (true) {
            final String intStr = source.substring(openIndex + 1, closeIndex);
            final int index = Integer.parseInt(intStr);
            sb.append(args[index]);
            startIndex = closeIndex + 1;
            openIndex = source.indexOf(123, startIndex);
            if (openIndex == -1) {
                sb.append(source.substring(startIndex));
                break;
            }
            closeIndex = source.indexOf(125, startIndex);
            if (closeIndex == -1 || openIndex > closeIndex) {
                sb.append(source.substring(startIndex));
                break;
            }
            sb.append(source.substring(startIndex, openIndex));
        }
        return sb.toString();
    }
    
    public static final String replaceString(final String source, final Map<String, Object> args) {
        int startIndex = 0;
        int openIndex = source.indexOf(123, startIndex);
        if (openIndex == -1) {
            return source;
        }
        int closeIndex = source.indexOf(125, startIndex);
        if (closeIndex == -1 || openIndex > closeIndex) {
            return source;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(source.substring(startIndex, openIndex));
        while (true) {
            final String key = source.substring(openIndex + 1, closeIndex);
            final Object val = args.get(key);
            if (val != null) {
                sb.append(val);
            }
            startIndex = closeIndex + 1;
            openIndex = source.indexOf(123, startIndex);
            if (openIndex == -1) {
                sb.append(source.substring(startIndex));
                break;
            }
            closeIndex = source.indexOf(125, startIndex);
            if (closeIndex == -1 || openIndex > closeIndex) {
                sb.append(source.substring(startIndex));
                break;
            }
            sb.append(source.substring(startIndex, openIndex));
        }
        return sb.toString();
    }
    
    public static final String formatHtml(final String source, final boolean bReplaceNl, final boolean bReplaceTag, final boolean bReplaceQuote) {
        final StringBuilder sb = new StringBuilder();
        for (int len = source.length(), i = 0; i < len; ++i) {
            final char c = source.charAt(i);
            switch (c) {
                case '\"': {
                    if (bReplaceQuote) {
                        sb.append("&quot;");
                        break;
                    }
                    sb.append(c);
                    break;
                }
                case '<': {
                    if (bReplaceTag) {
                        sb.append("&lt;");
                        break;
                    }
                    sb.append(c);
                    break;
                }
                case '>': {
                    if (bReplaceTag) {
                        sb.append("&gt;");
                        break;
                    }
                    sb.append(c);
                    break;
                }
                case '\n': {
                    if (!bReplaceNl) {
                        sb.append(c);
                        break;
                    }
                    if (bReplaceTag) {
                        sb.append("&lt;br&gt;");
                        break;
                    }
                    sb.append("<br>");
                    break;
                }
                case '\r': {
                    break;
                }
                case '&': {
                    sb.append("&amp;");
                    break;
                }
                default: {
                    sb.append(c);
                    break;
                }
            }
        }
        return sb.toString();
    }
    
    public static final String pad(final String src, final char padChar, final boolean rightPad, final int totalLength) {
        final int srcLength = src.length();
        if (srcLength >= totalLength) {
            return src;
        }
        final int padLength = totalLength - srcLength;
        final StringBuilder sb = new StringBuilder(padLength);
        for (int i = 0; i < padLength; ++i) {
            sb.append(padChar);
        }
        if (rightPad) {
            return src + sb.toString();
        }
        return sb.toString() + src;
    }
    
    public static final String toHexString(final byte[] res) {
        final StringBuilder sb = new StringBuilder(res.length << 1);
        for (int i = 0; i < res.length; ++i) {
            final String digit = Integer.toHexString(0xFF & res[i]);
            if (digit.length() == 1) {
                sb.append('0');
            }
            sb.append(digit);
        }
        return sb.toString().toUpperCase();
    }
    
    public static final byte[] toByteArray(final String hexString) {
        final int arrLength = hexString.length() >> 1;
        final byte[] buff = new byte[arrLength];
        for (int i = 0; i < arrLength; ++i) {
            final int index = i << 1;
            final String digit = hexString.substring(index, index + 2);
            buff[i] = (byte)Integer.parseInt(digit, 16);
        }
        return buff;
    }
}
