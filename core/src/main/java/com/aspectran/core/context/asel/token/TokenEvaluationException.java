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

import java.io.Serial;

/**
 * Thrown when an error occurs during the evaluation of an AsEL token.
 * <p>This exception is a wrapper around any underlying exception that occurs when the
 * {@link TokenEvaluator} attempts to resolve a token's value. For example, it could
 * be thrown if a specified bean is not found, a method invocation fails, or a property
 * cannot be accessed.</p>
 *
 * @see TokenEvaluator
 * @see Token
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
     * Returns the token that failed to be evaluated.
     * @return the token
     */
    public Token getToken() {
        return this.token;
    }

}
