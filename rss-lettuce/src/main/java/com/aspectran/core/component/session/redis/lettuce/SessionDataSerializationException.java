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
package com.aspectran.core.component.session.redis.lettuce;

import java.io.Serial;

/**
 * Exception thrown when an error occurs during serialization or
 * deserialization of session data.
 *
 * <p>Created: 2019/12/08</p>
 */
public class SessionDataSerializationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -4113369748730396646L;

    public SessionDataSerializationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
