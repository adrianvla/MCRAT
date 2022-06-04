// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import nonapi.io.github.classgraph.utils.LogNode;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Field;
import java.util.Iterator;
import nonapi.io.github.classgraph.types.ParseException;
import java.lang.reflect.Modifier;
import nonapi.io.github.classgraph.types.TypeUtils;
import java.util.List;

public class FieldInfo extends ScanResultObject implements Comparable<FieldInfo>, HasName
{
    private String declaringClassName;
    private String name;
    private int modifiers;
    private String typeSignatureStr;
    private String typeDescriptorStr;
    private transient TypeSignature typeSignature;
    private transient TypeSignature typeDescriptor;
    private ObjectTypedValueWrapper constantInitializerValue;
    AnnotationInfoList annotationInfo;
    private List<Classfile.TypeAnnotationDecorator> typeAnnotationDecorators;
    
    FieldInfo() {
    }
    
    FieldInfo(final String definingClassName, final String fieldName, final int modifiers, final String typeDescriptorStr, final String typeSignatureStr, final Object constantInitializerValue, final AnnotationInfoList annotationInfo, final List<Classfile.TypeAnnotationDecorator> typeAnnotationDecorators) {
        if (fieldName == null) {
            throw new IllegalArgumentException();
        }
        this.declaringClassName = definingClassName;
        this.name = fieldName;
        this.modifiers = modifiers;
        this.typeDescriptorStr = typeDescriptorStr;
        this.typeSignatureStr = typeSignatureStr;
        this.constantInitializerValue = ((constantInitializerValue == null) ? null : new ObjectTypedValueWrapper(constantInitializerValue));
        this.annotationInfo = ((annotationInfo == null || annotationInfo.isEmpty()) ? null : annotationInfo);
        this.typeAnnotationDecorators = typeAnnotationDecorators;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    public ClassInfo getClassInfo() {
        return super.getClassInfo();
    }
    
    @Deprecated
    public String getModifierStr() {
        return this.getModifiersStr();
    }
    
    public String getModifiersStr() {
        final StringBuilder buf = new StringBuilder();
        TypeUtils.modifiersToString(this.modifiers, TypeUtils.ModifierType.FIELD, false, buf);
        return buf.toString();
    }
    
    public boolean isPublic() {
        return Modifier.isPublic(this.modifiers);
    }
    
    public boolean isStatic() {
        return Modifier.isStatic(this.modifiers);
    }
    
    public boolean isFinal() {
        return Modifier.isFinal(this.modifiers);
    }
    
    public boolean isTransient() {
        return Modifier.isTransient(this.modifiers);
    }
    
    public int getModifiers() {
        return this.modifiers;
    }
    
    public TypeSignature getTypeDescriptor() {
        if (this.typeDescriptorStr == null) {
            return null;
        }
        if (this.typeDescriptor == null) {
            try {
                (this.typeDescriptor = TypeSignature.parse(this.typeDescriptorStr, this.declaringClassName)).setScanResult(this.scanResult);
                if (this.typeAnnotationDecorators != null) {
                    for (final Classfile.TypeAnnotationDecorator decorator : this.typeAnnotationDecorators) {
                        decorator.decorate(this.typeDescriptor);
                    }
                }
            }
            catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return this.typeDescriptor;
    }
    
    public String getTypeDescriptorStr() {
        return this.typeDescriptorStr;
    }
    
    public TypeSignature getTypeSignature() {
        if (this.typeSignatureStr == null) {
            return null;
        }
        if (this.typeSignature == null) {
            try {
                (this.typeSignature = TypeSignature.parse(this.typeSignatureStr, this.declaringClassName)).setScanResult(this.scanResult);
                if (this.typeAnnotationDecorators != null) {
                    for (final Classfile.TypeAnnotationDecorator decorator : this.typeAnnotationDecorators) {
                        decorator.decorate(this.typeSignature);
                    }
                }
            }
            catch (ParseException e) {
                throw new IllegalArgumentException("Invalid type signature for field " + this.getClassName() + "." + this.getName() + ((this.getClassInfo() != null) ? (" in classpath element " + this.getClassInfo().getClasspathElementURI()) : "") + " : " + this.typeSignatureStr, e);
            }
        }
        return this.typeSignature;
    }
    
    public String getTypeSignatureStr() {
        return this.typeSignatureStr;
    }
    
    public TypeSignature getTypeSignatureOrTypeDescriptor() {
        TypeSignature typeSig = null;
        try {
            typeSig = this.getTypeSignature();
            if (typeSig != null) {
                return typeSig;
            }
        }
        catch (Exception ex) {}
        return this.getTypeDescriptor();
    }
    
    public String getTypeSignatureOrTypeDescriptorStr() {
        if (this.typeSignatureStr != null) {
            return this.typeSignatureStr;
        }
        return this.typeDescriptorStr;
    }
    
    public Object getConstantInitializerValue() {
        if (!this.scanResult.scanSpec.enableStaticFinalFieldConstantInitializerValues) {
            throw new IllegalArgumentException("Please call ClassGraph#enableStaticFinalFieldConstantInitializerValues() before #scan()");
        }
        return (this.constantInitializerValue == null) ? null : this.constantInitializerValue.get();
    }
    
    public AnnotationInfoList getAnnotationInfo() {
        if (!this.scanResult.scanSpec.enableAnnotationInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableAnnotationInfo() before #scan()");
        }
        return (this.annotationInfo == null) ? AnnotationInfoList.EMPTY_LIST : AnnotationInfoList.getIndirectAnnotations(this.annotationInfo, null);
    }
    
    public AnnotationInfo getAnnotationInfo(final String annotationName) {
        return this.getAnnotationInfo().get(annotationName);
    }
    
    public AnnotationInfoList getAnnotationInfoRepeatable(final String annotationName) {
        return this.getAnnotationInfo().getRepeatable(annotationName);
    }
    
    public boolean hasAnnotation(final String annotationName) {
        return this.getAnnotationInfo().containsName(annotationName);
    }
    
    public Field loadClassAndGetField() throws IllegalArgumentException {
        try {
            return this.loadClass().getField(this.getName());
        }
        catch (NoSuchFieldException e1) {
            try {
                return this.loadClass().getDeclaredField(this.getName());
            }
            catch (NoSuchFieldException e2) {
                throw new IllegalArgumentException("No such field: " + this.getClassName() + "." + this.getName());
            }
        }
    }
    
    void handleRepeatableAnnotations(final Set<String> allRepeatableAnnotationNames) {
        if (this.annotationInfo != null) {
            this.annotationInfo.handleRepeatableAnnotations(allRepeatableAnnotationNames, this.getClassInfo(), ClassInfo.RelType.FIELD_ANNOTATIONS, ClassInfo.RelType.CLASSES_WITH_FIELD_ANNOTATION, ClassInfo.RelType.CLASSES_WITH_NONPRIVATE_FIELD_ANNOTATION);
        }
    }
    
    public String getClassName() {
        return this.declaringClassName;
    }
    
    @Override
    void setScanResult(final ScanResult scanResult) {
        super.setScanResult(scanResult);
        if (this.typeSignature != null) {
            this.typeSignature.setScanResult(scanResult);
        }
        if (this.typeDescriptor != null) {
            this.typeDescriptor.setScanResult(scanResult);
        }
        if (this.annotationInfo != null) {
            for (final AnnotationInfo ai : this.annotationInfo) {
                ai.setScanResult(scanResult);
            }
        }
    }
    
    @Override
    protected void findReferencedClassInfo(final Map<String, ClassInfo> classNameToClassInfo, final Set<ClassInfo> refdClassInfo, final LogNode log) {
        try {
            final TypeSignature fieldSig = this.getTypeSignature();
            if (fieldSig != null) {
                fieldSig.findReferencedClassInfo(classNameToClassInfo, refdClassInfo, log);
            }
        }
        catch (IllegalArgumentException e) {
            if (log != null) {
                log.log("Illegal type signature for field " + this.getClassName() + "." + this.getName() + ": " + this.getTypeSignatureStr());
            }
        }
        try {
            final TypeSignature fieldDesc = this.getTypeDescriptor();
            if (fieldDesc != null) {
                fieldDesc.findReferencedClassInfo(classNameToClassInfo, refdClassInfo, log);
            }
        }
        catch (IllegalArgumentException e) {
            if (log != null) {
                log.log("Illegal type descriptor for field " + this.getClassName() + "." + this.getName() + ": " + this.getTypeDescriptorStr());
            }
        }
        if (this.annotationInfo != null) {
            for (final AnnotationInfo ai : this.annotationInfo) {
                ai.findReferencedClassInfo(classNameToClassInfo, refdClassInfo, log);
            }
        }
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FieldInfo)) {
            return false;
        }
        final FieldInfo other = (FieldInfo)obj;
        return this.declaringClassName.equals(other.declaringClassName) && this.name.equals(other.name);
    }
    
