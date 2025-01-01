/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import java.io.Serial;

/**
 * The Class TokenEvaluationException.
 */
public class TokenEvaluationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2303202652519466514L;

    private final Token token;

    /**
     * Instantiates a new token evaluation exception.
     * @param token the token
     * @param cause the root cause
     */
    public TokenEvaluationException(Token token, Throwable cause) {
        super("Failed to evaluate token " + token, cause);
        this.token = token;
    }

    /**
     * Gets the token which is failed to evaluate expression.
     * @return the token
     */
    public Token getToken() {
        return this.token;
    }

}
