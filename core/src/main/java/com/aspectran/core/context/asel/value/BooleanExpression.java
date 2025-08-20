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
package com.aspectran.core.context.asel.value;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.asel.ExpressionParserException;

/**
 * Represents an expression that is expected to evaluate to a boolean value.
 * <p>This class is a specialized wrapper around a {@link ValueEvaluator} and is
 * typically used for conditional logic within Aspectran rules, such as in
 * <code>&lt;if&gt;</code> or <code>&lt;choose&gt;</code> elements.
 * The underlying expression can contain both AsEL tokens and OGNL syntax. It
 * follows the standard two-stage evaluation: AsEL tokens are resolved first,
 * and the resulting string is then evaluated as an OGNL expression. The final
 * result is coerced into a boolean.</p>
 *
 * <p>Created: 2019-01-06</p>
 *
 * @since 6.0.0
 */
public class BooleanExpression extends ValueExpression {

    /**
     * Constructs a new {@code BooleanExpression}, parsing the given expression string.
     * @param expression the expression string to parse
     * @throws ExpressionParserException if the expression has a syntax error
     */
    public BooleanExpression(String expression) throws ExpressionParserException {
        super(expression);
    }

    /**
     * Evaluates the expression and returns the boolean result.
     * <p>If the expression evaluates to {@code null}, this method returns {@code false}.</p>
     * @param activity the current activity context for evaluation
     * @return the boolean result of the expression evaluation
     */
    public boolean evaluate(Activity activity) {
        Boolean result = evaluate(activity, Boolean.class);
        if (result == null) {
            return false;
        }
        return result;
    }

}
