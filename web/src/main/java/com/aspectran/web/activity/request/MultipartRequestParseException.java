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
package com.aspectran.web.activity.request;

import com.aspectran.core.activity.request.RequestParseException;

import java.io.Serial;

/**
 * Exception thrown when multipart request parse fails.
 */
public class MultipartRequestParseException extends RequestParseException {

    @Serial
    private static final long serialVersionUID = -3261897166973264629L;

    /**
     * Constructor to create exception with a message.
     * @param msg a message to associate with the exception
     */
    public MultipartRequestParseException(String msg) {
        super(msg);
    }

    /**
     * Constructor to create exception to wrap another exception and pass a message.
     * @param msg the message
     * @param cause the real cause of the exception
     */
    public MultipartRequestParseException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
