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

import java.io.IOException;

public class JoinpointParameters extends AbstractParameters {

    public static final ParameterKey target;
    public static final ParameterKey methods;
    public static final ParameterKey headers;
    public static final ParameterKey pointcut;
    public static final ParameterKey expression;

    private static final ParameterKey[] parameterKeys;

    static {
        target = new ParameterKey("target", ValueType.STRING);
        methods = new ParameterKey("methods", ValueType.STRING, true);
        headers = new ParameterKey("headers", ValueType.STRING, true);
        pointcut = new ParameterKey("pointcut", PointcutParameters.class);
        expression = new ParameterKey("expression", "joinpoint", ValueType.STRING);

        parameterKeys = new ParameterKey[] {
                target,
                methods,
                headers,
                pointcut,
                expression
        };
    }

    public JoinpointParameters() {
        super(parameterKeys);
    }

    public JoinpointParameters(String text) throws IOException {
        this();
        readFrom(text);
    }

    public void setJoinpointTargetType(String joinpointTargetType) {
        putValue(target, joinpointTargetType);
    }

    public void setMethods(String... methods) {
        putValue(JoinpointParameters.methods, methods);
    }

    public void setHeaders(String... headers) {
        putValue(JoinpointParameters.headers, headers);
    }

    public PointcutParameters newPointcutParameters() {
        return newParameters(pointcut);
    }

    public PointcutParameters touchPointcutParameters() {
        return touchParameters(pointcut);
    }

}
