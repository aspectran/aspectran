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

import java.util.List;

public class ParameterHolder {

    private static final String PARAMETER_NAME = "item";

    private final VariableParameters parameters;

    public ParameterHolder(String text, Class<? extends AbstractParameters> parametersClass, boolean array) {
        ParameterDefinition[] parameterDefinitions = new ParameterDefinition[] {
            new ParameterDefinition(PARAMETER_NAME, parametersClass, array)
        };

        if (text != null) {
            if (array) {
                text = PARAMETER_NAME + ": [\n\t{\n" + text + "\n\t}\n]";
            } else {
                text = PARAMETER_NAME + ": {\n" + text + "\n}";
            }
        }

        this.parameters = new VariableParameters(parameterDefinitions, text);
    }

    public Parameters[] getParametersArray() {
        return parameters.getParametersArray(PARAMETER_NAME);
    }

    public List<Parameters> getParametersList() {
        return parameters.getParametersList(PARAMETER_NAME);
    }

}
