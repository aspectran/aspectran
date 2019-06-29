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
package com.aspectran.core.context.rule.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ValueType;

public class BeanParameters extends AbstractParameters {

    public static final ParameterDefinition description;
    public static final ParameterDefinition id;
    public static final ParameterDefinition className;
    public static final ParameterDefinition scan;
    public static final ParameterDefinition mask;
    public static final ParameterDefinition scope;
    public static final ParameterDefinition singleton;
    public static final ParameterDefinition factoryBean;
    public static final ParameterDefinition factoryMethod;
    public static final ParameterDefinition initMethod;
    public static final ParameterDefinition destroyMethod;
    public static final ParameterDefinition lazyInit;
    public static final ParameterDefinition important;
    public static final ParameterDefinition constructor;
    public static final ParameterDefinition properties;
    public static final ParameterDefinition filter;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        description = new ParameterDefinition("description", ValueType.TEXT);
        id = new ParameterDefinition("id", ValueType.STRING);
        className = new ParameterDefinition("class", ValueType.STRING);
        scan = new ParameterDefinition("scan", ValueType.STRING);
        mask = new ParameterDefinition("mask", ValueType.STRING);
        scope = new ParameterDefinition("scope", ValueType.STRING);
        singleton = new ParameterDefinition("singleton", ValueType.BOOLEAN);
        factoryBean = new ParameterDefinition("factoryBean", ValueType.STRING);
        factoryMethod = new ParameterDefinition("factoryMethod", ValueType.STRING);
        initMethod = new ParameterDefinition("initMethod", ValueType.STRING);
        destroyMethod = new ParameterDefinition("destroyMethod", ValueType.STRING);
        lazyInit = new ParameterDefinition("lazyInit", ValueType.BOOLEAN);
        important = new ParameterDefinition("important", ValueType.BOOLEAN);
        constructor = new ParameterDefinition("constructor", ConstructorParameters.class);
        properties = new ParameterDefinition("properties", ItemHolderParameters.class, true, true);
        filter = new ParameterDefinition("filter", FilterParameters.class);

        parameterDefinitions = new ParameterDefinition[] {
                description,
                id,
                className,
                scan,
                mask,
                scope,
                singleton,
                factoryBean,
                factoryMethod,
                initMethod,
                destroyMethod,
                lazyInit,
                important,
                constructor,
                properties,
                filter
        };
    }

    public BeanParameters() {
        super(parameterDefinitions);
    }

}
