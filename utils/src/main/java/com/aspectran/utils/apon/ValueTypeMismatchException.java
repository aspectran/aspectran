/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

/**
 * This exception will be thrown when the value type of the parameter does not match.
 *
 * <p>Created: 2019. 06. 15</p>
 */
public class ValueTypeMismatchException extends InvalidParameterValueException {

    private static final long serialVersionUID = 3022910656886563259L;

    public ValueTypeMismatchException(@NonNull Class<?> valueType,
                                      @NonNull Class<?> requiredType,
                                      @NonNull Throwable cause) {
        super("Failed to convert value of type [" + valueType.getName() + "] " +
            "to required type [" + requiredType.getName() + "]; Cause: " +
            ExceptionUtils.getRootCauseSimpleMessage(cause), cause);
    }

}