    @Override
    public int hashCode() {
        return this.name.hashCode() + this.declaringClassName.hashCode() * 11;
    }
    
    @Override
    public int compareTo(final FieldInfo other) {
        final int diff = this.declaringClassName.compareTo(other.declaringClassName);
        if (diff != 0) {
            return diff;
        }
        return this.name.compareTo(other.name);
    }
    
    @Override
    protected void toString(final boolean useSimpleNames, final StringBuilder buf) {
        if (this.annotationInfo != null) {
            for (final AnnotationInfo annotation : this.annotationInfo) {
                if (buf.length() > 0) {
                    buf.append(' ');
                }
                annotation.toString(useSimpleNames, buf);
            }
        }
        if (this.modifiers != 0) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            TypeUtils.modifiersToString(this.modifiers, TypeUtils.ModifierType.FIELD, false, buf);
        }
        if (buf.length() > 0) {
            buf.append(' ');
        }
        final TypeSignature typeSig = this.getTypeSignatureOrTypeDescriptor();
        typeSig.toStringInternal(useSimpleNames, this.annotationInfo, buf);
        buf.append(' ');
        buf.append(this.name);
        if (this.constantInitializerValue != null) {
            final Object val = this.constantInitializerValue.get();
            buf.append(" = ");
            if (val instanceof String) {
                buf.append('\"').append(((String)val).replace("\\", "\\\\").replace("\"", "\\\"")).append('\"');
            }
            else if (val instanceof Character) {
                buf.append('\'').append(((Character)val).toString().replace("\\", "\\\\").replaceAll("'", "\\'")).append('\'');
            }
            else {
                buf.append((val == null) ? "null" : val.toString());
            }
        }
    }
}
