// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl.listing;

import java.util.StringTokenizer;

public class ListArgumentParser
{
    public static ListArgument parse(String argument) {
        String file = "./";
        String options = "";
        String pattern = "*";
        if (argument != null) {
            argument = argument.trim();
            final StringBuilder optionsSb = new StringBuilder(4);
            final StringBuilder fileSb = new StringBuilder(16);
            final StringTokenizer st = new StringTokenizer(argument, " ", true);
            while (st.hasMoreTokens()) {
                final String token = st.nextToken();
                if (fileSb.length() != 0) {
                    fileSb.append(token);
                }
                else {
                    if (token.equals(" ")) {
                        continue;
                    }
                    if (token.charAt(0) == '-') {
                        if (token.length() <= 1) {
                            continue;
                        }
                        optionsSb.append(token.substring(1));
                    }
                    else {
                        fileSb.append(token);
                    }
                }
            }
            if (fileSb.length() != 0) {
                file = fileSb.toString();
            }
            options = optionsSb.toString();
        }
        final int slashIndex = file.lastIndexOf(47);
        if (slashIndex == -1) {
            if (containsPattern(file)) {
                pattern = file;
                file = "./";
            }
        }
        else if (slashIndex != file.length() - 1) {
            final String after = file.substring(slashIndex + 1);
            if (containsPattern(after)) {
                pattern = file.substring(slashIndex + 1);
                file = file.substring(0, slashIndex + 1);
            }
            if (containsPattern(file)) {
                throw new IllegalArgumentException("Directory path can not contain regular expression");
            }
        }
        if ("*".equals(pattern) || "".equals(pattern)) {
            pattern = null;
        }
        return new ListArgument(file, pattern, options.toCharArray());
    }
    
    private static boolean containsPattern(final String file) {
        return file.indexOf(42) > -1 || file.indexOf(63) > -1 || file.indexOf(91) > -1;
    }
}
