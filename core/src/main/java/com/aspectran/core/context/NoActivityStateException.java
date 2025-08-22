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
package com.aspectran.core.context;

import java.io.Serial;

/**
 * Exception thrown when an attempt is made to access the current activity,
 * but no activity is associated with the current thread.
 *
 * <p>This typically occurs when {@link ActivityContext#getCurrentActivity()} is called
 * outside the scope of an active request or execution.
 *
 * @see ActivityContext#getCurrentActivity()
 */
public class NoActivityStateException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 8373382682956966522L;

    /**
     * Constructs a new NoActivityStateException.
     */
    public NoActivityStateException() {
        super("No activity is currently bound to the current thread");
    }

}
