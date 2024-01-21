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

import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;

/**
 * The Class UnknownParameterException.
 */
public class UnknownParameterException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6446576507072773588L;

    /**
     * Constructor to create exception with a message.
     * @param parameterName the parameter name
     * @param parameters the Parameters object
     */
    public UnknownParameterException(String parameterName, Parameters parameters) {
        super("No such parameter '" + parameterName + "' in " + describe(parameters));
    }

    private static String describe(@NonNull Parameters parameters) {
        return parameters.describe();
    }

}
