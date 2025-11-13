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
import com.aspectran.utils.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link Parameter} implementation that stores the parameter's
 * metadata (name, declared {@link ValueType}, array/bracket flags, nesting
 * class for {@code PARAMETERS}) and its value(s).
 * <p>
 * Supports both single values and arrays/lists, automatic adjustment of value
 * type for {@link ValueType#VARIABLE}, and creation of nested {@link Parameters}
 * instances when the value type is {@code PARAMETERS}.
 * </p>
 */
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

    private List<Object> valueList;

    private boolean assigned;

    /**
     * Create a parameter value of the specified {@link ValueType}.
     * @param name the parameter name (not null)
     * @param valueType the declared value type for this parameter (not null)
     */
    public ParameterValue(String name, ValueType valueType) {
        this(name, valueType, false);
    }

    /**
     * Create a parameter value with an explicit array flag.
     * @param name the parameter name (not null)
     * @param valueType the declared value type (not null)
     * @param array whether this parameter can hold multiple values (array)
     */
    public ParameterValue(String name, ValueType valueType, boolean array) {
        this(name, valueType, array, false);
    }

    /**
     * Create a parameter value with array and bracket formatting controls.
     * @param name the parameter name (not null)
     * @param valueType the declared value type (not null)
     * @param array whether this parameter can hold multiple values (array)
     * @param noBrackets if {@code true}, arrays are not represented with square brackets in APON
     */
    public ParameterValue(String name, ValueType valueType, boolean array, boolean noBrackets) {
        this(name, valueType, array, noBrackets, false);
    }

    /**
     * Full constructor used internally to optionally fix the value type.
     * @param name the parameter name (not null)
     * @param valueType the declared value type (not null)
     * @param array whether this parameter can hold multiple values (array)
     * @param noBrackets if {@code true}, arrays are not represented with square brackets in APON
     * @param valueTypeFixed whether the value type is fixed (non-adjustable)
     */
    protected ParameterValue(
            String name, ValueType valueType, boolean array, boolean noBrackets, boolean valueTypeFixed) {
        this(name, valueType, null, array, noBrackets, valueTypeFixed);
    }

    /**
     * Full constructor used internally to optionally fix the value type.
     * @param name the parameter name (not null)
     * @param valueType the declared value type (not null)
     * @param parametersClass the Parameters implementation for nested values
     * @param array whether this parameter can hold multiple values (array)
     * @param noBrackets if {@code true}, arrays are not represented with square brackets in APON
     * @param valueTypeFixed whether the value type is fixed (non-adjustable)
     */
    protected ParameterValue(
            String name, ValueType valueType, Class<? extends AbstractParameters> parametersClass,
            boolean array, boolean noBrackets, boolean valueTypeFixed) {
        Assert.notNull(name, "Parameter name must not be null");
        Assert.notNull(valueType, "Parameter value type must not be null");
        if (parametersClass != null && valueType != ValueType.PARAMETERS) {
            throw new IllegalArgumentException("Parameter value type must be PARAMETERS");
        }
        this.name = name;
        this.valueType = valueType;
        this.originValueType = valueType;
        this.valueTypeFixed = (valueTypeFixed && valueType != ValueType.VARIABLE);
        this.parametersClass = parametersClass;
        if (array) {
            arraylize();
            if (noBrackets) {
                setBracketed(false);
            }
        }
    }

    /**
     * Return the owning container for this parameter.
     */
    @Override
    public Parameters getContainer() {
        return container;
    }

    /**
     * Set the container that owns this parameter (internal use).
     */
    public void setContainer(Parameters container) {
        this.container = container;
    }

    /**
     * Return the local name of this parameter.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Compute a qualified name using this parameter's owner chain.
     */
    @Override
    public String getQualifiedName() {
        if (container == null) {
            return name;
        }
        Parameter prototype = container.getProprietor();
        if (prototype != null) {
            return (prototype.getQualifiedName() + "." + name);
        }
        return name;
    }

    /**
     * Return the current declared value type.
     */
    @Override
    public ValueType getValueType() {
        return valueType;
    }

    /**
     * Set the declared value type.
     */
    @Override
    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    /**
     * Whether this parameter's type is fixed and not auto-adjusted.
     */
    @Override
    public boolean isValueTypeFixed() {
        return valueTypeFixed;
    }

    /**
     * Whether this parameter's type was derived from a name hint.
     */
    @Override
    public boolean isValueTypeHinted() {
        return valueTypeHinted;
    }

    /**
     * Mark whether this parameter's type came from a hint.
     */
    @Override
    public void setValueTypeHinted(boolean valueTypeHinted) {
        this.valueTypeHinted = valueTypeHinted;
    }

    @Override
    public Class<? extends AbstractParameters> getParametersClass() {
        return parametersClass;
    }

    /**
     * Whether this parameter holds multiple values.
     */
    @Override
    public boolean isArray() {
        return array;
    }

    /**
     * Whether array values are written with square brackets in APON.
     */
    @Override
    public boolean isBracketed() {
        return bracketed;
    }

    /**
     * Control whether array values are emitted with square brackets.
     */
    public void setBracketed(boolean bracketed) {
        this.bracketed = bracketed;
    }

    /**
     * Converts this parameter into an array-type parameter.
     * <p>If a single value has already been assigned, it is preserved by
     * becoming the first element of the new array. This allows for dynamic
     * conversion from a scalar to an array on-the-fly.
     * After conversion, subsequent values added via {@link #putValue(Object)}
     * will be appended to this array.
     * @throws IllegalStateException if it is already an array type
     */
    @Override
    public void arraylize() {
        Assert.state(!array, "This parameter cannot be converted to " +
                "an array type because it is already an array type");
        array = true;
        bracketed = true;
        valueList = new ArrayList<>();
        if (assigned) {
            addValue(value);
            value = null;
        }
    }

    /**
     * Whether any value (including null) has been assigned to this parameter.
     */
    @Override
    public boolean isAssigned() {
        return assigned;
    }

    public void touchValue() {
        assigned = true;
    }

    /**
     * Whether a non-null value is present.
     */
    @Override
    public boolean hasValue() {
        if (assigned) {
            if (array) {
                return (valueList != null && !valueList.isEmpty());
            } else {
                return (value != null);
            }
        } else {
            return false;
        }
    }

    /**
     * Assign or append a value to this parameter, converting or adjusting type as needed.
     * If not fixed and a prior scalar exists, the parameter becomes an array.
     * @param value the value to assign (may be null)
     */
    @Override
    public void putValue(Object value) {
        if (value != null) {
            if (valueTypeFixed) {
                value = resolveValueByType(value);
            } else {
                adjustValueType(value);
            }
        }
        if (!valueTypeFixed && !array && assigned) {
            arraylize();
            addValue(value);
        } else {
            if (array) {
                addValue(value);
            } else {
                this.value = value;
                assigned = true;
            }
        }
    }

    private void addValue(Object value) {
        Assert.state(valueList != null, "No list has been set");
        valueList.add(value);
        assigned = true;
    }

    /**
     * Remove the current value from this parameter and clear its assigned state.
     * If the value type is not fixed, also resets array/bracket flags.
     */
    @Override
    public void removeValue() {
        if (array) {
            if (valueList != null) {
                valueList.clear();
            }
        } else {
            value = null;
        }
        assigned = false;
    }

    /**
     * Return the raw value assigned to this parameter.
     * If this parameter is an array, returns the {@link #getValueList()} instead of a scalar.
     * @return the current value or list of values, or {@code null}
     */
    @Override
    public Object getValue() {
        return (array ? getValueList() : value);
    }

    /**
     * Return the internal list of values if this parameter is in array form.
     * When the original type is VARIABLE and a scalar was previously assigned,
     * this method may convert the scalar into a one-element list.
     * @return the list of values or {@code null} if none
     */
    @Override
    public List<?> getValueList() {
        if (!valueTypeFixed && !array && assigned && originValueType == ValueType.VARIABLE) {
            arraylize();
        }
        return valueList;
    }

    /**
     * Return the values as an Object array if this parameter is in array form.
     * @return an array of values or {@code null} if no values exist
     */
    @Override
    public Object[] getValueArray() {
        List<?> list = getValueList();
        return (list != null ? list.toArray(new Object[0]) : null);
    }

    /**
     * Retrieve the current value as a String. For non-string values, calls toString().
     * @return string representation of the current scalar value, or {@code null}
     */
    @Override
    public String getValueAsString() {
        return (value != null ? value.toString() : null);
    }

    /**
     * Retrieve the value as a String array. Converts each element via toString() when needed.
     * @return the String array or {@code null} if no value exists
     */
    @Override
    public String[] getValueAsStringArray() {
        if (array) {
            List<?> list = getValueList();
            if (list != null) {
                String[] arr = new String[list.size()];
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = list.get(i).toString();
                }
                return arr;
            }
        } else if (value != null) {
            return new String[] { value.toString() };
        }
        return null;
    }

    /**
     * Retrieve the value as a List of Strings. Non-string elements are converted via toString().
     * @return the list of strings or {@code null}
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<String> getValueAsStringList() {
        if (valueType == ValueType.STRING) {
            return (List<String>)getValueList();
        }
        List<?> list1 = getValueList();
        if (list1 != null) {
            List<String> list2 = new ArrayList<>(list1.size());
            for (Object o : list1) {
                list2.add(o != null ? o.toString() : null);
            }
            return list2;
        } else {
            return null;
        }
    }

    /**
     * Retrieve the current value as an {@link Integer}.
     * @return the integer value or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of INT type (and not VARIABLE)
     */
    @Override
    public Integer getValueAsInt() {
        checkValueType(ValueType.INT);
        return (Integer)value;
    }

    /**
     * Retrieve the current value as an array of {@link Integer}.
     * @return the integer array or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of INT type (and not VARIABLE)
     */
    @Override
    public Integer[] getValueAsIntArray() {
        List<Integer> intList = getValueAsIntList();
        return (intList != null ? intList.toArray(new Integer[0]): null);
    }

    /**
     * Retrieve the current value as a {@link List} of {@link Integer}.
     * @return the list of integers or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of INT type (and not VARIABLE)
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Integer> getValueAsIntList() {
        checkValueType(ValueType.INT);
        return (List<Integer>)getValueList();
    }

    /**
     * Retrieve the current value as a {@link Long}.
     * @return the long value or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of LONG type (and not VARIABLE)
     */
    @Override
    public Long getValueAsLong() {
        checkValueType(ValueType.LONG);
        return (Long)value;
    }

    /**
     * Retrieve the current value as an array of {@link Long}.
     * @return the long array or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of LONG type (and not VARIABLE)
     */
    @Override
    public Long[] getValueAsLongArray() {
        List<Long> longList = getValueAsLongList();
        return (longList != null ? longList.toArray(new Long[0]) : null);
    }

    /**
     * Retrieve the current value as a {@link List} of {@link Long}.
     * @return the list of longs or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of LONG type (and not VARIABLE)
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Long> getValueAsLongList() {
        checkValueType(ValueType.LONG);
        return (List<Long>)getValueList();
    }

    /**
     * Retrieve the current value as a {@link Float}.
     * @return the float value or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of FLOAT type (and not VARIABLE)
     */
    @Override
    public Float getValueAsFloat() {
        checkValueType(ValueType.FLOAT);
        return (Float)value;
    }

    /**
     * Retrieve the current value as an array of {@link Float}.
     * @return the float array or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of FLOAT type (and not VARIABLE)
     */
    @Override
    public Float[] getValueAsFloatArray() {
        List<Float> floatList = getValueAsFloatList();
        return (floatList != null ? floatList.toArray(new Float[0]) : null);
    }

    /**
     * Retrieve the current value as a {@link List} of {@link Float}.
     * @return the list of floats or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of FLOAT type (and not VARIABLE)
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Float> getValueAsFloatList() {
        checkValueType(ValueType.FLOAT);
        return (List<Float>)getValueList();
    }

    /**
     * Retrieve the current value as a {@link Double}.
     * @return the double value or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of DOUBLE type (and not VARIABLE)
     */
    @Override
    public Double getValueAsDouble() {
        checkValueType(ValueType.DOUBLE);
        return (Double)value;
    }

    /**
     * Retrieve the current value as an array of {@link Double}.
     * @return the double array or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of DOUBLE type (and not VARIABLE)
     */
    @Override
    public Double[] getValueAsDoubleArray() {
        List<Double> doubleList = getValueAsDoubleList();
        return (doubleList != null ? doubleList.toArray(new Double[0]) : null);
    }

    /**
     * Retrieve the current value as a {@link List} of {@link Double}.
     * @return the list of doubles or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of DOUBLE type (and not VARIABLE)
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Double> getValueAsDoubleList() {
        checkValueType(ValueType.DOUBLE);
        return (List<Double>)getValueList();
    }

    /**
     * Retrieve the current value as a {@link Boolean}.
     * @return the boolean value or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of BOOLEAN type (and not VARIABLE)
     */
    @Override
    public Boolean getValueAsBoolean() {
        checkValueType(ValueType.BOOLEAN);
        return (Boolean)value;
    }

    /**
     * Retrieve the current value as an array of {@link Boolean}.
     * @return the boolean array or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of BOOLEAN type (and not VARIABLE)
     */
    @Override
    public Boolean[] getValueAsBooleanArray() {
        List<Boolean> booleanList = getValueAsBooleanList();
        return (booleanList != null ? booleanList.toArray(new Boolean[0]) : null);
    }

    /**
     * Retrieve the current value as a {@link List} of {@link Boolean}.
     * @return the list of booleans or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of BOOLEAN type (and not VARIABLE)
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Boolean> getValueAsBooleanList() {
        checkValueType(ValueType.BOOLEAN);
        return (List<Boolean>)getValueList();
    }

    /**
     * Retrieve the current value as nested {@link Parameters}.
     * @return the nested container or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of PARAMETERS type (and not VARIABLE)
     */
    @Override
    public Parameters getValueAsParameters() {
        checkValueType(ValueType.PARAMETERS);
        return (Parameters)value;
    }

    /**
     * Retrieve the current value as an array of nested {@link Parameters}.
     * @return the array of nested containers or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of PARAMETERS type (and not VARIABLE)
     */
    @Override
    public Parameters[] getValueAsParametersArray() {
        List<Parameters> parametersList = getValueAsParametersList();
        return (parametersList != null ? parametersList.toArray(new Parameters[0]) : null);
    }

    /**
     * Retrieve the current value as a {@link List} of nested {@link Parameters}.
     * @return the list of nested containers or {@code null}
     * @throws IncompatibleValueTypeException if this parameter is not of PARAMETERS type (and not VARIABLE)
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Parameters> getValueAsParametersList() {
        if (valueType != ValueType.PARAMETERS) {
            throw new IncompatibleValueTypeException(this, ValueType.PARAMETERS);
        }
        return (List<Parameters>)getValueList();
    }

    /**
     * Create and attach a new nested {@link Parameters} instance under this parameter.
     * If the current type is VARIABLE, it is treated as PARAMETERS and a VariableParameters implementation is used by default.
     * @param <T> the type of nested container to return
     * @param identifier the parameter metadata/owner used to set the proprietor of the nested container
     * @return the created nested container instance
     * @throws IncompatibleValueTypeException if the declared type is not PARAMETERS (nor VARIABLE)
     * @throws InvalidParameterValueException if instantiation of the nested container fails
     */
    @Override
    public <T extends Parameters> T newParameters(Parameter identifier) {
        T ps = createParameters(identifier);
        putValue(ps);
        return ps;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T createParameters(Parameter identifier) {
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
            T ps = (T)ClassUtils.createInstance(parametersClass);
            ps.setProprietor(identifier);
            return ps;
        } catch (Exception e) {
            throw new InvalidParameterValueException("Failed to instantiate " + parametersClass, e);
        }
    }

    private void checkValueType(ValueType valueType) {
        if (this.valueType != ValueType.VARIABLE && this.valueType != valueType) {
            throw new IncompatibleValueTypeException(this, valueType);
        }
    }

    private void adjustValueType(Object value) {
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

    private Object resolveValueByType(Object value) {
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
                    value = AponReader.read(value.toString(), parametersClass);
                } catch (AponParseException e) {
                    throw new ValueTypeMismatchException(value.getClass(), parametersClass, e);
                }
            }
        }
        return value;
    }

    /**
     * Render a debug-friendly representation of this parameter including name, type, flags,
     * qualified name, and array size or nested class information as applicable.
     */
    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("valueType", valueType);
        tsb.append("array", array);
        if (array && valueList != null) {
            tsb.append("size", valueList.size());
        }
        tsb.append("bracketed", bracketed);
        tsb.append("qualifiedName", getQualifiedName());
        if (valueType == ValueType.PARAMETERS) {
            tsb.append("class", parametersClass);
        }
        return tsb.toString();
    }

}
