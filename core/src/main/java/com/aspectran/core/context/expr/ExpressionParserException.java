/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
package com.aspectran.core.context.expr;

import com.aspectran.core.context.rule.IllegalRuleException;

/**
 * Exception for all errors occurring during expression parsing.
 */
public class ExpressionParserException extends IllegalRuleException {

    private static final long serialVersionUID = -2232633791981628212L;

    private final String expression;

    /**
     * Instantiates a new expression parser exception.
     *
     * @param expression the expression to be evaluated
     * @param cause the root cause
     */
    public ExpressionParserException(String expression, Throwable cause) {
        super("Error parsing expression '" + expression + "'. Cause: " + cause, cause);
        this.expression = expression;
    }

    /**
     * Gets the expression that failed evaluation.
     *
     * @return the expression that failed evaluation
     */
    public String getExpression() {
        return this.expression;
    }

}
