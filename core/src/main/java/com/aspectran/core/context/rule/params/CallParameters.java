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

public class CallParameters extends AbstractParameters {

    public static final ParameterKey bean;
    public static final ParameterKey template;
    public static final ParameterKey parameter;
    public static final ParameterKey attribute;
    public static final ParameterKey property;

    private static final ParameterKey[] parameterKeys;

    static {
        bean = new ParameterKey("bean", ValueType.STRING);
        template = new ParameterKey("template", ValueType.STRING);
        parameter = new ParameterKey("parameter", ValueType.STRING);
        attribute = new ParameterKey("attribute", ValueType.STRING);
        property = new ParameterKey("property", ValueType.STRING);

        parameterKeys = new ParameterKey[] {
                bean,
                template,
                parameter,
                attribute,
                property
        };
    }

    public CallParameters() {
        super(parameterKeys);
    }

}
