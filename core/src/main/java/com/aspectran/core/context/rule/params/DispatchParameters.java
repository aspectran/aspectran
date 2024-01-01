/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

public class DispatchParameters extends AbstractParameters {

    public static final ParameterKey name;
    public static final ParameterKey dispatcher;
    public static final ParameterKey contentType;
    public static final ParameterKey encoding;
    public static final ParameterKey defaultResponse;

    private static final ParameterKey[] parameterKeys;

    static {
        name = new ParameterKey("name", ValueType.STRING);
        dispatcher = new ParameterKey("dispatcher", ValueType.STRING);
        contentType = new ParameterKey("contentType", ValueType.STRING);
        encoding = new ParameterKey("encoding", ValueType.STRING);
        defaultResponse = new ParameterKey("default", ValueType.BOOLEAN);

        parameterKeys = new ParameterKey[] {
                name,
                dispatcher,
                contentType,
                encoding,
                defaultResponse
        };
    }

    public DispatchParameters() {
        super(parameterKeys);
    }

}
