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
package com.aspectran.core.context.asel.value;

import com.aspectran.core.activity.Activity;

/**
 * It supports expressions in the CHOOSE-WHEN statement,
 * and evaluates the expression as a boolean result.
 *
 * <p>Created: 2019-01-06</p>
 *
 * @since 6.0.0
 */
public class BooleanExpression extends ValueExpression {

    public BooleanExpression(String expression) throws ExpressionParserException {
        super(expression);
    }

    public boolean evaluate(Activity activity) {
        Boolean result = evaluate(activity, Boolean.class);
        if (result == null) {
            return false;
        }
        return result;
    }

}
