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
package com.aspectran.core.context.rule.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterKey;
import com.aspectran.core.util.apon.ValueType;

public class RedirectParameters extends AbstractParameters {

    public static final ParameterKey contentType;
    public static final ParameterKey path;
    public static final ParameterKey encoding;
    public static final ParameterKey excludeNullParameters;
    public static final ParameterKey excludeEmptyParameters;
    public static final ParameterKey defaultResponse;
    public static final ParameterKey parameters;

    private static final ParameterKey[] parameterKeys;

    static {
        contentType = new ParameterKey("contentType", ValueType.STRING);
        path = new ParameterKey("path", ValueType.STRING);
        encoding = new ParameterKey("encoding", ValueType.STRING);
        excludeNullParameters = new ParameterKey("excludeNullParameters", ValueType.BOOLEAN);
        excludeEmptyParameters = new ParameterKey("excludeEmptyParameters", ValueType.BOOLEAN);
        defaultResponse = new ParameterKey("default", ValueType.BOOLEAN);
        parameters = new ParameterKey("parameters", ItemHolderParameters.class, true, true);

        parameterKeys = new ParameterKey[] {
                contentType,
                path,
                encoding,
                excludeNullParameters,
                excludeEmptyParameters,
                defaultResponse,
                parameters
        };
    }

    public RedirectParameters() {
        super(parameterKeys);
    }

}
