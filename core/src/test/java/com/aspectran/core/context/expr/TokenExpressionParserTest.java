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
package com.aspectran.core.context.expr;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TokenExpressionParserTest {

    private ActivityContext context;

    @Before
    public void ready() throws ActivityContextBuilderException {
        ActivityContextBuilder builder = new HybridActivityContextBuilder();
        context = builder.build();
    }

    @Test
    public void testEvaluateAsString() {
        Activity activity = new InstantActivity(context);
        activity.getRequestAdapter().setParameter("param1", "Apple");
        activity.getRequestAdapter().setAttribute("attr1", "Strawberry");

        Token[] tokens = TokenParser.parse("${param1}, ${param2:Tomato}, @{attr1}, @{attr2:Melon}");

        TokenEvaluator tokenEvaluator = new TokenExpressionParser(activity);
        String result = tokenEvaluator.evaluateAsString(tokens);

        assertEquals("Apple, Tomato, Strawberry, Melon", result);
    }

}