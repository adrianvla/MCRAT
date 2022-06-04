// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import nonapi.io.github.classgraph.types.Parser;
import java.util.List;

public class BaseTypeSignature extends TypeSignature
{
    private final char typeSignatureChar;
    
    BaseTypeSignature(final char typeSignatureChar) {
        switch (typeSignatureChar) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'V':
            case 'Z': {
                this.typeSignatureChar = typeSignatureChar;
            }
            default: {
                throw new IllegalArgumentException("Illegal " + BaseTypeSignature.class.getSimpleName() + " type: '" + typeSignatureChar + "'");
            }
        }
    }
    
    static String getTypeStr(final char typeChar) {
        switch (typeChar) {
            case 'B': {
                return "byte";
            }
            case 'C': {
                return "char";
            }
            case 'D': {
                return "double";
            }
            case 'F': {
                return "float";
            }
            case 'I': {
                return "int";
            }
            case 'J': {
                return "long";
            }
            case 'S': {
                return "short";
            }
            case 'Z': {
                return "boolean";
            }
            case 'V': {
                return "void";
            }
            default: {
                return null;
            }
        }
    }
    
    static char getTypeChar(final String typeStr) {
        switch (typeStr) {
            case "byte": {
                return 'B';
            }
            case "char": {
                return 'C';
            }
            case "double": {
                return 'D';
            }
            case "float": {
                return 'F';
            }
            case "int": {
                return 'I';
            }
            case "long": {
                return 'J';
            }
            case "short": {
                return 'S';
            }
            case "boolean": {
                return 'Z';
            }
            case "void": {
                return 'V';
            }
            default: {
                return '\0';
            }
        }
    }
    
    static Class<?> getType(final char typeChar) {
        switch (typeChar) {
            case 'B': {
                return Byte.TYPE;
            }
            case 'C': {
                return Character.TYPE;
            }
            case 'D': {
                return Double.TYPE;
            }
            case 'F': {
                return Float.TYPE;
            }
            case 'I': {
                return Integer.TYPE;
            }
            case 'J': {
                return Long.TYPE;
            }
            case 'S': {
                return Short.TYPE;
            }
            case 'Z': {
                return Boolean.TYPE;
            }
            case 'V': {
                return Void.TYPE;
            }
            default: {
                return null;
            }
        }
    }
    
    public char getTypeSignatureChar() {
        return this.typeSignatureChar;
    }
    
    public String getTypeStr() {
        return getTypeStr(this.typeSignatureChar);
    }
    
    public Class<?> getType() {
        return getType(this.typeSignatureChar);
    }
    
    @Override
    protected void addTypeAnnotation(final List<Classfile.TypePathNode> typePath, final AnnotationInfo annotationInfo) {
        this.addTypeAnnotation(annotationInfo);
    }
    
    @Override
    Class<?> loadClass() {
        return this.getType();
    }
    
    @Override
     <T> Class<T> loadClass(final Class<T> superclassOrInterfaceType) {
        final Class<?> type = this.getType();
        if (!superclassOrInterfaceType.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Primitive class " + this.getTypeStr() + " cannot be cast to " + superclassOrInterfaceType.getName());
        }
        final Class<T> classT = (Class<T>)type;
        return classT;
    }
    
    static BaseTypeSignature parse(final Parser parser) {
        switch (parser.peek()) {
            case 'B': {
                parser.next();
                return new BaseTypeSignature('B');
            }
            case 'C': {
                parser.next();
                return new BaseTypeSignature('C');
            }
            case 'D': {
                parser.next();
                return new BaseTypeSignature('D');
            }
            case 'F': {
                parser.next();
                return new BaseTypeSignature('F');
            }
            case 'I': {
                parser.next();
                return new BaseTypeSignature('I');
            }
            case 'J': {
                parser.next();
                return new BaseTypeSignature('J');
            }
            case 'S': {
                parser.next();
                return new BaseTypeSignature('S');
            }
            case 'Z': {
                parser.next();
                return new BaseTypeSignature('Z');
            }
            case 'V': {
                parser.next();
                return new BaseTypeSignature('V');
            }
            default: {
                return null;
            }
        }
    }
    
    @Override
    protected String getClassName() {
        return this.getTypeStr();
    }
    
    protected ClassInfo getClassInfo() {
        return null;
    }
    
    @Override
    protected void findReferencedClassNames(final Set<String> refdClassNames) {
    }
    
    @Override
    void setScanResult(final ScanResult scanResult) {
    }
    
    @Override
    public int hashCode() {
        return this.typeSignatureChar;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BaseTypeSignature)) {
            return false;
        }
        final BaseTypeSignature other = (BaseTypeSignature)obj;
        return Objects.equals(this.typeAnnotationInfo, other.typeAnnotationInfo) && other.typeSignatureChar == this.typeSignatureChar;
    }
    
    @Override
    public boolean equalsIgnoringTypeParams(final TypeSignature other) {
        return other instanceof BaseTypeSignature && this.typeSignatureChar == ((BaseTypeSignature)other).typeSignatureChar;
    }
    
    @Override
    protected void toStringInternal(final boolean useSimpleNames, final AnnotationInfoList annotationsToExclude, final StringBuilder buf) {
        if (this.typeAnnotationInfo != null) {
            for (final AnnotationInfo annotationInfo : this.typeAnnotationInfo) {
                if (annotationsToExclude == null || !annotationsToExclude.contains(annotationInfo)) {
                    annotationInfo.toString(useSimpleNames, buf);
                    buf.append(' ');
                }
            }
        }
        buf.append(this.getTypeStr());
    }
}
