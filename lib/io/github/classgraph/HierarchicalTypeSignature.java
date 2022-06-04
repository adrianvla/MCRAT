// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.List;
import java.util.Iterator;

public abstract class HierarchicalTypeSignature extends ScanResultObject
{
    protected AnnotationInfoList typeAnnotationInfo;
    
    protected void addTypeAnnotation(final AnnotationInfo annotationInfo) {
        if (this.typeAnnotationInfo == null) {
            this.typeAnnotationInfo = new AnnotationInfoList(1);
        }
        this.typeAnnotationInfo.add(annotationInfo);
    }
    
    @Override
    void setScanResult(final ScanResult scanResult) {
        super.setScanResult(scanResult);
        if (this.typeAnnotationInfo != null) {
            for (final AnnotationInfo annotationInfo : this.typeAnnotationInfo) {
                annotationInfo.setScanResult(scanResult);
            }
        }
    }
    
    protected abstract void addTypeAnnotation(final List<Classfile.TypePathNode> p0, final AnnotationInfo p1);
    
    protected abstract void toStringInternal(final boolean p0, final AnnotationInfoList p1, final StringBuilder p2);
    
    @Override
    protected void toString(final boolean useSimpleNames, final StringBuilder buf) {
        this.toStringInternal(useSimpleNames, null, buf);
    }
}
