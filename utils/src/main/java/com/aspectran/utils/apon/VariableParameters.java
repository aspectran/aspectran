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

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

/**
 * Flexible {@link Parameters} implementation with a dynamic structure.
 * <p>
 * Parameter names and value types are discovered at runtime as values are put
 * into the container. Useful for ad-hoc parameter sets without a predefined
 * schema.
 * </p>
 */
public class VariableParameters extends DefaultParameters implements Serializable {

    @Serial
    private static final long serialVersionUID = 4492298345259110525L;

    /**
     * Create an empty container with a dynamic structure.
     */
    public VariableParameters() {
        super(null);
    }

    /**
     * Create a dynamic container and read APON text into it.
     * @param apon APON text
     * @throws IOException if reading fails
     */
    public VariableParameters(String apon) throws IOException {
        this(null, apon);
    }

    /**
     * Create a container with optional predefined keys but still allowing dynamic additions.
     * @param parameterKeys predefined keys (may be null)
     */
    public VariableParameters(ParameterKey[] parameterKeys) {
        super(parameterKeys);
    }

    /**
     * Create a container with predefined keys and read APON text into it.
     * @param parameterKeys predefined keys (may be null)
     * @param apon APON text
     * @throws AponParseException if parsing fails
     */
    public VariableParameters(ParameterKey[] parameterKeys, String apon) throws AponParseException {
        super(parameterKeys);
        readFrom(apon);
    }

}
