// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import com.esotericsoftware.yamlbeans.emitter.EmitterConfig;
import java.util.Collection;
import java.util.ArrayList;
import com.esotericsoftware.yamlbeans.scalar.DateSerializer;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.HashMap;
import com.esotericsoftware.yamlbeans.scalar.ScalarSerializer;
import java.util.Map;

public class YamlConfig
{
    public final WriteConfig writeConfig;
    public final ReadConfig readConfig;
    final Map<String, String> classNameToTag;
    final Map<String, Class> tagToClass;
    final Map<Class, ScalarSerializer> scalarSerializers;
    final Map<Beans.Property, Class> propertyToElementType;
    final Map<Beans.Property, Class> propertyToDefaultType;
    boolean beanProperties;
    boolean privateFields;
    boolean privateConstructors;
    boolean allowDuplicates;
    String tagSuffix;
    
    public YamlConfig() {
        this.writeConfig = new WriteConfig();
        this.readConfig = new ReadConfig();
        this.classNameToTag = new HashMap<String, String>();
        this.tagToClass = new HashMap<String, Class>();
        this.scalarSerializers = new IdentityHashMap<Class, ScalarSerializer>();
        this.propertyToElementType = new HashMap<Beans.Property, Class>();
        this.propertyToDefaultType = new HashMap<Beans.Property, Class>();
        this.beanProperties = true;
        this.privateConstructors = true;
        this.allowDuplicates = true;
        this.scalarSerializers.put(Date.class, new DateSerializer());
        this.tagToClass.put("tag:yaml.org,2002:str", String.class);
        this.tagToClass.put("tag:yaml.org,2002:int", Integer.class);
        this.tagToClass.put("tag:yaml.org,2002:seq", ArrayList.class);
        this.tagToClass.put("tag:yaml.org,2002:map", HashMap.class);
        this.tagToClass.put("tag:yaml.org,2002:float", Float.class);
    }
    
    public void setAllowDuplicates(final boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
    }
    
    public void setClassTag(String tag, final Class type) {
        if (tag == null) {
            throw new IllegalArgumentException("tag cannot be null.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null.");
        }
        if (!tag.startsWith("!")) {
            tag = "!" + tag;
        }
        this.classNameToTag.put(type.getName(), tag);
        this.tagToClass.put(tag, type);
    }
    
