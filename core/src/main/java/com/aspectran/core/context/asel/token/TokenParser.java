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

import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.utils.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A high-level utility for parsing strings containing Aspectran Expression Language (AsEL) tokens.
 * <p>This class serves as the main entry point for parsing AsEL expressions. It uses the
 * low-level {@link Tokenizer} to break an expression string into a series of {@link Token}
 * objects and provides convenient methods for common use cases, such as:
 * <ul>
 *   <li>Simple tokenization of an expression string.</li>
 *   <li>Parsing value-list or key-value pair expressions into collections.</li>
 *   <li>Optimizing tokens by trimming extraneous whitespace.</li>
 * </ul>
 *
 * @see Token
 * @see Tokenizer
 */
public class TokenParser {

    /**
     * Parses the given expression string into an array of tokens.
     * @param expression the expression string to parse
     * @return an array of {@link Token} objects, or {@code null} if the expression is null
     */
    public static Token[] parse(String expression) {
        return parse(expression, false);
    }

    /**
     * Parses the given expression string into an array of tokens, with an option to optimize.
     * <p>Optimization involves trimming leading and trailing whitespace from the first and
     * last text tokens in the sequence, which can be useful for cleaning up user input.</p>
     * @param expression the expression string to parse
     * @param optimize {@code true} to trim whitespace from the first and last text tokens;
     *      {@code false} otherwise
     * @return an array of {@link Token} objects, or {@code null} if the expression is null
     */
    public static Token[] parse(String expression, boolean optimize) {
        if (expression == null) {
            return null;
        }
        if (expression.isEmpty()) {
            Token t = new Token(expression);
            return new Token[] { t };
        }
        Token[] tokens = null;
        List<Token> tokenList = Tokenizer.tokenize(expression, optimize);
        if (!tokenList.isEmpty()) {
            tokens = tokenList.toArray(new Token[0]);
            if (optimize) {
                tokens = Tokenizer.optimize(tokens);
            }
        }
        return tokens;
    }

    /**
     * Parses a space-separated expression into a list of token arrays.
     * <p>This method is designed for expressions that represent a list of values, where each
     * value can be either a literal or a token. The expression is tokenized, and each
     * resulting token (or text segment) is treated as an element in the list. Empty text
     * segments are discarded.</p>
     * <p>For example, the expression {@code "value1 @{attr1} value3"} would be parsed into a
     * list containing three token arrays, one for each element.</p>
     * @param expression the expression string to parse
     * @return a list of token arrays, or {@code null} if the expression is null or results in an empty list
     */
    public static List<Token[]> parseAsList(String expression) {
        if (expression == null) {
            return null;
        }
        List<Token> tokenList = Tokenizer.tokenize(expression, true);
        List<Token[]> tokensList = null;
        if (!tokenList.isEmpty()) {
            tokensList = new ArrayList<>();
            for (Token t : tokenList) {
                if (t.getType() == TokenType.TEXT) {
                    // except empty token
                    if (StringUtils.hasText(t.getDefaultValue())) {
                        tokensList.add(new Token[] { t });
                    }
                } else {
                    tokensList.add(new Token[] { t });
                }
            }
        }
        return (tokensList == null || tokensList.isEmpty() ? null : tokensList);
    }

    /**
     * Parses an expression and extracts tokens that have both a name and a value,
     * returning them as a map.
     * <p>This method is specifically designed to extract tokens where the token's
     * internal structure defines both a name and a value (which is parsed as the
     * token's {@code defaultValue}). The token's name becomes the key in the map,
     * and the token itself (as a single-element array) becomes the value.</p>
     * <p>A token is considered to have a valid name/value pair only if both its
     * {@code name} and {@code defaultValue} properties are non-empty.
     * For example, a token like {@code ${param:defaultValue}} is valid because
     * 'param' is the name and 'defaultValue' is the value. In contrast, tokens
     * like {@code @{attribute}} (no value) or {@code %{system:java.version}}
     * (no default value, only a directive value) will be ignored by this method.</p>
     * <p>Non-token text like "key:value" is also ignored.</p>
     * @param expression the expression string to parse
     * @return a map of token arrays, or {@code null} if no valid tokens are found
     */
    public static Map<String, Token[]> parseAsMap(String expression) {
        if (expression == null) {
            return null;
        }
        List<Token> tokenList = Tokenizer.tokenize(expression, true);
        Map<String, Token[]> tokensMap = null;
        if (!tokenList.isEmpty()) {
            tokensMap = new LinkedHashMap<>();
            for (Token t : tokenList) {
                if (t.getType() != TokenType.TEXT) {
                    if (StringUtils.hasLength(t.getName()) && StringUtils.hasLength(t.getDefaultValue())) {
                        tokensMap.put(t.getName(), new Token[] { t });
                    }
                }
            }
        }
        return (tokensMap == null || tokensMap.isEmpty() ? null : tokensMap);
    }

    /**
     * Creates an array of tokens from an expression string, with an option to skip tokenization.
     * @param expression the expression string
     * @param tokenize if {@code true}, the expression is parsed into multiple tokens;
     *      if {@code false}, the entire expression is treated as a single text token.
     * @return an array of {@link Token} objects, or {@code null} if the expression is null
     */
    public static Token[] makeTokens(String expression, boolean tokenize) {
        if (expression == null) {
            return null;
        }
        if (tokenize) {
            return parse(expression);
        } else {
            Token token = new Token(expression);
            return new Token[] { token };
        }
    }

    /**
     * Converts an array of tokens back into its string representation.
     * This method is the reverse of the {@link #parse(String)} method.
     * @param tokens the array of tokens to stringify
     * @return the string representation of the tokens
     */
    public static String toString(Token[] tokens) {
        if (tokens == null || tokens.length == 0) {
            return StringUtils.EMPTY;
        }
        if (tokens.length == 1) {
            return (tokens[0] == null ? StringUtils.EMPTY : tokens[0].stringify());
        }
        StringBuilder sb = new StringBuilder();
        for (Token t : tokens) {
            if (t != null) {
                sb.append(t.stringify());
            }
        }
        return sb.toString();
    }

    /**
     * Parses a path string in a "safe" mode, where only parameter ({@code ${...}})
     * and attribute ({@code @{...}}) tokens are treated as dynamic.
     * <p>All other token types (e.g., beans, properties) are converted back into plain text.
     * This method is intended for performance-sensitive operations like URL routing, where
     * only a limited, safe subset of tokens should be evaluated dynamically.
     * If the path contains no dynamic parameter or attribute tokens, this method returns
     * {@code null} to indicate that the path can be treated as a static string.</p>
     * @param path the path string to parse
     * @return an array of tokens if dynamic tokens are found; otherwise, {@code null}
     */
    @Nullable
    public static Token[] parsePathSafely(String path) {
        if (!StringUtils.hasText(path)) {
            return null;
        }

        Token[] tokens = Tokenizer.tokenize(path, true).toArray(new Token[0]);
        if (tokens.length == 1 && tokens[0].getType() == TokenType.TEXT) {
            return null;
        }

        int count = 0;
        for (Token token : tokens) {
            if (token.getType() == TokenType.PARAMETER || token.getType() == TokenType.ATTRIBUTE) {
                count++;
            }
        }

        if (count > 0) {
            for (int i = 0; i < tokens.length; i++) {
                Token token = tokens[i];
                if (token.getType() != TokenType.PARAMETER && token.getType() != TokenType.ATTRIBUTE) {
                    tokens[i] = new Token(token.stringify());
                }
            }
            return tokens;
        } else {
            return null;
        }
    }

}
