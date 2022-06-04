// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classloaderhandler;

import java.net.URL;
import io.github.classgraph.ClassGraphClassLoader;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import nonapi.io.github.classgraph.classpath.ClasspathOrder;
import nonapi.io.github.classgraph.classpath.ClassLoaderOrder;
import nonapi.io.github.classgraph.utils.LogNode;

class ClassGraphClassLoaderHandler implements ClassLoaderHandler
{
    private ClassGraphClassLoaderHandler() {
    }
    
    public static boolean canHandle(final Class<?> classLoaderClass, final LogNode log) {
        final boolean matches = "io.github.classgraph.ClassGraphClassLoader".equals(classLoaderClass.getName());
        if (matches && log != null) {
            log.log("Sharing a `ClassGraphClassLoader` between multiple nested scans is not advisable, because scan criteria may differ between scans. See: https://github.com/classgraph/classgraph/issues/485");
        }
        return matches;
    }
    
    public static void findClassLoaderOrder(final ClassLoader classLoader, final ClassLoaderOrder classLoaderOrder, final LogNode log) {
        classLoaderOrder.delegateTo(classLoader.getParent(), true, log);
        classLoaderOrder.add(classLoader, log);
    }
    
    public static void findClasspathOrder(final ClassLoader classLoader, final ClasspathOrder classpathOrder, final ScanSpec scanSpec, final LogNode log) {
        for (final URL url : ((ClassGraphClassLoader)classLoader).getURLs()) {
            if (url != null) {
                classpathOrder.addClasspathEntry(url, classLoader, scanSpec, log);
            }
        }
    }
}
