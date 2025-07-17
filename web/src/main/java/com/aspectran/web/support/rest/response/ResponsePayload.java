/*
 * Copyright (c) 2023-present The Aspectran Project
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

public class ResponsePayload {

    private final boolean success;

    private Object data;

    private ErrorPayload error;

    ResponsePayload(boolean success) {
        this.success = success;
    }

    ResponsePayload(boolean success, Object data) {
        this.success = success;
        this.data = data;
    }

    ResponsePayload(ErrorPayload errorPayload) {
        this.success = false;
        this.error = errorPayload;
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getData() {
        return data;
    }

    public ResponsePayload setData(Object data) {
        this.data = data;
        return this;
    }

    public ErrorPayload getError() {
        return error;
    }

    public ResponsePayload setError(ErrorPayload errorPayload) {
        this.error = errorPayload;
        return this;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.appendForce("success", success);
        tsb.append("data", data);
        tsb.append("error", error);
        return tsb.toString();
    }

}
