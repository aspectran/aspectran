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
package com.aspectran.utils.apon;

import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;

/**
 * Thrown when a value cannot be converted to the required target type due to
 * an APON value-type mismatch (e.g., attempting to coerce a string to an int).
 * <p>
 * This is a specialization of {@link InvalidParameterValueException} used by
 * converters and readers to signal type conversion errors while preserving the
 * original cause and describing both the actual and required Java types.
 * </p>
 * <p>Created: 2019. 06. 15</p>
 */
public class ValueTypeMismatchException extends InvalidParameterValueException {

    @Serial
    private static final long serialVersionUID = 3022910656886563259L;

    /**
     * Construct an exception describing a failed conversion due to incompatible value types.
     * @param valueType the actual source type encountered
     * @param requiredType the target type that was expected
     */
    public ValueTypeMismatchException(@NonNull Class<?> valueType,
                                      @NonNull Class<?> requiredType) {
        super("Failed to convert value of type [" + valueType.getName() + "] " +
                "to required type [" + requiredType.getName() + "]");
    }

    /**
     * Construct an exception describing a failed conversion due to incompatible value types.
     * @param valueType the actual source type encountered
     * @param requiredType the target type that was expected
     * @param cause the underlying cause of the failure
     */
    public ValueTypeMismatchException(@NonNull Class<?> valueType,
                                      @NonNull Class<?> requiredType,
                                      @NonNull Throwable cause) {
        super("Failed to convert value of type [" + valueType.getName() + "] " +
                "to required type [" + requiredType.getName() + "]; Cause: " +
                ExceptionUtils.getRootCauseSimpleMessage(cause), cause);
    }

}
