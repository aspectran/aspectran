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
package com.aspectran.core.context.rule.params;

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

public class SettingParameters extends AbstractParameters {

    public static final ParameterKey name;
    public static final ParameterKey value;

    private static final ParameterKey[] parameterKeys;

    static {
        name = new ParameterKey("name", ValueType.STRING);
        value = new ParameterKey("value", new String[] {"setting"}, ValueType.VARIABLE);

        parameterKeys = new ParameterKey[] {
                name,
                value
        };
    }

    public SettingParameters() {
        super(parameterKeys);
    }

    public String getName() {
        return getString(name);
    }

    public Object getValue() {
        return getValue(value);
    }

    public String getValueAsString() {
        return getString(value);
    }

}
