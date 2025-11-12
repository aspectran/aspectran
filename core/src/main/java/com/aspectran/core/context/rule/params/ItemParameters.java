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

import com.aspectran.utils.apon.DefaultParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

/**
 * Represents the parameters for an item rule.
 */
public class ItemParameters extends DefaultParameters {

    public static final ParameterKey type;
    public static final ParameterKey name;
    public static final ParameterKey value;
    public static final ParameterKey entry;
    public static final ParameterKey bean;
    public static final ParameterKey valueType;
    public static final ParameterKey tokenize;
    public static final ParameterKey mandatory;
    public static final ParameterKey secret;

    private static final ParameterKey[] parameterKeys;

    static {
        type = new ParameterKey("type", ValueType.STRING);
        name = new ParameterKey("name", ValueType.STRING);
        value = new ParameterKey("value", new String[] {"item"}, ValueType.VARIABLE);
        entry = new ParameterKey("entry", EntryParameters.class, true, true);
        bean = new ParameterKey("bean", BeanParameters.class, true, true);
        valueType = new ParameterKey("valueType", ValueType.STRING);
        tokenize = new ParameterKey("tokenize", ValueType.BOOLEAN);
        mandatory = new ParameterKey("mandatory", ValueType.BOOLEAN);
        secret = new ParameterKey("secret", ValueType.BOOLEAN);

        parameterKeys = new ParameterKey[] {
                type,
                name,
                value,
                entry,
                bean,
                valueType,
                tokenize,
                mandatory,
                secret
        };
    }

    public ItemParameters() {
        super(parameterKeys);
    }

}
