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
import com.aspectran.utils.ToStringBuilder;

/**
 * Describes a parameter schema entry: name, aliases, value type and array/format flags.
 * <p>
 * Used by {@link AbstractParameters} implementations to predefine the structure of
 * a {@link Parameters} set and to instantiate {@link ParameterValue} instances.
 * </p>
 */
public class ParameterKey {

    /**
     * Primary name of the parameter as it appears in APON text and in API lookups.
     */
    private final String name;

    /**
     * Optional alternative names (aliases) that resolve to the same parameter.
     * These are useful when accepting legacy or shorthand names.
     */
    private final String[] altNames;

    /**
     * Declared {@link ValueType} for this parameter (or PARAMETERS if nested structure).
     */
    private final ValueType valueType;

    /**
     * If {@link #valueType} is {@link ValueType#PARAMETERS}, the concrete class
     * to instantiate for nested parameter blocks; otherwise {@code null}.
     */
    private final Class<? extends AbstractParameters> parametersClass;

    /**
     * Whether the parameter accepts multiple values (array semantics).
     */
    private final boolean array;

    /**
     * When {@link #array} is {@code true}, whether to omit square brackets in APON output
     * for this parameter (supported for parameters of type {@link ValueType#PARAMETERS}).
     */
    private final boolean noBrackets;

    /**
     * Construct a key for a scalar parameter of the given type.
     * @param name the parameter name (must not be {@code null})
     * @param valueType the declared value type (must not be {@code null})
     */
    public ParameterKey(String name, ValueType valueType) {
        this(name, null, valueType);
    }

    /**
     * Construct a key for a scalar parameter with optional aliases.
     * @param name the primary parameter name
     * @param altNames alternative names (may be {@code null})
     * @param valueType the declared value type
     */
    public ParameterKey(String name, String[] altNames, ValueType valueType) {
        this(name, altNames, valueType, false);
    }

    /**
     * Construct a key for a parameter that may hold multiple values.
     * @param name the parameter name
     * @param valueType the declared value type
     * @param array whether the parameter accepts multiple values
     */
    public ParameterKey(String name, ValueType valueType, boolean array) {
        this(name, null, valueType, array);
    }

    /**
     * Construct a key for an array-capable parameter with optional aliases.
     * @param name the primary parameter name
     * @param altNames alternative names (may be {@code null})
     * @param valueType the declared value type
     * @param array whether the parameter accepts multiple values
     */
    public ParameterKey(String name, String[] altNames, ValueType valueType, boolean array) {
        this(name, altNames, valueType, array, false);
    }

    /**
     * Construct a key with array and bracket-format controls.
     * @param name the parameter name
     * @param valueType the declared value type
     * @param array whether the parameter accepts multiple values
     * @param noBrackets when {@code true} and the value is PARAMETERS, omit brackets in APON output
     */
    public ParameterKey(String name, ValueType valueType, boolean array, boolean noBrackets) {
        this(name, null, valueType, array, noBrackets);
    }

    /**
     * Construct a key with aliases, array flag, and bracket-format control.
     * @param name the primary parameter name
     * @param altNames alternative names (may be {@code null})
     * @param valueType the declared value type
     * @param array whether the parameter accepts multiple values
     * @param noBrackets when {@code true} and the value is PARAMETERS, omit brackets in APON output
     */
    public ParameterKey(String name, String[] altNames, ValueType valueType, boolean array, boolean noBrackets) {
        Assert.notNull(name, "Parameter name must not be null");
        Assert.notNull(valueType, "Parameter value type must not be null");
        this.name = name;
        this.altNames = altNames;
        this.valueType = valueType;
        this.parametersClass = null;
        this.array = array;
        this.noBrackets = (array && valueType == ValueType.PARAMETERS && noBrackets);
    }

    /**
     * Construct a key for nested {@link Parameters} values using the given element class.
     * @param name the parameter name
     * @param parametersClass concrete {@code Parameters} implementation for nested blocks
     */
    public ParameterKey(String name, Class<? extends AbstractParameters> parametersClass) {
        this(name, null, parametersClass);
    }

