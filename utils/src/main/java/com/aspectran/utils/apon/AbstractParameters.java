/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.utils.apon;

import com.aspectran.utils.Assert;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for {@link Parameters} implementations.
 * <p>This class manages the underlying structure of parameters, which can be either
 * fixed (with predefined {@link ParameterKey}s) or variable (where parameters can be
 * added at runtime). It handles the storage of parameter values and their hierarchical
 * relationships (parent/proprietor) but delegates the implementation of type-safe
 * value accessor methods (e.g., {@code getString}, {@code getInt}) to subclasses.</p>
 *
 * @see DefaultParameters
 * @see VariableParameters
 */
public abstract class AbstractParameters implements Parameters {

    private final Map<String, ParameterValue> parameterValueMap;

    private final Map<String, ParameterValue> altParameterValueMap;

    private final boolean structureFixed;

    private Parameter proprietor;

    private String actualName;

    private boolean compactStyle = true;

    /**
     * Instantiates a new abstract parameters.
     * @param parameterKeys the parameter keys
     */
    protected AbstractParameters(ParameterKey[] parameterKeys) {
        Map<String, ParameterValue> valueMap = new LinkedHashMap<>();
        if (parameterKeys != null) {
            Map<String, ParameterValue> altValueMap = new HashMap<>();
            for (ParameterKey pk : parameterKeys) {
                ParameterValue pv = pk.createParameterValue();
                pv.setContainer(this);
                valueMap.put(pk.getName(), pv);
                if (pk.getAltNames() != null) {
                    for (String altName : pk.getAltNames()) {
                        altValueMap.put(altName, pv);
                    }
                }
            }
            this.parameterValueMap = Collections.unmodifiableMap(valueMap);
            this.altParameterValueMap = (altValueMap.isEmpty() ?
                    Collections.emptyMap() : Collections.unmodifiableMap(altValueMap));
            this.structureFixed = true;
        } else {
            this.parameterValueMap = valueMap;
            this.altParameterValueMap = Collections.emptyMap();
            this.structureFixed = false;
        }
    }

    /**
     * Instantiates a new abstract parameters.
     * @param topParameterKeys the top parameter keys
     * @param bottomParameterKeys the bottom parameter keys
     */
    protected AbstractParameters(ParameterKey[] topParameterKeys, ParameterKey[] bottomParameterKeys) {
        this(mergeParameterKeys(topParameterKeys, bottomParameterKeys));
    }

    @Override
    public boolean isCompactStyle() {
        return compactStyle;
    }

    @Override
    public void setCompactStyle(boolean compactStyle) {
        this.compactStyle = compactStyle;
    }

    @Override
    public boolean isStructureFixed() {
        return structureFixed;
    }

    @Override
    public Parameter getProprietor() {
        return proprietor;
    }

    @Override
    public void setProprietor(Parameter proprietor) {
        this.proprietor = proprietor;
    }

    @Override
    public Parameter getParent() {
        if (proprietor != null && proprietor.getContainer() != null) {
            return proprietor.getContainer().getProprietor();
        } else {
            return null;
        }
    }

    @Override
    public String getActualName() {
        if (proprietor == null) {
            return actualName;
        }
        return (actualName != null ? actualName : proprietor.getName());
    }

    @Override
    public void setActualName(String actualName) {
        this.actualName = actualName;
    }

    @Override
    public String getQualifiedName() {
        if (proprietor != null) {
            return proprietor.getQualifiedName();
        }
        return this.getClass().getName();
    }

    @Override
    public String getQualifiedName(String name) {
        ParameterValue pv = getParameterValue(name);
        return pv.getQualifiedName();
    }

    @Override
    public String getQualifiedName(ParameterKey key) {
        checkKey(key);
        ParameterValue pv = getParameterValue(key.getName());
        return pv.getQualifiedName();
    }

    @Override
    @NonNull
    public String[] getParameterNames() {
        return parameterValueMap.keySet().toArray(new String[0]);
    }

    @Override
    public ParameterValue getParameterValue(String name) {
        Assert.notNull(name, "name must not be null");
        ParameterValue pv = parameterValueMap.get(name);
        if (pv != null) {
            return pv;
        }
        if (structureFixed) {
            return altParameterValueMap.get(name);
        }
        return null;
    }

    @Override
    public ParameterValue getParameterValue(ParameterKey key) {
        checkKey(key);
        return getParameterValue(key.getName());
    }

