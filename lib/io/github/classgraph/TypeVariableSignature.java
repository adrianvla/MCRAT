// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Objects;
import java.util.Set;
import java.util.ArrayList;
import nonapi.io.github.classgraph.types.ParseException;
import nonapi.io.github.classgraph.types.TypeUtils;
import nonapi.io.github.classgraph.types.Parser;
import java.util.List;
import java.util.Iterator;

public final class TypeVariableSignature extends ClassRefOrTypeVariableSignature
{
    private final String name;
    private final String definingClassName;
    MethodTypeSignature containingMethodSignature;
    
    private TypeVariableSignature(final String typeVariableName, final String definingClassName) {
        this.name = typeVariableName;
        this.definingClassName = definingClassName;
    }
    
    public String getName() {
        return this.name;
    }
    
    public TypeParameter resolve() {
        if (this.containingMethodSignature != null && this.containingMethodSignature.typeParameters != null && !this.containingMethodSignature.typeParameters.isEmpty()) {
            for (final TypeParameter typeParameter : this.containingMethodSignature.typeParameters) {
                if (typeParameter.name.equals(this.name)) {
                    return typeParameter;
                }
            }
        }
        final ClassInfo containingClassInfo = this.getClassInfo();
        if (containingClassInfo == null) {
            throw new IllegalArgumentException("Could not find ClassInfo object for " + this.definingClassName);
        }
        ClassTypeSignature containingClassSignature = null;
        try {
            containingClassSignature = containingClassInfo.getTypeSignature();
        }
        catch (Exception ex) {}
        if (containingClassSignature != null && containingClassSignature.typeParameters != null && !containingClassSignature.typeParameters.isEmpty()) {
            for (final TypeParameter typeParameter2 : containingClassSignature.typeParameters) {
                if (typeParameter2.name.equals(this.name)) {
                    return typeParameter2;
                }
            }
        }
        throw new IllegalArgumentException("Could not resolve " + this.name + " against parameters of the defining method or enclosing class");
    }
    
    @Override
    protected void addTypeAnnotation(final List<Classfile.TypePathNode> typePath, final AnnotationInfo annotationInfo) {
        if (typePath.isEmpty()) {
            this.addTypeAnnotation(annotationInfo);
            return;
        }
        throw new IllegalArgumentException("Type variable should have empty typePath");
    }
    
    static TypeVariableSignature parse(final Parser parser, final String definingClassName) throws ParseException {
        final char peek = parser.peek();
        if (peek != 'T') {
            return null;
        }
        parser.next();
        if (!TypeUtils.getIdentifierToken(parser, false)) {
            throw new ParseException(parser, "Could not parse type variable signature");
        }
        parser.expect(';');
        final TypeVariableSignature typeVariableSignature = new TypeVariableSignature(parser.currToken(), definingClassName);
        List<TypeVariableSignature> typeVariableSignatures = (List<TypeVariableSignature>)parser.getState();
        if (typeVariableSignatures == null) {
            parser.setState(typeVariableSignatures = new ArrayList<TypeVariableSignature>());
        }
        typeVariableSignatures.add(typeVariableSignature);
        return typeVariableSignature;
    }
    
    @Override
    protected String getClassName() {
        return this.definingClassName;
    }
    
    @Override
    protected void findReferencedClassNames(final Set<String> refdClassNames) {
    }
    
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TypeVariableSignature)) {
            return false;
        }
        final TypeVariableSignature other = (TypeVariableSignature)obj;
        return other.name.equals(this.name) && Objects.equals(other.typeAnnotationInfo, this.typeAnnotationInfo);
    }
    
    @Override
    public boolean equalsIgnoringTypeParams(final TypeSignature other) {
        if (!(other instanceof ClassRefTypeSignature)) {
            return this.equals(other);
        }
        if (((ClassRefTypeSignature)other).className.equals("java.lang.Object")) {
            return true;
        }
        TypeParameter typeParameter;
        try {
            typeParameter = this.resolve();
        }
        catch (IllegalArgumentException e) {
            return true;
        }
        if (typeParameter.classBound == null && (typeParameter.interfaceBounds == null || typeParameter.interfaceBounds.isEmpty())) {
            return true;
        }
        if (typeParameter.classBound != null) {
            if (!(typeParameter.classBound instanceof ClassRefTypeSignature)) {
                return typeParameter.classBound instanceof TypeVariableSignature && this.equalsIgnoringTypeParams(typeParameter.classBound);
            }
            if (typeParameter.classBound.equals(other)) {
                return true;
            }
        }
        if (typeParameter.interfaceBounds != null) {
            for (final ReferenceTypeSignature interfaceBound : typeParameter.interfaceBounds) {
                if (!(interfaceBound instanceof ClassRefTypeSignature)) {
                    return interfaceBound instanceof TypeVariableSignature && this.equalsIgnoringTypeParams(interfaceBound);
                }
                if (interfaceBound.equals(other)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public String toStringWithTypeBound() {
        try {
            return this.resolve().toString();
        }
        catch (IllegalArgumentException e) {
            return this.name;
        }
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
        buf.append(this.name);
    }
}
