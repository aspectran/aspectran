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

public class ActionParameters extends AbstractParameters {

    public static final ParameterKey id;

    public static final ParameterKey bean;
    public static final ParameterKey method;
    public static final ParameterKey arguments;
    public static final ParameterKey properties;

    public static final ParameterKey translet;
    public static final ParameterKey parameters;
    public static final ParameterKey attributes;

    public static final ParameterKey item;

    public static final ParameterKey hidden;

    public static final ParameterKey when;
    public static final ParameterKey otherwise;

    private static final ParameterKey[] parameterKeys;

    static {
        id = new ParameterKey("id", ValueType.STRING);
        bean = new ParameterKey("bean", ValueType.STRING);
        method = new ParameterKey("method", ValueType.STRING);
        arguments = new ParameterKey("arguments", ItemHolderParameters.class, true, true);
        properties = new ParameterKey("properties", ItemHolderParameters.class, true, true);
        translet = new ParameterKey("translet", ValueType.STRING);
        parameters = new ParameterKey("parameters", ItemHolderParameters.class, true, true);
        attributes = new ParameterKey("attributes", ItemHolderParameters.class, true, true);
        item = new ParameterKey("item", ItemParameters.class, true, true);
        hidden = new ParameterKey("hidden", ValueType.BOOLEAN);
        when = new ParameterKey("when", ChooseWhenParameters.class, true, true);
        otherwise = new ParameterKey("otherwise", ChooseWhenParameters.class);

        parameterKeys = new ParameterKey[] {
                id,
                bean,
                method,
                arguments,
                properties,
                translet,
                parameters,
                attributes,
                item,
                hidden,
                when,
                otherwise
        };
    }

    public ActionParameters() {
        super(parameterKeys);
    }

}
