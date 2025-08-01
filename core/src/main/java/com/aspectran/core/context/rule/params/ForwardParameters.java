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

public class ForwardParameters extends AbstractParameters {

    public static final ParameterKey contentType;
    public static final ParameterKey translet;
    public static final ParameterKey method;
    public static final ParameterKey defaultResponse;
    public static final ParameterKey attributes;

    private static final ParameterKey[] parameterKeys;

    static {
        contentType = new ParameterKey("contentType", ValueType.STRING);
        translet = new ParameterKey("translet", ValueType.STRING);
        method = new ParameterKey("method", ValueType.STRING);
        defaultResponse = new ParameterKey("default", ValueType.BOOLEAN);
        attributes = new ParameterKey("attributes", ItemHolderParameters.class, true, true);

        parameterKeys = new ParameterKey[] {
                contentType,
                translet,
                method,
                defaultResponse,
                attributes
        };
    }

    public ForwardParameters() {
        super(parameterKeys);
    }

}
