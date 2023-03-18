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
package com.aspectran.core.activity.response.transform;

import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.context.rule.TransformRule;

/**
 * Thrown if an error occurs while transforming the data needed for a response.
 * 
 * <p>Created: 2008. 01. 07 AM 3:35:55</p>
 */
public class TransformResponseException extends ResponseException {

    /** @serial */
    private static final long serialVersionUID = -2902489274291058715L;

    private final TransformRule transformRule;

    /**
     * Instantiates a new TransformResponseException.
     *
     * @param transformRule the transform rule
     * @param cause the real cause of the exception
     */
    public TransformResponseException(TransformRule transformRule, Throwable cause) {
        super("Failed to transform " + transformRule + "; nested exception is " + cause, cause);
        this.transformRule = transformRule;
    }

    /**
     * Gets the transform rule.
     *
     * @return the transform rule
     */
    public TransformRule getTransformRule() {
        return transformRule;
    }

}
