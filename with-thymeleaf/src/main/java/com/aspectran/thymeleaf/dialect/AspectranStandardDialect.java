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
package com.aspectran.thymeleaf.dialect;

import com.aspectran.thymeleaf.expression.ASELVariableExpressionEvaluator;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;

public class AspectranStandardDialect extends StandardDialect {

    public static final String NAME = "AspectranStandard";

    public static final String PREFIX = "th";

    public static final int PROCESSOR_PRECEDENCE = 1000;

    public static final ASELVariableExpressionEvaluator EVALUATOR = new ASELVariableExpressionEvaluator(true);

    public AspectranStandardDialect() {
        super(NAME, PREFIX, PROCESSOR_PRECEDENCE);
    }

    @Override
    public IStandardVariableExpressionEvaluator getVariableExpressionEvaluator() {
        return EVALUATOR;
    }

    @Override
    public void setVariableExpressionEvaluator(IStandardVariableExpressionEvaluator variableExpressionEvaluator) {
        throw new UnsupportedOperationException(
                "Variable Expression Evaluator cannot be modified in AspectranStandardDialect");
    }

}