    @Override
    @NonNull
    public Collection<ParameterValue> getParameterValues() {
        return parameterValueMap.values();
    }

    @Override
    public void mergeParameterValues(Parameters parameters) {
        Assert.notNull(parameters, "parameters must not be null");
        if (structureFixed) {
            throw new IllegalStateException("Not allowed in fixed structures");
        }
        for (ParameterValue parameterValue : parameters.getParameterValues()) {
            parameterValue.setContainer(this);
            parameterValueMap.put(parameterValue.getName(), parameterValue);
        }
    }

    @Override
    public int size() {
        return parameterValueMap.size();
    }

    @Override
    public boolean isEmpty() {
        return parameterValueMap.isEmpty();
    }

    @Override
    public Parameter getParameter(String name) {
        Parameter p = getParameterValue(name);
        if (p == null && structureFixed) {
            throw new UnknownParameterException(name, this);
        }
        return p;
    }

    @Override
    public Parameter getParameter(ParameterKey key) {
        checkKey(key);
        return getParameter(key.getName());
    }

    @Override
    public void removeParameter(String name) {
        Assert.notNull(name, "name must not be null");
        if (structureFixed) {
            throw new IllegalStateException("Not allowed in fixed structures");
        }
        parameterValueMap.remove(name);
    }

    @Override
    public void removeParameter(ParameterKey key) {
        checkKey(key);
        removeParameter(key.getName());
    }

    @Override
    public boolean hasParameter(String name) {
        return (getParameterValue(name) != null);
    }

    @Override
    public boolean hasParameter(ParameterKey key) {
        checkKey(key);
        return hasParameter(key.getName());
    }

    /**
     * Creates and adds a new {@link ParameterValue} to this container.
     * This method is only supported for {@code Parameters} with a dynamic (non-fixed) structure.
     * The new parameter is automatically added to this container's internal map.
     * @param name the name of the new parameter
     * @param valueType the {@link ValueType} of the new parameter
     * @return the newly created {@link ParameterValue} instance
     * @throws IllegalStateException if this {@code Parameters} instance has a fixed structure
     */
    @Override
    public ParameterValue attachParameterValue(String name, ValueType valueType) {
        return attachParameterValue(name, valueType, false);
    }

    /**
     * Creates and adds a new array-type {@link ParameterValue} to this container.
     * This method is only supported for {@code Parameters} with a dynamic (non-fixed) structure.
     * The new parameter is automatically added to this container's internal map.
     * @param name the name of the new parameter
     * @param valueType the {@link ValueType} of the new parameter
     * @param array whether the new parameter is an array type
     * @return the newly created {@link ParameterValue} instance
     * @throws IllegalStateException if this {@code Parameters} instance has a fixed structure
     */
    @Override
    public ParameterValue attachParameterValue(String name, ValueType valueType, boolean array) {
        Assert.state(!structureFixed, "Unknown parameter: " + name);
        ParameterValue pv = new ParameterValue(name, valueType, array);
        pv.setContainer(this);
        parameterValueMap.put(name, pv);
        return pv;
    }

    /**
     * Creates a new nested {@link Parameters} instance and attaches it as the value for the specified parameter name.
     * <p>If a parameter with the given name does not exist in a dynamic-schema container, it will be created.
     * This method delegates to {@link Parameter#attachParameters(Parameter)}, which handles the creation and attachment.
     * The resulting nested {@code Parameters} instance is set as the value of the specified parameter.
     * @param name the name of the parameter for which to create and attach a new {@code Parameters} instance
     * @param <T> the type of the nested {@code Parameters}
     * @return the newly created and attached {@code Parameters} instance
     * @throws UnknownParameterException if the parameter name is not defined in a fixed-schema container
     */
    @Override
    public <T extends Parameters> T attachParameters(String name) {
        Parameter p = getParameter(name);
        if (structureFixed) {
            if (p == null) {
                throw new UnknownParameterException(name, this);
            }
        } else {
            if (p == null) {
                p = attachParameterValue(name, ValueType.PARAMETERS);
            }
        }
        T ps = p.attachParameters(p);
        ps.setActualName(name);
        return ps;
    }

    @Override
    public <T extends Parameters> T attachParameters(ParameterKey key) {
        checkKey(key);
        return attachParameters(key.getName());
    }

