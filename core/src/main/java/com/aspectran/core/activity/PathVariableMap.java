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
package com.aspectran.core.activity;

import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.type.TokenType;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class PathVariableMap.
 *
 * <p>This class is generally not thread-safe. It is primarily designed for use in a single thread only.
 *
 * <p>Created: 2016. 2. 13.</p>
 */
public class PathVariableMap extends HashMap<Token, String> {

    private static final long serialVersionUID = -3327966082696522044L;

    protected void apply(Translet translet) {
        for (Map.Entry<Token, String> entry : entrySet()) {
            Token token = entry.getKey();
            if (token.getType() == TokenType.PARAMETER) {
                translet.setParameter(token.getName(), entry.getValue());
            } else if (token.getType() == TokenType.ATTRIBUTE) {
                translet.setAttribute(token.getName(), entry.getValue());
            }
        }
    }

    public static PathVariableMap newInstance(Token[] nameTokens, String requestTransletRuleName) {
        PathVariableMap pathVariableMap = new PathVariableMap();

        /*
            /example/customers/123-567/approval
            /example/customers/
            ${id1}
            -
            ${id2}
            /approval
        */
        int beginIndex = 0;
        int endIndex;
        Token prevToken = null;
        Token lastToken = null;

        for (Token token : nameTokens) {
            TokenType type = token.getType();
            if (type == TokenType.PARAMETER || type == TokenType.ATTRIBUTE) {
                lastToken = token;
            } else {
                String term = token.stringify();
                endIndex = requestTransletRuleName.indexOf(term, beginIndex);
                if (endIndex == -1) {
                    return null;
                }
                if (endIndex > beginIndex) {
                    String value = requestTransletRuleName.substring(beginIndex, endIndex);
                    if (!value.isEmpty()) {
                        pathVariableMap.put(prevToken, value);
                    }
                    beginIndex += value.length();
                } else {
                    if (prevToken != null && prevToken.getDefaultValue() != null) {
                        // If the last token ends with a "/" can be given a default value.
                        pathVariableMap.put(prevToken, prevToken.getDefaultValue());
                    }
                }
                beginIndex += term.length();
            }
            prevToken = (token.getType() != TokenType.TEXT ? token : null);
        }

        if (lastToken != null && prevToken == lastToken) {
            String value = requestTransletRuleName.substring(beginIndex);
            if (!value.isEmpty()) {
                pathVariableMap.put(lastToken, value);
            } else if (lastToken.getDefaultValue() != null) {
                // If the last token ends with a "/" can be given a default value.
                pathVariableMap.put(lastToken, lastToken.getDefaultValue());
            }
        }

        return pathVariableMap;
    }

}
