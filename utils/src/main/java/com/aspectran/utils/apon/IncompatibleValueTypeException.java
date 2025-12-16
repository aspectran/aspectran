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

import org.jspecify.annotations.NonNull;

import java.io.Serial;

/**
 * Thrown when the actual value type of a parameter does not match the expected type.
 * <p>This exception is typically raised while parsing or validating APON values when a
 * {@link ParameterValue} is provided in a form that is incompatible with the declared
 * {@link ValueType}.</p>
 */
public class IncompatibleValueTypeException extends InvalidParameterValueException {

    @Serial
    private static final long serialVersionUID = 1557599183505068164L;

    /**
     * Creates a new exception indicating that the value held by the given parameter
     * is incompatible with the expected value type.
     * @param parameterValue the parameter value that caused the mismatch
     * @param expectedValueType the expected value type
     */
    public IncompatibleValueTypeException(ParameterValue parameterValue, ValueType expectedValueType) {
        super(buildMessage(parameterValue, expectedValueType));
    }

    @NonNull
    private static String buildMessage(ParameterValue parameterValue, ValueType expectedValueType) {
        String paramName = (parameterValue != null ? parameterValue.getQualifiedName() : null);
        ValueType actualType = (parameterValue != null ? parameterValue.getValueType() : null);
        StringBuilder msg = new StringBuilder();
        msg.append("Incompatible value type");
        if (paramName != null) {
            msg.append(" for parameter '").append(paramName).append("'");
        }
        if (actualType != null) {
            msg.append("; actual=").append(actualType);
        }
        if (expectedValueType != null) {
            msg.append(", expected=").append(expectedValueType);
        }
        return msg.toString();
    }

}
