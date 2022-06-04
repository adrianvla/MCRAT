// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Collections;
import nonapi.io.github.classgraph.types.ParseException;
import nonapi.io.github.classgraph.types.TypeUtils;
import nonapi.io.github.classgraph.types.Parser;
import java.util.Objects;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ClassRefTypeSignature extends ClassRefOrTypeVariableSignature
{
    final String className;
    private final List<TypeArgument> typeArguments;
    private final List<String> suffixes;
    private final List<List<TypeArgument>> suffixTypeArguments;
    private List<AnnotationInfoList> suffixTypeAnnotations;
    
    private ClassRefTypeSignature(final String className, final List<TypeArgument> typeArguments, final List<String> suffixes, final List<List<TypeArgument>> suffixTypeArguments) {
        this.className = className;
        this.typeArguments = typeArguments;
        this.suffixes = suffixes;
        this.suffixTypeArguments = suffixTypeArguments;
    }
    
    public String getBaseClassName() {
        return this.className;
    }
    
    public String getFullyQualifiedClassName() {
        if (this.suffixes.isEmpty()) {
            return this.className;
        }
        final StringBuilder buf = new StringBuilder();
        buf.append(this.className);
        for (final String suffix : this.suffixes) {
            buf.append('$');
            buf.append(suffix);
        }
        return buf.toString();
    }
    
    public List<TypeArgument> getTypeArguments() {
        return this.typeArguments;
    }
    
    public List<String> getSuffixes() {
        return this.suffixes;
    }
    
    public List<List<TypeArgument>> getSuffixTypeArguments() {
        return this.suffixTypeArguments;
    }
    
    public List<AnnotationInfoList> getSuffixTypeAnnotationInfo() {
        return this.suffixTypeAnnotations;
    }
    
    private void addSuffixTypeAnnotation(final int suffixIdx, final AnnotationInfo annotationInfo) {
        if (this.suffixTypeAnnotations == null) {
            this.suffixTypeAnnotations = new ArrayList<AnnotationInfoList>(this.suffixes.size());
            for (int i = 0; i < this.suffixes.size(); ++i) {
                this.suffixTypeAnnotations.add(new AnnotationInfoList(1));
            }
        }
        this.suffixTypeAnnotations.get(suffixIdx).add(annotationInfo);
    }
    
    @Override
    protected void addTypeAnnotation(final List<Classfile.TypePathNode> typePath, final AnnotationInfo annotationInfo) {
        int numDeeperNestedLevels = 0;
        int nextTypeArgIdx = -1;
        int i = 0;
        while (i < typePath.size()) {
            final Classfile.TypePathNode typePathNode = typePath.get(i);
            if (typePathNode.typePathKind == 1) {
                ++numDeeperNestedLevels;
                ++i;
            }
            else {
                if (typePathNode.typePathKind == 3) {
                    nextTypeArgIdx = typePathNode.typeArgumentIdx;
                    break;
                }
                throw new IllegalArgumentException("Bad typePathKind: " + typePathNode.typePathKind);
            }
        }
        int suffixIdx = -1;
        int nestingLevel = -1;
        String typePrefix = this.className;
        while (suffixIdx < this.suffixes.size()) {
            boolean skipSuffix;
            if (suffixIdx == this.suffixes.size() - 1) {
                skipSuffix = false;
            }
            else {
                final ClassInfo outerClassInfo = this.scanResult.getClassInfo(typePrefix);
                typePrefix = typePrefix + '$' + this.suffixes.get(suffixIdx + 1);
                final ClassInfo innerClassInfo = this.scanResult.getClassInfo(typePrefix);
                skipSuffix = (outerClassInfo == null || innerClassInfo == null || outerClassInfo.isInterfaceOrAnnotation() || innerClassInfo.isInterfaceOrAnnotation() || innerClassInfo.isStatic() || !outerClassInfo.getInnerClasses().contains(innerClassInfo));
            }
            if (!skipSuffix && ++nestingLevel >= numDeeperNestedLevels) {
                if (nextTypeArgIdx == -1) {
                    if (suffixIdx == -1) {
                        this.addTypeAnnotation(annotationInfo);
                    }
                    else {
                        this.addSuffixTypeAnnotation(suffixIdx, annotationInfo);
                    }
                }
                else {
                    final List<TypeArgument> typeArgumentList = (suffixIdx == -1) ? this.typeArguments : this.suffixTypeArguments.get(suffixIdx);
                    if (nextTypeArgIdx < typeArgumentList.size()) {
                        typeArgumentList.get(nextTypeArgIdx).addTypeAnnotation(typePath.subList(numDeeperNestedLevels + 1, typePath.size()), annotationInfo);
                    }
                }
                return;
            }
            ++suffixIdx;
        }
        throw new IllegalArgumentException("Ran out of nested types while trying to add type annotation");
    }
    
    public Class<?> loadClass(final boolean ignoreExceptions) {
        return super.loadClass(ignoreExceptions);
    }
    
    public Class<?> loadClass() {
        return super.loadClass();
    }
    
    @Override
    protected String getClassName() {
        return this.getFullyQualifiedClassName();
    }
    
    public ClassInfo getClassInfo() {
        return super.getClassInfo();
    }
    
    @Override
    void setScanResult(final ScanResult scanResult) {
        super.setScanResult(scanResult);
        for (final TypeArgument typeArgument : this.typeArguments) {
            typeArgument.setScanResult(scanResult);
        }
        for (final List<TypeArgument> typeArgumentList : this.suffixTypeArguments) {
            for (final TypeArgument typeArgument2 : typeArgumentList) {
                typeArgument2.setScanResult(scanResult);
            }
        }
    }
    
    @Override
    protected void findReferencedClassNames(final Set<String> refdClassNames) {
        refdClassNames.add(this.getFullyQualifiedClassName());
        for (final TypeArgument typeArgument : this.typeArguments) {
            typeArgument.findReferencedClassNames(refdClassNames);
        }
        for (final List<TypeArgument> typeArgumentList : this.suffixTypeArguments) {
            for (final TypeArgument typeArgument2 : typeArgumentList) {
                typeArgument2.findReferencedClassNames(refdClassNames);
            }
        }
    }
    
    @Override
    public int hashCode() {
        return this.className.hashCode() + 7 * this.typeArguments.hashCode() + 15 * this.suffixTypeArguments.hashCode() + 31 * ((this.typeAnnotationInfo == null) ? 0 : this.typeAnnotationInfo.hashCode()) + 64 * ((this.suffixTypeAnnotations == null) ? 0 : this.suffixTypeAnnotations.hashCode());
    }
    
    private static boolean suffixesMatch(final ClassRefTypeSignature a, final ClassRefTypeSignature b) {
        return a.suffixes.equals(b.suffixes) && a.suffixTypeArguments.equals(b.suffixTypeArguments) && Objects.equals(a.suffixTypeAnnotations, b.suffixTypeAnnotations);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ClassRefTypeSignature)) {
            return false;
        }
        final ClassRefTypeSignature o = (ClassRefTypeSignature)obj;
        return o.className.equals(this.className) && o.typeArguments.equals(this.typeArguments) && Objects.equals(this.typeAnnotationInfo, o.typeAnnotationInfo) && suffixesMatch(o, this);
    }
    
    @Override
    public boolean equalsIgnoringTypeParams(final TypeSignature other) {
        if (other instanceof TypeVariableSignature) {
            return other.equalsIgnoringTypeParams(this);
        }
        if (!(other instanceof ClassRefTypeSignature)) {
            return false;
        }
        final ClassRefTypeSignature o = (ClassRefTypeSignature)other;
        return o.className.equals(this.className) && Objects.equals(this.typeAnnotationInfo, o.typeAnnotationInfo) && suffixesMatch(o, this);
    }
    
    @Override
    protected void toStringInternal(final boolean useSimpleNames, final AnnotationInfoList annotationsToExclude, final StringBuilder buf) {
        if (!useSimpleNames || this.suffixes.isEmpty()) {
            if (this.typeAnnotationInfo != null) {
                for (final AnnotationInfo annotationInfo : this.typeAnnotationInfo) {
                    if (annotationsToExclude == null || !annotationsToExclude.contains(annotationInfo)) {
                        annotationInfo.toString(useSimpleNames, buf);
                        buf.append(' ');
                    }
                }
            }
            buf.append(useSimpleNames ? ClassInfo.getSimpleName(this.className) : this.className);
            if (!this.typeArguments.isEmpty()) {
                buf.append('<');
                for (int i = 0; i < this.typeArguments.size(); ++i) {
                    if (i > 0) {
                        buf.append(", ");
                    }
                    this.typeArguments.get(i).toString(useSimpleNames, buf);
                }
                buf.append('>');
            }
        }
        if (!this.suffixes.isEmpty()) {
            for (int i = useSimpleNames ? (this.suffixes.size() - 1) : 0; i < this.suffixes.size(); ++i) {
                final AnnotationInfoList typeAnnotations = (this.suffixTypeAnnotations == null) ? null : this.suffixTypeAnnotations.get(i);
                if (!useSimpleNames) {
                    if (Character.isDigit(this.suffixes.get(i).charAt(0))) {
                        buf.append('$');
                    }
                    else {
                        buf.append('.');
                    }
                }
                if (typeAnnotations != null && !typeAnnotations.isEmpty()) {
                    for (final AnnotationInfo annotationInfo2 : typeAnnotations) {
                        annotationInfo2.toString(useSimpleNames, buf);
                        buf.append(' ');
                    }
                }
                buf.append(this.suffixes.get(i));
                final List<TypeArgument> suffixTypeArgumentsList = this.suffixTypeArguments.get(i);
                if (!suffixTypeArgumentsList.isEmpty()) {
                    buf.append('<');
                    for (int j = 0; j < suffixTypeArgumentsList.size(); ++j) {
                        if (j > 0) {
                            buf.append(", ");
                        }
                        suffixTypeArgumentsList.get(j).toString(useSimpleNames, buf);
                    }
                    buf.append('>');
                }
            }
        }
    }
    
    static ClassRefTypeSignature parse(final Parser parser, final String definingClassName) throws ParseException {
        if (parser.peek() != 'L') {
            return null;
        }
        parser.next();
        final int startParserPosition = parser.getPosition();
        if (!TypeUtils.getIdentifierToken(parser, true)) {
            throw new ParseException(parser, "Could not parse identifier token");
        }
        String className = parser.currToken();
        final List<TypeArgument> typeArguments = TypeArgument.parseList(parser, definingClassName);
        boolean dropSuffixes = false;
        List<String> suffixes;
        List<List<TypeArgument>> suffixTypeArguments;
        if (parser.peek() == '.' || parser.peek() == '$') {
            suffixes = new ArrayList<String>();
            suffixTypeArguments = new ArrayList<List<TypeArgument>>();
            while (parser.peek() == '.' || parser.peek() == '$') {
                parser.advance(1);
                if (!TypeUtils.getIdentifierToken(parser, true)) {
                    suffixes.add("");
                    suffixTypeArguments.add(Collections.emptyList());
                    dropSuffixes = true;
                }
                else {
                    suffixes.add(parser.currToken());
                    suffixTypeArguments.add(TypeArgument.parseList(parser, definingClassName));
                }
            }
            if (dropSuffixes) {
                className = parser.getSubstring(startParserPosition, parser.getPosition()).replace('/', '.');
                suffixes = Collections.emptyList();
                suffixTypeArguments = Collections.emptyList();
            }
        }
        else {
            suffixes = Collections.emptyList();
            suffixTypeArguments = Collections.emptyList();
        }
        parser.expect(';');
        return new ClassRefTypeSignature(className, typeArguments, suffixes, suffixTypeArguments);
    }
}
