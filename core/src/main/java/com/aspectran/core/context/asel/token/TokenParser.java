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
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class for parsing strings containing Aspectran Expression Language (AsEL) tokens.
 * <p>This class acts as a high-level parser that uses a {@link Tokenizer} to split an
 * expression string into a series of {@link Token} objects. It provides methods to
 * handle different parsing scenarios, such as simple tokenization, optimization
 * (trimming whitespace), and parsing into specific collection types like lists or maps.</p>
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
     * Parses the given expression into a list of token arrays.
     * <p>This method is useful for handling expressions that represent a list of values,
     * where each value can itself be a token. It tokenizes the entire expression and then
     * splits the result into a list, where each element is a single-token array.
     * Empty text tokens are excluded.</p>
     * <p>For example, the expression "<code>value1 @{attr1} value3</code>" would be parsed into a
     * list of three token arrays.</p>
     * @param expression the expression string to parse
     * @return a list of token arrays, or {@code null} if the expression is null or empty
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
     * Parses the given expression into a map of token arrays.
     * <p>This method is designed to parse expressions that represent key-value pairs.
     * It tokenizes the expression and looks for tokens that have both a name and a
     * default value (e.g., <code>key1:value1 key2:@{attr2}</code>). The token's name is used
     * as the map key, and the token itself (as a single-element array) becomes the value.</p>
     * @param expression the expression string to parse
     * @return a map of token arrays, or {@code null} if the expression is null or empty
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
     * Parses a path string in a "safe" mode, where only parameter (<code>${...}</code>)
     * and attribute (<code>@{...}</code>) tokens are treated as dynamic.
     * <p>All other token types are converted back into plain text. This is useful for
     * performance-sensitive operations like URL routing, where only a subset of tokens
     * should be evaluated.</p>
     * <p>If the path contains no dynamic tokens, this method returns {@code null}.</p>
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
