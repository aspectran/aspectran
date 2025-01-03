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
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

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
        expression = new ParameterKey("expression", new String[] {"joinpoint"}, ValueType.TEXT);

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

    public JoinpointParameters(String apon) throws AponParseException {
        this();
        readFrom(apon);
    }

    public void setJoinpointTargetType(String joinpointTargetType) {
        putValue(target, joinpointTargetType);
    }

    public void setMethods(String[] methods) {
        removeValue(JoinpointParameters.methods);
        putValue(JoinpointParameters.methods, methods);
    }

    public void addMethod(String method) {
        putValue(JoinpointParameters.methods, method);
    }

    public void setHeaders(String[] headers) {
        removeValue(JoinpointParameters.headers);
        putValue(JoinpointParameters.headers, headers);
    }

    public void addHeaders(String header) {
        putValue(JoinpointParameters.headers, header);
    }

    public PointcutParameters newPointcutParameters() {
        return newParameters(pointcut);
    }

    public PointcutParameters touchPointcutParameters() {
        return touchParameters(pointcut);
    }

    public void setPointcutParameters(PointcutParameters pointcutParameters) {
        putValue(JoinpointParameters.pointcut, pointcutParameters);
    }

}
