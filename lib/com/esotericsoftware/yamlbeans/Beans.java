// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans;

import java.lang.reflect.WildcardType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.lang.reflect.Field;
import java.util.TreeSet;
import java.util.LinkedHashSet;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Constructor;
import java.lang.annotation.Annotation;

class Beans
{
    private Beans() {
    }
    
    public static boolean isScalar(final Class c) {
        return c.isPrimitive() || c == String.class || c == Integer.class || c == Boolean.class || c == Float.class || c == Long.class || c == Double.class || c == Short.class || c == Byte.class || c == Character.class;
    }
    
    public static DeferredConstruction getDeferredConstruction(final Class type, final YamlConfig config) {
        final YamlConfig.ConstructorParameters parameters = config.readConfig.constructorParameters.get(type);
        if (parameters != null) {
            return new DeferredConstruction(parameters.constructor, parameters.parameterNames);
        }
        try {
            final Class constructorProperties = Class.forName("java.beans.ConstructorProperties");
            for (final Constructor typeConstructor : type.getConstructors()) {
                final Annotation annotation = typeConstructor.getAnnotation(constructorProperties);
                if (annotation != null) {
                    final String[] parameterNames = (String[])constructorProperties.getMethod("value", (Class[])new Class[0]).invoke(annotation, (Object[])null);
                    return new DeferredConstruction(typeConstructor, parameterNames);
                }
            }
        }
        catch (Exception ex) {}
        return null;
    }
    
    public static Object createObject(final Class type, final boolean privateConstructors) throws InvocationTargetException {
        Constructor constructor = null;
        for (final Constructor typeConstructor : type.getConstructors()) {
            if (typeConstructor.getParameterTypes().length == 0) {
                constructor = typeConstructor;
                break;
            }
        }
        if (constructor == null && privateConstructors) {
            try {
                constructor = type.getDeclaredConstructor((Class<?>[])new Class[0]);
                constructor.setAccessible(true);
            }
            catch (SecurityException ex2) {}
            catch (NoSuchMethodException ex3) {}
        }
        if (constructor == null) {
            try {
                if (List.class.isAssignableFrom(type)) {
                    constructor = ArrayList.class.getConstructor((Class<?>[])new Class[0]);
                }
                else if (Set.class.isAssignableFrom(type)) {
                    constructor = HashSet.class.getConstructor((Class<?>[])new Class[0]);
                }
                else if (Map.class.isAssignableFrom(type)) {
                    constructor = HashMap.class.getConstructor((Class<?>[])new Class[0]);
                }
            }
            catch (Exception ex) {
                throw new InvocationTargetException(ex, "Error getting constructor for class: " + type.getName());
            }
        }
        if (constructor == null) {
            throw new InvocationTargetException(null, "Unable to find a no-arg constructor for class: " + type.getName());
        }
        try {
            return constructor.newInstance(new Object[0]);
        }
        catch (Exception ex) {
            throw new InvocationTargetException(ex, "Error constructing instance of class: " + type.getName());
        }
    }
    
