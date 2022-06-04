// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import nonapi.io.github.classgraph.types.ParseException;
import nonapi.io.github.classgraph.types.Parser;
import java.util.Iterator;
import java.util.Objects;
import java.lang.reflect.Array;
import java.util.Set;
import java.util.List;

public class ArrayTypeSignature extends ReferenceTypeSignature
{
    private final String typeSignatureStr;
    private String className;
    private ArrayClassInfo arrayClassInfo;
    private Class<?> elementClassRef;
    private final TypeSignature nestedType;
    
    ArrayTypeSignature(final TypeSignature elementTypeSignature, final int numDims, final String typeSignatureStr) {
        final boolean typeSigHasTwoOrMoreDims = typeSignatureStr.startsWith("[[");
        if (numDims < 1) {
            throw new IllegalArgumentException("numDims < 1");
        }
        if (numDims >= 2 != typeSigHasTwoOrMoreDims) {
            throw new IllegalArgumentException("numDims does not match type signature");
        }
        this.typeSignatureStr = typeSignatureStr;
        this.nestedType = (typeSigHasTwoOrMoreDims ? new ArrayTypeSignature(elementTypeSignature, numDims - 1, typeSignatureStr.substring(1)) : elementTypeSignature);
    }
    
    public String getTypeSignatureStr() {
        return this.typeSignatureStr;
    }
    
    public TypeSignature getElementTypeSignature() {
        ArrayTypeSignature curr;
        for (curr = this; curr.nestedType instanceof ArrayTypeSignature; curr = (ArrayTypeSignature)curr.nestedType) {}
        return curr.getNestedType();
    }
    
    public int getNumDimensions() {
        int numDims = 1;
        for (ArrayTypeSignature curr = this; curr.nestedType instanceof ArrayTypeSignature; curr = (ArrayTypeSignature)curr.nestedType, ++numDims) {}
        return numDims;
    }
    
    public TypeSignature getNestedType() {
        return this.nestedType;
    }
    
    @Override
    protected void addTypeAnnotation(final List<Classfile.TypePathNode> typePath, final AnnotationInfo annotationInfo) {
        if (typePath.isEmpty()) {
            this.addTypeAnnotation(annotationInfo);
        }
        else {
            final Classfile.TypePathNode head = typePath.get(0);
            if (head.typePathKind != 0 || head.typeArgumentIdx != 0) {
                throw new IllegalArgumentException("typePath element contains bad values: " + head);
            }
            this.nestedType.addTypeAnnotation(typePath.subList(1, typePath.size()), annotationInfo);
        }
    }
    
    @Override
    public AnnotationInfoList getTypeAnnotationInfo() {
        return this.typeAnnotationInfo;
    }
    
    @Override
    protected String getClassName() {
        if (this.className == null) {
            this.className = this.toString();
        }
        return this.className;
    }
    
    protected ClassInfo getClassInfo() {
        return this.getArrayClassInfo();
    }
    
    public ArrayClassInfo getArrayClassInfo() {
        if (this.arrayClassInfo == null) {
            if (this.scanResult != null) {
                final String clsName = this.getClassName();
                this.arrayClassInfo = this.scanResult.classNameToClassInfo.get(clsName);
                if (this.arrayClassInfo == null) {
                    this.scanResult.classNameToClassInfo.put(clsName, this.arrayClassInfo = new ArrayClassInfo(this));
                    this.arrayClassInfo.setScanResult(this.scanResult);
                }
            }
            else {
                this.arrayClassInfo = new ArrayClassInfo(this);
            }
        }
        return this.arrayClassInfo;
    }
    
    @Override
    void setScanResult(final ScanResult scanResult) {
        super.setScanResult(scanResult);
        this.nestedType.setScanResult(scanResult);
        if (this.arrayClassInfo != null) {
            this.arrayClassInfo.setScanResult(scanResult);
        }
    }
    
    @Override
    protected void findReferencedClassNames(final Set<String> refdClassNames) {
        this.nestedType.findReferencedClassNames(refdClassNames);
    }
    
