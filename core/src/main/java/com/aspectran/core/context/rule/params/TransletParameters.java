/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

public class TransletParameters extends AbstractParameters {

    public static final ParameterDefinition description;
    public static final ParameterDefinition name;
    public static final ParameterDefinition scan;
    public static final ParameterDefinition mask;
    public static final ParameterDefinition method;
    public static final ParameterDefinition request;
    public static final ParameterDefinition parameters;
    public static final ParameterDefinition attributes;
    public static final ParameterDefinition contents;
    public static final ParameterDefinition content;
    public static final ParameterDefinition action;
    public static final ParameterDefinition response;
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
        parameters = new ParameterDefinition("parameters", ItemHolderParameters.class);
        attributes = new ParameterDefinition("attributes", ItemHolderParameters.class);
        contents = new ParameterDefinition("contents", ContentsParameters.class);
        content = new ParameterDefinition("content", ContentParameters.class, true, true);
        action = new ParameterDefinition("action", ActionParameters.class, true, true);
        response = new ParameterDefinition("response", ResponseParameters.class, true, true);
        transform = new ParameterDefinition("transform", TransformParameters.class);
        dispatch = new ParameterDefinition("dispatch", DispatchParameters.class);
        redirect = new ParameterDefinition("redirect", RedirectParameters.class);
        forward = new ParameterDefinition("forward", ForwardParameters.class);
        exception = new ParameterDefinition("exception", ExceptionParameters.class);

        parameterDefinitions = new ParameterDefinition[] {
                description,
                name,
                scan,
                mask,
                method,
                request,
                parameters,
                attributes,
                contents,
                content,
                action,
                response,
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

}
