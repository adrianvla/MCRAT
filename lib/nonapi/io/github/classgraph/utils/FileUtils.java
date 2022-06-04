// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.utils;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.lang.reflect.Method;

public final class FileUtils
{
    private static Method directByteBufferCleanerMethod;
    private static Method cleanerCleanMethod;
    private static Method attachmentMethod;
    private static Object theUnsafe;
    private static String currDirPath;
    public static final int MAX_BUFFER_SIZE = 2147483639;
    
    private FileUtils() {
    }
    
    public static String currDirPath() {
        if (FileUtils.currDirPath == null) {
            Path path = null;
            final String currDirPathStr = System.getProperty("user.dir");
            if (currDirPathStr != null) {
                try {
                    path = Paths.get(currDirPathStr, new String[0]);
                }
                catch (InvalidPathException ex) {}
            }
            if (path == null) {
                try {
                    path = Paths.get("", new String[0]);
                }
                catch (InvalidPathException ex2) {}
            }
            FileUtils.currDirPath = FastPathResolver.resolve((path == null) ? "" : path.toString());
        }
        return FileUtils.currDirPath;
    }
    
    public static String sanitizeEntryPath(final String path, final boolean removeInitialSlash, final boolean removeFinalSlash) {
        if (path.isEmpty()) {
            return "";
        }
        boolean foundSegmentToSanitize = false;
        final int pathLen = path.length();
        final char[] pathChars = new char[pathLen];
        path.getChars(0, pathLen, pathChars, 0);
        int lastSepIdx = -1;
        char prevC = '\0';
        for (int i = 0, ii = pathLen + 1; i < ii; ++i) {
            final char c = (i == pathLen) ? '\0' : pathChars[i];
            if (c == '/' || c == '!' || c == '\0') {
                final int segmentLength = i - (lastSepIdx + 1);
                if ((segmentLength == 0 && prevC == c) || (segmentLength == 1 && pathChars[i - 1] == '.') || (segmentLength == 2 && pathChars[i - 2] == '.' && pathChars[i - 1] == '.')) {
                    foundSegmentToSanitize = true;
                }
                lastSepIdx = i;
            }
            prevC = c;
        }
        final boolean pathHasInitialSlash = pathLen > 0 && pathChars[0] == '/';
        final StringBuilder pathSanitized = new StringBuilder(pathLen + 16);
        if (foundSegmentToSanitize) {
            final List<List<CharSequence>> allSectionSegments = new ArrayList<List<CharSequence>>();
            List<CharSequence> currSectionSegments = new ArrayList<CharSequence>();
            allSectionSegments.add(currSectionSegments);
            int lastSepIdx2 = -1;
            for (int j = 0; j < pathLen + 1; ++j) {
                final char c2 = (j == pathLen) ? '\0' : pathChars[j];
                if (c2 == '/' || c2 == '!' || c2 == '\0') {
                    final int segmentStartIdx = lastSepIdx2 + 1;
                    final int segmentLen = j - segmentStartIdx;
                    if (segmentLen != 0) {
                        if (segmentLen != 1 || pathChars[segmentStartIdx] != '.') {
                            if (segmentLen == 2 && pathChars[segmentStartIdx] == '.' && pathChars[segmentStartIdx + 1] == '.') {
                                if (!currSectionSegments.isEmpty()) {
                                    currSectionSegments.remove(currSectionSegments.size() - 1);
                                }
                            }
                            else {
                                currSectionSegments.add(path.subSequence(segmentStartIdx, segmentStartIdx + segmentLen));
                            }
                        }
                    }
                    if (c2 == '!' && !currSectionSegments.isEmpty()) {
                        currSectionSegments = new ArrayList<CharSequence>();
                        allSectionSegments.add(currSectionSegments);
                    }
                    lastSepIdx2 = j;
                }
            }
            for (final List<CharSequence> sectionSegments : allSectionSegments) {
                if (!sectionSegments.isEmpty()) {
                    if (pathSanitized.length() > 0) {
                        pathSanitized.append('!');
                    }
                    for (final CharSequence sectionSegment : sectionSegments) {
                        pathSanitized.append('/');
                        pathSanitized.append(sectionSegment);
                    }
                }
            }
            if (pathSanitized.length() == 0 && pathHasInitialSlash) {
                pathSanitized.append('/');
            }
        }
        else {
            pathSanitized.append(path);
        }
        int startIdx = 0;
        if (removeInitialSlash || !pathHasInitialSlash) {
            while (startIdx < pathSanitized.length() && pathSanitized.charAt(startIdx) == '/') {
                ++startIdx;
            }
        }
        if (removeFinalSlash) {
            while (pathSanitized.length() > 0 && pathSanitized.charAt(pathSanitized.length() - 1) == '/') {
                pathSanitized.setLength(pathSanitized.length() - 1);
            }
        }
        return pathSanitized.substring(startIdx);
    }
    
