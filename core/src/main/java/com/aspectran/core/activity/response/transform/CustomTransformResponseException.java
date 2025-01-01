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
package com.aspectran.core.activity.response.transform;

import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.context.rule.CustomTransformRule;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;

/**
 * The Class CustomTransformResponseException.
 *
 * <p>Created: 2019. 06. 16</p>
 */
public class CustomTransformResponseException extends ResponseException {

    @Serial
    private static final long serialVersionUID = -5289305670156473011L;

    /**
     * Instantiates a new CustomTransformResponseException.
     * @param customTransformRule the custom transform rule
     * @param cause the real cause of the exception
     */
    public CustomTransformResponseException(@NonNull CustomTransformRule customTransformRule,
                                            @NonNull Throwable cause) {
        super("Failed to transform " + customTransformRule + "; Cause: " +
            ExceptionUtils.getRootCauseSimpleMessage(cause), cause);
    }

    /**
     * Instantiates a new CustomTransformResponseException.
     * @param customTransformer the custom transformer
     * @param cause the real cause of the exception
     */
    public CustomTransformResponseException(@NonNull CustomTransformer customTransformer,
                                            @NonNull Throwable cause) {
        super("Failed to transform with " + customTransformer + "; Cause: " +
            ExceptionUtils.getRootCauseSimpleMessage(cause), cause);
    }

}
