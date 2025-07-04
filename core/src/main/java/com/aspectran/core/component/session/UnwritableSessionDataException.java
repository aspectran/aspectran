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
package com.aspectran.core.component.session;

import java.io.Serial;

/**
 * Exception raised when session data can not be written.
 *
 * <p>Created: 2017. 9. 27.</p>
 */
public class UnwritableSessionDataException extends Exception {

    @Serial
    private static final long serialVersionUID = -8936664134645201545L;

    private final String id;

    public UnwritableSessionDataException(String id, Throwable t) {
        super("Unwritable session " + id, t);
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