    /**
     * Construct a key for nested parameters with optional aliases.
     * @param name the primary parameter name
     * @param altNames alternative names (may be {@code null})
     * @param parametersClass concrete {@code Parameters} implementation for nested blocks
     */
    public ParameterKey(String name, String[] altNames, Class<? extends AbstractParameters> parametersClass) {
        this(name, altNames, parametersClass, false);
    }

    /**
     * Construct an array-capable key for nested parameters.
     * @param name the parameter name
     * @param parametersClass concrete {@code Parameters} implementation for nested blocks
     * @param array whether multiple nested blocks are allowed
     */
    public ParameterKey(String name, Class<? extends AbstractParameters> parametersClass, boolean array) {
        this(name, null, parametersClass, array);
    }

    /**
     * Construct an array-capable key for nested parameters with optional aliases.
     * @param name the primary parameter name
     * @param altNames alternative names (may be {@code null})
     * @param parametersClass concrete {@code Parameters} implementation for nested blocks
     * @param array whether multiple nested blocks are allowed
     */
    public ParameterKey(String name, String[] altNames, Class<? extends AbstractParameters> parametersClass,
                        boolean array) {
        this(name, altNames, parametersClass, array, false);
    }

    /**
     * Construct a key for nested parameters with array flag and bracket-format control.
     * @param name the parameter name
     * @param parametersClass concrete {@code Parameters} implementation for nested blocks
     * @param array whether multiple nested blocks are allowed
     * @param noBrackets when {@code true}, omit brackets for array output
     */
    public ParameterKey(String name, Class<? extends AbstractParameters> parametersClass, boolean array,
                        boolean noBrackets) {
        this(name, null, parametersClass, array, noBrackets);
    }

    /**
     * Construct a key for nested parameters with aliases, array flag, and bracket-format control.
     * @param name the primary parameter name
     * @param altNames alternative names (may be {@code null})
     * @param parametersClass concrete {@code Parameters} implementation for nested blocks
     * @param array whether multiple nested blocks are allowed
     * @param noBrackets when {@code true}, omit brackets for array output
     */
    public ParameterKey(String name, String[] altNames, Class<? extends AbstractParameters> parametersClass,
                        boolean array, boolean noBrackets) {
        Assert.notNull(name, "Parameter name must not be null");
        Assert.notNull(parametersClass, "parametersClass must not be null");
        this.name = name;
        this.altNames = altNames;
        this.valueType = ValueType.PARAMETERS;
        this.parametersClass = parametersClass;
        this.array = array;
        this.noBrackets = (array && noBrackets);
    }

    /**
     * Return the primary parameter name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the alternative names (aliases) for this parameter, if any.
     * @return the alias array, or {@code null}
     */
    public String[] getAltNames() {
        return altNames;
    }

    /**
     * Return the declared {@link ValueType} for this parameter.
     * @return the value type
     */
    public ValueType getValueType() {
        return valueType;
    }

    /**
     * Whether this parameter accepts multiple values (array semantics).
     * @return {@code true} if array-capable; {@code false} otherwise
     */
    public boolean isArray() {
        return array;
    }

    /**
     * Whether array values are emitted without square brackets in APON output.
     * @return {@code true} to omit brackets for arrays; {@code false} otherwise
     */
    public boolean isNoBrackets() {
        return noBrackets;
    }

    /**
     * Create a new {@link ParameterValue} holder instance consistent with this key's
     * configuration (type, array flag, bracket formatting, nested class).
     * @return a new parameter value holder
     */
    public ParameterValue newParameterValue() {
        ParameterValue parameterValue;
        if (valueType == ValueType.PARAMETERS && parametersClass != null) {
            parameterValue = new ParameterValue(name, parametersClass, array, noBrackets, true);
        } else {
            parameterValue = new ParameterValue(name, valueType, array, noBrackets, true);
        }
        return parameterValue;
    }

    /**
     * Render debug information for this key including name, aliases, type, nested class, and flags.
     */
    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("altNames", altNames);
        tsb.append("valueType", valueType);
        tsb.append("class", parametersClass);
        tsb.append("array", array);
        return tsb.toString();
    }

}
