/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

public class AdviceParameters extends AbstractParameters {

    public static final ParameterKey bean;
    public static final ParameterKey beforeAdvice;
    public static final ParameterKey afterAdvice;
    public static final ParameterKey aroundAdvice;
    public static final ParameterKey finallyAdvice;

    private static final ParameterKey[] parameterKeys;

    static {
        bean = new ParameterKey("bean", ValueType.STRING);
        beforeAdvice = new ParameterKey("before", AdviceActionParameters.class);
        afterAdvice = new ParameterKey("after", AdviceActionParameters.class);
        aroundAdvice = new ParameterKey("around", AdviceActionParameters.class);
        finallyAdvice = new ParameterKey("finally", AdviceActionParameters.class);

        parameterKeys = new ParameterKey[] {
                bean,
                beforeAdvice,
                afterAdvice,
                aroundAdvice,
                finallyAdvice
        };
    }

    public AdviceParameters() {
        super(parameterKeys);
    }

}
