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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

    public PathVariableMap() {
        super();
    }

    public PathVariableMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Applies the stored path variables to the given {@link Translet}.
     * <p>
     * This typically involves making the variable values accessible
     * to expressions or parameters within the translet execution context.
     * </p>
     * @param translet the translet to which variables should be applied
     */
    public void applyTo(@NonNull Translet translet) {
        Assert.notNull(translet, "translet must not be null");
        if (isEmpty()) {
            return;
        }
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
     * @return a {@code PathVariableMap} containing resolved variables, or an empty map
     *         if matched with no variables; or {@code null} if the request name does not match the template
     */
    @Nullable
    public static PathVariableMap parse(@NonNull Token[] nameTokens, @NonNull String requestName) {
        Assert.notNull(nameTokens, "nameTokens must not be null");
        Assert.notNull(requestName, "requestName must not be null");

        PathVariableMap pathVariables = new PathVariableMap();

        int beginIndex = 0;
        Token prevToken = null;
        Token lastToken = null;

        for (Token token : nameTokens) {
            TokenType type = token.getType();
            if (type == TokenType.PARAMETER || type == TokenType.ATTRIBUTE) {
                lastToken = token;
            } else {
                String term = token.stringify();
                int endIndex = requestName.indexOf(term, beginIndex);
                if (endIndex == -1) {
                    return null;
                }

                if (endIndex > beginIndex) {
                    if (prevToken == null) {
                        // A literal was found but not at the expected start position
                        return null;
                    }
                    String value = requestName.substring(beginIndex, endIndex);
                    if (!value.isEmpty()) {
                        pathVariables.put(prevToken, value);
                    }
                    beginIndex += value.length();
                } else if (prevToken != null && prevToken.getDefaultValue() != null) {
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
                pathVariables.put(lastToken, lastToken.getDefaultValue());
            }
        }

        return pathVariables;
    }

}
