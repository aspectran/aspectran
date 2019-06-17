/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.web.activity.response;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.response.transform.CustomTransformer;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpStatus;

/**
 * <p>Created: 2019-06-16</p>
 */
public class RestResponseTransformer extends AbstractRestResponse<RestResponseTransformer> implements CustomTransformer {

    private static final String APPLICATION_JSON = "application/json";

    private static final String APPLICATION_APON = "application/apon";

    private final Object data;

    public RestResponseTransformer() {
        this(null);
    }

    public RestResponseTransformer(Object data) {
        super();
        this.data = data;
    }

    @Override
    public void transform(Activity activity) throws Exception {
        ResponseAdapter responseAdapter = activity.getResponseAdapter();

        String acceptContentType = determineAcceptContentType(responseAdapter);
        if (APPLICATION_JSON.equals(acceptContentType)) {
            toJSON(activity, data);
        } else if (APPLICATION_APON.equals(acceptContentType)) {
            toAPON(activity, data);
        } else {
            responseAdapter.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
            return;
        }

        if (getStatus() > 0) {
            responseAdapter.setStatus(getStatus());
            if (getStatus() == HttpStatus.CREATED.value() && getLocation() != null) {
                responseAdapter.setHeader(HttpHeaders.LOCATION, getLocation());
            }
        }
    }

}
