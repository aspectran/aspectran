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
import com.aspectran.core.context.rule.CustomTransformRule;

/**
 * The Class CustomTransformResponseException.
 *
 * Created: 2019. 06. 16
 */
public class CustomTransformResponseException extends ResponseException {

    /** @serial */
    private static final long serialVersionUID = -5289305670156473011L;

    /**
     * Instantiates a new CustomTransformResponseException.
     *
     * @param customTransformRule the custom transform rule
     * @param cause the real cause of the exception
     */
    public CustomTransformResponseException(CustomTransformRule customTransformRule, Throwable cause) {
        super("Failed to transform " + customTransformRule + "; nested exception is " + cause, cause);
    }

    /**
     * Instantiates a new CustomTransformResponseException.
     *
     * @param customTransformer the custom transformer
     * @param cause the real cause of the exception
     */
    public CustomTransformResponseException(CustomTransformer customTransformer, Throwable cause) {
        super("Failed to transform with " + customTransformer + "; nested exception is " + cause, cause);
    }

}