    public static boolean isClassfile(final String path) {
        final int len = path.length();
        return len > 6 && path.regionMatches(true, len - 6, ".class", 0, 6);
    }
    
    public static boolean canRead(final File file) {
        try {
            return file.canRead();
        }
        catch (SecurityException e) {
            return false;
        }
    }
    
    public static boolean canReadAndIsFile(final File file) {
        try {
            if (!file.canRead()) {
                return false;
            }
        }
        catch (SecurityException e) {
            return false;
        }
        return file.isFile();
    }
    
    public static boolean canReadAndIsFile(final Path path) {
        try {
            if (!Files.exists(path, new LinkOption[0])) {
                return false;
            }
        }
        catch (SecurityException e) {
            return false;
        }
        return Files.isRegularFile(path, new LinkOption[0]);
    }
    
    public static void checkCanReadAndIsFile(final File file) throws IOException {
        try {
            if (!file.canRead()) {
                throw new FileNotFoundException("File does not exist or cannot be read: " + file);
            }
        }
        catch (SecurityException e) {
            throw new FileNotFoundException("File " + file + " cannot be accessed: " + e);
        }
        if (!file.isFile()) {
            throw new IOException("Not a regular file: " + file);
        }
    }
    
    public static void checkCanReadAndIsFile(final Path path) throws IOException {
        try {
            if (!Files.exists(path, new LinkOption[0])) {
                throw new FileNotFoundException("Path does not exist or cannot be read: " + path);
            }
        }
        catch (SecurityException e) {
            throw new FileNotFoundException("Path " + path + " cannot be accessed: " + e);
        }
        if (!Files.isRegularFile(path, new LinkOption[0])) {
            throw new IOException("Not a regular file: " + path);
        }
    }
    
    public static boolean canReadAndIsDir(final File file) {
        try {
            if (!file.canRead()) {
                return false;
            }
        }
        catch (SecurityException e) {
            return false;
        }
        return file.isDirectory();
    }
    
    public static boolean canReadAndIsDir(final Path path) {
        try {
            if (!Files.exists(path, new LinkOption[0])) {
                return false;
            }
        }
        catch (SecurityException e) {
            return false;
        }
        return Files.isDirectory(path, new LinkOption[0]);
    }
    
    public static void checkCanReadAndIsDir(final File file) throws IOException {
        try {
            if (!file.canRead()) {
                throw new FileNotFoundException("Directory does not exist or cannot be read: " + file);
            }
        }
        catch (SecurityException e) {
            throw new FileNotFoundException("File " + file + " cannot be accessed: " + e);
        }
        if (!file.isDirectory()) {
            throw new IOException("Not a directory: " + file);
        }
    }
    
    public static String getParentDirPath(final String path, final char separator) {
        final int lastSlashIdx = path.lastIndexOf(separator);
        if (lastSlashIdx <= 0) {
            return "";
        }
        return path.substring(0, lastSlashIdx);
    }
    
    public static String getParentDirPath(final String path) {
        return getParentDirPath(path, '/');
    }
    