    public static Set<Property> getProperties(final Class type, final boolean beanProperties, final boolean privateFields, final YamlConfig config) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null.");
        }
        final Set<Property> properties = (Set<Property>)(config.writeConfig.keepBeanPropertyOrder ? new LinkedHashSet<Object>() : new TreeSet<Object>());
        for (final Field field : getAllFields(type)) {
            final Property property = getProperty(type, beanProperties, privateFields, config, field);
            if (property != null) {
                properties.add(property);
            }
        }
        return properties;
    }
    
    private static String toJavaIdentifier(final String name) {
        final StringBuilder buffer = new StringBuilder();
        for (int i = 0, n = name.length(); i < n; ++i) {
            final char c = name.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                buffer.append(c);
            }
        }
        return buffer.toString();
    }
    
    public static Property getProperty(final Class type, String name, final boolean beanProperties, final boolean privateFields, final YamlConfig config) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null.");
        }
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("name cannot be null or empty.");
        }
        name = toJavaIdentifier(name);
        Property property = null;
        for (final Field field : getAllFields(type)) {
            if (field.getName().equals(name)) {
                property = getProperty(type, beanProperties, privateFields, config, field);
                break;
            }
        }
        return property;
    }
    
    private static Property getProperty(final Class<?> type, final boolean beanProperties, final boolean privateFields, final YamlConfig config, final Field field) {
        Property property = null;
        if (beanProperties) {
            final String name = field.getName();
            final DeferredConstruction deferredConstruction = getDeferredConstruction(type, config);
            final boolean constructorProperty = deferredConstruction != null && deferredConstruction.hasParameter(name);
            final String nameUpper = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            String setMethodName = "set" + nameUpper;
            String getMethodName = "get" + nameUpper;
            if (field.getType().equals(Boolean.class) || field.getType().equals(Boolean.TYPE)) {
                setMethodName = (name.startsWith("is") ? ("set" + Character.toUpperCase(name.charAt(2)) + name.substring(3)) : setMethodName);
                getMethodName = (name.startsWith("is") ? name : ("is" + nameUpper));
            }
            Method getMethod = null;
            Method setMethod = null;
            try {
                setMethod = type.getMethod(setMethodName, field.getType());
            }
            catch (Exception ex) {}
            try {
                getMethod = type.getMethod(getMethodName, (Class<?>[])new Class[0]);
            }
            catch (Exception ex2) {}
            Label_0352: {
                if (getMethod == null) {
                    if (!field.getType().equals(Boolean.class)) {
                        if (!field.getType().equals(Boolean.TYPE)) {
                            break Label_0352;
                        }
                    }
                    try {
                        getMethod = type.getMethod("get" + nameUpper, (Class<?>[])new Class[0]);
                    }
                    catch (Exception ex3) {}
                }
            }
            if (getMethod != null && (setMethod != null || constructorProperty)) {
                return new MethodProperty(name, setMethod, getMethod);
            }
        }
        final int modifiers = field.getModifiers();
        if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers) && (Modifier.isPublic(modifiers) || privateFields)) {
            field.setAccessible(true);
            property = new FieldProperty(field);
        }
        return property;
    }
    
    private static ArrayList<Field> getAllFields(final Class type) {
        final ArrayList<Class> classes = new ArrayList<Class>();
        for (Class nextClass = type; nextClass != null && nextClass != Object.class; nextClass = nextClass.getSuperclass()) {
            classes.add(nextClass);
        }
        final ArrayList<Field> allFields = new ArrayList<Field>();
        for (int i = classes.size() - 1; i >= 0; --i) {
            Collections.addAll(allFields, classes.get(i).getDeclaredFields());
        }
        return allFields;
    }
    
    public static class MethodProperty extends Property
    {
        private final Method setMethod;
        private final Method getMethod;
        
        public MethodProperty(final String name, final Method setMethod, final Method getMethod) {
            super(getMethod.getDeclaringClass(), name, getMethod.getReturnType(), getMethod.getGenericReturnType());
            this.setMethod = setMethod;
            this.getMethod = getMethod;
        }
        
        @Override
        public void set(final Object object, final Object value) throws Exception {
            if (object instanceof DeferredConstruction) {
                ((DeferredConstruction)object).storeProperty(this, value);
                return;
            }
            this.setMethod.invoke(object, value);
        }
        
        @Override
        public Object get(final Object object) throws Exception {
            return this.getMethod.invoke(object, new Object[0]);
        }
    }
    
    public static class FieldProperty extends Property
    {
        private final Field field;
        
        public FieldProperty(final Field field) {
            super(field.getDeclaringClass(), field.getName(), field.getType(), field.getGenericType());
            this.field = field;
        }
        
        @Override
        public void set(final Object object, final Object value) throws Exception {
            if (object instanceof DeferredConstruction) {
                ((DeferredConstruction)object).storeProperty(this, value);
                return;
            }
            this.field.set(object, value);
        }
        
        @Override
        public Object get(final Object object) throws Exception {
            return this.field.get(object);
        }
    }
    
    public abstract static class Property implements Comparable<Property>
    {
        private final Class declaringClass;
        private final String name;
        private final Class type;
        private final Class elementType;
        
        Property(final Class declaringClass, final String name, final Class type, final Type genericType) {
            this.declaringClass = declaringClass;
            this.name = name;
            this.type = type;
            this.elementType = this.getElementTypeFromGenerics(genericType);
        }
        
        private Class getElementTypeFromGenerics(final Type type) {
            if (type instanceof ParameterizedType) {
                final ParameterizedType parameterizedType = (ParameterizedType)type;
                final Type rawType = parameterizedType.getRawType();
                if (this.isCollection(rawType) || this.isMap(rawType)) {
                    final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    if (actualTypeArguments.length > 0) {
                        final Type cType = actualTypeArguments[actualTypeArguments.length - 1];
                        if (cType instanceof Class) {
                            return (Class)cType;
                        }
                        if (cType instanceof WildcardType) {
                            final WildcardType t = (WildcardType)cType;
                            final Type bound = t.getUpperBounds()[0];
                            return (bound instanceof Class) ? ((Class)bound) : null;
                        }
                        if (cType instanceof ParameterizedType) {
                            final ParameterizedType t2 = (ParameterizedType)cType;
                            final Type rt = t2.getRawType();
                            return (rt instanceof Class) ? ((Class)rt) : null;
                        }
                    }
                }
            }
            return null;
        }
        
        private boolean isMap(final Type type) {
            return Map.class.isAssignableFrom((Class<?>)type);
        }
        
        private boolean isCollection(final Type type) {
            return Collection.class.isAssignableFrom((Class<?>)type);
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = 31 * result + ((this.declaringClass == null) ? 0 : this.declaringClass.hashCode());
            result = 31 * result + ((this.name == null) ? 0 : this.name.hashCode());
            result = 31 * result + ((this.type == null) ? 0 : this.type.hashCode());
            result = 31 * result + ((this.elementType == null) ? 0 : this.elementType.hashCode());
            return result;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final Property other = (Property)obj;
            if (this.declaringClass == null) {
                if (other.declaringClass != null) {
                    return false;
                }
            }
            else if (!this.declaringClass.equals(other.declaringClass)) {
                return false;
            }
            if (this.name == null) {
                if (other.name != null) {
                    return false;
                }
            }
            else if (!this.name.equals(other.name)) {
                return false;
            }
            if (this.type == null) {
                if (other.type != null) {
                    return false;
                }
            }
            else if (!this.type.equals(other.type)) {
                return false;
            }
            if (this.elementType == null) {
                if (other.elementType != null) {
                    return false;
                }
            }
            else if (!this.elementType.equals(other.elementType)) {
                return false;
            }
            return true;
        }
        
        public Class getDeclaringClass() {
            return this.declaringClass;
        }
        
        public Class getElementType() {
            return this.elementType;
        }
        
        public Class getType() {
            return this.type;
        }
        
        public String getName() {
            return this.name;
        }
        
        @Override
        public String toString() {
            return this.name;
        }
        
        public int compareTo(final Property o) {
            final int comparison = this.name.compareTo(o.name);
            if (comparison != 0) {
                if (this.name.equals("id")) {
                    return -1;
                }
                if (o.name.equals("id")) {
                    return 1;
                }
                if (this.name.equals("name")) {
                    return -1;
                }
                if (o.name.equals("name")) {
                    return 1;
                }
            }
            return comparison;
        }
        
        public abstract void set(final Object p0, final Object p1) throws Exception;
        
        public abstract Object get(final Object p0) throws Exception;
    }
}
