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

import com.aspectran.utils.apon.DefaultParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

/**
 * Represents the parameters for a translet rule.
 */
public class TransletParameters extends DefaultParameters {

    public static final ParameterKey description;
    public static final ParameterKey name;
    public static final ParameterKey scan;
    public static final ParameterKey mask;
    public static final ParameterKey method;
    public static final ParameterKey async;
    public static final ParameterKey timeout;
    public static final ParameterKey request;
    public static final ParameterKey parameters;
    public static final ParameterKey attributes;
    public static final ParameterKey contents;
    public static final ParameterKey content;
    public static final ParameterKey action;
    public static final ParameterKey response;
    public static final ParameterKey transform;
    public static final ParameterKey dispatch;
    public static final ParameterKey forward;
    public static final ParameterKey redirect;
    public static final ParameterKey exception;

    private static final ParameterKey[] parameterKeys;

    static {
        description = new ParameterKey("description", DescriptionParameters.class, true, true);
        name = new ParameterKey("name", ValueType.STRING);
        scan = new ParameterKey("scan", ValueType.STRING);
        mask = new ParameterKey("mask", ValueType.STRING);
        method = new ParameterKey("method", ValueType.STRING);
        async = new ParameterKey("async", ValueType.BOOLEAN);
        timeout = new ParameterKey("timeout", ValueType.LONG);
        request = new ParameterKey("request", RequestParameters.class);
        parameters = new ParameterKey("parameters", ItemHolderParameters.class, true, true);
        attributes = new ParameterKey("attributes", ItemHolderParameters.class, true, true);
        contents = new ParameterKey("contents", ContentsParameters.class);
        content = new ParameterKey("content", ContentParameters.class, true, true);
        action = new ParameterKey("action", new String[] {"echo", "headers", "include", "choose"},
                ActionParameters.class, true, true);
        response = new ParameterKey("response", ResponseParameters.class, true, true);
        transform = new ParameterKey("transform", TransformParameters.class);
        dispatch = new ParameterKey("dispatch", DispatchParameters.class);
        forward = new ParameterKey("forward", ForwardParameters.class);
        redirect = new ParameterKey("redirect", RedirectParameters.class);
        exception = new ParameterKey("exception", ExceptionParameters.class);

        parameterKeys = new ParameterKey[] {
                description,
                name,
                scan,
                mask,
                method,
                async,
                timeout,
                request,
                parameters,
                attributes,
                contents,
                content,
                action,
                response,
                transform,
                dispatch,
                forward,
                redirect,
                exception
        };
    }

    public TransletParameters() {
        super(parameterKeys);
    }

}
