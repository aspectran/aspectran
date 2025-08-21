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
package com.aspectran.core.context.asel;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.asel.token.Token;
import ognl.OgnlContext;

import java.util.Set;

/**
 * Defines the contract for an evaluator that executes a pre-parsed AsEL expression.
 * <p>This interface is the core component for the second stage of AsEL processing,
 * where token expressions have been resolved and the entire expression is ready to be
 * evaluated as an OGNL expression.</p>
 *
 * <p>Created: 2024-11-27</p>
 */
public interface ExpressionEvaluator {

    /**
     * Returns the original, unparsed expression string.
     * @return the original expression string
     */
    String getExpressionString();

    /**
     * Returns the expression string after AsEL tokens have been substituted with
     * temporary OGNL variable references.
     * @return the substituted expression string
     */
    String getSubstitutedExpression();

    /**
     * Returns the compiled OGNL expression object.
     * @return the parsed OGNL expression tree
     */
    Object getParsedExpression();

    /**
     * Returns the array of tokens parsed from the original expression.
     * @return an array of {@link Token} objects
     */
    Token[] getTokens();

    /**
     * Returns a set of the names of the temporary variables used to hold resolved
     * token values during OGNL evaluation.
     * @return a set of token variable names
     */
    Set<String> getTokenVarNames();

    /**
     * Evaluates the expression against the provided activity.
     * @param activity the current activity, providing the context for evaluation
     * @param ognlContext the OGNL context, used to hold variables during evaluation
     * @return the result of the expression evaluation
     */
    Object evaluate(Activity activity, OgnlContext ognlContext);

    /**
     * Evaluates the expression using the given root object.
     * @param activity the current activity
     * @param ognlContext the OGNL context
     * @param root the root object for the OGNL evaluation
     * @return the result of the expression evaluation
     */
    Object evaluate(Activity activity, OgnlContext ognlContext, Object root);

    /**
     * Evaluates the expression and coerces the result to the specified type.
     * @param activity the current activity
     * @param ognlContext the OGNL context
     * @param root the root object for the OGNL evaluation
     * @param resultType the desired type for the result
     * @return the result of the expression evaluation, converted to the resultType
     */
    Object evaluate(Activity activity, OgnlContext ognlContext, Object root, Class<?> resultType);

}
