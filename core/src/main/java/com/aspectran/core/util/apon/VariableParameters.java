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
package com.aspectran.core.util.apon;

import java.io.IOException;
import java.io.Serializable;

/**
 * Parameters that determine the name and value type of each parameter in real time.
 */
public class VariableParameters extends AbstractParameters implements Serializable {

    private static final long serialVersionUID = 4492298345259110525L;

    public VariableParameters() {
        super(null);
    }

    public VariableParameters(String text) throws IOException {
        this(null, text);
    }

    public VariableParameters(ParameterKey[] parameterKeys) {
        super(parameterKeys);
    }

    public VariableParameters(ParameterKey[] parameterKeys, String apon) throws AponParseException {
        super(parameterKeys);
        readFrom(apon);
    }

    @Override
    public void putValue(String name, Object value) {
        Parameter p = touchParameterValue(name, value);
        p.putValue(value);
    }

    private Parameter touchParameterValue(String name, Object value) {
        Parameter p = getParameterValueMap().get(name);
        if (p == null && !isStructureFixed()) {
            p = newParameterValue(name, ValueType.determineValueType(value));
        }
        if (p == null) {
            throw new UnknownParameterException(name, this);
        }
        return p;
    }

}
