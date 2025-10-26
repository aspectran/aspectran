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
package com.aspectran.core.component;

import java.io.Serial;

/**
 * Exception thrown when a component is not in an available state for the
 * requested operation (e.g., it has not been initialized or has already
 * been destroyed).
 */
public class UnavailableException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = -7917663896173904952L;

    /**
     * Constructs an UnavailableException with the specified detail message.
     * @param message the detail message
     */
    public UnavailableException(String message) {
        super(message);
    }

}
