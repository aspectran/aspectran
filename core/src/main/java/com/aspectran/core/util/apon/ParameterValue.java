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

import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class ParameterValue implements Parameter {

    private Parameters container;

    private final String name;

    private final ParameterValueType originParameterValueType;

    private ParameterValueType valueType;

    private final boolean valueTypeFixed;

    private boolean valueTypeHinted;

    private Class<? extends AbstractParameters> parametersClass;

    private boolean array;

    private boolean bracketed;

    private volatile Object value;

    private List<Object> list;

    private boolean assigned;

    public ParameterValue(String name, ParameterValueType valueType) {
        this(name, valueType, false);
    }

    public ParameterValue(String name, ParameterValueType valueType, boolean array) {
        this(name, valueType, array, false);
    }

    public ParameterValue(String name, ParameterValueType valueType, boolean array,
                          boolean noBracket) {
        this(name, valueType, array, noBracket, false);
    }

    protected ParameterValue(String name, ParameterValueType valueType, boolean array,
                             boolean noBracket, boolean valueTypeFixed) {
        this.name = name;
        this.valueType = valueType;
        this.originParameterValueType = valueType;
        this.array = array;
        this.valueTypeFixed = (valueTypeFixed && valueType != ParameterValueType.VARIABLE);
        if (this.array && !noBracket) {
            this.bracketed = true;
        }
    }

    public ParameterValue(String name, Class<? extends AbstractParameters> parametersClass) {
        this(name, parametersClass, false);
    }

    public ParameterValue(String name, Class<? extends AbstractParameters> parametersClass,
                          boolean array) {
        this(name, parametersClass, array, false);
    }

    public ParameterValue(String name, Class<? extends AbstractParameters> parametersClass,
                          boolean array, boolean noBracket) {
        this(name, parametersClass, array, noBracket, false);
    }

    protected ParameterValue(String name, Class<? extends AbstractParameters> parametersClass,
                             boolean array, boolean noBracket, boolean valueTypeFixed) {
        this.name = name;
        this.valueType = ParameterValueType.PARAMETERS;
        this.originParameterValueType = valueType;
        this.parametersClass = parametersClass;
        this.array = array;
        this.valueTypeFixed = valueTypeFixed;
        if (this.array && !noBracket) {
            this.bracketed = true;
        }
    }

    @Override
    public Parameters getContainer() {
        return container;
    }

    public void setContainer(Parameters container) {
        this.container = container;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getQualifiedName() {
        if (container == null) {
            return name;
        }
        Parameter prototype = container.getIdentifier();
        if (prototype != null) {
            return prototype.getQualifiedName() + "." + name;
        }
        return name;
    }

    @Override
    public ParameterValueType getValueType() {
        return valueType;
    }

    @Override
    public void setValueType(ParameterValueType valueType) {
        this.valueType = valueType;
    }

    @Override
    public boolean isValueTypeFixed() {
        return valueTypeFixed;
    }

    @Override
    public boolean isValueTypeHinted() {
        return valueTypeHinted;
    }

    @Override
    public void setValueTypeHinted(boolean valueTypeHinted) {
        this.valueTypeHinted = valueTypeHinted;
    }

    @Override
    public boolean isArray() {
        return array;
    }

    @Override
    public boolean isBracketed() {
        return bracketed;
    }

    public void setBracketed(boolean bracketed) {
        this.bracketed = bracketed;
    }

    @Override
    public boolean isAssigned() {
        return assigned;
    }

    @Override
    public boolean hasValue() {
        if (assigned) {
            if (array) {
                return (list != null);
            } else {
                return (value != null);
            }
        } else {
            return false;
        }
    }

    @Override
    public int getArraySize() {
        List<?> list = getValueList();
        return (list != null ? list.size() : 0);
    }

    @Override
    public void arraylize() {
        if (assigned) {
            throw new IllegalStateException(
                    "This parameter can not be converted to an array type: it already has a value assigned to it");
        }
        array = true;
        bracketed = true;
    }

    @Override
    public void putValue(Object value) {
        if (value != null) {
            if (valueTypeFixed) {
                value = fitValue(value);
            } else {
                determineValueType(value);
            }
        }
        if (!valueTypeFixed && !array && this.value != null) {
            addValue(this.value);
            addValue(value);
            this.value = null;
            array = true;
            bracketed = true;
        } else {
            if (array) {
                addValue(value);
            } else {
                this.value = value;
                assigned = true;
            }
        }
    }

    private synchronized void addValue(Object value) {
        if (list == null) {
            list = new ArrayList<>();
            assigned = true;
        }
        list.add(value);
    }

    @Override
    public void clearValue() {
        value = null;
        list = null;
        assigned = false;
        if (!valueTypeFixed) {
            array = false;
            bracketed = false;
        }
    }

    @Override
    public Object getValue() {
        return (array ? getValueList() : value);
    }

    @Override
    public List<?> getValueList() {
        if (!valueTypeFixed && value != null && list == null &&
                originParameterValueType == ParameterValueType.VARIABLE) {
            addValue(value);
        }
        return list;
    }

    @Override
    public Object[] getValues() {
        List<?> list = getValueList();
        return (list != null ? list.toArray(new Object[0]) : null);
    }

    @Override
    public String getValueAsString() {
        return (value != null ? value.toString() : null);
    }

    @Override
    public String[] getValueAsStringArray() {
        if (array) {
            List<?> list = getValueList();
            if (list != null) {
                String[] s = new String[list.size()];
                for (int i = 0; i < s.length; i++) {
                    s[i] = list.get(i).toString();
                }
                return s;
            } else {
                return null;
            }
        } else {
            if (value != null) {
                return new String[] { value.toString() };
            } else {
                return null;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getValueAsStringList() {
        if (valueType == ParameterValueType.STRING) {
            return (List<String>)getValueList();
        } else {
            List<?> list1 = getValueList();
            if (list1 != null) {
                List<String> list2 = new ArrayList<>();
                for (Object o : list1) {
                    list2.add(o.toString());
                }
                return list2;
            } else {
                return null;
            }
        }
    }

    @Override
    public Integer getValueAsInt() {
        checkValueType(ParameterValueType.INT);
        return (Integer)value;
    }

    @Override
    public Integer[] getValueAsIntArray() {
        List<Integer> intList = getValueAsIntList();
        return (intList != null ? intList.toArray(new Integer[0]): null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Integer> getValueAsIntList() {
        checkValueType(ParameterValueType.INT);
        return (List<Integer>)getValueList();
    }

    @Override
    public Long getValueAsLong() {
        checkValueType(ParameterValueType.LONG);
        return (Long)value;
    }

    @Override
    public Long[] getValueAsLongArray() {
        List<Long> longList = getValueAsLongList();
        return (longList != null ? longList.toArray(new Long[0]) : null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Long> getValueAsLongList() {
        checkValueType(ParameterValueType.LONG);
        return (List<Long>)getValueList();
    }

    @Override
    public Float getValueAsFloat() {
        checkValueType(ParameterValueType.FLOAT);
        return (Float)value;
    }

    @Override
    public Float[] getValueAsFloatArray() {
        List<Float> floatList = getValueAsFloatList();
        return (floatList != null ? floatList.toArray(new Float[0]) : null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Float> getValueAsFloatList() {
        checkValueType(ParameterValueType.FLOAT);
        return (List<Float>)getValueList();
    }

    @Override
    public Double getValueAsDouble() {
        checkValueType(ParameterValueType.DOUBLE);
        return (Double)value;
    }

    @Override
    public Double[] getValueAsDoubleArray() {
        List<Double> doubleList = getValueAsDoubleList();
        return (doubleList != null ? doubleList.toArray(new Double[0]) : null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Double> getValueAsDoubleList() {
        checkValueType(ParameterValueType.DOUBLE);
        return (List<Double>)getValueList();
    }

    @Override
    public Boolean getValueAsBoolean() {
        checkValueType(ParameterValueType.BOOLEAN);
        return (Boolean)value;
    }

    @Override
    public Boolean[] getValueAsBooleanArray() {
        List<Boolean> booleanList = getValueAsBooleanList();
        return (booleanList != null ? booleanList.toArray(new Boolean[0]) : null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Boolean> getValueAsBooleanList() {
        checkValueType(ParameterValueType.BOOLEAN);
        return (List<Boolean>)getValueList();
    }

    @Override
    public Parameters getValueAsParameters() {
        checkValueType(ParameterValueType.PARAMETERS);
        return (Parameters)value;
    }

    @Override
    public Parameters[] getValueAsParametersArray() {
        List<Parameters> parametersList = getValueAsParametersList();
        return (parametersList != null ? parametersList.toArray(new Parameters[0]) : null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Parameters> getValueAsParametersList() {
        if (valueType != ParameterValueType.PARAMETERS) {
            throw new IncompatibleParameterValueTypeException(this, ParameterValueType.PARAMETERS);
        }
        return (List<Parameters>)getValueList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T newParameters(Parameter identifier) {
        if (valueType == ParameterValueType.VARIABLE) {
            valueType = ParameterValueType.PARAMETERS;
            parametersClass = VariableParameters.class;
        } else {
            checkValueType(ParameterValueType.PARAMETERS);
            if (parametersClass == null) {
                parametersClass = VariableParameters.class;
            }
        }
        try {
            T p = (T)ClassUtils.createInstance(parametersClass);
            p.setIdentifier(identifier);
            putValue(p);
            return p;
        } catch (Exception e) {
            throw new InvalidParameterValueException("Failed to instantiate " + parametersClass, e);
        }
    }

    private void checkValueType(ParameterValueType valueType) {
        if (this.valueType != ParameterValueType.VARIABLE && this.valueType != valueType) {
            throw new IncompatibleParameterValueTypeException(this, valueType);
        }
    }

    private void determineValueType(Object value) {
        if (valueType == ParameterValueType.STRING) {
            if (value.toString().indexOf(AponFormat.NEW_LINE_CHAR) != -1) {
                valueType = ParameterValueType.TEXT;
            }
        } else if (valueType == ParameterValueType.VARIABLE && value instanceof String) {
            if (value.toString().indexOf(AponFormat.NEW_LINE_CHAR) != -1) {
                valueType = ParameterValueType.TEXT;
            } else {
                valueType = ParameterValueType.STRING;
            }
        }
    }

    private Object fitValue(Object value) {
        if (valueType == ParameterValueType.BOOLEAN) {
            if (!(value instanceof Boolean)) {
                return Boolean.valueOf(value.toString());
            }
        } else if (valueType == ParameterValueType.INT) {
            if (!(value instanceof Integer)) {
                try {
                    return Integer.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    throw new ParameterValueTypeMismatchException(value.getClass(), Integer.class, e);
                }
            }
        } else if (valueType == ParameterValueType.LONG) {
            if (!(value instanceof Long)) {
                try {
                    return Long.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    throw new ParameterValueTypeMismatchException(value.getClass(), Long.class, e);
                }
            }
        } else if (valueType == ParameterValueType.FLOAT) {
            if (!(value instanceof Float)) {
                try {
                    return Float.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    throw new ParameterValueTypeMismatchException(value.getClass(), Float.class, e);
                }
            }
        } else if (valueType == ParameterValueType.DOUBLE) {
            if (!(value instanceof Double)) {
                try {
                    return Double.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    throw new ParameterValueTypeMismatchException(value.getClass(), Double.class, e);
                }
            }
        }
        return value;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("valueType", valueType);
        tsb.append("array", array);
        if (array) {
            tsb.append("arraySize", getArraySize());
        }
        tsb.append("bracketed", bracketed);
        tsb.append("qualifiedName", getQualifiedName());
        if (valueType == ParameterValueType.PARAMETERS) {
            tsb.append("class", parametersClass);
        }
        return tsb.toString();
    }

}
