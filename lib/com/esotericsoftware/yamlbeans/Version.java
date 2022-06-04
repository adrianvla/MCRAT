// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans;

public class Version
{
    public static final Version V1_0;
    public static final Version V1_1;
    public static final Version DEFAULT_VERSION;
    private final int major;
    private final int minor;
    
    private Version(final int major, final int minor) {
        this.major = major;
        this.minor = minor;
    }
    
    public static Version getVersion(final String value) {
        Version version = null;
        if (value != null) {
            final int dotIndex = value.indexOf(46);
            int major = 0;
            int minor = 0;
            if (dotIndex > 0) {
                try {
                    major = Integer.parseInt(value.substring(0, dotIndex));
                    minor = Integer.parseInt(value.substring(dotIndex + 1));
                }
                catch (NumberFormatException e) {
                    return null;
                }
            }
            if (major == Version.V1_0.major && minor == Version.V1_0.minor) {
                version = Version.V1_0;
            }
            else if (major == Version.V1_1.major && minor == Version.V1_1.minor) {
                version = Version.V1_1;
            }
        }
        return version;
    }
    
    public int getMajor() {
        return this.major;
    }
    
    public int getMinor() {
        return this.minor;
    }
    
    @Override
    public String toString() {
        return this.major + "." + this.minor;
    }
    
    static {
        V1_0 = new Version(1, 0);
        V1_1 = new Version(1, 1);
        DEFAULT_VERSION = Version.V1_1;
    }
}
