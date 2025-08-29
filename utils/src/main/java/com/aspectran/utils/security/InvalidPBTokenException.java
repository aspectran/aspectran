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
package com.aspectran.utils.security;

import java.io.Serial;

/**
 * Thrown when a password-based token is found to be invalid.
 * <p>This could happen if the token is malformed, has been tampered with,
 * or could not be decrypted with the current password.</p>
 */
public class InvalidPBTokenException extends Exception {

    @Serial
    private static final long serialVersionUID = 3538105957787473922L;

    /** The invalid token that caused the exception. */
    private final String token;

    /**
     * Constructs a new exception with the specified invalid token.
     * @param token the invalid token
     */
    public InvalidPBTokenException(String token) {
        this("Invalid password based token: " + token, token);
    }

    /**
     * Constructs a new exception with the specified invalid token and cause.
     * @param token the invalid token
     * @param cause the cause
     */
    public InvalidPBTokenException(String token, Throwable cause) {
        this("Invalid password based token: " + token, token, cause);
    }

    /**
     * Constructs a new exception with the specified detail message and invalid token.
     * @param msg the detail message
     * @param token the invalid token
     */
    public InvalidPBTokenException(String msg, String token) {
        super(msg);
        this.token = token;
    }

    /**
     * Constructs a new exception with the specified detail message, invalid token, and cause.
     * @param msg the detail message
     * @param token the invalid token
     * @param cause the cause
     */
    public InvalidPBTokenException(String msg, String token, Throwable cause) {
        super(msg, cause);
        this.token = token;
    }

    /**
     * Returns the invalid token that caused this exception.
     * @return the invalid token
     */
    public String getToken() {
        return token;
    }

}
