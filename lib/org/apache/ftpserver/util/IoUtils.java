// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.util;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.File;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Random;

public class IoUtils
{
    private static final Random RANDOM_GEN;
    
    public static final BufferedInputStream getBufferedInputStream(final InputStream in) {
        BufferedInputStream bin = null;
        if (in instanceof BufferedInputStream) {
            bin = (BufferedInputStream)in;
        }
        else {
            bin = new BufferedInputStream(in);
        }
        return bin;
    }
    
    public static final BufferedOutputStream getBufferedOutputStream(final OutputStream out) {
        BufferedOutputStream bout = null;
        if (out instanceof BufferedOutputStream) {
            bout = (BufferedOutputStream)out;
        }
        else {
            bout = new BufferedOutputStream(out);
        }
        return bout;
    }
    
    public static final BufferedReader getBufferedReader(final Reader reader) {
        BufferedReader buffered = null;
        if (reader instanceof BufferedReader) {
            buffered = (BufferedReader)reader;
        }
        else {
            buffered = new BufferedReader(reader);
        }
        return buffered;
    }
    
    public static final BufferedWriter getBufferedWriter(final Writer wr) {
        BufferedWriter bw = null;
        if (wr instanceof BufferedWriter) {
            bw = (BufferedWriter)wr;
        }
        else {
            bw = new BufferedWriter(wr);
        }
        return bw;
    }
    
    public static final File getUniqueFile(final File oldFile) {
        File newFile;
        for (newFile = oldFile; newFile.exists(); newFile = new File(oldFile.getAbsolutePath() + '.' + Math.abs(IoUtils.RANDOM_GEN.nextLong()))) {}
        return newFile;
    }
    
    public static final void close(final InputStream is) {
        if (is != null) {
            try {
                is.close();
            }
            catch (Exception ex) {}
        }
    }
    
    public static final void close(final OutputStream os) {
        if (os != null) {
            try {
                os.close();
            }
            catch (Exception ex) {}
        }
    }
    
    public static final void close(final Reader rd) {
        if (rd != null) {
            try {
                rd.close();
            }
            catch (Exception ex) {}
        }
    }
    
    public static final void close(final Writer wr) {
        if (wr != null) {
            try {
                wr.close();
            }
            catch (Exception ex) {}
        }
    }
    
    public static final String getStackTrace(final Throwable ex) {
        String result = "";
        if (ex != null) {
            try {
                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                pw.close();
                sw.close();
                result = sw.toString();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    
    public static final void copy(final Reader input, final Writer output, final int bufferSize) throws IOException {
        final char[] buffer = new char[bufferSize];
        int n = 0;
        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
        }
    }
    
    public static final void copy(final InputStream input, final OutputStream output, final int bufferSize) throws IOException {
        final byte[] buffer = new byte[bufferSize];
        int n = 0;
        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
        }
    }
    
    public static final String readFully(final Reader reader) throws IOException {
        final StringWriter writer = new StringWriter();
        copy(reader, writer, 1024);
        return writer.toString();
    }
    
    public static final String readFully(final InputStream input) throws IOException {
        final StringWriter writer = new StringWriter();
        final InputStreamReader reader = new InputStreamReader(input);
        copy(reader, writer, 1024);
        return writer.toString();
    }
    
    public static final void delete(final File file) throws IOException {
        if (file.isDirectory()) {
            deleteDir(file);
        }
        else {
            deleteFile(file);
        }
    }
    
    private static final void deleteDir(final File dir) throws IOException {
        final File[] children = dir.listFiles();
        if (children == null) {
            return;
        }
        for (int i = 0; i < children.length; ++i) {
            final File file = children[i];
            delete(file);
        }
        if (!dir.delete()) {
            throw new IOException("Failed to delete directory: " + dir);
        }
    }
    
    private static final void deleteFile(final File file) throws IOException {
        if (!file.delete()) {
            if (OS.isFamilyWindows()) {
                System.gc();
            }
            try {
                Thread.sleep(10L);
            }
            catch (InterruptedException ex) {}
            if (!file.delete()) {
                throw new IOException("Failed to delete file: " + file);
            }
        }
    }
    
    static {
        RANDOM_GEN = new Random(System.currentTimeMillis());
    }
}
