/*
 * Copyright (c) 2008-2022 The Aspectran Project
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

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterKey;
import com.aspectran.core.util.apon.ValueType;

public class EntryParameters extends AbstractParameters {

    public static final ParameterKey name;
    public static final ParameterKey value;
    public static final ParameterKey bean;
    public static final ParameterKey tokenize;

    private static final ParameterKey[] parameterKeys;

    static {
        name = new ParameterKey("name", ValueType.STRING);
        value = new ParameterKey("value", new String[] {"entry"}, ValueType.STRING);
        tokenize = new ParameterKey("tokenize", ValueType.BOOLEAN);
        bean = new ParameterKey("bean", BeanParameters.class);

        parameterKeys = new ParameterKey[] {
                name,
                value,
                tokenize,
                bean
        };
    }

    public EntryParameters() {
        super(parameterKeys);
    }

}
