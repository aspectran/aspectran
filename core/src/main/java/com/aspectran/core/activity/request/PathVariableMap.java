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
package com.aspectran.core.activity.request;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * A map that holds path variables extracted from a request URI.
 * <p>
 * Path variables are dynamic segments of a URI template (e.g., <code>/users/${id}</code>)
 * that are resolved at runtime. This class provides lookup access to those values,
 * allowing activities to adapt behavior based on request paths.
 * </p>
 *
 * <p>Responsibilities may include:</p>
 * <ul>
 *   <li>Mapping variable names defined in URI templates to actual values in the request</li>
 *   <li>Supporting type conversion of path variable values</li>
 *   <li>Providing iteration over available variables</li>
 * </ul>
 *
 * <p>Created: 2016. 2. 13.</p>
 */
public class PathVariableMap extends HashMap<Token, String> {

    @Serial
    private static final long serialVersionUID = -3327966082696522044L;

    public void applyTo(Translet translet) {
        for (Map.Entry<Token, String> entry : entrySet()) {
            Token token = entry.getKey();
            if (token.getType() == TokenType.PARAMETER) {
                translet.setParameter(token.getName(), entry.getValue());
            } else if (token.getType() == TokenType.ATTRIBUTE) {
                translet.setAttribute(token.getName(), entry.getValue());
            }
        }
    }

    @Nullable
    public static PathVariableMap parse(Token[] nameTokens, String requestName) {
        Assert.notNull(nameTokens, "nameTokens must not be null");
        Assert.notNull(requestName, "requestName must not be null");

        PathVariableMap pathVariables = new PathVariableMap();

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
                endIndex = requestName.indexOf(term, beginIndex);
                if (endIndex == -1) {
                    return null;
                }
                if (endIndex > beginIndex) {
                    String value = requestName.substring(beginIndex, endIndex);
                    if (prevToken != null) {
                        if (!value.isEmpty()) {
                            pathVariables.put(prevToken, value);
                        }
                    } else if (!term.equals(value)) {
                        return null;
                    }
                    beginIndex += value.length();
                } else if (prevToken != null && prevToken.getDefaultValue() != null) {
                    // If the last token ends with a "/" can be given a default value.
                    pathVariables.put(prevToken, prevToken.getDefaultValue());
                }
                beginIndex += term.length();
            }
            prevToken = (token.getType() != TokenType.TEXT ? token : null);
        }

        if (lastToken != null && prevToken == lastToken) {
            String value = requestName.substring(beginIndex);
            if (!value.isEmpty()) {
                pathVariables.put(lastToken, value);
            } else if (lastToken.getDefaultValue() != null) {
                // If the last token ends with a "/" can be given a default value.
                pathVariables.put(lastToken, lastToken.getDefaultValue());
            }
        }

        return pathVariables;
    }

}
