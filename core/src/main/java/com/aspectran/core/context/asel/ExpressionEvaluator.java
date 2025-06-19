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
 * <p>Created: 2024-11-27</p>
 */
public interface ExpressionEvaluator {

    String getExpressionString();

    String getSubstitutedExpression();

    Object getParsedExpression();

    Token[] getTokens();

    Set<String> getTokenVarNames();

    Object evaluate(Activity activity, OgnlContext ognlContext);

    Object evaluate(Activity activity, OgnlContext ognlContext, Object root);

    Object evaluate(Activity activity, OgnlContext ognlContext, Object root, Class<?> resultType);

}
