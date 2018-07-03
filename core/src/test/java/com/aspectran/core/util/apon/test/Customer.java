/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.util.apon.test;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class Customer extends AbstractParameters implements Parameters {

    public static final ParameterDefinition id;
    public static final ParameterDefinition name;
    public static final ParameterDefinition age;
    public static final ParameterDefinition episode;
    public static final ParameterDefinition approved;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        id = new ParameterDefinition("id", ParameterValueType.STRING);
        name = new ParameterDefinition("name", ParameterValueType.STRING);
        age = new ParameterDefinition("age", ParameterValueType.INT);
        episode = new ParameterDefinition("episode", ParameterValueType.TEXT);
        approved = new ParameterDefinition("approved", ParameterValueType.BOOLEAN);

        parameterDefinitions = new ParameterDefinition[] {
                id,
                name,
                age,
                episode,
                approved
        };
    }

    /**
     * Instantiates a new customer.
     */
    public Customer() {
        super(parameterDefinitions);
    }

}
