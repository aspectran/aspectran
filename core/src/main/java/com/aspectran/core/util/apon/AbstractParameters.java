/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.util.apon;

import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ToStringBuilder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractParameters implements Parameters {

    private final Map<String, ParameterValue> parameterValueMap = new LinkedHashMap<>();

    private final boolean predefined;

    private Parameter identifier;

    private String indentString;

    protected AbstractParameters(ParameterDefinition[] parameterDefinitions) {
        if (parameterDefinitions != null) {
            for (ParameterDefinition pd : parameterDefinitions) {
                ParameterValue pv = pd.newParameterValue();
                pv.setContainer(this);
                this.parameterValueMap.put(pd.getName(), pv);
            }
            this.predefined = true;
        } else {
            this.predefined = false;
        }
    }

    @Override
    public boolean isPredefined() {
        return predefined;
    }

    @Override
    public Parameter getIdentifier() {
        return identifier;
    }

    @Override
    public void setIdentifier(Parameter identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getQualifiedName() {
        if (identifier != null) {
            return identifier.getQualifiedName();
        }
        return this.getClass().getName();
    }

    @Override
    public Parameter getParent() {
        if (identifier != null) {
            if (identifier.getContainer() != null) {
                if (identifier.getContainer().getIdentifier() != null) {
                    return identifier.getContainer().getIdentifier();
                }
            }
        }
        return null;
    }

    @Override
    public void updateContainer(Parameters parameters) {
        for (ParameterValue parameterValue : parameters.getParameterValueMap().values()) {
            parameterValue.setContainer(parameters);
        }
    }

    @Override
    public Map<String, ParameterValue> getParameterValueMap() {
        return parameterValueMap;
    }

    @Override
    public String[] getParameterNames() {
        return parameterValueMap.keySet().toArray(new String[0]);
    }

    @Override
    public Set<String> getParameterNameSet() {
        return parameterValueMap.keySet();
    }

    @Override
    public boolean hasParameter(String name) {
        return (parameterValueMap.get(name) != null);
    }

    @Override
    public boolean hasParameter(ParameterDefinition parameterDefinition) {
        return hasParameter(parameterDefinition.getName());
    }

    @Override
    public boolean isAssigned(String name) {
        Parameter p = parameterValueMap.get(name);
        return (p != null && p.isAssigned());
    }

    @Override
    public boolean isAssigned(ParameterDefinition parameterDefinition) {
        return isAssigned(parameterDefinition.getName());
    }

    @Override
    public boolean hasValue(String name) {
        Parameter p = parameterValueMap.get(name);
        return (p != null && p.hasValue());
    }

    @Override
    public boolean hasValue(ParameterDefinition parameterDefinition) {
        Parameter p = parameterValueMap.get(parameterDefinition.getName());
        return (p != null && p.hasValue());
    }

    @Override
    public Parameter getParameter(String name) {
        Parameter p = parameterValueMap.get(name);
        if (predefined && p == null) {
            throw new UnknownParameterException(name, this);
        }
        return p;
    }

    @Override
    public Parameter getParameter(ParameterDefinition parameterDefinition) {
        return getParameter(parameterDefinition.getName());
    }

    @Override
    public Object getValue(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValue() : null);
    }

    @Override
    public Object getValue(ParameterDefinition parameterDefinition) {
        return getValue(parameterDefinition.getName());
    }

    @Override
    public void putAll(Parameters parameters) {
        for (ParameterValue parameterValue : parameters.getParameterValueMap().values()) {
            parameterValue.setContainer(this);
        }
        parameterValueMap.putAll(parameters.getParameterValueMap());
    }

    @Override
    public void putValue(String name, Object value) {
        Parameter p = getParameter(name);
        if (p == null) {
            p = newParameterValue(name, ParameterValueType.determineValueType(value));
        }
        p.putValue(value);
        if (value instanceof Parameters) {
            ((Parameters)value).updateContainer(this);
        }
    }

    @Override
    public void putValue(ParameterDefinition parameterDefinition, Object value) {
        putValue(parameterDefinition.getName(), value);
    }

    @Override
    public void putValueNonNull(String name, Object value) {
        if (value != null) {
            putValue(name, value);
        }
    }

    @Override
    public void putValueNonNull(ParameterDefinition parameterDefinition, Object value) {
        if (value != null) {
            putValue(parameterDefinition.getName(), value);
        }
    }

    @Override
    public void clearValue(String name) {
        if (predefined) {
            Parameter p = getParameter(name);
            if (p != null) {
                p.clearValue();
            }
        } else {
            parameterValueMap.remove(name);
        }
    }

    @Override
    public void clearValue(ParameterDefinition parameterDefinition) {
        clearValue(parameterDefinition.getName());
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
    public String getString(ParameterDefinition parameterDefinition) {
        return getString(parameterDefinition.getName());
    }

    @Override
    public String getString(ParameterDefinition parameterDefinition, String defaultValue) {
        return getString(parameterDefinition.getName(), defaultValue);
    }

    @Override
    public String[] getStringArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsStringArray() : null);
    }

    @Override
    public String[] getStringArray(ParameterDefinition parameterDefinition) {
        return getStringArray(parameterDefinition.getName());
    }

    @Override
    public List<String> getStringList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsStringList() : null);
    }

    @Override
    public List<String> getStringList(ParameterDefinition parameterDefinition) {
        return getStringList(parameterDefinition.getName());
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
    public Integer getInt(ParameterDefinition parameterDefinition) {
        return getInt(parameterDefinition.getName());
    }

    @Override
    public int getInt(ParameterDefinition parameterDefinition, int defaultValue) {
        return getInt(parameterDefinition.getName(), defaultValue);
    }

    @Override
    public Integer[] getIntArray(ParameterDefinition parameterDefinition) {
        return getIntArray(parameterDefinition.getName());
    }

    @Override
    public List<Integer> getIntList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsIntList() : null);
    }

    @Override
    public List<Integer> getIntList(ParameterDefinition parameterDefinition) {
        return getIntList(parameterDefinition.getName());
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
    public Long getLong(ParameterDefinition parameterDefinition) {
        return getLong(parameterDefinition.getName());
    }

    @Override
    public long getLong(ParameterDefinition parameterDefinition, long defaultValue) {
        return getLong(parameterDefinition.getName(), defaultValue);
    }

    @Override
    public Long[] getLongArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsLongArray() : null);
    }

    @Override
    public Long[] getLongArray(ParameterDefinition parameterDefinition) {
        return getLongArray(parameterDefinition.getName());
    }

    @Override
    public List<Long> getLongList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsLongList() : null);
    }

    @Override
    public List<Long> getLongList(ParameterDefinition parameterDefinition) {
        return getLongList(parameterDefinition.getName());
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
    public Float getFloat(ParameterDefinition parameterDefinition) {
        return getFloat(parameterDefinition.getName());
    }

    @Override
    public float getFloat(ParameterDefinition parameterDefinition, float defaultValue) {
        return getFloat(parameterDefinition.getName(), defaultValue);
    }

    @Override
    public Float[] getFloatArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsFloatArray() : null);
    }

    @Override
    public Float[] getFloatArray(ParameterDefinition parameterDefinition) {
        return getFloatArray(parameterDefinition.getName());
    }

    @Override
    public List<Float> getFloatList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsFloatList() : null);
    }

    @Override
    public List<Float> getFloatList(ParameterDefinition parameterDefinition) {
        return getFloatList(parameterDefinition.getName());
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
    public Double getDouble(ParameterDefinition parameterDefinition) {
        return getDouble(parameterDefinition.getName());
    }

    @Override
    public double getDouble(ParameterDefinition parameterDefinition, double defaultValue) {
        return getDouble(parameterDefinition.getName(), defaultValue);
    }

    @Override
    public Double[] getDoubleArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsDoubleArray() : null);
    }

    @Override
    public Double[] getDoubleArray(ParameterDefinition parameterDefinition) {
        return getDoubleArray(parameterDefinition.getName());
    }

    @Override
    public List<Double> getDoubleList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsDoubleList() : null);
    }

    @Override
    public List<Double> getDoubleList(ParameterDefinition parameterDefinition) {
        return getDoubleList(parameterDefinition.getName());
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
    public Boolean getBoolean(ParameterDefinition parameterDefinition) {
        return getBoolean(parameterDefinition.getName());
    }

    @Override
    public boolean getBoolean(ParameterDefinition parameterDefinition, boolean defaultValue) {
        return getBoolean(parameterDefinition.getName(), defaultValue);
    }

    @Override
    public Boolean[] getBooleanArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsBooleanArray() : null);
    }

    @Override
    public Boolean[] getBooleanArray(ParameterDefinition parameterDefinition) {
        return getBooleanArray(parameterDefinition.getName());
    }

    @Override
    public List<Boolean> getBooleanList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsBooleanList() : null);
    }

    @Override
    public List<Boolean> getBooleanList(ParameterDefinition parameterDefinition) {
        return getBooleanList(parameterDefinition.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T getParameters(String name) {
        Parameter p = getParameter(name);
        return (p != null ? (T)p.getValue() : null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T getParameters(ParameterDefinition parameterDefinition) {
        return (T)getParameters(parameterDefinition.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T[] getParametersArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? (T[])p.getValueAsParametersArray() : null);
    }

    @Override
    public <T extends Parameters> T[] getParametersArray(ParameterDefinition parameterDefinition) {
        return getParametersArray(parameterDefinition.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> List<T> getParametersList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? (List<T>)p.getValueAsParametersList() : null);
    }

    @Override
    public <T extends Parameters> List<T> getParametersList(ParameterDefinition parameterDefinition) {
        return getParametersList(parameterDefinition.getName());
    }

    @Override
    public ParameterValue newParameterValue(String name, ParameterValueType valueType) {
        return newParameterValue(name, valueType, false);
    }

    @Override
    public ParameterValue newParameterValue(String name, ParameterValueType valueType, boolean array) {
        ParameterValue pv = new ParameterValue(name, valueType, array);
        pv.setContainer(this);
        parameterValueMap.put(name, pv);
        return pv;
    }

    @Override
    public <T extends Parameters> T newParameters(String name) {
        Parameter p = getParameter(name);
        if (p == null) {
            throw new UnknownParameterException(name, this);
        }
        return p.newParameters(p);
    }

    @Override
    public <T extends Parameters> T newParameters(ParameterDefinition parameterDefinition) {
        return newParameters(parameterDefinition.getName());
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
    public <T extends Parameters> T touchParameters(ParameterDefinition parameterDefinition) {
        return touchParameters(parameterDefinition.getName());
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
        } else {
            tsb.append("parameters", getParameterNames());
        }
        tsb.append("parent", getParent());
        return tsb.toString();
    }

    @Override
    public void setIndentString(String indentString) {
        this.indentString = indentString;
    }

    @Override
    public void readFrom(String text) throws IOException {
        if (text != null) {
            AponReader.parse(text, this);
        }
    }

    @Override
    public String toString() {
        if (indentString != null) {
            return AponWriter.stringify(this, indentString);
        } else {
            return AponWriter.stringify(this);
        }
    }

}
