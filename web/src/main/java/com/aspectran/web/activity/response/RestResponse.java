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
import com.aspectran.core.activity.response.transform.JsonTransformResponse;
import com.aspectran.core.activity.response.transform.apon.ContentsAponConverter;
import com.aspectran.core.activity.response.transform.json.ContentsJsonWriter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.util.apon.AponConverter;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.json.JsonWriter;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpStatus;

import java.io.IOException;
import java.io.Writer;

/**
 * <p>Created: 2019-06-16</p>
 */
public class RestResponse implements CustomTransformer {

    private static final String APPLICATION_JSON = "application/json";

    private static final String APPLICATION_APON = "application/apon";

    private final Object data;

    private boolean prettyPrint = true;

    public RestResponse() {
        this(null);
    }

    public RestResponse(Object data) {
        this.data = data;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    @Override
    public void transform(Activity activity) throws Exception {
        ResponseAdapter responseAdapter = activity.getResponseAdapter();
        String acceptContentType = determineAcceptContentType(responseAdapter);
        if (APPLICATION_JSON.equals(acceptContentType)) {
            toJSON(activity);
        } else if (APPLICATION_APON.equals(acceptContentType)) {
            toAPON(activity);
        } else {
            responseAdapter.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
        }
    }

    private void toJSON(Activity activity) throws IOException {
        RequestAdapter requestAdapter = activity.getRequestAdapter();
        ResponseAdapter responseAdapter = activity.getResponseAdapter();
        Writer writer = responseAdapter.getWriter();

        // support for jsonp
        String callback = requestAdapter.getParameter(JsonTransformResponse.CALLBACK_PARAM_NAME);
        if (callback != null) {
            writer.write(callback + JsonTransformResponse.ROUND_BRACKET_OPEN);
        }
        if (data != null) {
            JsonWriter jsonWriter;
            if (prettyPrint) {
                String indentString = activity.getSetting("indentString");
                jsonWriter = new JsonWriter(writer, indentString);
            } else {
                jsonWriter = new JsonWriter(writer, false);
            }
            jsonWriter.write(data);
        } else {
            JsonWriter jsonWriter;
            if (prettyPrint) {
                String indentString = activity.getSetting("indentString");
                jsonWriter = new ContentsJsonWriter(writer, indentString);
            } else {
                jsonWriter = new ContentsJsonWriter(writer, false);
            }
            jsonWriter.write(activity.getProcessResult());
        }
        if (callback != null) {
            writer.write(JsonTransformResponse.ROUND_BRACKET_CLOSE);
        }
    }

    private void toAPON(Activity activity) throws IOException {
        ResponseAdapter responseAdapter = activity.getResponseAdapter();
        Writer writer = responseAdapter.getWriter();

        AponWriter aponWriter = new AponWriter(writer);
        if (data != null) {
            Parameters parameters = AponConverter.from(data);
            if (prettyPrint) {
                String indentString = activity.getSetting("indentString");
                if (indentString != null) {
                    aponWriter.setIndentString(indentString);
                }
            } else {
                aponWriter.setIndentString(null);
            }
            aponWriter.write(parameters);
        } else {
            Parameters parameters = ContentsAponConverter.from(activity.getProcessResult());
            if (prettyPrint) {
                String indentString = activity.getSetting("indentString");
                if (indentString != null) {
                    aponWriter.setIndentString(indentString);
                }
            } else {
                aponWriter.setIndentString(null);
            }
            aponWriter.write(parameters);
        }
    }

    private static String determineAcceptContentType(ResponseAdapter responseAdapter) {
        String acceptType = getAcceptType(responseAdapter);
        if (acceptType == null) {
            return getContentType(responseAdapter);
        } else {
            return null;
        }
    }

    private static String getAcceptType(ResponseAdapter responseAdapter) {
        // String acceptType = responseAdapter.getHeader(HttpHeaders.ACCEPT);
        // TODO
        return null;
    }

    private static String getContentType(ResponseAdapter responseAdapter) {
        String contentType = responseAdapter.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType.startsWith(APPLICATION_JSON)) {
            return APPLICATION_JSON;
        } else if (contentType.startsWith(APPLICATION_APON)) {
            return APPLICATION_APON;
        } else {
            return null;
        }
    }

}