    private static void lookupCleanMethodPrivileged() {
        while (true) {
            if (VersionFinder.JAVA_MAJOR_VERSION < 9) {
                try {
                    (FileUtils.cleanerCleanMethod = Class.forName("sun.misc.Cleaner").getDeclaredMethod("clean", (Class<?>[])new Class[0])).setAccessible(true);
                    final Class<?> directByteBufferClass = Class.forName("sun.nio.ch.DirectBuffer");
                    FileUtils.directByteBufferCleanerMethod = directByteBufferClass.getDeclaredMethod("cleaner", (Class<?>[])new Class[0]);
                    (FileUtils.attachmentMethod = directByteBufferClass.getMethod("attachment", (Class<?>[])new Class[0])).setAccessible(true);
                    return;
                }
                catch (SecurityException e) {
                    throw new RuntimeException("You need to grant classgraph RuntimePermission(\"accessClassInPackage.sun.misc\") and ReflectPermission(\"suppressAccessChecks\")", e);
                }
                catch (ReflectiveOperationException | LinkageError ex) {
                    return;
                }
                try {
                    Class<?> unsafeClass;
                    try {
                        unsafeClass = Class.forName("sun.misc.Unsafe");
                    }
                    catch (ReflectiveOperationException | LinkageError ex2) {
                        final Throwable t;
                        final Throwable e2 = t;
                        throw new RuntimeException("Could not get class sun.misc.Unsafe", e2);
                    }
                    final Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
                    theUnsafeField.setAccessible(true);
                    FileUtils.theUnsafe = theUnsafeField.get(null);
                    (FileUtils.cleanerCleanMethod = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class)).setAccessible(true);
                }
                catch (SecurityException e) {
                    throw new RuntimeException("You need to grant classgraph RuntimePermission(\"accessClassInPackage.sun.misc\") and ReflectPermission(\"suppressAccessChecks\")", e);
                }
                catch (ReflectiveOperationException ex3) {}
                catch (LinkageError linkageError) {}
                return;
            }
            continue;
        }
    }
    
    private static boolean closeDirectByteBufferPrivileged(final ByteBuffer byteBuffer, final LogNode log) {
        if (!byteBuffer.isDirect()) {
            return true;
        }
        try {
            if (VersionFinder.JAVA_MAJOR_VERSION < 9) {
                if (FileUtils.attachmentMethod == null) {
                    if (log != null) {
                        log.log("Could not unmap ByteBuffer, attachmentMethod == null");
                    }
                    return false;
                }
                if (FileUtils.attachmentMethod.invoke(byteBuffer, new Object[0]) != null) {
                    return false;
                }
                if (FileUtils.directByteBufferCleanerMethod == null) {
                    if (log != null) {
                        log.log("Could not unmap ByteBuffer, cleanerMethod == null");
                    }
                    return false;
                }
                try {
                    FileUtils.directByteBufferCleanerMethod.setAccessible(true);
                }
                catch (Exception e2) {
                    if (log != null) {
                        log.log("Could not unmap ByteBuffer, cleanerMethod.setAccessible(true) failed");
                    }
                    return false;
                }
                final Object cleanerInstance = FileUtils.directByteBufferCleanerMethod.invoke(byteBuffer, new Object[0]);
                if (cleanerInstance == null) {
                    if (log != null) {
                        log.log("Could not unmap ByteBuffer, cleaner == null");
                    }
                    return false;
                }
                if (FileUtils.cleanerCleanMethod == null) {
                    if (log != null) {
                        log.log("Could not unmap ByteBuffer, cleanMethod == null");
                    }
                    return false;
                }
                try {
                    FileUtils.cleanerCleanMethod.invoke(cleanerInstance, new Object[0]);
                    return true;
                }
                catch (Exception e) {
                    if (log != null) {
                        log.log("Could not unmap ByteBuffer, cleanMethod.invoke(cleaner) failed: " + e);
                    }
                    return false;
                }
            }
            if (FileUtils.theUnsafe == null) {
                if (log != null) {
                    log.log("Could not unmap ByteBuffer, theUnsafe == null");
                }
                return false;
            }
            if (FileUtils.cleanerCleanMethod == null) {
                if (log != null) {
                    log.log("Could not unmap ByteBuffer, cleanMethod == null");
                }
                return false;
            }
            try {
                FileUtils.cleanerCleanMethod.invoke(FileUtils.theUnsafe, byteBuffer);
                return true;
            }
            catch (IllegalArgumentException e3) {
                return false;
            }
        }
        catch (ReflectiveOperationException | SecurityException ex2) {
            final Exception ex;
            final Exception e2 = ex;
            if (log != null) {
                log.log("Could not unmap ByteBuffer: " + e2);
            }
            return false;
        }
    }
    
    public static boolean closeDirectByteBuffer(final ByteBuffer byteBuffer, final LogNode log) {
        return byteBuffer != null && byteBuffer.isDirect() && AccessController.doPrivileged((PrivilegedAction<Boolean>)new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                return closeDirectByteBufferPrivileged(byteBuffer, log);
            }
        });
    }
    
    static {
        AccessController.doPrivileged((PrivilegedAction<Object>)new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                lookupCleanMethodPrivileged();
                return null;
            }
        });
    }
}
