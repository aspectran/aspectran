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

import com.aspectran.utils.ToStringBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents the payload of a RESTful response.
 * <p>This class is a generic container for API responses, encapsulating the
 * success status, a data payload, and error information. It is generally
 * used via its subclasses, {@link SuccessResponse} and {@link FailureResponse}.</p>
 */
public class ResponsePayload {

    private static final String SUCCESS_NAME = "success";

    private static final String DATA_NAME = "data";

    private static final String ERROR_NAME = "error";

    private final boolean success;

    private Object data;

    private ErrorPayload error;

    private boolean protectedData;

    /**
     * Instantiates a new ResponsePayload.
     * @param success true if the operation was successful
     */
    ResponsePayload(boolean success) {
        this.success = success;
    }

    /**
     * Instantiates a new ResponsePayload.
     * @param success true if the operation was successful
     * @param data the data payload
     */
    ResponsePayload(boolean success, Object data) {
        this.success = success;
        this.data = data;
    }

    /**
     * Instantiates a new ResponsePayload for a failed operation.
     * @param errorPayload the error details
     */
    ResponsePayload(ErrorPayload errorPayload) {
        this.success = false;
        this.error = errorPayload;
    }

    /**
     * Returns whether the operation was successful.
     * @return true if the operation was successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the data payload of the response.
     * @return the data payload
     */
    public Object getData() {
        return data;
    }

    /**
     * Sets the data payload of the response.
     * @param data the data payload
     * @return this {@code ResponsePayload} for fluent chaining
     */
    public ResponsePayload setData(Object data) {
        this.data = data;
        return this;
    }

    /**
     * Returns the error details if the operation failed.
     * @return the error details, or null if the operation was successful
     */
    public ErrorPayload getError() {
        return error;
    }

    /**
     * Sets the error details for a failed operation.
     * @param errorPayload the error details
     * @return this {@code ResponsePayload} for fluent chaining
     */
    public ResponsePayload setError(ErrorPayload errorPayload) {
        this.error = errorPayload;
        return this;
    }

    /**
     * Returns a map representing the response payload, with fields included
     * based on the success status.
     * <p>In a successful response, the "error" field is omitted. In a failed
     * response, the "data" field is omitted if it is null.</p>
     * @return a map representing the response payload
     */
    public Map<String, Object> toEntity() {
        Map<String, Object> entity = new LinkedHashMap<>();
        entity.put(SUCCESS_NAME, success);
        if (success) {
            if (data != null) {
                entity.put(DATA_NAME, data);
            }
        } else {
            if (data != null) {
                entity.put(DATA_NAME, data);
            }
            if (error != null) {
                entity.put(ERROR_NAME, error);
            }
        }
        return entity;
    }

    /**
     * Returns whether the data payload is protected from being exposed in string representations.
     * @return true if the data payload is protected, false otherwise
     */
    public boolean isProtected() {
        return protectedData;
    }

    /**
     * Protects the data payload from being exposed in string representations.
     * @return this {@code ResponsePayload} for fluent chaining
     */
    public ResponsePayload protect() {
        return protect(true);
    }

    /**
     * Sets whether the data payload should be protected from being exposed in string representations.
     * @param protect true to protect the data payload, false otherwise
     * @return this {@code ResponsePayload} for fluent chaining
     */
    public ResponsePayload protect(boolean protect) {
        this.protectedData = protect;
        return this;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.appendForce(SUCCESS_NAME, success);
        if (protectedData && data != null) {
            tsb.append(DATA_NAME, "[PROTECTED]");
        } else {
            tsb.append(DATA_NAME, data);
        }
        tsb.append(ERROR_NAME, error);
        return tsb.toString();
    }

}
