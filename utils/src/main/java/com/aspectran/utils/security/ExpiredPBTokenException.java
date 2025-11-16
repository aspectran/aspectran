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
 * Thrown to indicate that a time-limited password-based token has expired.
 * This is a subclass of {@link InvalidPBTokenException}.
 */
public class ExpiredPBTokenException extends InvalidPBTokenException {

    @Serial
    private static final long serialVersionUID = -3377771930951220888L;

    /**
     * Constructs a new exception for an expired token.
     * @param token the expired token
     */
    public ExpiredPBTokenException(String token) {
        super("Expired password based token", token);
    }

}
