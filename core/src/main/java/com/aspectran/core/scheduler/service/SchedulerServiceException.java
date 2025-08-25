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
package com.aspectran.core.scheduler.service;

import com.aspectran.core.service.CoreServiceException;

import java.io.Serial;

/**
 * Exception thrown when a problem occurs within the scheduler service.
 * <p>This exception extends {@link CoreServiceException} and is used to indicate
 * errors specific to the lifecycle and operation of the {@link SchedulerService}.</p>
 *
 * @since 2.0.0
 */
public class SchedulerServiceException extends CoreServiceException {

    @Serial
    private static final long serialVersionUID = -6814416137683710109L;

    /**
     * Constructs a new SchedulerServiceException.
     */
    public SchedulerServiceException() {
        super();
    }

    /**
     * Constructs a new SchedulerServiceException with the specified detail message.
     * @param msg the detail message
     */
    public SchedulerServiceException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new SchedulerServiceException with the specified cause.
     * @param cause the underlying cause of the exception
     */
    public SchedulerServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new SchedulerServiceException with the specified detail message and cause.
     * @param msg the detail message
     * @param cause the underlying cause of the exception
     */
    public SchedulerServiceException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
