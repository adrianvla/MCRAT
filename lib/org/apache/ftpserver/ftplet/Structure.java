// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

public enum Structure
{
    FILE;
    
    public static Structure parseArgument(final char argument) {
        switch (argument) {
            case 'F':
            case 'f': {
                return Structure.FILE;
            }
            default: {
                throw new IllegalArgumentException("Unknown structure: " + argument);
            }
        }
    }
}
