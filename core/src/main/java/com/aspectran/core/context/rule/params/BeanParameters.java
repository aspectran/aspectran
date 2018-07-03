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
package com.aspectran.core.context.rule.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

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
        description = new ParameterDefinition("description", ParameterValueType.TEXT);
        id = new ParameterDefinition("id", ParameterValueType.STRING);
        className = new ParameterDefinition("class", ParameterValueType.STRING);
        scan = new ParameterDefinition("scan", ParameterValueType.STRING);
        mask = new ParameterDefinition("mask", ParameterValueType.STRING);
        scope = new ParameterDefinition("scope", ParameterValueType.STRING);
        singleton = new ParameterDefinition("singleton", ParameterValueType.BOOLEAN);
        factoryBean = new ParameterDefinition("factoryBean", ParameterValueType.STRING);
        factoryMethod = new ParameterDefinition("factoryMethod", ParameterValueType.STRING);
        initMethod = new ParameterDefinition("initMethod", ParameterValueType.STRING);
        destroyMethod = new ParameterDefinition("destroyMethod", ParameterValueType.STRING);
        lazyInit = new ParameterDefinition("lazyInit", ParameterValueType.BOOLEAN);
        important = new ParameterDefinition("important", ParameterValueType.BOOLEAN);
        constructor = new ParameterDefinition("constructor", ConstructorParameters.class);
        properties = new ParameterDefinition("properties", ItemHolderParameters.class);
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