    /**
     * Retrieves the nested {@link Parameters} instance for the specified name, creating and attaching it if it does not exist.
     * <p>This method provides "get-or-create" semantics. If a {@code Parameters} value is already
     * associated with the given name, it is returned. Otherwise, a new {@code Parameters} instance is
     * created using {@link #attachParameters(String)} and returned.
     * @param name the name of the nested {@code Parameters} to retrieve or create
     * @param <T> the type of the nested {@code Parameters}
     * @return the existing or newly created {@code Parameters} instance
     * @throws UnknownParameterException if the parameter name is not defined in a fixed-schema container
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T touchParameters(String name) {
        Parameters parameters = getParameters(name);
        if (parameters == null) {
            parameters = attachParameters(name);
        }
        return (T)parameters;
    }

    @Override
    public <T extends Parameters> T touchParameters(ParameterKey key) {
        checkKey(key);
        return touchParameters(key.getName());
    }

    /**
     * Creates a new nested {@link Parameters} instance but does not attach it as the value for the specified parameter.
     * <p>This method is a factory for creating new {@code Parameters} instances that are configured to be part of this
     * container's hierarchy (by setting their proprietor), but they are not automatically added as a value.
     * This is useful when you need to create an instance and manipulate it before deciding whether to attach it.
     * @param name the name of the parameter for which to create a new {@code Parameters} instance
     * @param <T> the type of the nested {@code Parameters}
     * @return the newly created, unattached {@code Parameters} instance
     * @throws UnknownParameterException if the parameter name is not defined in a fixed-schema container
     */
    @Override
    public <T extends Parameters> T createParameters(String name) {
        Parameter p = getParameter(name);
        if (structureFixed) {
            if (p == null) {
                throw new UnknownParameterException(name, this);
            }
        } else {
            if (p == null) {
                p = attachParameterValue(name, ValueType.PARAMETERS);
            }
        }
        T ps = p.createParameters(p);
        ps.setActualName(name);
        return ps;
    }

    @Override
    public void updateContainer(Parameters container) {
        Assert.notNull(container, "container must not be null");
        for (ParameterValue parameterValue : container.getParameterValues()) {
            parameterValue.setContainer(container);
        }
    }

        @Override
    public boolean isAssigned(String name) {
        Parameter p = getParameterValue(name);
        return (p != null && p.isAssigned());
    }

    @Override
    public boolean isAssigned(ParameterKey key) {
        checkKey(key);
        return isAssigned(key.getName());
    }

    @Override
    public boolean hasValue(String name) {
        Parameter p = getParameterValue(name);
        return (p != null && p.hasValue());
    }

    @Override
    public boolean hasValue(ParameterKey key) {
        checkKey(key);
        return hasValue(key.getName());
    }

    @Override
    public void putValue(String name, Object value) {
        putValue(name, value, false);
    }

    @Override
    public void putValue(ParameterKey key, Object value) {
        checkKey(key);
        putValue(key.getName(), value);
    }

    @Override
    public void putValueIfNotNull(String name, Object value) {
        putValue(name, value, true);
    }

    @Override
    public void putValueIfNotNull(ParameterKey key, Object value) {
        checkKey(key);
        putValueIfNotNull(key.getName(), value);
    }

