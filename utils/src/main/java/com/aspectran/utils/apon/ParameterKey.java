/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.utils.ToStringBuilder;

/**
 * A class for defining parameter attributes.
 */
public class ParameterKey {

    private final String name;

    private final String[] altNames;

    private final ValueType valueType;

    private final Class<? extends AbstractParameters> parametersClass;

    private final boolean array;

    private final boolean noBrackets;

    public ParameterKey(String name, ValueType valueType) {
        this(name, null, valueType);
    }

    public ParameterKey(String name, String[] altNames, ValueType valueType) {
        this(name, altNames, valueType, false);
    }

    public ParameterKey(String name, ValueType valueType, boolean array) {
        this(name, null, valueType, array);
    }

    public ParameterKey(String name, String[] altNames, ValueType valueType, boolean array) {
        this(name, altNames, valueType, array, false);
    }

    public ParameterKey(String name, ValueType valueType, boolean array, boolean noBrackets) {
        this(name, null, valueType, array, noBrackets);
    }

    public ParameterKey(String name, String[] altNames, ValueType valueType, boolean array,
                        boolean noBrackets) {
        this.name = name;
        this.altNames = altNames;
        this.valueType = valueType;
        this.parametersClass = null;
        this.array = array;
        this.noBrackets = (array && valueType == ValueType.PARAMETERS && noBrackets);
    }

    public ParameterKey(String name, Class<? extends AbstractParameters> parametersClass) {
        this(name, null, parametersClass);
    }

    public ParameterKey(String name, String[] altNames, Class<? extends AbstractParameters> parametersClass) {
        this(name, altNames, parametersClass, false);
    }

    public ParameterKey(String name, Class<? extends AbstractParameters> parametersClass, boolean array) {
        this(name, null, parametersClass, array);
    }

    public ParameterKey(String name, String[] altNames, Class<? extends AbstractParameters> parametersClass,
                        boolean array) {
        this(name, altNames, parametersClass, array, false);
    }

    public ParameterKey(String name, Class<? extends AbstractParameters> parametersClass, boolean array,
                        boolean noBrackets) {
        this(name, null, parametersClass, array, noBrackets);
    }

    public ParameterKey(String name, String[] altNames, Class<? extends AbstractParameters> parametersClass,
                        boolean array, boolean noBrackets) {
        this.name = name;
        this.altNames = altNames;
        this.valueType = ValueType.PARAMETERS;
        this.parametersClass = parametersClass;
        this.array = array;
        this.noBrackets = (array && noBrackets);
    }

    public String getName() {
        return name;
    }

    public String[] getAltNames() {
        return altNames;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public boolean isArray() {
        return array;
    }

    public boolean isNoBrackets() {
        return noBrackets;
    }

    public ParameterValue newParameterValue() {
        ParameterValue parameterValue;
        if (valueType == ValueType.PARAMETERS && parametersClass != null) {
            parameterValue = new ParameterValue(name, parametersClass, array, noBrackets, true);
        } else {
            parameterValue = new ParameterValue(name, valueType, array, noBrackets, true);
        }
        return parameterValue;
    }

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
