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
package com.aspectran.core.context.expr.token;

import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class TokenParser.
 */
public class TokenParser {

    /**
     * Returns an array of tokens that contains tokenized string.
     *
     * @param text the string to parse
     * @return an array of tokens
     */
    public static Token[] parse(String text) {
        return parse(text, false);
    }

    /**
     * Returns an array of tokens that contains tokenized string.
     *
     * @param text the string to parse
     * @param optimize whether to optimize tokens
     * @return an array of tokens
     */
    public static Token[] parse(String text, boolean optimize) {
        if (text == null) {
            return null;
        }
        if (text.length() == 0) {
            Token t = new Token(text);
            return new Token[] { t };
        }

        Token[] tokens = null;
        List<Token> tokenList = Tokenizer.tokenize(text, optimize);

        if (!tokenList.isEmpty()) {
            tokens = tokenList.toArray(new Token[0]);
            if (optimize) {
                tokens = Tokenizer.optimize(tokens);
            }
        }

        return tokens;
    }

    public static List<Token[]> parseAsList(String text) {
        if (text == null) {
            return null;
        }

        List<Token> tokenList = Tokenizer.tokenize(text, true);
        List<Token[]> tokensList = null;

        if (!tokenList.isEmpty()) {
            tokensList = new ArrayList<>();
            for (Token t : tokenList) {
                if (t.getType() == TokenType.TEXT) {
                    // except empty token
                    if (StringUtils.hasText(t.getDefaultValue())) {
                        tokensList.add(new Token[] {t});
                    }
                } else {
                    tokensList.add(new Token[] {t});
                }
            }
        }

        return (tokensList == null || tokensList.isEmpty() ? null : tokensList);
    }

    public static Map<String, Token[]> parseAsMap(String text) {
        if (text == null) {
            return null;
        }

        List<Token> tokenList = Tokenizer.tokenize(text, true);
        Map<String, Token[]> tokensMap = null;

        if (!tokenList.isEmpty()) {
            tokensMap = new LinkedHashMap<>();
            for (Token t : tokenList) {
                if (t.getType() != TokenType.TEXT) {
                    if (StringUtils.hasLength(t.getName()) && StringUtils.hasLength(t.getDefaultValue())) {
                        tokensMap.put(t.getName(), new Token[] {t});
                    }
                }
            }
        }

        return (tokensMap == null || tokensMap.isEmpty() ? null : tokensMap);
    }

    /**
     * Convert the given string into tokens.
     *
     * @param text the text
     * @param tokenize whether to tokenize
     * @return the token[]
     */
    public static Token[] makeTokens(String text, boolean tokenize) {
        if (text == null) {
            return null;
        }
        Token[] tokens;
        if (tokenize) {
            tokens = parse(text);
        } else {
            tokens = new Token[1];
            tokens[0] = new Token(text);
        }
        return tokens;
    }

    /**
     * Convert to string from the token array.
     *
     * @param tokens the tokens
     * @return the string
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

}
