/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.activity.request;

import com.aspectran.core.activity.ActivityException;

/**
 * This exception will be thrown when a translet request is failed.
 * 
 * <p>Created: 2008. 01. 07 AM 3:35:55</p>
 */
public class RequestException extends ActivityException {

    /** @serial */
    private static final long serialVersionUID = -890371130094039206L;

    /**
     * Instantiates a new RequestException.
     */
    public RequestException() {
        super();
    }

    /**
     * Instantiates a new RequestException.
     *
     * @param msg the detail message
     */
    public RequestException(String msg) {
        super(msg);
    }

    /**
     * Instantiates a new RequestException.
     *
     * @param cause the real cause of the exception
     */
    public RequestException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new RequestException.
     *
     * @param msg the detail message
     * @param cause the real cause of the exception
     */
    public RequestException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
