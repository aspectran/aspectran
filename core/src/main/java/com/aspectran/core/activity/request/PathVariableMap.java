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
 * A specialized map that binds {@link Token} objects to resolved path variable values.
 * <p>
 * Path variables are typically extracted from request URIs using a template pattern
 * and are then made available to an {@link com.aspectran.core.activity.Translet}.
 * </p>
 *
 * <p>Created: 2016. 2. 13.</p>
 */
public class PathVariableMap extends HashMap<Token, String> {

    @Serial
    private static final long serialVersionUID = -3327966082696522044L;

    /**
     * Applies the stored path variables to the given {@link Translet}.
     * <p>
     * This typically involves making the variable values accessible
     * to expressions or parameters within the translet execution context.
     * </p>
     * @param translet the translet to which variables should be applied
     */
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


    /**
     * Parses a set of name tokens against the provided request name and produces
     * a {@code PathVariableMap} containing matched variables.
     * @param nameTokens the template tokens representing variable names in the path
     * @param requestName the actual request path to match
     * @return a {@code PathVariableMap} containing resolved variables, or an empty
     *         map if none were matched
     */
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
                // This is a variable token (e.g., ${id}), so save it for the next iteration
                // which will look for the text token that follows it.
                lastToken = token;
            } else {
                // This is a text token (a literal part of the path).
                String term = token.stringify();
                endIndex = requestName.indexOf(term, beginIndex);
                if (endIndex == -1) {
                    // The literal part of the template was not found in the request path, so it's a mismatch.
                    return null;
                }

                // Check if there is a string between the previous literal and this one.
                if (endIndex > beginIndex) {
                    // This substring is the value for the previous variable token.
                    String value = requestName.substring(beginIndex, endIndex);
                    if (prevToken != null) {
                        if (!value.isEmpty()) {
                            pathVariables.put(prevToken, value);
                        }
                    } else if (!term.equals(value)) {
                        // This case handles a mismatch at the beginning of the path.
                        // If the template starts with a literal, the request path must also start with it.
                        return null;
                    }
                    beginIndex += value.length();
                } else if (prevToken != null && prevToken.getDefaultValue() != null) {
                    // The substring for the variable is empty (e.g., two consecutive slashes "//" for "/${var}/").
                    // If the variable token has a default value, use it.
                    pathVariables.put(prevToken, prevToken.getDefaultValue());
                }
                beginIndex += term.length();
            }
            prevToken = (token.getType() != TokenType.TEXT ? token : null);
        }

        // This handles the case where the path ends with a variable token.
        if (lastToken != null && prevToken == lastToken) {
            // The rest of the request string from the last matched position is the variable's value.
            String value = requestName.substring(beginIndex);
            if (!value.isEmpty()) {
                pathVariables.put(lastToken, value);
            } else if (lastToken.getDefaultValue() != null) {
                // The variable part is empty, but if it has a default value, use it.
                pathVariables.put(lastToken, lastToken.getDefaultValue());
            }
        }

        return pathVariables;
    }

}
