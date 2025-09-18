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
 * Represents a failure RESTful response.
 */
public class FailureResponse extends DefaultRestResponse {

    public FailureResponse() {
        super(new ResponsePayload(false));
    }

    public FailureResponse(Object data) {
        super(new ResponsePayload(false, data));
    }

    public FailureResponse(String code) {
        this(code, null);
    }

    public FailureResponse(String code, String message) {
        super(new ResponsePayload(new ErrorPayload(code, message)));
    }

    @Override
    public ResponsePayload getData() {
        return (ResponsePayload)super.getData();
    }

    @Override
    public FailureResponse setData(Object data) {
        if (hasData()) {
            getData().setData(data);
        }
        return this;
    }

    public FailureResponse setError(String code) {
        return setError(code, null);
    }

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