    private void putValue(String name, Object value, boolean notNullOnly) {
        if (value == null && notNullOnly) {
            return;
        }
        if (value != null && value.getClass().isArray()) {
            int len = Array.getLength(value);
            int affected = 0;
            for (int i = 0; i < len; i++) {
                Object obj = Array.get(value, i);
                if (obj != null || !notNullOnly) {
                    putArrayValue(name, obj);
                    affected++;
                }
            }
            if (affected == 0 && !notNullOnly) {
                touchEmptyArrayParameter(name);
            }
        } else if (value instanceof Collection<?> collection) {
            int affected = 0;
            for (Object obj : collection) {
                if (obj != null || !notNullOnly) {
                    putArrayValue(name, obj);
                    affected++;
                }
            }
            if (affected == 0 && !notNullOnly) {
                touchEmptyArrayParameter(name);
            }
        } else if (value instanceof Iterator<?> iterator) {
            int affected = 0;
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj != null || !notNullOnly) {
                    putArrayValue(name, obj);
                    affected++;
                }
            }
            if (affected == 0 && !notNullOnly) {
                touchEmptyArrayParameter(name);
            }
        } else if (value instanceof Enumeration<?> enumeration) {
            int affected = 0;
            while (enumeration.hasMoreElements()) {
                Object obj = enumeration.nextElement();
                if (obj != null || !notNullOnly) {
                    putArrayValue(name, obj);
                    affected++;
                }
            }
            if (affected == 0 && !notNullOnly) {
                touchEmptyArrayParameter(name);
            }
        } else if (value instanceof Map<?, ?> map) {
            int affected = 0;
            Parameters ps = touchParameters(name);
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object obj = entry.getValue();
                if (obj != null || !notNullOnly) {
                    ps.putValue(entry.getKey().toString(), obj);
                    affected++;
                }
            }
            if (affected == 0) {
                if (notNullOnly) {
                    removeValue(name);
                } else {
                    putValue(name, null, false);
                }
            }
        } else {
            Parameter p = getParameter(name);
            if (p == null) {
                ValueType valueType = ValueType.resolveFrom(value);
                p = attachParameterValue(name, valueType);
            }
            putValue(p, name, value);
        }
    }

    private void touchEmptyArrayParameter(String name) {
        ParameterValue pv = getParameterValue(name);
        if (pv == null) {
            pv = attachParameterValue(name, ValueType.VARIABLE, true);
        }
        pv.touchValue();
    }

    private void putArrayValue(String name, Object value) {
        Parameter p = getParameter(name);
        if (p == null) {
            ValueType valueType = ValueType.resolveFrom(value);
            p = attachParameterValue(name, valueType, true);
        }
        checkArrayType(p);
        if (value != null && value.getClass().isArray()) {
            putValue(p, name, value.toString());
        } else {
            putValue(p, name, value);
        }
    }

    private void putValue(@NonNull Parameter p, String name, Object value) {
        p.putValue(value);
        if (value instanceof Parameters parameters) {
            parameters.setActualName(name);
            parameters.updateContainer(this);
        }
    }

    @Override
    public void removeValue(String name) {
        Assert.notNull(name, "name must not be null");
        Parameter p = getParameter(name);
        if (p != null) {
            p.removeValue();
        }
    }

    @Override
    public void removeValue(ParameterKey key) {
        checkKey(key);
        removeValue(key.getName());
    }

    @Override
    public void readFrom(String apon) throws AponParseException {
        if (apon != null) {
            AponReader.read(apon, this);
        }
    }

    @Override
    public void readFrom(VariableParameters parameters) throws AponParseException {
        if (parameters != null) {
            readFrom(parameters.toString());
        }
    }

    @Override
    public void readFrom(File file) throws AponParseException {
        if (file != null) {
            AponReader.read(file, this);
        }
    }

    @Override
    public void readFrom(File file, String encoding) throws AponParseException {
        if (file != null) {
            AponReader.read(file, encoding, this);
        }
    }

    @Override
    public void readFrom(Reader reader) throws AponParseException {
        if (reader != null) {
            AponReader.read(reader, this);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T copy() {
        try {
            T parameters = (T)ClassUtils.createInstance(getClass());
            parameters.readFrom(toString());
            return parameters;
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy " + ObjectUtils.identityToString(this), e);
        }
    }

    @Override
    public String describe() {
        return describe(false);
    }

    @Override
    public String describe(boolean details) {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("qualifiedName", getQualifiedName());
        if (details) {
            tsb.append("parameters", parameterValueMap);
            if (structureFixed && !altParameterValueMap.isEmpty()) {
                tsb.append("altParameters", altParameterValueMap);
            }
        } else {
            tsb.append("parameters", getParameterNames());
        }
        tsb.append("class", getClass().getName());
        tsb.append("parent", getParent());
        return tsb.toString();
    }

    @Override
    public String toString() {
        try {
            return new AponWriter()
                    .write(this)
                    .toString();
        } catch (IOException e) {
            return StringUtils.EMPTY;
        }
    }

    protected void checkKey(ParameterKey key) {
        Assert.notNull(key, "key must not be null");
    }

    protected void checkArrayType(Parameter parameter) {
        if (structureFixed && !parameter.isArray()) {
            throw new IllegalArgumentException("Not an array type parameter: " + parameter);
        }
    }

    @NonNull
    private static ParameterKey[] mergeParameterKeys(ParameterKey[] topParameterKeys, ParameterKey[] bottomParameterKeys) {
        Assert.notEmpty(topParameterKeys, "Top parameter keys must not be empty");
        Assert.notEmpty(bottomParameterKeys, "Bottom parameter keys must not be empty");
        List<ParameterKey> keys = new ArrayList<>(topParameterKeys.length + bottomParameterKeys.length);
        Collections.addAll(keys, topParameterKeys);
        Collections.addAll(keys, bottomParameterKeys);
        return keys.toArray(new ParameterKey[0]);
    }

}
