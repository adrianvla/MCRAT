// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

public enum DataType
{
    BINARY, 
    ASCII;
    
    public static DataType parseArgument(final char argument) {
        switch (argument) {
            case 'A':
            case 'a': {
                return DataType.ASCII;
            }
            case 'I':
            case 'i': {
                return DataType.BINARY;
            }
            default: {
                throw new IllegalArgumentException("Unknown data type: " + argument);
            }
        }
    }
}
