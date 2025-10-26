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
package com.aspectran.core.context.converter;

import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;

/**
 * Exception thrown when a type conversion fails.
 *
 * <p>Created: 2025. 10. 26.</p>
 */
public class TypeConversionException extends Exception {

    @Serial
    private static final long serialVersionUID = -3249912193393989296L;

    private final Object value;

    private final Class<?> targetType;

    /**
     * Constructs a new TypeConversionException.
     * @param value the value that failed to be converted
     * @param targetType the target type
     * @param cause the root cause
     */
    public TypeConversionException(Object value, @NonNull Class<?> targetType, Throwable cause) {
        super("Failed to convert value '" + value + "' to required type [" +
                targetType.getName() + "]; Cause: " +
                ExceptionUtils.getRootCauseSimpleMessage(cause), cause);
        this.value = value;
        this.targetType = targetType;
    }

    /**
     * Returns the value that failed to be converted.
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the target type.
     * @return the target type
     */
    @NonNull
    public Class<?> getTargetType() {
        return targetType;
    }

}