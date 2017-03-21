/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

public class TransletParameters extends AbstractParameters {

    public static final ParameterDefinition description;
    public static final ParameterDefinition name;
    public static final ParameterDefinition scan;
    public static final ParameterDefinition mask;
    public static final ParameterDefinition method;
    public static final ParameterDefinition request;
    public static final ParameterDefinition contents1;
    public static final ParameterDefinition contents2;
    public static final ParameterDefinition actions;
    public static final ParameterDefinition responses;
    public static final ParameterDefinition transform;
    public static final ParameterDefinition dispatch;
    public static final ParameterDefinition redirect;
    public static final ParameterDefinition forward;
    public static final ParameterDefinition exception;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        description = new ParameterDefinition("description", ParameterValueType.TEXT);
        name = new ParameterDefinition("name", ParameterValueType.STRING);
        scan = new ParameterDefinition("scan", ParameterValueType.STRING);
        mask = new ParameterDefinition("mask", ParameterValueType.STRING);
        method = new ParameterDefinition("method", ParameterValueType.STRING);
        request = new ParameterDefinition("request", RequestParameters.class);
        contents1 = new ParameterDefinition("contents", ContentsParameters.class);
        contents2 = new ParameterDefinition("content", ContentParameters.class, true, true);
        actions = new ParameterDefinition("action", ActionParameters.class, true, true);
        responses = new ParameterDefinition("response", ResponseParameters.class, true, true);
        transform = new ParameterDefinition("transform", TransformParameters.class);
        dispatch = new ParameterDefinition("dispatch", DispatchParameters.class);
        redirect = new ParameterDefinition("redirect", RedirectParameters.class);
        forward = new ParameterDefinition("forward", ForwardParameters.class);
        exception = new ParameterDefinition("exception", ExceptionParameters.class, true, true);

        parameterDefinitions = new ParameterDefinition[] {
            description,
            name,
            scan,
            mask,
            method,
            request,
            contents1,
            contents2,
            actions,
            responses,
            transform,
            dispatch,
            redirect,
            forward,
            exception
        };
    }

    public TransletParameters() {
        super(parameterDefinitions);
    }

    public TransletParameters(String text) {
        super(parameterDefinitions, text);
    }

}
