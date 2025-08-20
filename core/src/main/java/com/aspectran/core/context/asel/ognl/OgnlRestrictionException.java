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
package com.aspectran.core.context.asel.ognl;

import java.io.Serial;

/**
 * Thrown when an OGNL expression attempts to access a class, method, or field
 * that is forbidden by the current security restrictions.
 *
 * @see OgnlRestrictions
 * @see OgnlMemberAccess
 */
public class OgnlRestrictionException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -8829028434827240355L;

    /**
     * Constructs a new {@code OgnlRestrictionException} with the specified detail message.
     * @param message the detail message
     */
    public OgnlRestrictionException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code OgnlRestrictionException} with the specified detail message and cause.
     * @param message the detail message
     * @param cause the root cause
     */
    public OgnlRestrictionException(String message, Throwable cause) {
        super(message, cause);
    }

}
