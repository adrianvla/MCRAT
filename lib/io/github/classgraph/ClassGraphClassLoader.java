// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.io.InputStream;
import java.util.Enumeration;
import java.nio.ByteBuffer;
import java.util.Iterator;
import nonapi.io.github.classgraph.utils.VersionFinder;
import java.io.IOException;
import java.security.ProtectionDomain;
import nonapi.io.github.classgraph.utils.JarUtils;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import java.util.Collection;
import java.util.Collections;
import java.net.URLClassLoader;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ClassGraphClassLoader extends ClassLoader
{
    private final ScanResult scanResult;
    private final boolean initializeLoadedClasses;
    private Set<ClassLoader> environmentClassLoaderDelegationOrder;
    private List<ClassLoader> overrideClassLoaders;
    private final ClassLoader classpathClassLoader;
    private Set<ClassLoader> addedClassLoaderDelegationOrder;
    
    ClassGraphClassLoader(final ScanResult scanResult) {
        super(null);
        registerAsParallelCapable();
        this.scanResult = scanResult;
        final ScanSpec scanSpec = scanResult.scanSpec;
        this.initializeLoadedClasses = scanSpec.initializeLoadedClasses;
        final boolean classpathOverridden = scanSpec.overrideClasspath != null && !scanSpec.overrideClasspath.isEmpty();
        final boolean classloadersOverridden = scanSpec.overrideClassLoaders != null && !scanSpec.overrideClassLoaders.isEmpty();
        final boolean clasloadersAdded = scanSpec.addedClassLoaders != null && !scanSpec.addedClassLoaders.isEmpty();
        if (!classpathOverridden && !classloadersOverridden) {
            (this.environmentClassLoaderDelegationOrder = new LinkedHashSet<ClassLoader>()).add(null);
            final ClassLoader[] envClassLoaderOrder = scanResult.getClassLoaderOrderRespectingParentDelegation();
            if (envClassLoaderOrder != null) {
                for (final ClassLoader envClassLoader : envClassLoaderOrder) {
                    this.environmentClassLoaderDelegationOrder.add(envClassLoader);
                }
            }
        }
        final List<URL> classpathURLs = scanResult.getClasspathURLs();
        this.classpathClassLoader = (classpathURLs.isEmpty() ? null : new URLClassLoader(classpathURLs.toArray(new URL[0])));
        this.overrideClassLoaders = (classloadersOverridden ? scanSpec.overrideClassLoaders : null);
        if (this.overrideClassLoaders == null && classpathOverridden && this.classpathClassLoader != null) {
            this.overrideClassLoaders = Collections.singletonList(this.classpathClassLoader);
        }
        if (clasloadersAdded) {
            (this.addedClassLoaderDelegationOrder = new LinkedHashSet<ClassLoader>()).addAll(scanSpec.addedClassLoaders);
            if (this.environmentClassLoaderDelegationOrder != null) {
                this.addedClassLoaderDelegationOrder.removeAll(this.environmentClassLoaderDelegationOrder);
            }
        }
    }
    
    @Override
    protected Class<?> findClass(final String className) throws ClassNotFoundException, LinkageError, SecurityException {
        final ClassGraphClassLoader delegateClassGraphClassLoader = this.scanResult.classpathFinder.getDelegateClassGraphClassLoader();
        LinkageError linkageError = null;
        if (delegateClassGraphClassLoader != null) {
            try {
                return Class.forName(className, this.initializeLoadedClasses, delegateClassGraphClassLoader);
            }
            catch (ClassNotFoundException ex) {}
            catch (LinkageError e) {
                linkageError = e;
            }
        }
        if (this.overrideClassLoaders != null) {
            for (final ClassLoader overrideClassLoader : this.overrideClassLoaders) {
                try {
                    return Class.forName(className, this.initializeLoadedClasses, overrideClassLoader);
                }
                catch (ClassNotFoundException ex2) {}
                catch (LinkageError e2) {
                    if (linkageError != null) {
                        continue;
                    }
                    linkageError = e2;
                }
            }
        }
        if (this.overrideClassLoaders == null && this.environmentClassLoaderDelegationOrder != null && !this.environmentClassLoaderDelegationOrder.isEmpty()) {
            for (final ClassLoader envClassLoader : this.environmentClassLoaderDelegationOrder) {
                try {
                    return Class.forName(className, this.initializeLoadedClasses, envClassLoader);
                }
                catch (ClassNotFoundException ex3) {}
                catch (LinkageError e2) {
                    if (linkageError != null) {
                        continue;
                    }
                    linkageError = e2;
                }
            }
        }
        ClassLoader classInfoClassLoader = null;
        final ClassInfo classInfo = (this.scanResult.classNameToClassInfo == null) ? null : this.scanResult.classNameToClassInfo.get(className);
        if (classInfo != null) {
            classInfoClassLoader = classInfo.classLoader;
            Label_0290: {
                if (classInfoClassLoader != null) {
                    if (this.environmentClassLoaderDelegationOrder != null) {
                        if (this.environmentClassLoaderDelegationOrder.contains(classInfoClassLoader)) {
                            break Label_0290;
                        }
                    }
                    try {
                        return Class.forName(className, this.initializeLoadedClasses, classInfoClassLoader);
                    }
                    catch (ClassNotFoundException ex4) {}
                    catch (LinkageError e2) {
                        if (linkageError == null) {
                            linkageError = e2;
                        }
                    }
                }
            }
            if (classInfo.classpathElement instanceof ClasspathElementModule && !classInfo.isPublic()) {
                throw new ClassNotFoundException("Classfile for class " + className + " was found in a module, but the context and system classloaders could not load the class, probably because the class is not public.");
            }
        }
        if (this.overrideClassLoaders == null && this.classpathClassLoader != null) {
            try {
                return Class.forName(className, this.initializeLoadedClasses, this.classpathClassLoader);
            }
            catch (ClassNotFoundException ex5) {}
            catch (LinkageError e2) {
                if (linkageError == null) {
                    linkageError = e2;
                }
            }
        }
        if (this.addedClassLoaderDelegationOrder != null && !this.addedClassLoaderDelegationOrder.isEmpty()) {
            for (final ClassLoader additionalClassLoader : this.addedClassLoaderDelegationOrder) {
                if (additionalClassLoader != classInfoClassLoader) {
                    try {
                        return Class.forName(className, this.initializeLoadedClasses, additionalClassLoader);
                    }
                    catch (ClassNotFoundException ex6) {}
                    catch (LinkageError e3) {
                        if (linkageError != null) {
                            continue;
                        }
                        linkageError = e3;
                    }
                }
            }
        }
        final ResourceList classfileResources = this.scanResult.getResourcesWithPath(JarUtils.classNameToClassfilePath(className));
        if (classfileResources != null) {
            for (final Resource resource : classfileResources) {
                try {
                    try {
                        final ByteBuffer resourceByteBuffer = resource.read();
                        return this.defineClass(className, resourceByteBuffer, null);
                    }
                    finally {
                        resource.close();
                    }
                }
                catch (IOException e4) {
                    throw new ClassNotFoundException("Could not load classfile for class " + className + " : " + e4);
                }
                catch (LinkageError e5) {
                    if (linkageError == null) {
                        linkageError = e5;
                    }
                }
                finally {
                    resource.close();
                }
            }
        }
        if (linkageError != null) {
            if (VersionFinder.OS == VersionFinder.OperatingSystem.Windows) {
                final String msg = linkageError.getMessage();
                if (msg != null) {
                    final String wrongName = "(wrong name: ";
                    final int wrongNameIdx = msg.indexOf("(wrong name: ");
                    if (wrongNameIdx > -1) {
                        final String theWrongName = msg.substring(wrongNameIdx + "(wrong name: ".length(), msg.length() - 1);
                        if (theWrongName.replace('/', '.').equalsIgnoreCase(className)) {
                            throw new LinkageError("You appear to have two classfiles with the same case-insensitive name in the same directory on a case-insensitive filesystem -- this is not allowed on Windows, and therefore your code is not portable. Class name: " + className, linkageError);
                        }
                    }
                }
            }
            throw linkageError;
        }
        throw new ClassNotFoundException("Could not find or load classfile for class " + className);
    }
    
    public URL[] getURLs() {
        return this.scanResult.getClasspathURLs().toArray(new URL[0]);
    }
    
    @Override
    public URL getResource(final String path) {
        if (!this.environmentClassLoaderDelegationOrder.isEmpty()) {
            for (final ClassLoader envClassLoader : this.environmentClassLoaderDelegationOrder) {
                final URL resource = envClassLoader.getResource(path);
                if (resource != null) {
                    return resource;
                }
            }
        }
        if (!this.addedClassLoaderDelegationOrder.isEmpty()) {
            for (final ClassLoader additionalClassLoader : this.addedClassLoaderDelegationOrder) {
                final URL resource = additionalClassLoader.getResource(path);
                if (resource != null) {
                    return resource;
                }
            }
        }
        final ResourceList resourceList = this.scanResult.getResourcesWithPath(path);
        if (resourceList == null || resourceList.isEmpty()) {
            return super.getResource(path);
        }
        return resourceList.get(0).getURL();
    }
    
    @Override
    public Enumeration<URL> getResources(final String path) throws IOException {
        if (!this.environmentClassLoaderDelegationOrder.isEmpty()) {
            for (final ClassLoader envClassLoader : this.environmentClassLoaderDelegationOrder) {
                final Enumeration<URL> resources = envClassLoader.getResources(path);
                if (resources != null && resources.hasMoreElements()) {
                    return resources;
                }
            }
        }
        if (!this.addedClassLoaderDelegationOrder.isEmpty()) {
            for (final ClassLoader additionalClassLoader : this.addedClassLoaderDelegationOrder) {
                final Enumeration<URL> resources = additionalClassLoader.getResources(path);
                if (resources != null && resources.hasMoreElements()) {
                    return resources;
                }
            }
        }
        final ResourceList resourceList = this.scanResult.getResourcesWithPath(path);
        if (resourceList == null || resourceList.isEmpty()) {
            return Collections.emptyEnumeration();
        }
        return new Enumeration<URL>() {
            int idx;
            
            @Override
            public boolean hasMoreElements() {
                return this.idx < resourceList.size();
            }
            
            @Override
            public URL nextElement() {
                return resourceList.get(this.idx++).getURL();
            }
        };
    }
    
    @Override
    public InputStream getResourceAsStream(final String path) {
        if (!this.environmentClassLoaderDelegationOrder.isEmpty()) {
            for (final ClassLoader envClassLoader : this.environmentClassLoaderDelegationOrder) {
                final InputStream inputStream = envClassLoader.getResourceAsStream(path);
                if (inputStream != null) {
                    return inputStream;
                }
            }
        }
        if (!this.addedClassLoaderDelegationOrder.isEmpty()) {
            for (final ClassLoader additionalClassLoader : this.addedClassLoaderDelegationOrder) {
                final InputStream inputStream = additionalClassLoader.getResourceAsStream(path);
                if (inputStream != null) {
                    return inputStream;
                }
            }
        }
        final ResourceList resourceList = this.scanResult.getResourcesWithPath(path);
        if (resourceList == null || resourceList.isEmpty()) {
            return super.getResourceAsStream(path);
        }
        try {
            return resourceList.get(0).open();
        }
        catch (IOException e) {
            return null;
        }
    }
}