    public Class<?> loadElementClass(final boolean ignoreExceptions) {
        if (this.elementClassRef == null) {
            final TypeSignature elementTypeSignature = this.getElementTypeSignature();
            if (elementTypeSignature instanceof BaseTypeSignature) {
                this.elementClassRef = ((BaseTypeSignature)elementTypeSignature).getType();
            }
            else if (this.scanResult != null) {
                this.elementClassRef = elementTypeSignature.loadClass(ignoreExceptions);
            }
            else {
                final String elementTypeName = ((ClassRefTypeSignature)elementTypeSignature).getClassName();
                try {
                    this.elementClassRef = Class.forName(elementTypeName);
                }
                catch (Throwable t) {
                    if (!ignoreExceptions) {
                        throw new IllegalArgumentException("Could not load array element class " + elementTypeName, t);
                    }
                }
            }
        }
        return this.elementClassRef;
    }
    
    public Class<?> loadElementClass() {
        return this.loadElementClass(false);
    }
    
    public Class<?> loadClass(final boolean ignoreExceptions) {
        if (this.classRef == null) {
            Class<?> eltClassRef = null;
            Label_0029: {
                if (ignoreExceptions) {
                    try {
                        eltClassRef = this.loadElementClass();
                        break Label_0029;
                    }
                    catch (IllegalArgumentException e) {
                        return null;
                    }
                }
                eltClassRef = this.loadElementClass();
            }
            if (eltClassRef == null) {
                throw new IllegalArgumentException("Could not load array element class " + this.getElementTypeSignature());
            }
            final Object eltArrayInstance = Array.newInstance(eltClassRef, new int[this.getNumDimensions()]);
            this.classRef = eltArrayInstance.getClass();
        }
        return this.classRef;
    }
    
    public Class<?> loadClass() {
        return this.loadClass(false);
    }
    
    @Override
    public int hashCode() {
        return 1 + this.nestedType.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ArrayTypeSignature)) {
            return false;
        }
        final ArrayTypeSignature other = (ArrayTypeSignature)obj;
        return Objects.equals(this.typeAnnotationInfo, other.typeAnnotationInfo) && this.nestedType.equals(other.nestedType);
    }
    
    @Override
    public boolean equalsIgnoringTypeParams(final TypeSignature other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ArrayTypeSignature)) {
            return false;
        }
        final ArrayTypeSignature o = (ArrayTypeSignature)other;
        return this.nestedType.equalsIgnoringTypeParams(o.nestedType);
    }
    
    @Override
    protected void toStringInternal(final boolean useSimpleNames, final AnnotationInfoList annotationsToExclude, final StringBuilder buf) {
        this.getElementTypeSignature().toStringInternal(useSimpleNames, annotationsToExclude, buf);
        ArrayTypeSignature curr = this;
        while (true) {
            if (curr.typeAnnotationInfo != null && !curr.typeAnnotationInfo.isEmpty()) {
                for (final AnnotationInfo annotationInfo : curr.typeAnnotationInfo) {
                    if (buf.length() == 0 || buf.charAt(buf.length() - 1) != ' ') {
                        buf.append(' ');
                    }
                    annotationInfo.toString(useSimpleNames, buf);
                }
                buf.append(' ');
            }
            buf.append("[]");
            if (!(curr.nestedType instanceof ArrayTypeSignature)) {
                break;
            }
            curr = (ArrayTypeSignature)curr.nestedType;
        }
    }
    
    static ArrayTypeSignature parse(final Parser parser, final String definingClassName) throws ParseException {
        int numArrayDims = 0;
        final int begin = parser.getPosition();
        while (parser.peek() == '[') {
            ++numArrayDims;
            parser.next();
        }
        if (numArrayDims <= 0) {
            return null;
        }
        final TypeSignature elementTypeSignature = TypeSignature.parse(parser, definingClassName);
        if (elementTypeSignature == null) {
            throw new ParseException(parser, "elementTypeSignature == null");
        }
        final String typeSignatureStr = parser.getSubsequence(begin, parser.getPosition()).toString();
        return new ArrayTypeSignature(elementTypeSignature, numArrayDims, typeSignatureStr);
    }
}
