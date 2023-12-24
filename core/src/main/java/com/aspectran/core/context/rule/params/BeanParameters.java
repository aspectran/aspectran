/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

public class BeanParameters extends AbstractParameters {

    public static final ParameterKey description;
    public static final ParameterKey id;
    public static final ParameterKey className;
    public static final ParameterKey scan;
    public static final ParameterKey mask;
    public static final ParameterKey scope;
    public static final ParameterKey singleton;
    public static final ParameterKey factoryBean;
    public static final ParameterKey factoryMethod;
    public static final ParameterKey initMethod;
    public static final ParameterKey destroyMethod;
    public static final ParameterKey lazyInit;
    public static final ParameterKey lazyDestroy;
    public static final ParameterKey important;
    public static final ParameterKey arguments;
    public static final ParameterKey properties;
    public static final ParameterKey filter;

    private static final ParameterKey[] parameterKeys;

    static {
        description = new ParameterKey("description", DescriptionParameters.class, true, true);
        id = new ParameterKey("id", ValueType.STRING);
        className = new ParameterKey("class", ValueType.STRING);
        scan = new ParameterKey("scan", ValueType.STRING);
        mask = new ParameterKey("mask", ValueType.STRING);
        scope = new ParameterKey("scope", ValueType.STRING);
        singleton = new ParameterKey("singleton", ValueType.BOOLEAN);
        factoryBean = new ParameterKey("factoryBean", ValueType.STRING);
        factoryMethod = new ParameterKey("factoryMethod", ValueType.STRING);
        initMethod = new ParameterKey("initMethod", ValueType.STRING);
        destroyMethod = new ParameterKey("destroyMethod", ValueType.STRING);
        lazyInit = new ParameterKey("lazyInit", ValueType.BOOLEAN);
        lazyDestroy = new ParameterKey("lazyDestroy", ValueType.BOOLEAN);
        important = new ParameterKey("important", ValueType.BOOLEAN);
        arguments = new ParameterKey("arguments", ItemHolderParameters.class, true, true);
        properties = new ParameterKey("properties", ItemHolderParameters.class, true, true);
        filter = new ParameterKey("filter", FilterParameters.class);

        parameterKeys = new ParameterKey[] {
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
                lazyDestroy,
                important,
                arguments,
                properties,
                filter
        };
    }

    public BeanParameters() {
        super(parameterKeys);
    }

}
