// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.command.impl.listing;

public class ListArgument
{
    private final String file;
    private final String pattern;
    private final char[] options;
    
    public ListArgument(final String file, final String pattern, final char[] options) {
        this.file = file;
        this.pattern = pattern;
        if (options == null) {
            this.options = new char[0];
        }
        else {
            this.options = options.clone();
        }
    }
    
    public char[] getOptions() {
        return this.options.clone();
    }
    
    public String getPattern() {
        return this.pattern;
    }
    
    public boolean hasOption(final char option) {
        for (int i = 0; i < this.options.length; ++i) {
            if (option == this.options[i]) {
                return true;
            }
        }
        return false;
    }
    
    public String getFile() {
        return this.file;
    }
}
