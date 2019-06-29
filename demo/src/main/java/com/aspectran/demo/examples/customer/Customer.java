/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.demo.examples.customer;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ValueType;

/**
 * Defines an APON object that contains the customer's information.
 */
public class Customer extends AbstractParameters {

    public static final ParameterDefinition id;
    public static final ParameterDefinition name;
    public static final ParameterDefinition age;
    public static final ParameterDefinition approved;
    
    private static final ParameterDefinition[] parameterDefinitions;
    
    static {
        id = new ParameterDefinition("id", ValueType.INT);
        name = new ParameterDefinition("name", ValueType.STRING);
        age = new ParameterDefinition("age", ValueType.INT);
        approved = new ParameterDefinition("approved", ValueType.BOOLEAN);
        
        parameterDefinitions = new ParameterDefinition[] {
            id,
            name,
            age,
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
