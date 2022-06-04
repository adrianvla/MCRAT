// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans;

import java.lang.reflect.Array;
import java.util.Collection;
import java.lang.reflect.InvocationTargetException;
import com.esotericsoftware.yamlbeans.scalar.ScalarSerializer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.esotericsoftware.yamlbeans.parser.ScalarEvent;
import com.esotericsoftware.yamlbeans.parser.CollectionStartEvent;
import com.esotericsoftware.yamlbeans.parser.AliasEvent;
import java.util.Iterator;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.tokenizer.Tokenizer;
import com.esotericsoftware.yamlbeans.parser.EventType;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.io.Reader;
import java.util.Map;
import com.esotericsoftware.yamlbeans.parser.Parser;

public class YamlReader
{
    private final YamlConfig config;
    Parser parser;
    private final Map<String, Object> anchors;
    
    public YamlReader(final Reader reader) {
        this(reader, new YamlConfig());
    }
    
    public YamlReader(final Reader reader, final YamlConfig config) {
        this.anchors = new HashMap<String, Object>();
        this.config = config;
        this.parser = new Parser(reader, config.readConfig.defaultVersion);
    }
    
    public YamlReader(final String yaml) {
        this(new StringReader(yaml));
    }
    
    public YamlReader(final String yaml, final YamlConfig config) {
        this(new StringReader(yaml), config);
    }
    
    public YamlConfig getConfig() {
        return this.config;
    }
    
    public Object get(final String alias) {
        return this.anchors.get(alias);
    }
    
    public void close() throws IOException {
        this.parser.close();
        this.anchors.clear();
    }
    
    public Object read() throws YamlException {
        return this.read((Class<Object>)null);
    }
    
    public <T> T read(final Class<T> type) throws YamlException {
        return this.read(type, null);
    }
    
    public <T> T read(final Class<T> type, final Class elementType) throws YamlException {
        this.anchors.clear();
        try {
            while (true) {
                final Event event = this.parser.getNextEvent();
                if (event == null) {
                    return null;
                }
                if (event.type == EventType.STREAM_END) {
                    return null;
                }
                if (event.type == EventType.DOCUMENT_START) {
                    final Object object = this.readValue(type, elementType, null);
                    this.parser.getNextEvent();
                    return (T)object;
                }
            }
        }
        catch (Parser.ParserException ex) {
            throw new YamlException("Error parsing YAML.", ex);
        }
        catch (Tokenizer.TokenizerException ex2) {
            throw new YamlException("Error tokenizing YAML.", ex2);
        }
    }
    
