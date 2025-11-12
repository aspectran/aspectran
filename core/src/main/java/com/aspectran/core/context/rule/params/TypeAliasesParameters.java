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

/**
 * Represents the parameters for a collection of type aliases.
 */
public class TypeAliasesParameters extends DefaultParameters {

    public static final ParameterKey typeAlias;

    private static final ParameterKey[] parameterKeys;

    static {
        typeAlias = new ParameterKey("typeAlias", TypeAliasParameters.class, true, true);

        parameterKeys = new ParameterKey[] {
                typeAlias
        };
    }

    public TypeAliasesParameters() {
        super(parameterKeys);
    }

    public void putTypeAlias(String alias, Object type) {
        TypeAliasParameters typeAliasParameters = newParameters(typeAlias);
        typeAliasParameters.putValue(TypeAliasParameters.alias, alias);
        typeAliasParameters.putValue(TypeAliasParameters.type, type);
    }

}
