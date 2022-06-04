// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.util;

public class RegularExpr
{
    private char[] pattern;
    
    public RegularExpr(final String pattern) {
        this.pattern = pattern.toCharArray();
    }
    
    public boolean isMatch(final String name) {
        return (this.pattern.length == 1 && this.pattern[0] == '*') || this.isMatch(name.toCharArray(), 0, 0);
    }
    
    private boolean isMatch(final char[] strName, int strIndex, int patternIndex) {
    Label_0331:
        while (patternIndex < this.pattern.length) {
            char pc = this.pattern[patternIndex++];
            switch (pc) {
                case '[': {
                    if (strIndex >= strName.length) {
                        return false;
                    }
                    final char fc = strName[strIndex++];
                    char lastc = '\0';
                    boolean bMatch = false;
                    boolean bNegete = false;
                    boolean bFirst = true;
                    while (patternIndex < this.pattern.length) {
                        pc = this.pattern[patternIndex++];
                        if (pc == ']') {
                            if (bFirst) {
                                bMatch = true;
                            }
                            if (bNegete) {
                                if (bMatch) {
                                    return false;
                                }
                                continue Label_0331;
                            }
                            else {
                                if (!bMatch) {
                                    return false;
                                }
                                continue Label_0331;
                            }
                        }
                        else {
                            if (bMatch) {
                                continue;
                            }
                            if (pc == '^' && bFirst) {
                                bNegete = true;
                            }
                            else {
                                bFirst = false;
                                if (pc == '-') {
                                    if (patternIndex >= this.pattern.length) {
                                        return false;
                                    }
                                    pc = this.pattern[patternIndex++];
                                    bMatch = (fc >= lastc && fc <= pc);
                                    lastc = pc;
                                }
                                else {
                                    lastc = pc;
                                    bMatch = (pc == fc);
                                }
                            }
                        }
                    }
                    return false;
                }
                case '*': {
                    if (patternIndex >= this.pattern.length) {
                        return true;
                    }
                    while (!this.isMatch(strName, strIndex++, patternIndex)) {
                        if (strIndex >= strName.length) {
                            return false;
                        }
                    }
                    return true;
                }
                case '?': {
                    if (strIndex >= strName.length) {
                        return false;
                    }
                    ++strIndex;
                    continue;
                }
                default: {
                    if (strIndex >= strName.length) {
                        return false;
                    }
                    if (strName[strIndex++] != pc) {
                        return false;
                    }
                    continue;
                }
            }
        }
        return strIndex == strName.length;
    }
}
