/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import com.aspectran.utils.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class ParameterValue implements Parameter {

    private Parameters container;

    private final String name;

    private final ValueType originValueType;

    private ValueType valueType;

    private final boolean valueTypeFixed;

    private boolean valueTypeHinted;

    private Class<? extends AbstractParameters> parametersClass;

    private boolean array;

    private boolean bracketed;

    private volatile Object value;

    private List<Object> list;

    private boolean assigned;

    public ParameterValue(String name, ValueType valueType) {
        this(name, valueType, false);
    }

    public ParameterValue(String name, ValueType valueType, boolean array) {
        this(name, valueType, array, false);
    }

    public ParameterValue(String name, ValueType valueType, boolean array,
                          boolean noBracket) {
        this(name, valueType, array, noBracket, false);
    }

    protected ParameterValue(String name, ValueType valueType, boolean array,
                             boolean noBracket, boolean valueTypeFixed) {
        this.name = name;
        this.valueType = valueType;
        this.originValueType = valueType;
        this.array = array;
        this.valueTypeFixed = (valueTypeFixed && valueType != ValueType.VARIABLE);
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
        this.valueType = ValueType.PARAMETERS;
        this.originValueType = valueType;
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
        Parameter prototype = container.getProprietor();
        if (prototype != null) {
            return prototype.getQualifiedName() + "." + name;
        }
        return name;
    }

    @Override
    public ValueType getValueType() {
        return valueType;
    }

    @Override
    public void setValueType(ValueType valueType) {
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
        Assert.state(!assigned, "This parameter cannot be converted to " +
                "an array type because it already has a value assigned to it");
        array = true;
        bracketed = true;
    }

    @Override
    public void putValue(Object value) {
        if (value != null) {
            if (valueTypeFixed) {
                value = determineValue(value);
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
    public void removeValue() {
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
                originValueType == ValueType.VARIABLE) {
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
        if (valueType == ValueType.STRING) {
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
        checkValueType(ValueType.INT);
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
        checkValueType(ValueType.INT);
        return (List<Integer>)getValueList();
    }

    @Override
    public Long getValueAsLong() {
        checkValueType(ValueType.LONG);
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
        checkValueType(ValueType.LONG);
        return (List<Long>)getValueList();
    }

    @Override
    public Float getValueAsFloat() {
        checkValueType(ValueType.FLOAT);
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
        checkValueType(ValueType.FLOAT);
        return (List<Float>)getValueList();
    }

    @Override
    public Double getValueAsDouble() {
        checkValueType(ValueType.DOUBLE);
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
        checkValueType(ValueType.DOUBLE);
        return (List<Double>)getValueList();
    }

    @Override
    public Boolean getValueAsBoolean() {
        checkValueType(ValueType.BOOLEAN);
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
        checkValueType(ValueType.BOOLEAN);
        return (List<Boolean>)getValueList();
    }

    @Override
    public Parameters getValueAsParameters() {
        checkValueType(ValueType.PARAMETERS);
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
        if (valueType != ValueType.PARAMETERS) {
            throw new IncompatibleValueTypeException(this, ValueType.PARAMETERS);
        }
        return (List<Parameters>)getValueList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T newParameters(Parameter identifier) {
        if (valueType == ValueType.VARIABLE) {
            valueType = ValueType.PARAMETERS;
            parametersClass = VariableParameters.class;
        } else {
            checkValueType(ValueType.PARAMETERS);
            if (parametersClass == null) {
                parametersClass = VariableParameters.class;
            }
        }
        try {
            T p = (T)ClassUtils.createInstance(parametersClass);
            p.setProprietor(identifier);
            putValue(p);
            return p;
        } catch (Exception e) {
            throw new InvalidParameterValueException("Failed to instantiate " + parametersClass, e);
        }
    }

    private void checkValueType(ValueType valueType) {
        if (this.valueType != ValueType.VARIABLE && this.valueType != valueType) {
            throw new IncompatibleValueTypeException(this, valueType);
        }
    }

    private void determineValueType(Object value) {
        if (valueType == ValueType.STRING) {
            if (value.toString().contains(AponFormat.NEW_LINE)) {
                valueType = ValueType.TEXT;
            }
        } else if (valueType == ValueType.VARIABLE) {
            if (value instanceof CharSequence) {
                if (value.toString().contains(AponFormat.NEW_LINE)) {
                    valueType = ValueType.TEXT;
                } else {
                    valueType = ValueType.STRING;
                }
            } else if (value instanceof Character) {
                valueType = ValueType.STRING;
            }
        }
    }

    private Object determineValue(Object value) {
        if (valueType == ValueType.STRING || valueType == ValueType.TEXT) {
            return value.toString();
        } else if (valueType == ValueType.BOOLEAN) {
            if (!(value instanceof Boolean)) {
                return Boolean.valueOf(value.toString());
            }
        } else if (valueType == ValueType.INT) {
            if (!(value instanceof Integer)) {
                try {
                    return Integer.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    throw new ValueTypeMismatchException(value.getClass(), Integer.class, e);
                }
            }
        } else if (valueType == ValueType.LONG) {
            if (!(value instanceof Long)) {
                try {
                    return Long.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    throw new ValueTypeMismatchException(value.getClass(), Long.class, e);
                }
            }
        } else if (valueType == ValueType.FLOAT) {
            if (!(value instanceof Float)) {
                try {
                    return Float.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    throw new ValueTypeMismatchException(value.getClass(), Float.class, e);
                }
            }
        } else if (valueType == ValueType.DOUBLE) {
            if (!(value instanceof Double)) {
                try {
                    return Double.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    throw new ValueTypeMismatchException(value.getClass(), Double.class, e);
                }
            }
        } else if (valueType == ValueType.PARAMETERS) {
            if (!(value instanceof Parameters)) {
                try {
                    value = AponReader.parse(value.toString(), parametersClass);
                } catch (AponParseException e) {
                    throw new ValueTypeMismatchException(value.getClass(), parametersClass, e);
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
        if (valueType == ValueType.PARAMETERS) {
            tsb.append("class", parametersClass);
        }
        return tsb.toString();
    }

}
