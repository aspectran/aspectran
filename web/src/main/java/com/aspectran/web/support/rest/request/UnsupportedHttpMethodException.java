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
package com.aspectran.web.support.rest.request;

import com.aspectran.core.context.rule.type.MethodType;

import java.io.Serial;

/**
 * Exception thrown when an unsupported HTTP method is used.
 *
 * @since 2024-07-17
 */
public class UnsupportedHttpMethodException extends Exception {

    @Serial
    private static final long serialVersionUID = -6585791875428999995L;

    private final MethodType methodType;

    /**
     * Instantiates a new UnsupportedHttpMethodException.
     * @param methodType the unsupported method type
     */
    public UnsupportedHttpMethodException(MethodType methodType) {
        super("Unsupported HTTP method: " + methodType);
        this.methodType = methodType;
    }

    /**
     * Gets the unsupported HTTP method.
     * @return the unsupported method type
     */
    public MethodType getMethodType() {
        return methodType;
    }

}
