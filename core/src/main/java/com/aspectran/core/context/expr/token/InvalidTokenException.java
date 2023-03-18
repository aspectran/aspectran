/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

/**
 * The Class InvalidTokenException.
 */
public class InvalidTokenException extends RuntimeException {

    /** @serial */
    private static final long serialVersionUID = -3013940354563756601L;

    private final Token token;

    /**
     * Instantiates a new invalid token exception.
     *
     * @param token the token
     */
    public InvalidTokenException(Token token) {
        this("Invalid token", token);
    }

    /**
     * Instantiates a new invalid token exception.
     *
     * @param msg the detail message
     * @param token the token
     */
    public InvalidTokenException(String msg, Token token) {
        super(msg + " " + token);
        this.token = token;
    }

    public Token getToken() {
        return this.token;
    }

}
