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
import com.aspectran.core.lang.Nullable;
import com.aspectran.core.util.apon.AponConverter;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.json.JsonWriter;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpMediaTypeNotAcceptableException;
import com.aspectran.web.support.http.HttpStatus;
import com.aspectran.web.support.http.MediaType;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Created: 2019-06-16</p>
 */
public class RestResponseTransformer extends AbstractRestResponse<RestResponseTransformer> implements CustomTransformer {

    private static final List<MediaType> supportedContentTypes;
    static {
        List<MediaType> contentTypes = new ArrayList<>();
        contentTypes.add(MediaType.APPLICATION_JSON);
        contentTypes.add(MediaType.APPLICATION_APON);
        supportedContentTypes = Collections.unmodifiableList(contentTypes);
    }

    private static final Map<String, MediaType> supportedPathExtensions;
    static {
        Map<String, MediaType> pathExtensions = new HashMap<>();
        pathExtensions.put("json", MediaType.APPLICATION_JSON);
        pathExtensions.put("apon", MediaType.APPLICATION_APON);
        supportedPathExtensions = Collections.unmodifiableMap(pathExtensions);
    }

    public RestResponseTransformer() {
        super();
    }

    public RestResponseTransformer(@Nullable Object data) {
        super(data);
    }

    @Override
    protected List<MediaType> getSupportedContentTypes() {
        return supportedContentTypes;
    }

    @Override
    protected MediaType getContentTypeByPathExtension(String extension) {
        return supportedPathExtensions.get(extension);
    }

    @Override
    public void transform(Activity activity) throws Exception {
        ResponseAdapter responseAdapter = activity.getResponseAdapter();

        MediaType contentType;
        try {
            contentType = determineContentType(activity);
        } catch (HttpMediaTypeNotAcceptableException e) {
            responseAdapter.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
            return;
        }

        if (contentType.equals(MediaType.APPLICATION_JSON)) {
            toJSON(activity);
        } else if (contentType.equals(MediaType.APPLICATION_APON)) {
            toAPON(activity);
        }

        if (getStatus() > 0) {
            responseAdapter.setStatus(getStatus());
            if (getStatus() == HttpStatus.CREATED.value() && getLocation() != null) {
                responseAdapter.setHeader(HttpHeaders.LOCATION, getLocation());
            }
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
        if (getData() != null) {
            JsonWriter jsonWriter;
            if (isPrettyPrint()) {
                String indentString = activity.getSetting("indentString");
                jsonWriter = new JsonWriter(writer, indentString);
            } else {
                jsonWriter = new JsonWriter(writer, false);
            }
            jsonWriter.write(getData());
        } else {
            JsonWriter jsonWriter;
            if (isPrettyPrint()) {
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
        if (getData() != null) {
            Parameters parameters = AponConverter.from(getData());
            if (isPrettyPrint()) {
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
            if (isPrettyPrint()) {
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

}
