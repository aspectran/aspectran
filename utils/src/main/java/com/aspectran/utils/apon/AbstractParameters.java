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
import com.aspectran.utils.BooleanUtils;
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
 * Base implementation of {@link Parameters} that stores parameter definitions
 * and values and provides convenient, type-safe accessors.
 * <p>
 * Instances may be created with a fixed structure (predefined {@link ParameterKey}s)
 * or with a variable structure where parameters can be added at runtime. This class
 * also supports hierarchical nesting of parameter groups (value type parameters).
 * </p>
 */
public abstract class AbstractParameters implements Parameters {

    private final Map<String, ParameterValue> parameterValueMap;

    private final Map<String, ParameterValue> altParameterValueMap;

    private final boolean structureFixed;

    private Parameter proprietor;

    private String actualName;

    /**
     * Instantiates a new abstract parameters.
     * @param parameterKeys the parameter keys
     */
    protected AbstractParameters(ParameterKey[] parameterKeys) {
        Map<String, ParameterValue> valueMap = new LinkedHashMap<>();
        if (parameterKeys != null) {
            Map<String, ParameterValue> altValueMap = new HashMap<>();
            for (ParameterKey pk : parameterKeys) {
                ParameterValue pv = pk.newParameterValue();
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
    @NonNull
    public String[] getParameterNames() {
        return parameterValueMap.keySet().toArray(new String[0]);
    }

    @Override
    public void putAll(Parameters parameters) {
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
    public boolean hasParameter(String name) {
        return (parameterValueMap.containsKey(name) || structureFixed && altParameterValueMap.containsKey(name));
    }

    @Override
    public boolean hasParameter(ParameterKey key) {
        checkKey(key);
        return hasParameter(key.getName());
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
                if (!hasParameter(name)) {
                    newParameterValue(name, ValueType.VARIABLE, true);
                }
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
                if (!hasParameter(name)) {
                    newParameterValue(name, ValueType.VARIABLE, true);
                }
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
                if (!hasParameter(name)) {
                    newParameterValue(name, ValueType.VARIABLE, true);
                }
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
                if (!hasParameter(name)) {
                    newParameterValue(name, ValueType.VARIABLE, true);
                }
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
                p = newParameterValue(name, valueType);
            }
            putValue(p, name, value);
        }
    }

    private void putArrayValue(String name, Object value) {
        Parameter p = getParameter(name);
        if (p == null) {
            ValueType valueType = ValueType.resolveFrom(value);
            p = newParameterValue(name, valueType, true);
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
    public Object getValue(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValue() : null);
    }

    @Override
    public Object getValue(ParameterKey key) {
        checkKey(key);
        return getValue(key.getName());
    }

    @Override
    public List<?> getValueList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueList() : null);
    }

    @Override
    public List<?> getValueList(ParameterKey key) {
        checkKey(key);
        return getValueList(key.getName());
    }

    @Override
    public String getString(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsString() : null);
    }

    @Override
    public String getString(String name, String defaultValue) {
        String s = getString(name);
        return (s != null ? s : defaultValue);
    }

    @Override
    public String getString(ParameterKey key) {
        checkKey(key);
        return getString(key.getName());
    }

    @Override
    public String getString(ParameterKey key, String defaultValue) {
        checkKey(key);
        return getString(key.getName(), defaultValue);
    }

