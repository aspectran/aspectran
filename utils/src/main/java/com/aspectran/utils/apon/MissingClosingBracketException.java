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

import java.io.Serial;

/**
 * Thrown when a closing bracket is missing for a block/array/text value while parsing APON.
 */
public class MissingClosingBracketException extends MalformedAponException {

    @Serial
    private static final long serialVersionUID = -6294265620028959255L;

    /**
     * Create the exception indicating the kind of bracket that was expected and the current parameter context.
     * @param bracketShape textual description of the bracket type (e.g., "curly", "square", "round")
     * @param name the parameter name, if available
     * @param parameterValue the parameter value holder providing additional context; may be null
     */
    public MissingClosingBracketException(String bracketShape, String name, ParameterValue parameterValue) {
        super("The end of the string was reached with no closing " + bracketShape + " bracket found: " +
                (parameterValue == null ? name : parameterValue));
    }

}
