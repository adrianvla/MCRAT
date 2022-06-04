// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config.plugins.visitors;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Configuration;
import java.lang.reflect.Member;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import java.lang.annotation.Annotation;

public interface PluginVisitor<A extends Annotation>
{
    PluginVisitor<A> setAnnotation(final Annotation p0);
    
    PluginVisitor<A> setAliases(final String... p0);
    
    PluginVisitor<A> setConversionType(final Class<?> p0);
    
    PluginVisitor<A> setStrSubstitutor(final StrSubstitutor p0);
    
    PluginVisitor<A> setMember(final Member p0);
    
    Object visit(final Configuration p0, final Node p1, final LogEvent p2, final StringBuilder p3);
}
