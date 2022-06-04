// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classloaderhandler;

import nonapi.io.github.classgraph.scanspec.ScanSpec;
import nonapi.io.github.classgraph.classpath.ClasspathOrder;
import nonapi.io.github.classgraph.utils.ReflectionUtils;
import nonapi.io.github.classgraph.classpath.ClassLoaderOrder;
import nonapi.io.github.classgraph.utils.LogNode;

class CxfContainerClassLoaderHandler implements ClassLoaderHandler
{
    private CxfContainerClassLoaderHandler() {
    }
    
    public static boolean canHandle(final Class<?> classLoaderClass, final LogNode log) {
        return "org.apache.openejb.server.cxf.transport.util.CxfContainerClassLoader".equals(classLoaderClass.getName());
    }
    
    public static void findClassLoaderOrder(final ClassLoader classLoader, final ClassLoaderOrder classLoaderOrder, final LogNode log) {
        try {
            classLoaderOrder.delegateTo(Class.forName("org.apache.openejb.server.cxf.transport.util.CxfUtil").getClassLoader(), true, log);
        }
        catch (LinkageError linkageError) {}
        catch (ClassNotFoundException ex) {}
        classLoaderOrder.delegateTo((ClassLoader)ReflectionUtils.invokeMethod(classLoader, "tccl", false), false, log);
        classLoaderOrder.add(classLoader, log);
    }
    
    public static void findClasspathOrder(final ClassLoader classLoader, final ClasspathOrder classpathOrder, final ScanSpec scanSpec, final LogNode log) {
    }
}