    @Override
    public String[] getStringArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsStringArray() : null);
    }

    @Override
    public String[] getStringArray(ParameterKey key) {
        checkKey(key);
        return getStringArray(key.getName());
    }

    @Override
    public List<String> getStringList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsStringList() : null);
    }

    @Override
    public List<String> getStringList(ParameterKey key) {
        checkKey(key);
        return getStringList(key.getName());
    }

    @Override
    public Integer getInt(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsInt() : null);
    }

    @Override
    public int getInt(String name, int defaultValue) {
        Parameter p = getParameter(name);
        if (p != null) {
            Integer val = p.getValueAsInt();
            return (val != null ? val : defaultValue);
        }
        return defaultValue;
    }

    @Override
    public Integer[] getIntArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsIntArray() : null);
    }

    @Override
    public Integer getInt(ParameterKey key) {
        checkKey(key);
        return getInt(key.getName());
    }

    @Override
    public int getInt(ParameterKey key, int defaultValue) {
        checkKey(key);
        return getInt(key.getName(), defaultValue);
    }

    @Override
    public Integer[] getIntArray(ParameterKey key) {
        checkKey(key);
        return getIntArray(key.getName());
    }

    @Override
    public List<Integer> getIntList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsIntList() : null);
    }

    @Override
    public List<Integer> getIntList(ParameterKey key) {
        checkKey(key);
        return getIntList(key.getName());
    }

    @Override
    public Long getLong(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsLong() : null);
    }

    @Override
    public long getLong(String name, long defaultValue) {
        Parameter p = getParameter(name);
        if (p != null) {
            Long val = p.getValueAsLong();
            return (val != null ? val : defaultValue);
        }
        return defaultValue;
    }

    @Override
    public Long getLong(ParameterKey key) {
        checkKey(key);
        return getLong(key.getName());
    }

    @Override
    public long getLong(ParameterKey key, long defaultValue) {
        checkKey(key);
        return getLong(key.getName(), defaultValue);
    }

    @Override
    public Long[] getLongArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsLongArray() : null);
    }

    @Override
    public Long[] getLongArray(ParameterKey key) {
        checkKey(key);
        return getLongArray(key.getName());
    }

    @Override
    public List<Long> getLongList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsLongList() : null);
    }

    @Override
    public List<Long> getLongList(ParameterKey key) {
        checkKey(key);
        return getLongList(key.getName());
    }

    @Override
    public Float getFloat(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsFloat() : null);
    }

    @Override
    public float getFloat(String name, float defaultValue) {
        Parameter p = getParameter(name);
        if (p != null) {
            Float val = p.getValueAsFloat();
            return (val != null ? val : defaultValue);
        }
        return defaultValue;
    }

    @Override
    public Float getFloat(ParameterKey key) {
        checkKey(key);
        return getFloat(key.getName());
    }

    @Override
    public float getFloat(ParameterKey key, float defaultValue) {
        checkKey(key);
        return getFloat(key.getName(), defaultValue);
    }

    @Override
    public Float[] getFloatArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsFloatArray() : null);
    }

    @Override
    public Float[] getFloatArray(ParameterKey key) {
        checkKey(key);
        return getFloatArray(key.getName());
    }

    @Override
    public List<Float> getFloatList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsFloatList() : null);
    }

    @Override
    public List<Float> getFloatList(ParameterKey key) {
        checkKey(key);
        return getFloatList(key.getName());
    }

    @Override
    public Double getDouble(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsDouble() : null);
    }

    @Override
    public double getDouble(String name, double defaultValue) {
        Parameter p = getParameter(name);
        if (p != null) {
            Double val = p.getValueAsDouble();
            return (val != null ? val : defaultValue);
        }
        return defaultValue;
    }

    @Override
    public Double getDouble(ParameterKey key) {
        checkKey(key);
        return getDouble(key.getName());
    }

    @Override
    public double getDouble(ParameterKey key, double defaultValue) {
        checkKey(key);
        return getDouble(key.getName(), defaultValue);
    }

    @Override
    public Double[] getDoubleArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsDoubleArray() : null);
    }

    @Override
    public Double[] getDoubleArray(ParameterKey key) {
        checkKey(key);
        return getDoubleArray(key.getName());
    }

    @Override
    public List<Double> getDoubleList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsDoubleList() : null);
    }

    @Override
    public List<Double> getDoubleList(ParameterKey key) {
        checkKey(key);
        return getDoubleList(key.getName());
    }

    @Override
    public Boolean getBoolean(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsBoolean() : null);
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        Parameter p = getParameter(name);
        return (p != null ? BooleanUtils.toBoolean(p.getValueAsBoolean(), defaultValue) : defaultValue);
    }

    @Override
    public Boolean getBoolean(ParameterKey key) {
        checkKey(key);
        return getBoolean(key.getName());
    }

    @Override
    public boolean getBoolean(ParameterKey key, boolean defaultValue) {
        checkKey(key);
        return getBoolean(key.getName(), defaultValue);
    }

    @Override
    public Boolean[] getBooleanArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsBooleanArray() : null);
    }

    @Override
    public Boolean[] getBooleanArray(ParameterKey key) {
        checkKey(key);
        return getBooleanArray(key.getName());
    }

    @Override
    public List<Boolean> getBooleanList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsBooleanList() : null);
    }

    @Override
    public List<Boolean> getBooleanList(ParameterKey key) {
        checkKey(key);
        return getBooleanList(key.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T getParameters(String name) {
        Parameter p = getParameter(name);
        return (p != null ? (T)p.getValueAsParameters() : null);
    }

    @Override
    public <T extends Parameters> T getParameters(ParameterKey key) {
        checkKey(key);
        return getParameters(key.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T[] getParametersArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? (T[])p.getValueAsParametersArray() : null);
    }

    @Override
    public <T extends Parameters> T[] getParametersArray(ParameterKey key) {
        checkKey(key);
        return getParametersArray(key.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> List<T> getParametersList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? (List<T>)p.getValueAsParametersList() : null);
    }

    @Override
    public <T extends Parameters> List<T> getParametersList(ParameterKey key) {
        checkKey(key);
        return getParametersList(key.getName());
    }

    @Override
    public ParameterValue newParameterValue(String name, ValueType valueType) {
        return newParameterValue(name, valueType, false);
    }

    @Override
    public ParameterValue newParameterValue(String name, ValueType valueType, boolean array) {
        Assert.state(!structureFixed, "Unknown parameter: " + name);
        ParameterValue pv = new ParameterValue(name, valueType, array);
        pv.setContainer(this);
        parameterValueMap.put(name, pv);
        return pv;
    }

    @Override
    public <T extends Parameters> T newParameters(String name) {
        Parameter p = getParameter(name);
        if (structureFixed) {
            if (p == null) {
                throw new UnknownParameterException(name, this);
            }
        } else {
            if (p == null) {
                p = newParameterValue(name, ValueType.PARAMETERS);
            }
        }
        T ps = p.newParameters(p);
        ps.setActualName(name);
        return ps;
    }

    @Override
    public <T extends Parameters> T newParameters(ParameterKey key) {
        checkKey(key);
        return newParameters(key.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T touchParameters(String name) {
        Parameters parameters = getParameters(name);
        if (parameters == null) {
            parameters = newParameters(name);
        }
        return (T)parameters;
    }

    @Override
    public <T extends Parameters> T touchParameters(ParameterKey key) {
        checkKey(key);
        return touchParameters(key.getName());
    }

    @Override
    public void updateContainer(@NonNull Parameters container) {
        for (ParameterValue parameterValue : container.getParameterValues()) {
            parameterValue.setContainer(container);
        }
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
                    .nullWritable(false)
                    .write(this)
                    .toString();
        } catch (IOException e) {
            return StringUtils.EMPTY;
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

    private void checkKey(ParameterKey key) {
        Assert.notNull(key, "key must not be null");
    }

    private void checkArrayType(Parameter parameter) {
        if (structureFixed && !parameter.isArray()) {
            throw new IllegalArgumentException("Not an array type parameter: " + parameter);
        }
    }

}
