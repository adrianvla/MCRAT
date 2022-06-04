// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.util;

public class ClassUtils
{
    public static boolean extendsClass(final Class<?> clazz, final String className) {
        for (Class<?> superClass = clazz.getSuperclass(); superClass != null; superClass = superClass.getSuperclass()) {
            if (superClass.getName().equals(className)) {
                return true;
            }
        }
        return false;
    }
}
