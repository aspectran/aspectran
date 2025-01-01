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
package com.aspectran.core.context;

import java.io.Serial;

/**
 * Exception occurs when there is no current activity.
 */
public class InactivityStateException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 8373382682956966522L;

    /**
     * Constructs a InactivityStateException.
     */
    public InactivityStateException() {
        super("No activity");
    }

}
