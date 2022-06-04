// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Collections;
import java.util.ArrayList;
import nonapi.io.github.classgraph.types.ParseException;
import nonapi.io.github.classgraph.types.Parser;
import java.util.List;

public final class TypeArgument extends HierarchicalTypeSignature
{
    private final Wildcard wildcard;
    private final ReferenceTypeSignature typeSignature;
    
    private TypeArgument(final Wildcard wildcard, final ReferenceTypeSignature typeSignature) {
        this.wildcard = wildcard;
        this.typeSignature = typeSignature;
    }
    
    public Wildcard getWildcard() {
        return this.wildcard;
    }
    
    public ReferenceTypeSignature getTypeSignature() {
        return this.typeSignature;
    }
    
    @Override
    protected void addTypeAnnotation(final List<Classfile.TypePathNode> typePath, final AnnotationInfo annotationInfo) {
        if (typePath.size() == 0 && this.wildcard != Wildcard.NONE) {
            this.addTypeAnnotation(annotationInfo);
        }
        else if (typePath.size() > 0 && typePath.get(0).typePathKind == 2) {
            this.typeSignature.addTypeAnnotation(typePath.subList(1, typePath.size()), annotationInfo);
        }
        else {
            this.typeSignature.addTypeAnnotation(typePath, annotationInfo);
        }
    }
    
    private static TypeArgument parse(final Parser parser, final String definingClassName) throws ParseException {
        final char peek = parser.peek();
        if (peek == '*') {
            parser.expect('*');
            return new TypeArgument(Wildcard.ANY, null);
        }
        if (peek == '+') {
            parser.expect('+');
            final ReferenceTypeSignature typeSignature = ReferenceTypeSignature.parseReferenceTypeSignature(parser, definingClassName);
            if (typeSignature == null) {
                throw new ParseException(parser, "Missing '+' type bound");
            }
            return new TypeArgument(Wildcard.EXTENDS, typeSignature);
        }
        else if (peek == '-') {
            parser.expect('-');
            final ReferenceTypeSignature typeSignature = ReferenceTypeSignature.parseReferenceTypeSignature(parser, definingClassName);
            if (typeSignature == null) {
                throw new ParseException(parser, "Missing '-' type bound");
            }
            return new TypeArgument(Wildcard.SUPER, typeSignature);
        }
        else {
            final ReferenceTypeSignature typeSignature = ReferenceTypeSignature.parseReferenceTypeSignature(parser, definingClassName);
            if (typeSignature == null) {
                throw new ParseException(parser, "Missing type bound");
            }
            return new TypeArgument(Wildcard.NONE, typeSignature);
        }
    }
    
    static List<TypeArgument> parseList(final Parser parser, final String definingClassName) throws ParseException {
        if (parser.peek() == '<') {
            parser.expect('<');
            final List<TypeArgument> typeArguments = new ArrayList<TypeArgument>(2);
            while (parser.peek() != '>') {
                if (!parser.hasMore()) {
                    throw new ParseException(parser, "Missing '>'");
                }
                typeArguments.add(parse(parser, definingClassName));
            }
            parser.expect('>');
            return typeArguments;
        }
        return Collections.emptyList();
    }
    
    @Override
    protected String getClassName() {
        throw new IllegalArgumentException("getClassName() cannot be called here");
    }
    
    protected ClassInfo getClassInfo() {
        throw new IllegalArgumentException("getClassInfo() cannot be called here");
    }
    
    @Override
    void setScanResult(final ScanResult scanResult) {
        super.setScanResult(scanResult);
        if (this.typeSignature != null) {
            this.typeSignature.setScanResult(scanResult);
        }
    }
    
    public void findReferencedClassNames(final Set<String> refdClassNames) {
        if (this.typeSignature != null) {
            this.typeSignature.findReferencedClassNames(refdClassNames);
        }
    }
    
    @Override
    public int hashCode() {
        return ((this.typeSignature != null) ? this.typeSignature.hashCode() : 0) + 7 * this.wildcard.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TypeArgument)) {
            return false;
        }
        final TypeArgument other = (TypeArgument)obj;
        return Objects.equals(this.typeAnnotationInfo, other.typeAnnotationInfo) && Objects.equals(this.typeSignature, other.typeSignature) && other.wildcard.equals(this.wildcard);
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
        switch (this.wildcard) {
            case ANY: {
                buf.append('?');
                break;
            }
            case EXTENDS: {
                final String typeSigStr = this.typeSignature.toString(useSimpleNames);
                buf.append(typeSigStr.equals("java.lang.Object") ? "?" : ("? extends " + typeSigStr));
                break;
            }
            case SUPER: {
                buf.append("? super ");
                this.typeSignature.toString(useSimpleNames, buf);
                break;
            }
            default: {
                this.typeSignature.toString(useSimpleNames, buf);
                break;
            }
        }
    }
    
    public enum Wildcard
    {
        NONE, 
        ANY, 
        EXTENDS, 
        SUPER;
    }
}
