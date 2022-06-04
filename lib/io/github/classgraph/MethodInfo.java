// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import nonapi.io.github.classgraph.utils.LogNode;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.ArrayList;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import nonapi.io.github.classgraph.types.ParseException;
import nonapi.io.github.classgraph.types.TypeUtils;
import java.util.List;

public class MethodInfo extends ScanResultObject implements Comparable<MethodInfo>, HasName
{
    private String declaringClassName;
    private String name;
    private int modifiers;
    AnnotationInfoList annotationInfo;
    private String typeDescriptorStr;
    private transient MethodTypeSignature typeDescriptor;
    private String typeSignatureStr;
    private transient MethodTypeSignature typeSignature;
    private String[] parameterNames;
    private int[] parameterModifiers;
    AnnotationInfo[][] parameterAnnotationInfo;
    private transient MethodParameterInfo[] parameterInfo;
    private boolean hasBody;
    private List<Classfile.MethodTypeAnnotationDecorator> typeAnnotationDecorators;
    
    MethodInfo() {
    }
    
    MethodInfo(final String definingClassName, final String methodName, final AnnotationInfoList methodAnnotationInfo, final int modifiers, final String typeDescriptorStr, final String typeSignatureStr, final String[] parameterNames, final int[] parameterModifiers, final AnnotationInfo[][] parameterAnnotationInfo, final boolean hasBody, final List<Classfile.MethodTypeAnnotationDecorator> methodTypeAnnotationDecorators) {
        this.declaringClassName = definingClassName;
        this.name = methodName;
        this.modifiers = modifiers;
        this.typeDescriptorStr = typeDescriptorStr;
        this.typeSignatureStr = typeSignatureStr;
        this.parameterNames = parameterNames;
        this.parameterModifiers = parameterModifiers;
        this.parameterAnnotationInfo = parameterAnnotationInfo;
        this.annotationInfo = ((methodAnnotationInfo == null || methodAnnotationInfo.isEmpty()) ? null : methodAnnotationInfo);
        this.hasBody = hasBody;
        this.typeAnnotationDecorators = methodTypeAnnotationDecorators;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    public int getModifiers() {
        return this.modifiers;
    }
    
    public String getModifiersStr() {
        final StringBuilder buf = new StringBuilder();
        TypeUtils.modifiersToString(this.modifiers, TypeUtils.ModifierType.METHOD, this.isDefault(), buf);
        return buf.toString();
    }
    
    public ClassInfo getClassInfo() {
        return super.getClassInfo();
    }
    
    public MethodTypeSignature getTypeDescriptor() {
        if (this.typeDescriptor == null) {
            try {
                (this.typeDescriptor = MethodTypeSignature.parse(this.typeDescriptorStr, this.declaringClassName)).setScanResult(this.scanResult);
                if (this.typeAnnotationDecorators != null) {
                    for (final Classfile.MethodTypeAnnotationDecorator decorator : this.typeAnnotationDecorators) {
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
    
    public MethodTypeSignature getTypeSignature() {
        if (this.typeSignature == null && this.typeSignatureStr != null) {
            try {
                (this.typeSignature = MethodTypeSignature.parse(this.typeSignatureStr, this.declaringClassName)).setScanResult(this.scanResult);
                if (this.typeAnnotationDecorators != null) {
                    for (final Classfile.MethodTypeAnnotationDecorator decorator : this.typeAnnotationDecorators) {
                        decorator.decorate(this.typeSignature);
                    }
                }
            }
            catch (ParseException e) {
                throw new IllegalArgumentException("Invalid type signature for method " + this.getClassName() + "." + this.getName() + ((this.getClassInfo() != null) ? (" in classpath element " + this.getClassInfo().getClasspathElementURI()) : "") + " : " + this.typeSignatureStr, e);
            }
        }
        return this.typeSignature;
    }
    
    public String getTypeSignatureStr() {
        return this.typeSignatureStr;
    }
    
    public MethodTypeSignature getTypeSignatureOrTypeDescriptor() {
        MethodTypeSignature typeSig = null;
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
    
    public boolean isConstructor() {
        return "<init>".equals(this.name);
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
    
    public boolean isSynchronized() {
        return Modifier.isSynchronized(this.modifiers);
    }
    
    public boolean isBridge() {
        return (this.modifiers & 0x40) != 0x0;
    }
    
    public boolean isSynthetic() {
        return (this.modifiers & 0x1000) != 0x0;
    }
    
    public boolean isVarArgs() {
        return (this.modifiers & 0x80) != 0x0;
    }
    
    public boolean isNative() {
        return Modifier.isNative(this.modifiers);
    }
    
    public boolean hasBody() {
        return this.hasBody;
    }
    
    public boolean isDefault() {
        final ClassInfo classInfo = this.getClassInfo();
        return classInfo != null && classInfo.isInterface() && this.hasBody;
    }
    
    public MethodParameterInfo[] getParameterInfo() {
        if (this.parameterInfo == null) {
            List<TypeSignature> paramTypeDescriptors = null;
            int numParams = 0;
            try {
                final MethodTypeSignature typeSig = this.getTypeDescriptor();
                if (typeSig != null) {
                    paramTypeDescriptors = typeSig.getParameterTypeSignatures();
                    numParams = paramTypeDescriptors.size();
                }
            }
            catch (Exception ex) {}
            List<TypeSignature> paramTypeSignatures = null;
            try {
                final MethodTypeSignature typeSig2 = this.getTypeSignature();
                if (typeSig2 != null) {
                    paramTypeSignatures = typeSig2.getParameterTypeSignatures();
                }
            }
            catch (Exception ex2) {}
            if (paramTypeSignatures != null && paramTypeSignatures.size() > numParams) {
                throw new ClassGraphException("typeSignatureParamTypes.size() > typeDescriptorParamTypes.size() for method " + this.declaringClassName + "." + this.name);
            }
            final int otherParamMax = Math.max((this.parameterNames == null) ? 0 : this.parameterNames.length, Math.max((this.parameterModifiers == null) ? 0 : this.parameterModifiers.length, (this.parameterAnnotationInfo == null) ? 0 : this.parameterAnnotationInfo.length));
            if (otherParamMax > numParams) {
                throw new ClassGraphException("Type descriptor for method " + this.declaringClassName + "." + this.name + " has insufficient parameters");
            }
            String[] paramNamesAligned = null;
            if (this.parameterNames != null && numParams > 0) {
                if (this.parameterNames.length == numParams) {
                    paramNamesAligned = this.parameterNames;
                }
                else {
                    paramNamesAligned = new String[numParams];
                    int i = 0;
                    final int lenDiff = numParams - this.parameterNames.length;
                    while (i < this.parameterNames.length) {
                        paramNamesAligned[lenDiff + i] = this.parameterNames[i];
                        ++i;
                    }
                }
            }
            int[] paramModifiersAligned = null;
            if (this.parameterModifiers != null && numParams > 0) {
                if (this.parameterModifiers.length == numParams) {
                    paramModifiersAligned = this.parameterModifiers;
                }
                else {
                    paramModifiersAligned = new int[numParams];
                    int j = 0;
                    final int lenDiff2 = numParams - this.parameterModifiers.length;
                    while (j < this.parameterModifiers.length) {
                        paramModifiersAligned[lenDiff2 + j] = this.parameterModifiers[j];
                        ++j;
                    }
                }
            }
            AnnotationInfo[][] paramAnnotationInfoAligned = null;
            if (this.parameterAnnotationInfo != null && numParams > 0) {
                if (this.parameterAnnotationInfo.length == numParams) {
                    paramAnnotationInfoAligned = this.parameterAnnotationInfo;
                }
                else {
                    paramAnnotationInfoAligned = new AnnotationInfo[numParams][];
                    int k = 0;
                    final int lenDiff3 = numParams - this.parameterAnnotationInfo.length;
                    while (k < this.parameterAnnotationInfo.length) {
                        paramAnnotationInfoAligned[lenDiff3 + k] = this.parameterAnnotationInfo[k];
                        ++k;
                    }
                }
            }
            List<TypeSignature> paramTypeSignaturesAligned = null;
            if (paramTypeSignatures != null && numParams > 0) {
                if (paramTypeSignatures.size() == numParams) {
                    paramTypeSignaturesAligned = paramTypeSignatures;
                }
                else {
                    paramTypeSignaturesAligned = new ArrayList<TypeSignature>(numParams);
                    for (int l = 0, n = numParams - paramTypeSignatures.size(); l < n; ++l) {
                        paramTypeSignaturesAligned.add(null);
                    }
                    paramTypeSignaturesAligned.addAll(paramTypeSignatures);
                }
            }
            this.parameterInfo = new MethodParameterInfo[numParams];
            for (int l = 0; l < numParams; ++l) {
                (this.parameterInfo[l] = new MethodParameterInfo(this, (AnnotationInfo[])((paramAnnotationInfoAligned == null) ? null : paramAnnotationInfoAligned[l]), (paramModifiersAligned == null) ? 0 : paramModifiersAligned[l], (paramTypeDescriptors == null) ? null : paramTypeDescriptors.get(l), (paramTypeSignaturesAligned == null) ? null : paramTypeSignaturesAligned.get(l), (paramNamesAligned == null) ? null : paramNamesAligned[l])).setScanResult(this.scanResult);
            }
        }
        return this.parameterInfo;
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
    
    public boolean hasParameterAnnotation(final String annotationName) {
        for (final MethodParameterInfo methodParameterInfo : this.getParameterInfo()) {
            if (methodParameterInfo.hasAnnotation(annotationName)) {
                return true;
            }
        }
        return false;
    }
    
    private Class<?>[] loadParameterClasses() {
        final MethodParameterInfo[] allParameterInfo = this.getParameterInfo();
        final List<Class<?>> parameterClasses = new ArrayList<Class<?>>(allParameterInfo.length);
        for (final MethodParameterInfo mpi : allParameterInfo) {
            final TypeSignature parameterType = mpi.getTypeSignatureOrTypeDescriptor();
            parameterClasses.add(parameterType.loadClass());
        }
        return parameterClasses.toArray(new Class[0]);
    }
    
    public Method loadClassAndGetMethod() throws IllegalArgumentException {
        if (this.isConstructor()) {
            throw new IllegalArgumentException("Need to call loadClassAndGetConstructor() for constructors, not loadClassAndGetMethod()");
        }
        final Class<?>[] parameterClassesArr = this.loadParameterClasses();
        try {
            return this.loadClass().getMethod(this.getName(), parameterClassesArr);
        }
        catch (NoSuchMethodException e1) {
            try {
                return this.loadClass().getDeclaredMethod(this.getName(), parameterClassesArr);
            }
            catch (NoSuchMethodException es2) {
                throw new IllegalArgumentException("Method not found: " + this.getClassName() + "." + this.getName());
            }
        }
    }
    
    public Constructor<?> loadClassAndGetConstructor() throws IllegalArgumentException {
        if (!this.isConstructor()) {
            throw new IllegalArgumentException("Need to call loadClassAndGetMethod() for non-constructor methods, not loadClassAndGetConstructor()");
        }
        final Class<?>[] parameterClassesArr = this.loadParameterClasses();
        try {
            return this.loadClass().getConstructor(parameterClassesArr);
        }
        catch (NoSuchMethodException e1) {
            try {
                return this.loadClass().getDeclaredConstructor(parameterClassesArr);
            }
            catch (NoSuchMethodException es2) {
                throw new IllegalArgumentException("Constructor not found for class " + this.getClassName());
            }
        }
    }
    
    void handleRepeatableAnnotations(final Set<String> allRepeatableAnnotationNames) {
        if (this.annotationInfo != null) {
            this.annotationInfo.handleRepeatableAnnotations(allRepeatableAnnotationNames, this.getClassInfo(), ClassInfo.RelType.METHOD_ANNOTATIONS, ClassInfo.RelType.CLASSES_WITH_METHOD_ANNOTATION, ClassInfo.RelType.CLASSES_WITH_NONPRIVATE_METHOD_ANNOTATION);
        }
        if (this.parameterAnnotationInfo != null) {
            for (int i = 0; i < this.parameterAnnotationInfo.length; ++i) {
                final AnnotationInfo[] pai = this.parameterAnnotationInfo[i];
                if (pai != null && pai.length > 0) {
                    boolean hasRepeatableAnnotation = false;
                    for (final AnnotationInfo ai : pai) {
                        if (allRepeatableAnnotationNames.contains(ai.getName())) {
                            hasRepeatableAnnotation = true;
                            break;
                        }
                    }
                    if (hasRepeatableAnnotation) {
                        final AnnotationInfoList aiList = new AnnotationInfoList(pai.length);
                        for (final AnnotationInfo ai2 : pai) {
                            aiList.add(ai2);
                        }
                        aiList.handleRepeatableAnnotations(allRepeatableAnnotationNames, this.getClassInfo(), ClassInfo.RelType.METHOD_PARAMETER_ANNOTATIONS, ClassInfo.RelType.CLASSES_WITH_METHOD_PARAMETER_ANNOTATION, ClassInfo.RelType.CLASSES_WITH_NONPRIVATE_METHOD_PARAMETER_ANNOTATION);
                        this.parameterAnnotationInfo[i] = aiList.toArray(new AnnotationInfo[0]);
                    }
                }
            }
        }
    }
    
    public String getClassName() {
        return this.declaringClassName;
    }
    
    @Override
    void setScanResult(final ScanResult scanResult) {
        super.setScanResult(scanResult);
        if (this.typeDescriptor != null) {
            this.typeDescriptor.setScanResult(scanResult);
        }
        if (this.typeSignature != null) {
            this.typeSignature.setScanResult(scanResult);
        }
        if (this.annotationInfo != null) {
            for (final AnnotationInfo ai : this.annotationInfo) {
                ai.setScanResult(scanResult);
            }
        }
        if (this.parameterAnnotationInfo != null) {
            for (final AnnotationInfo[] pai : this.parameterAnnotationInfo) {
                if (pai != null) {
                    for (final AnnotationInfo ai2 : pai) {
                        ai2.setScanResult(scanResult);
                    }
                }
            }
        }
        if (this.parameterInfo != null) {
            for (final MethodParameterInfo mpi : this.parameterInfo) {
                mpi.setScanResult(scanResult);
            }
        }
    }
    
    @Override
    protected void findReferencedClassInfo(final Map<String, ClassInfo> classNameToClassInfo, final Set<ClassInfo> refdClassInfo, final LogNode log) {
        try {
            final MethodTypeSignature methodSig = this.getTypeSignature();
            if (methodSig != null) {
                methodSig.findReferencedClassInfo(classNameToClassInfo, refdClassInfo, log);
            }
        }
        catch (IllegalArgumentException e) {
            if (log != null) {
                log.log("Illegal type signature for method " + this.getClassName() + "." + this.getName() + ": " + this.getTypeSignatureStr());
            }
        }
        try {
            final MethodTypeSignature methodDesc = this.getTypeDescriptor();
            if (methodDesc != null) {
                methodDesc.findReferencedClassInfo(classNameToClassInfo, refdClassInfo, log);
            }
        }
        catch (IllegalArgumentException e) {
            if (log != null) {
                log.log("Illegal type descriptor for method " + this.getClassName() + "." + this.getName() + ": " + this.getTypeDescriptorStr());
            }
        }
        if (this.annotationInfo != null) {
            for (final AnnotationInfo ai : this.annotationInfo) {
                ai.findReferencedClassInfo(classNameToClassInfo, refdClassInfo, log);
            }
        }
        for (final MethodParameterInfo mpi : this.getParameterInfo()) {
            final AnnotationInfo[] aiArr = mpi.annotationInfo;
            if (aiArr != null) {
                for (final AnnotationInfo ai2 : aiArr) {
                    ai2.findReferencedClassInfo(classNameToClassInfo, refdClassInfo, log);
                }
            }
        }
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MethodInfo)) {
            return false;
        }
        final MethodInfo other = (MethodInfo)obj;
        return this.declaringClassName.equals(other.declaringClassName) && this.typeDescriptorStr.equals(other.typeDescriptorStr) && this.name.equals(other.name);
    }
    
    @Override
    public int hashCode() {
        return this.name.hashCode() + this.typeDescriptorStr.hashCode() * 11 + this.declaringClassName.hashCode() * 57;
    }
    
    @Override
    public int compareTo(final MethodInfo other) {
        final int diff0 = this.declaringClassName.compareTo(other.declaringClassName);
        if (diff0 != 0) {
            return diff0;
        }
        final int diff2 = this.name.compareTo(other.name);
        if (diff2 != 0) {
            return diff2;
        }
        return this.typeDescriptorStr.compareTo(other.typeDescriptorStr);
    }
    
    @Override
    protected void toString(final boolean useSimpleNames, final StringBuilder buf) {
        final MethodTypeSignature methodType = this.getTypeSignatureOrTypeDescriptor();
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
            TypeUtils.modifiersToString(this.modifiers, TypeUtils.ModifierType.METHOD, this.isDefault(), buf);
        }
        final List<TypeParameter> typeParameters = methodType.getTypeParameters();
        if (!typeParameters.isEmpty()) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append('<');
            for (int i = 0; i < typeParameters.size(); ++i) {
                if (i > 0) {
                    buf.append(", ");
                }
                typeParameters.get(i).toString(useSimpleNames, buf);
            }
            buf.append('>');
        }
        if (!this.isConstructor()) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            methodType.getResultType().toStringInternal(useSimpleNames, this.annotationInfo, buf);
        }
        buf.append(' ');
        if (this.name != null) {
            buf.append(useSimpleNames ? ClassInfo.getSimpleName(this.name) : this.name);
        }
        final MethodParameterInfo[] allParamInfo = this.getParameterInfo();
        boolean hasParamNames = false;
        for (final MethodParameterInfo methodParamInfo : allParamInfo) {
            if (methodParamInfo.getName() != null) {
                hasParamNames = true;
                break;
            }
        }
        int varArgsParamIndex = -1;
        if (this.isVarArgs()) {
            for (int j = allParamInfo.length - 1; j >= 0; --j) {
                final int mods = allParamInfo[j].getModifiers();
                if ((mods & 0x1000) == 0x0 && (mods & 0x8000) == 0x0) {
                    final TypeSignature paramType = allParamInfo[j].getTypeSignatureOrTypeDescriptor();
                    if (paramType instanceof ArrayTypeSignature) {
                        varArgsParamIndex = j;
                        break;
                    }
                }
            }
        }
        buf.append('(');
        for (int j = 0, numParams = allParamInfo.length; j < numParams; ++j) {
            final MethodParameterInfo paramInfo = allParamInfo[j];
            if (j > 0) {
                buf.append(", ");
            }
            if (paramInfo.annotationInfo != null) {
                for (final AnnotationInfo ai : paramInfo.annotationInfo) {
                    ai.toString(useSimpleNames, buf);
                    buf.append(' ');
                }
            }
            MethodParameterInfo.modifiersToString(paramInfo.getModifiers(), buf);
            final TypeSignature paramTypeSignature = paramInfo.getTypeSignatureOrTypeDescriptor();
            if (j == varArgsParamIndex) {
                if (!(paramTypeSignature instanceof ArrayTypeSignature)) {
                    throw new IllegalArgumentException("Got non-array type for last parameter of varargs method " + this.name);
                }
                final ArrayTypeSignature arrayType = (ArrayTypeSignature)paramTypeSignature;
                if (arrayType.getNumDimensions() == 0) {
                    throw new IllegalArgumentException("Got a zero-dimension array type for last parameter of varargs method " + this.name);
                }
                arrayType.getElementTypeSignature().toString(useSimpleNames, buf);
                for (int k = 0; k < arrayType.getNumDimensions() - 1; ++k) {
                    buf.append("[]");
                }
                buf.append("...");
            }
            else {
                AnnotationInfoList annotationsToExclude;
                if (paramInfo.annotationInfo == null || paramInfo.annotationInfo.length == 0) {
                    annotationsToExclude = null;
                }
                else {
                    annotationsToExclude = new AnnotationInfoList(paramInfo.annotationInfo.length);
                    for (int k = 0; k < paramInfo.annotationInfo.length; ++k) {
                        annotationsToExclude.add(paramInfo.annotationInfo[k]);
                    }
                }
                paramTypeSignature.toStringInternal(useSimpleNames, annotationsToExclude, buf);
            }
            if (hasParamNames) {
                final String paramName = paramInfo.getName();
                if (paramName != null) {
                    buf.append(' ');
                    buf.append(paramName);
                }
            }
        }
        buf.append(')');
        if (!methodType.getThrowsSignatures().isEmpty()) {
            buf.append(" throws ");
            for (int j = 0; j < methodType.getThrowsSignatures().size(); ++j) {
                if (j > 0) {
                    buf.append(", ");
                }
                methodType.getThrowsSignatures().get(j).toString(useSimpleNames, buf);
            }
        }
    }
}
