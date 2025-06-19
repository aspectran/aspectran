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
package com.aspectran.utils.apon.test;

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

public class Customer extends AbstractParameters {

    public static final ParameterKey id;
    public static final ParameterKey name;
    public static final ParameterKey age;
    public static final ParameterKey episode;
    public static final ParameterKey approved;

    private static final ParameterKey[] parameterKeys;

    static {
        id = new ParameterKey("id", ValueType.STRING);
        name = new ParameterKey("name", ValueType.STRING);
        age = new ParameterKey("age", ValueType.INT);
        approved = new ParameterKey("approved", ValueType.BOOLEAN);
        episode = new ParameterKey("episode", ValueType.TEXT);

        parameterKeys = new ParameterKey[] {
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
        super(parameterKeys);
    }

}
