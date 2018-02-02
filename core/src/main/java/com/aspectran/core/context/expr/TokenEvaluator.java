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

import com.aspectran.core.context.expr.token.Token;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Evaluates Token expression.
 * 
 * <p>Created: 2010. 5. 6. AM 1:35:16</p>
 */
public interface TokenEvaluator {

    Object evaluate(Token token);

    Object evaluate(Token[] tokens);

    void evaluate(Token[] tokens, Writer writer) throws IOException;

    String evaluateAsString(Token[] tokens);

    Object evaluate(String parameterName, Token[] tokens);

    String evaluateAsString(String parameterName, Token[] tokens);

    List<Object> evaluateAsList(String parameterName, List<Token[]> tokensList);

    Set<Object> evaluateAsSet(String parameterName, Set<Token[]> tokensSet);

    Map<String, Object> evaluateAsMap(String parameterName, Map<String, Token[]> tokensMap);

    Properties evaluateAsProperties(String parameterName, Properties tokensProp);

}
