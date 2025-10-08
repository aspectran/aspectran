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
package com.aspectran.web.support.rest.response;

import com.aspectran.web.activity.response.DefaultRestResponse;

/**
 * Represents a failed (e.g., 4xx, 5xx) REST response.
 * <p>This response contains a {@link ResponsePayload} that indicates failure
 * and holds an {@link ErrorPayload} with details about the error.</p>
 */
public class FailureResponse extends DefaultRestResponse {

    /**
     * Instantiates a new FailureResponse.
     */
    public FailureResponse() {
        super(new ResponsePayload(false));
    }

    /**
     * Instantiates a new FailureResponse with the given data payload.
     * @param data the response data
     */
    public FailureResponse(Object data) {
        super(new ResponsePayload(false, data));
    }

    /**
     * Instantiates a new FailureResponse with the given error code.
     * @param code the error code
     */
    public FailureResponse(String code) {
        this(code, null);
    }

    /**
     * Instantiates a new FailureResponse with the given error code and message.
     * @param code the error code
     * @param message the error message
     */
    public FailureResponse(String code, String message) {
        super(new ResponsePayload(new ErrorPayload(code, message)));
    }

    /**
     * Returns the underlying response payload.
     * @return the response payload
     */
    @Override
    public ResponsePayload getData() {
        return (ResponsePayload)super.getData();
    }

    /**
     * Sets the data on the underlying response payload.
     * @param data the response data
     * @return this {@code FailureResponse} for fluent chaining
     */
    @Override
    public FailureResponse setData(Object data) {
        if (hasData()) {
            getData().setData(data);
        }
        return this;
    }

    /**
     * Sets the error code on the underlying error payload.
     * @param code the error code
     * @return this {@code FailureResponse} for fluent chaining
     */
    public FailureResponse setError(String code) {
        return setError(code, null);
    }

    /**
     * Sets the error code and message on the underlying error payload.
     * @param code the error code
     * @param message the error message
     * @return this {@code FailureResponse} for fluent chaining
     */
    public FailureResponse setError(String code, String message) {
        if (hasData()) {
            getData().setError(new ErrorPayload(code, message));
        }
        return this;
    }

    @Override
    public String toString() {
        if (hasData()) {
            return getData().toString();
        } else {
            return super.toString();
        }
    }

}
