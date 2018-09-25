/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

    private ParameterValueType parameterValueType;

    private boolean valueTypeHinted;

    private Class<? extends AbstractParameters> parametersClass;

    private boolean array;

    private boolean bracketed;

    private final boolean predefined;

    private volatile Object value;

    private List<Object> list;

    private boolean assigned;

    public ParameterValue(String name, ParameterValueType parameterValueType) {
        this(name, parameterValueType, false);
    }

    public ParameterValue(String name, ParameterValueType parameterValueType, boolean array) {
        this(name, parameterValueType, array, false);
    }

    public ParameterValue(String name, ParameterValueType parameterValueType, boolean array,
                          boolean noBracket) {
        this(name, parameterValueType, array, noBracket, false);
    }

    protected ParameterValue(String name, ParameterValueType parameterValueType, boolean array,
                             boolean noBracket, boolean predefined) {
        this.name = name;
        this.parameterValueType = parameterValueType;
        this.originParameterValueType = parameterValueType;
        this.array = array;
        this.predefined = (predefined && parameterValueType != ParameterValueType.VARIABLE);
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
                             boolean array, boolean noBracket, boolean predefined) {
        this.name = name;
        this.parameterValueType = ParameterValueType.PARAMETERS;
        this.originParameterValueType = parameterValueType;
        this.parametersClass = parametersClass;
        this.array = array;
        this.predefined = predefined;
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
    public ParameterValueType getParameterValueType() {
        return parameterValueType;
    }

    @Override
    public void setParameterValueType(ParameterValueType parameterValueType) {
        this.parameterValueType = parameterValueType;
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
    public boolean isPredefined() {
        return predefined;
    }

    @Override
    public boolean isAssigned() {
        return assigned;
    }

    @Override
    public int getArraySize() {
        List<?> list = getValueList();
        return (list != null ? list.size() : 0);
    }

    @Override
    public void arraylize() {
        if (assigned) {
            throw new IllegalStateException("Can not change to an array type because a variable is already assigned a value");
        }
        array = true;
        bracketed = true;
    }

    @Override
    public void putValue(Object value) {
        if (!predefined && value != null) {
            if (parameterValueType == ParameterValueType.STRING) {
                if (value.toString().indexOf(AponFormat.NEW_LINE_CHAR) != -1) {
                    parameterValueType = ParameterValueType.TEXT;
                }
            } else if (parameterValueType == ParameterValueType.VARIABLE && value instanceof String) {
                if (value.toString().indexOf(AponFormat.NEW_LINE_CHAR) != -1) {
                    parameterValueType = ParameterValueType.TEXT;
                } else {
                    parameterValueType = ParameterValueType.STRING;
                }
            }
        }
        if (!predefined && !array && this.value != null) {
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
    public Object getValue() {
        return (array ? getValueList() : value);
    }

    @Override
    public List<?> getValueList() {
        if (!predefined && value != null && list == null &&
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
                return new String[] {value.toString()};
            } else {
                return null;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getValueAsStringList() {
        if (parameterValueType == ParameterValueType.STRING) {
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
        checkParameterValueType(ParameterValueType.INT);
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
        checkParameterValueType(ParameterValueType.INT);
        return (List<Integer>)getValueList();
    }

    @Override
    public Long getValueAsLong() {
        checkParameterValueType(ParameterValueType.LONG);
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
        checkParameterValueType(ParameterValueType.LONG);
        return (List<Long>)getValueList();
    }

    @Override
    public Float getValueAsFloat() {
        checkParameterValueType(ParameterValueType.FLOAT);
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
        checkParameterValueType(ParameterValueType.FLOAT);
        return (List<Float>)getValueList();
    }

    @Override
    public Double getValueAsDouble() {
        checkParameterValueType(ParameterValueType.DOUBLE);
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
        checkParameterValueType(ParameterValueType.DOUBLE);
        return (List<Double>)getValueList();
    }

    @Override
    public Boolean getValueAsBoolean() {
        checkParameterValueType(ParameterValueType.BOOLEAN);
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
        checkParameterValueType(ParameterValueType.BOOLEAN);
        return (List<Boolean>)getValueList();
    }

    @Override
    public Parameters getValueAsParameters() {
        checkParameterValueType(ParameterValueType.PARAMETERS);
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
        if (parameterValueType != ParameterValueType.PARAMETERS) {
            throw new IncompatibleParameterValueTypeException(this, ParameterValueType.PARAMETERS);
        }
        return (List<Parameters>)getValueList();
    }

    @Override
    public Parameters newParameters(Parameter identifier) {
        if (parameterValueType == ParameterValueType.VARIABLE) {
            parameterValueType = ParameterValueType.PARAMETERS;
            parametersClass = VariableParameters.class;
        } else {
            checkParameterValueType(ParameterValueType.PARAMETERS);
            if (parametersClass == null) {
                parametersClass = VariableParameters.class;
            }
        }
        try {
            Parameters p = ClassUtils.createInstance(parametersClass);
            assert p != null;
            p.setIdentifier(identifier);
            putValue(p);
            return p;
        } catch (Exception e) {
            throw new InvalidParameterException("Could not instantiate Parameters with " + parametersClass, e);
        }
    }

    private void checkParameterValueType(ParameterValueType parameterValueType) {
        if (this.parameterValueType != ParameterValueType.VARIABLE && this.parameterValueType != parameterValueType) {
            throw new IncompatibleParameterValueTypeException(this, parameterValueType);
        }
    }

    @Override
    public void clearValue() {
        value = null;
        list = null;
        assigned = false;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("parameterValueType", parameterValueType);
        if (parameterValueType == ParameterValueType.PARAMETERS) {
            tsb.append("parametersClass", parametersClass);
        }
        tsb.append("array", array);
        tsb.append("bracketed", bracketed);
        if (array) {
            tsb.append("arraySize", getArraySize());
        }
        tsb.append("qualifiedName", getQualifiedName());
        return tsb.toString();
    }

}
