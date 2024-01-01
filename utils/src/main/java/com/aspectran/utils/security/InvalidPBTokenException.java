/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

/**
 * The Class InvalidPBTokenException.
 */
public class InvalidPBTokenException extends Exception {

    private static final long serialVersionUID = 3538105957787473922L;

    private final String token;

    public InvalidPBTokenException(String token) {
        this("Invalid password based token: " + token, token);
    }

    public InvalidPBTokenException(String token, Throwable cause) {
        this("Invalid password based token: " + token, token, cause);
    }

    public InvalidPBTokenException(String msg, String token) {
        super(msg);
        this.token = token;
    }

    public InvalidPBTokenException(String msg, String token, Throwable cause) {
        super(msg, cause);
        this.token = token;
    }

    public String getToken() {
        return token;
    }

}
