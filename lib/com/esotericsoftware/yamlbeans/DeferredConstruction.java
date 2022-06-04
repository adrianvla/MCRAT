// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans;

import java.util.Iterator;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Constructor;

class DeferredConstruction
{
    private final Constructor constructor;
    private final String[] parameterNames;
    private final ParameterValue[] parameterValues;
    private final List<PropertyValue> propertyValues;
    
    public DeferredConstruction(final Constructor constructor, final String[] parameterNames) {
        this.propertyValues = new ArrayList<PropertyValue>(16);
        this.constructor = constructor;
        this.parameterNames = parameterNames;
        this.parameterValues = new ParameterValue[parameterNames.length];
    }
    
    public Object construct() throws InvocationTargetException {
        try {
            final Object[] parameters = new Object[this.parameterValues.length];
            int i = 0;
            boolean missingParameter = false;
            for (final ParameterValue parameter : this.parameterValues) {
                if (parameter == null) {
                    missingParameter = true;
                }
                else {
                    parameters[i++] = parameter.value;
                }
            }
            Object object = null;
            Label_0149: {
                if (missingParameter) {
                    try {
                        object = this.constructor.getDeclaringClass().getConstructor((Class<?>[])new Class[0]).newInstance(new Object[0]);
                        break Label_0149;
                    }
                    catch (Exception ex2) {
                        throw new InvocationTargetException(new YamlException("Missing constructor property: " + this.parameterNames[i]));
                    }
                }
                object = this.constructor.newInstance(parameters);
            }
            for (final PropertyValue propertyValue : this.propertyValues) {
                if (propertyValue.value != null) {
                    propertyValue.property.set(object, propertyValue.value);
                }
            }
            return object;
        }
        catch (Exception ex) {
            throw new InvocationTargetException(ex, "Error constructing instance of class: " + this.constructor.getDeclaringClass().getName());
        }
    }
    
    public void storeProperty(final Beans.Property property, final Object value) {
        int index = 0;
        for (final String name : this.parameterNames) {
            if (property.getName().equals(name)) {
                final ParameterValue parameterValue = new ParameterValue();
                parameterValue.value = value;
                this.parameterValues[index] = parameterValue;
                return;
            }
            ++index;
        }
        final PropertyValue propertyValue = new PropertyValue();
        propertyValue.property = property;
        propertyValue.value = value;
        this.propertyValues.add(propertyValue);
    }
    
    public boolean hasParameter(final String name) {
        for (final String s : this.parameterNames) {
            if (s.equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    static class PropertyValue
    {
        Beans.Property property;
        Object value;
    }
    
    static class ParameterValue
    {
        Object value;
    }
}
