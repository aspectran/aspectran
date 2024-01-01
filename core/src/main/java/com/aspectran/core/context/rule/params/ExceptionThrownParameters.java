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

public class ExceptionThrownParameters extends AbstractParameters {

    public static final ParameterKey type;
    public static final ParameterKey action;
    public static final ParameterKey transform;
    public static final ParameterKey dispatch;
    public static final ParameterKey redirect;

    private static final ParameterKey[] parameterKeys;

    static {
        type = new ParameterKey("type", ValueType.STRING, true);
        action = new ParameterKey("action", new String[] {"echo", "headers"}, ActionParameters.class);
        transform = new ParameterKey("transform", TransformParameters.class, true, true);
        dispatch = new ParameterKey("dispatch", DispatchParameters.class, true, true);
        redirect = new ParameterKey("redirect", RedirectParameters.class, true, true);

        parameterKeys = new ParameterKey[] {
                type,
                action,
                transform,
                dispatch,
                redirect
        };
    }

    public ExceptionThrownParameters() {
        super(parameterKeys);
    }

}
