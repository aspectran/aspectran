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
package com.aspectran.core.component.bean.async;

import com.aspectran.core.component.bean.BeanException;

import java.io.Serial;

/**
 * Exception thrown when an error occurs during asynchronous execution.
 *
 * <p>Created: 2024. 8. 24.</p>
 */
public class AsyncExecutionException extends BeanException {

    @Serial
    private static final long serialVersionUID = 1496387160074614441L;

    public AsyncExecutionException(String msg) {
        super(msg);
    }

    public AsyncExecutionException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