    public void setScalarSerializer(final Class type, final ScalarSerializer serializer) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null.");
        }
        if (serializer == null) {
            throw new IllegalArgumentException("serializer cannot be null.");
        }
        this.scalarSerializers.put(type, serializer);
    }
    
    public void setPropertyElementType(final Class type, final String propertyName, final Class elementType) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null.");
        }
        if (propertyName == null) {
            throw new IllegalArgumentException("propertyName cannot be null.");
        }
        if (elementType == null) {
            throw new IllegalArgumentException("propertyType cannot be null.");
        }
        final Beans.Property property = Beans.getProperty(type, propertyName, this.beanProperties, this.privateFields, this);
        if (property == null) {
            throw new IllegalArgumentException("The class " + type.getName() + " does not have a property named: " + propertyName);
        }
        if (!Collection.class.isAssignableFrom(property.getType()) && !Map.class.isAssignableFrom(property.getType())) {
            throw new IllegalArgumentException("The '" + propertyName + "' property on the " + type.getName() + " class must be a Collection or Map: " + property.getType());
        }
        this.propertyToElementType.put(property, elementType);
    }
    
    public void setPropertyDefaultType(final Class type, final String propertyName, final Class defaultType) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null.");
        }
        if (propertyName == null) {
            throw new IllegalArgumentException("propertyName cannot be null.");
        }
        if (defaultType == null) {
            throw new IllegalArgumentException("defaultType cannot be null.");
        }
        final Beans.Property property = Beans.getProperty(type, propertyName, this.beanProperties, this.privateFields, this);
        if (property == null) {
            throw new IllegalArgumentException("The class " + type.getName() + " does not have a property named: " + propertyName);
        }
        this.propertyToDefaultType.put(property, defaultType);
    }
    
    public void setBeanProperties(final boolean beanProperties) {
        this.beanProperties = beanProperties;
    }
    
    public void setPrivateFields(final boolean privateFields) {
        this.privateFields = privateFields;
    }
    
    public void setPrivateConstructors(final boolean privateConstructors) {
        this.privateConstructors = privateConstructors;
    }
    
    public void setTagSuffix(final String tagSuffix) {
        this.tagSuffix = tagSuffix;
    }
    
    public static class WriteConfig
    {
        boolean explicitFirstDocument;
        boolean explicitEndDocument;
        boolean writeDefaultValues;
        boolean writeRootTags;
        boolean writeRootElementTags;
        boolean autoAnchor;
        boolean keepBeanPropertyOrder;
        WriteClassName writeClassName;
        Quote quote;
        Version version;
        Map<String, String> tags;
        boolean flowStyle;
        EmitterConfig emitterConfig;
        
        WriteConfig() {
            this.explicitFirstDocument = false;
            this.explicitEndDocument = false;
            this.writeDefaultValues = false;
            this.writeRootTags = true;
            this.writeRootElementTags = true;
            this.autoAnchor = true;
            this.keepBeanPropertyOrder = false;
            this.writeClassName = WriteClassName.AUTO;
            this.quote = Quote.NONE;
            (this.emitterConfig = new EmitterConfig()).setUseVerbatimTags(false);
        }
        
        public void setExplicitFirstDocument(final boolean explicitFirstDocument) {
            this.explicitFirstDocument = explicitFirstDocument;
        }
        
        public void setExplicitEndDocument(final boolean explicitEndDocument) {
            this.explicitEndDocument = explicitEndDocument;
        }
        
        public void setWriteRootTags(final boolean writeRootTags) {
            this.writeRootTags = writeRootTags;
        }
        
        public void setWriteRootElementTags(final boolean writeRootElementTags) {
            this.writeRootElementTags = writeRootElementTags;
        }
        
        public void setWriteDefaultValues(final boolean writeDefaultValues) {
            this.writeDefaultValues = writeDefaultValues;
        }
        
        public void setAutoAnchor(final boolean autoAnchor) {
            this.autoAnchor = autoAnchor;
        }
        
        public void setKeepBeanPropertyOrder(final boolean keepBeanPropertyOrder) {
            this.keepBeanPropertyOrder = keepBeanPropertyOrder;
        }
        
        public void setVersion(final Version version) {
            this.version = version;
        }
        
        public void setTags(final Map<String, String> tags) {
            this.tags = tags;
        }
        
        public void setCanonical(final boolean canonical) {
            this.emitterConfig.setCanonical(canonical);
        }
        
        public void setIndentSize(final int indentSize) {
            this.emitterConfig.setIndentSize(indentSize);
        }
        
        public void setWrapColumn(final int wrapColumn) {
            this.emitterConfig.setWrapColumn(wrapColumn);
        }
        
        public void setUseVerbatimTags(final boolean useVerbatimTags) {
            this.emitterConfig.setUseVerbatimTags(useVerbatimTags);
        }
        
        public void setEscapeUnicode(final boolean escapeUnicode) {
            this.emitterConfig.setEscapeUnicode(escapeUnicode);
        }
        
        public void setWriteClassname(final WriteClassName write) {
            this.writeClassName = write;
        }
        
        public void setQuoteChar(final Quote quote) {
            this.quote = quote;
        }
        
        public Quote getQuote() {
            return this.quote;
        }
        
        public void setFlowStyle(final boolean flowStyle) {
            this.flowStyle = flowStyle;
        }
        
        public boolean isFlowStyle() {
            return this.flowStyle;
        }
        
        public void setPrettyFlow(final boolean prettyFlow) {
            this.emitterConfig.setPrettyFlow(prettyFlow);
        }
    }
    
    public static class ReadConfig
    {
        Version defaultVersion;
        ClassLoader classLoader;
        final Map<Class, ConstructorParameters> constructorParameters;
        boolean ignoreUnknownProperties;
        boolean autoMerge;
        boolean classTags;
        boolean guessNumberTypes;
        
        ReadConfig() {
            this.defaultVersion = Version.DEFAULT_VERSION;
            this.constructorParameters = new IdentityHashMap<Class, ConstructorParameters>();
            this.autoMerge = true;
            this.classTags = true;
        }
        
        public void setDefaultVersion(final Version defaultVersion) {
            if (defaultVersion == null) {
                throw new IllegalArgumentException("defaultVersion cannot be null.");
            }
            this.defaultVersion = defaultVersion;
        }
        
        public void setClassLoader(final ClassLoader classLoader) {
            this.classLoader = classLoader;
        }
        
        public void setConstructorParameters(final Class type, final Class[] parameterTypes, final String[] parameterNames) {
            if (type == null) {
                throw new IllegalArgumentException("type cannot be null.");
            }
            if (parameterTypes == null) {
                throw new IllegalArgumentException("parameterTypes cannot be null.");
            }
            if (parameterNames == null) {
                throw new IllegalArgumentException("parameterNames cannot be null.");
            }
            final ConstructorParameters parameters = new ConstructorParameters();
            try {
                parameters.constructor = type.getConstructor((Class[])parameterTypes);
            }
            catch (Exception ex) {
                throw new IllegalArgumentException("Unable to find constructor: " + type.getName() + "(" + Arrays.toString(parameterTypes) + ")", ex);
            }
            parameters.parameterNames = parameterNames;
            this.constructorParameters.put(type, parameters);
        }
        
        public void setIgnoreUnknownProperties(final boolean allowUnknownProperties) {
            this.ignoreUnknownProperties = allowUnknownProperties;
        }
        
        public void setClassTags(final boolean classTags) {
            this.classTags = classTags;
        }
        
        public void setGuessNumberTypes(final boolean guessNumberTypes) {
            this.guessNumberTypes = guessNumberTypes;
        }
    }
    
    static class ConstructorParameters
    {
        public Constructor constructor;
        public String[] parameterNames;
    }
    
    public enum WriteClassName
    {
        ALWAYS, 
        NEVER, 
        AUTO;
    }
    
    public enum Quote
    {
        NONE('\0'), 
        SINGLE('\''), 
        DOUBLE('\"'), 
        LITERAL('|'), 
        FOLDED('>');
        
        char c;
        
        private Quote(final char c) {
            this.c = c;
        }
        
        public char getStyle() {
            return this.c;
        }
    }
}
