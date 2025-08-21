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
package com.aspectran.core.context.asel.token;

import com.aspectran.core.activity.Activity;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines the contract for evaluating parsed AsEL tokens.
 * <p>This interface provides methods to resolve {@link Token} objects into their actual
 * values against the current activity context. It handles the logic for retrieving
 * values from various sources like beans, parameters, attributes, and properties.</p>
 *
 * <p>Created: 2010. 5. 6. AM 1:35:16</p>
 */
public interface TokenEvaluator {

    /**
     * Returns the current activity context used for evaluation.
     * @return the current activity
     */
    Activity getActivity();

    /**
     * Evaluates a single token.
     * @param token the token to evaluate
     * @return the resolved value of the token
     */
    Object evaluate(Token token);

    /**
     * Evaluates an array of tokens. If the array contains a single token,
     * its evaluated value is returned directly. If it contains multiple tokens,
     * their string representations are concatenated.
     * @param tokens the array of tokens to evaluate
     * @return the evaluated result, either as a single object or a concatenated string
     */
    Object evaluate(Token[] tokens);

    /**
     * Evaluates an array of tokens and writes the result to the given writer.
     * @param tokens the tokens to evaluate
     * @param writer the writer to output the result to
     * @throws IOException if an I/O error occurs
     */
    void evaluate(Token[] tokens, Writer writer) throws IOException;

    /**
     * Evaluates an array of tokens and returns the result as a single string.
     * @param tokens the tokens to evaluate
     * @return the concatenated string result of the evaluation
     */
    String evaluateAsString(Token[] tokens);

    /**
     * Evaluates a list of token arrays, returning a list of the evaluated results.
     * @param tokensList a list where each element is an array of tokens
     * @return a list containing the evaluated value for each token array
     */
    List<Object> evaluateAsList(List<Token[]> tokensList);

    /**
     * Evaluates a set of token arrays, returning a set of the evaluated results.
     * @param tokensSet a set where each element is an array of tokens
     * @return a set containing the evaluated value for each token array
     */
    Set<Object> evaluateAsSet(Set<Token[]> tokensSet);

    /**
     * Evaluates a map of token arrays, returning a map with the same keys and
     * the evaluated results as values.
     * @param tokensMap a map where each value is an array of tokens
     * @return a map containing the evaluated value for each token array
     */
    Map<String, Object> evaluateAsMap(Map<String, Token[]> tokensMap);

    /**
     * A static helper method that parses and evaluates an expression string.
     * If the expression contains no tokens, it is returned as is.
     * @param expression the expression string to evaluate
     * @param activity the current activity context
     * @return the evaluated result
     */
    static Object evaluate(String expression, Activity activity) {
        if (Token.hasToken(expression)) {
            Token[] tokens = TokenParser.parse(expression);
            return activity.getTokenEvaluator().evaluate(tokens);
        } else {
            return expression;
        }
    }

}
