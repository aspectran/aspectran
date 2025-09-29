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

import com.aspectran.utils.Assert;
import com.aspectran.utils.ToStringBuilder;

/**
 * Represents an error payload in a RESTful response.
 * <p>This is typically contained within a {@link FailureResponse} to provide
 * details about an error that occurred.</p>
 */
public class ErrorPayload {

    private String code;

    private String message;

    /**
     * Instantiates a new ErrorPayload.
     * @param code the error code
     * @param message the error message
     */
    ErrorPayload(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Gets the error code.
     * @return the error code
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the error code.
     * @param code the error code
     */
    public void setCode(String code) {
        Assert.hasText(code, "code must not be empty");
        this.code = code;
    }

    /**
     * Gets the error message.
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the error message.
     * @param message the error message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("code", code);
        tsb.append("message", message);
        return tsb.toString();
    }

}