    public <T> Iterator<T> readAll(final Class<T> type) {
        final Iterator<T> iterator = new Iterator<T>() {
            public boolean hasNext() {
                final Event event = YamlReader.this.parser.peekNextEvent();
                return event != null && event.type != EventType.STREAM_END;
            }
            
            public T next() {
                try {
                    return YamlReader.this.read(type);
                }
                catch (YamlException e) {
                    throw new RuntimeException("Iterative reading documents exception", e);
                }
            }
            
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return iterator;
    }
    
    protected Object readValue(final Class type, final Class elementType, final Class defaultType) throws YamlException, Parser.ParserException, Tokenizer.TokenizerException {
        String tag = null;
        String anchor = null;
        final Event event = this.parser.peekNextEvent();
        switch (event.type) {
            case ALIAS: {
                this.parser.getNextEvent();
                anchor = ((AliasEvent)event).anchor;
                final Object value = this.anchors.get(anchor);
                if (value == null) {
                    throw new YamlReaderException("Unknown anchor: " + anchor);
                }
                return value;
            }
            case MAPPING_START:
            case SEQUENCE_START: {
                tag = ((CollectionStartEvent)event).tag;
                anchor = ((CollectionStartEvent)event).anchor;
                break;
            }
            case SCALAR: {
                tag = ((ScalarEvent)event).tag;
                anchor = ((ScalarEvent)event).anchor;
                break;
            }
        }
        return this.readValueInternal(this.chooseType(tag, defaultType, type), elementType, anchor);
    }
    
    private Class<?> chooseType(String tag, final Class<?> defaultType, final Class<?> providedType) throws YamlReaderException {
        if (tag != null && this.config.readConfig.classTags) {
            final Class<?> userConfiguredByTag = this.config.tagToClass.get(tag);
            if (userConfiguredByTag != null) {
                return userConfiguredByTag;
            }
            final ClassLoader classLoader = (this.config.readConfig.classLoader == null) ? this.getClass().getClassLoader() : this.config.readConfig.classLoader;
            tag = tag.replace("!", "");
            try {
                final Class<?> loadedFromTag = this.findTagClass(tag, classLoader);
                if (loadedFromTag != null) {
                    return loadedFromTag;
                }
            }
            catch (ClassNotFoundException e) {
                throw new YamlReaderException("Unable to find class specified by tag: " + tag);
            }
        }
        if (defaultType != null) {
            return defaultType;
        }
        return providedType;
    }
    
    protected Class<?> findTagClass(final String tag, final ClassLoader classLoader) throws ClassNotFoundException {
        return Class.forName(tag, true, classLoader);
    }
    
    private Object readValueInternal(Class type, Class elementType, final String anchor) throws YamlException, Parser.ParserException, Tokenizer.TokenizerException {
        if (type == null || type == Object.class) {
            final Event event = this.parser.peekNextEvent();
            switch (event.type) {
                case MAPPING_START: {
                    type = LinkedHashMap.class;
                    break;
                }
                case SCALAR: {
                    if (this.config.readConfig.guessNumberTypes) {
                        final String value = ((ScalarEvent)event).value;
                        if (value != null) {
                            final Number number = this.valueConvertedNumber(value);
                            if (number != null) {
                                if (anchor != null) {
                                    this.anchors.put(anchor, number);
                                }
                                this.parser.getNextEvent();
                                return number;
                            }
                        }
                    }
                    type = String.class;
                    break;
                }
                case SEQUENCE_START: {
                    type = ArrayList.class;
                    break;
                }
                default: {
                    throw new YamlReaderException("Expected scalar, sequence, or mapping but found: " + event.type);
                }
            }
        }
        if (Beans.isScalar(type)) {
            final Event event = this.parser.getNextEvent();
            if (event.type != EventType.SCALAR) {
                throw new YamlReaderException("Expected scalar for primitive type '" + type.getClass() + "' but found: " + event.type);
            }
            final String value = ((ScalarEvent)event).value;
            try {
                Object convertedValue;
                if (value == null) {
                    convertedValue = null;
                }
                else if (type == String.class) {
                    convertedValue = value;
                }
                else if (type == Integer.TYPE || type == Integer.class) {
                    convertedValue = Integer.decode(value);
                }
                else if (type == Boolean.TYPE || type == Boolean.class) {
                    convertedValue = Boolean.valueOf(value);
                }
                else if (type == Float.TYPE || type == Float.class) {
                    convertedValue = Float.valueOf(value);
                }
                else if (type == Double.TYPE || type == Double.class) {
                    convertedValue = Double.valueOf(value);
                }
                else if (type == Long.TYPE || type == Long.class) {
                    convertedValue = Long.decode(value);
                }
                else if (type == Short.TYPE || type == Short.class) {
                    convertedValue = Short.decode(value);
                }
                else if (type == Character.TYPE || type == Character.class) {
                    convertedValue = value.charAt(0);
                }
                else {
                    if (type != Byte.TYPE && type != Byte.class) {
                        throw new YamlException("Unknown field type.");
                    }
                    convertedValue = Byte.decode(value);
                }
                if (anchor != null) {
                    this.anchors.put(anchor, convertedValue);
                }
                return convertedValue;
            }
            catch (Exception ex) {
                throw new YamlReaderException("Unable to convert value to required type \"" + type + "\": " + value, ex);
            }
        }
        for (final Map.Entry<Class, ScalarSerializer> entry : this.config.scalarSerializers.entrySet()) {
            if (entry.getKey().isAssignableFrom(type)) {
                final ScalarSerializer serializer = entry.getValue();
                final Event event2 = this.parser.getNextEvent();
                if (event2.type != EventType.SCALAR) {
                    throw new YamlReaderException("Expected scalar for type '" + type + "' to be deserialized by scalar serializer '" + serializer.getClass().getName() + "' but found: " + event2.type);
                }
                final Object value2 = serializer.read(((ScalarEvent)event2).value);
                if (anchor != null) {
                    this.anchors.put(anchor, value2);
                }
                return value2;
            }
        }
        if (Enum.class.isAssignableFrom(type)) {
            final Event event = this.parser.getNextEvent();
            if (event.type != EventType.SCALAR) {
                throw new YamlReaderException("Expected scalar for enum type but found: " + event.type);
            }
            final String enumValueName = ((ScalarEvent)event).value;
            if (enumValueName == null) {
                return null;
            }
            try {
                return Enum.valueOf((Class<Object>)type, enumValueName);
            }
            catch (Exception ex) {
                throw new YamlReaderException("Unable to find enum value '" + enumValueName + "' for enum class: " + type.getName());
            }
        }
        Event event = this.parser.peekNextEvent();
        switch (event.type) {
            case MAPPING_START: {
                event = this.parser.getNextEvent();
                Object object;
                try {
                    object = this.createObject(type);
                }
                catch (InvocationTargetException ex2) {
                    throw new YamlReaderException("Error creating object.", ex2);
                }
                if (anchor != null) {
                    this.anchors.put(anchor, object);
                }
                final ArrayList keys = new ArrayList();
                while (this.parser.peekNextEvent().type != EventType.MAPPING_END) {
                    Object key = this.readValue(null, null, null);
                    final boolean isExplicitKey = key instanceof Map;
                    Object value3 = null;
                    if (isExplicitKey) {
                        final Map.Entry nameValuePair = (Map.Entry)((Map)key).entrySet().iterator().next();
                        key = nameValuePair.getKey();
                        value3 = nameValuePair.getValue();
                    }
                    if (object instanceof Map) {
                        if (this.config.tagSuffix != null) {
                            final Event nextEvent = this.parser.peekNextEvent();
                            switch (nextEvent.type) {
                                case MAPPING_START:
                                case SEQUENCE_START: {
                                    ((Map)object).put(key + this.config.tagSuffix, ((CollectionStartEvent)nextEvent).tag);
                                    break;
                                }
                                case SCALAR: {
                                    ((Map)object).put(key + this.config.tagSuffix, ((ScalarEvent)nextEvent).tag);
                                    break;
                                }
                            }
                        }
                        if (!isExplicitKey) {
                            value3 = this.readValue(elementType, null, null);
                        }
                        if (!this.config.allowDuplicates && ((Map)object).containsKey(key)) {
                            throw new YamlReaderException("Duplicate key found '" + key + "'");
                        }
                        if (this.config.readConfig.autoMerge && "<<".equals(key) && value3 != null) {
                            this.mergeMap((Map<String, Object>)object, value3);
                        }
                        else {
                            ((Map)object).put(key, value3);
                        }
                    }
                    else {
                        try {
                            if (!this.config.allowDuplicates && keys.contains(key)) {
                                throw new YamlReaderException("Duplicate key found '" + key + "'");
                            }
                            keys.add(key);
                            final Beans.Property property = Beans.getProperty(type, (String)key, this.config.beanProperties, this.config.privateFields, this.config);
                            if (property == null) {
                                if (!this.config.readConfig.ignoreUnknownProperties) {
                                    throw new YamlReaderException("Unable to find property '" + key + "' on class: " + type.getName());
                                }
                                final Event nextEvent2 = this.parser.peekNextEvent();
                                final EventType nextType = nextEvent2.type;
                                if (nextType == EventType.SEQUENCE_START || nextType == EventType.MAPPING_START) {
                                    this.skipRange();
                                }
                                else {
                                    this.parser.getNextEvent();
                                }
                            }
                            else {
                                Class propertyElementType = this.config.propertyToElementType.get(property);
                                if (propertyElementType == null) {
                                    propertyElementType = property.getElementType();
                                }
                                final Class propertyDefaultType = this.config.propertyToDefaultType.get(property);
                                if (!isExplicitKey) {
                                    value3 = this.readValue(property.getType(), propertyElementType, propertyDefaultType);
                                }
                                property.set(object, value3);
                            }
                        }
                        catch (Exception ex3) {
                            if (ex3 instanceof YamlReaderException) {
                                throw (YamlReaderException)ex3;
                            }
                            throw new YamlReaderException("Error setting property '" + key + "' on class: " + type.getName(), ex3);
                        }
                    }
                }
                this.parser.getNextEvent();
                if (object instanceof DeferredConstruction) {
                    try {
                        object = ((DeferredConstruction)object).construct();
                        if (anchor != null) {
                            this.anchors.put(anchor, object);
                        }
                    }
                    catch (InvocationTargetException ex4) {
                        throw new YamlReaderException("Error creating object.", ex4);
                    }
                }
                return object;
            }
            case SEQUENCE_START: {
                event = this.parser.getNextEvent();
                Collection collection = null;
                Label_1855: {
                    if (Collection.class.isAssignableFrom(type)) {
                        try {
                            collection = (Collection)Beans.createObject(type, this.config.privateConstructors);
                            break Label_1855;
                        }
                        catch (InvocationTargetException ex2) {
                            throw new YamlReaderException("Error creating object.", ex2);
                        }
                    }
                    if (!type.isArray()) {
                        throw new YamlReaderException("A sequence is not a valid value for the type: " + type.getName());
                    }
                    collection = new ArrayList();
                    elementType = type.getComponentType();
                }
                if (!type.isArray() && anchor != null) {
                    this.anchors.put(anchor, collection);
                }
                while (true) {
                    event = this.parser.peekNextEvent();
                    if (event.type == EventType.SEQUENCE_END) {
                        break;
                    }
                    collection.add(this.readValue(elementType, null, null));
                }
                this.parser.getNextEvent();
                if (!type.isArray()) {
                    return collection;
                }
                final Object array = Array.newInstance(elementType, collection.size());
                int i = 0;
                for (final Object object2 : collection) {
                    Array.set(array, i++, object2);
                }
                if (anchor != null) {
                    this.anchors.put(anchor, array);
                }
                return array;
            }
            default: {
                throw new YamlReaderException("Expected data for a " + type.getName() + " field but found: " + event.type);
            }
        }
    }
    
    private void mergeMap(final Map<String, Object> dest, final Object source) throws YamlReaderException {
        if (source instanceof Collection) {
            for (final Object item : (Collection)source) {
                this.mergeMap(dest, item);
            }
        }
        else {
            if (!(source instanceof Map)) {
                throw new YamlReaderException("Expected a mapping or a sequence of mappings for a '<<' merge field but found: " + source.getClass().getSimpleName());
            }
            final Map<String, Object> map = (Map<String, Object>)source;
            for (final Map.Entry<String, Object> entry : map.entrySet()) {
                if (!dest.containsKey(entry.getKey())) {
                    dest.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }
    
    protected Object createObject(final Class type) throws InvocationTargetException {
        final DeferredConstruction deferredConstruction = Beans.getDeferredConstruction(type, this.config);
        if (deferredConstruction != null) {
            return deferredConstruction;
        }
        return Beans.createObject(type, this.config.privateConstructors);
    }
    
    private Number valueConvertedNumber(final String value) {
        Number number = null;
        try {
            number = Long.decode(value);
        }
        catch (NumberFormatException ex) {}
        if (number == null) {
            try {
                number = Double.parseDouble(value);
            }
            catch (NumberFormatException ex2) {}
        }
        return number;
    }
    
    private void skipRange() {
        int depth = 0;
        do {
            final Event nextEvent = this.parser.getNextEvent();
            switch (nextEvent.type) {
                default: {
                    continue;
                }
                case SEQUENCE_START: {
                    ++depth;
                    continue;
                }
                case MAPPING_START: {
                    ++depth;
                    continue;
                }
                case SEQUENCE_END: {
                    --depth;
                    continue;
                }
                case MAPPING_END: {
                    --depth;
                    continue;
                }
            }
        } while (depth > 0);
    }
    
    public class YamlReaderException extends YamlException
    {
        public YamlReaderException(final String message, final Throwable cause) {
            super("Line " + YamlReader.this.parser.getLineNumber() + ", column " + YamlReader.this.parser.getColumn() + ": " + message, cause);
        }
        
        public YamlReaderException(final YamlReader this$0, final String message) {
            this(this$0, message, null);
        }
    }
}
