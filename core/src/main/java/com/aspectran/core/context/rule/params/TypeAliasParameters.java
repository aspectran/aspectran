/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

public class TypeAliasParameters extends AbstractParameters {

    public static final ParameterKey alias;
    public static final ParameterKey type;

    private static final ParameterKey[] parameterKeys;

    static {
        alias = new ParameterKey("alias", ValueType.STRING);
        type = new ParameterKey("type", new String[] {"typeAlias"}, ValueType.STRING);

        parameterKeys = new ParameterKey[] {
                alias,
                type
        };
    }

    public TypeAliasParameters() {
        super(parameterKeys);
    }

    public String getAlias() {
        return getString(alias);
    }

    public Object getType() {
        return getValue(type);
    }

}
