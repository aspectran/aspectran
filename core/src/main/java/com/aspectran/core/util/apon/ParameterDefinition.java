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

import com.aspectran.core.util.ToStringBuilder;

/**
 * A class for defining parameter attributes.
 */
public class ParameterDefinition {

    private final String name;

    private final ParameterValueType parameterValueType;

    private final Class<? extends AbstractParameters> parametersClass;

    private final boolean array;

    private final boolean noBracket;

    public ParameterDefinition(String name, ParameterValueType parameterValueType) {
        this(name, parameterValueType, false);
    }

    public ParameterDefinition(String name, ParameterValueType parameterValueType, boolean array) {
        this(name, parameterValueType, array, false);
    }

    public ParameterDefinition(String name, ParameterValueType parameterValueType, boolean array, boolean noBracket) {
        this.name = name;
        this.parameterValueType = parameterValueType;
        this.parametersClass = null;
        this.array = array;
        this.noBracket = (array && parameterValueType == ParameterValueType.PARAMETERS && noBracket);
    }

    public ParameterDefinition(String name, Class<? extends AbstractParameters> parametersClass) {
        this(name, parametersClass, false);
    }

    public ParameterDefinition(String name, Class<? extends AbstractParameters> parametersClass, boolean array) {
        this(name, parametersClass, array, false);
    }

    public ParameterDefinition(String name, Class<? extends AbstractParameters> parametersClass, boolean array, boolean noBracket) {
        this.name = name;
        this.parameterValueType = ParameterValueType.PARAMETERS;
        this.parametersClass = parametersClass;
        this.array = array;
        this.noBracket = (array && noBracket);
    }

    public String getName() {
        return name;
    }

    public ParameterValueType getParameterValueType() {
        return parameterValueType;
    }

    public boolean isArray() {
        return array;
    }

    public boolean isNoBracket() {
        return noBracket;
    }

    public ParameterValue newParameterValue() {
        ParameterValue parameterValue;
        if (parameterValueType == ParameterValueType.PARAMETERS && parametersClass != null) {
            parameterValue = new ParameterValue(name, parametersClass, array, noBracket, true);
        } else {
            parameterValue = new ParameterValue(name, parameterValueType, array, noBracket, true);
        }
        return parameterValue;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("parameterValueType", parameterValueType);
        tsb.append("parametersClass", parametersClass);
        tsb.append("array", array);
        return tsb.toString();
    }

}
