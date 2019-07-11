/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

public class ActionParameters extends AbstractParameters {

    public static final ParameterKey caseNo;

    public static final ParameterKey id;

    public static final ParameterKey bean;
    public static final ParameterKey method;
    public static final ParameterKey arguments;
    public static final ParameterKey properties;

    public static final ParameterKey include;
    public static final ParameterKey parameters;
    public static final ParameterKey attributes;

    public static final ParameterKey profile;
    public static final ParameterKey item;

    public static final ParameterKey hidden;

    private static final ParameterKey[] parameterKeys;

    static {
        caseNo = new ParameterKey("caseNo", ValueType.INT);
        id = new ParameterKey("id", ValueType.STRING);
        bean = new ParameterKey("bean", ValueType.STRING);
        method = new ParameterKey("method", ValueType.STRING);
        arguments = new ParameterKey("arguments", ItemHolderParameters.class, true, true);
        properties = new ParameterKey("properties", ItemHolderParameters.class, true, true);
        include = new ParameterKey("include", ValueType.STRING);
        parameters = new ParameterKey("parameters", ItemHolderParameters.class, true, true);
        attributes = new ParameterKey("attributes", ItemHolderParameters.class, true, true);
        profile = new ParameterKey("profile", ValueType.STRING);
        item = new ParameterKey("item", ItemParameters.class, true, true);
        hidden = new ParameterKey("hidden", ValueType.BOOLEAN);

        parameterKeys = new ParameterKey[] {
                caseNo,
                id,
                bean,
                method,
                arguments,
                properties,
                include,
                parameters,
                attributes,
                profile,
                item,
                hidden
        };
    }

    public ActionParameters() {
        super(parameterKeys);
    }

}
