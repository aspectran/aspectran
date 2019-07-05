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
import com.aspectran.core.activity.response.transform.JsonTransformResponse;
import com.aspectran.core.activity.response.transform.XmlTransformResponse;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.util.Assert;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.ObjectToApon;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.apon.VariableParameters;
import com.aspectran.core.util.json.JsonWriter;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpMediaTypeNotAcceptableException;
import com.aspectran.web.support.http.HttpStatus;
import com.aspectran.web.support.http.MediaType;

import javax.xml.transform.TransformerException;
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
public class DefaultRestResponse extends AbstractRestResponse {

    private static final int MAX_INDENT = 8;

    private static final List<MediaType> supportedContentTypes;
    static {
        List<MediaType> contentTypes = new ArrayList<>();
        contentTypes.add(MediaType.APPLICATION_JSON);
        contentTypes.add(MediaType.APPLICATION_APON);
        contentTypes.add(MediaType.APPLICATION_XML);
        supportedContentTypes = Collections.unmodifiableList(contentTypes);
    }

    private static final Map<String, MediaType> supportedPathExtensions;
    static {
        Map<String, MediaType> pathExtensions = new HashMap<>();
        pathExtensions.put("json", MediaType.APPLICATION_JSON);
        pathExtensions.put("apon", MediaType.APPLICATION_APON);
        pathExtensions.put("xml", MediaType.APPLICATION_XML);
        supportedPathExtensions = Collections.unmodifiableMap(pathExtensions);
    }

    public DefaultRestResponse() {
        super();
    }

    public DefaultRestResponse(Object data) {
        super(data);
    }

    public DefaultRestResponse(String label, Object data) {
        super(label, data);
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
        Assert.notNull(activity, "'activity' must not be null");
        ResponseAdapter responseAdapter = activity.getResponseAdapter();

        String encoding = determineEncoding(activity);
        if (encoding != null) {
            responseAdapter.setEncoding(encoding);
        }

        MediaType contentType;
        try {
            contentType = determineContentType(activity);
        } catch (HttpMediaTypeNotAcceptableException e) {
            responseAdapter.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
            return;
        }
        responseAdapter.setContentType(contentType.toString());

        transformByContentType(activity, contentType);

        if (getStatus() > 0) {
            responseAdapter.setStatus(getStatus());
            if (getStatus() == HttpStatus.CREATED.value() && getLocation() != null) {
                responseAdapter.setHeader(HttpHeaders.LOCATION, getLocation());
            }
        }
    }

    protected void transformByContentType(Activity activity, MediaType contentType) throws Exception {
        if (MediaType.APPLICATION_JSON.equalsTypeAndSubtype(contentType)) {
            toJSON(activity, parseIndent(contentType));
        } else if (MediaType.APPLICATION_APON.equalsTypeAndSubtype(contentType)) {
            toAPON(activity, parseIndent(contentType));
        } else if (MediaType.APPLICATION_XML.equalsTypeAndSubtype(contentType)) {
            toXML(activity);
        }
    }

    private void toJSON(Activity activity, int indent) throws IOException {
        RequestAdapter requestAdapter = activity.getRequestAdapter();
        ResponseAdapter responseAdapter = activity.getResponseAdapter();
        Writer writer = responseAdapter.getWriter();

        // support for jsonp
        String callback = requestAdapter.getParameter(JsonTransformResponse.CALLBACK_PARAM_NAME);
        if (callback != null) {
            writer.write(callback + JsonTransformResponse.ROUND_BRACKET_OPEN);
        }
        if (getName() != null || getData() != null) {
            JsonWriter jsonWriter;
            if (isPrettyPrint()) {
                String indentString;
                if (indent > -1) {
                    indentString = StringUtils.repeat(' ', indent);
                } else {
                    indentString = activity.getSetting("indentString");
                }
                jsonWriter = new JsonWriter(writer, indentString);
            } else {
                jsonWriter = new JsonWriter(writer, false);
            }
            if (getName() != null) {
                jsonWriter.beginBlock();
                jsonWriter.writeName(getName());
            }
            jsonWriter.write(getData());
            if (getName() != null) {
                jsonWriter.endBlock();
            }
        }
        if (callback != null) {
            writer.write(JsonTransformResponse.ROUND_BRACKET_CLOSE);
        }
    }

    private void toAPON(Activity activity, int indent) throws IOException {
        ResponseAdapter responseAdapter = activity.getResponseAdapter();
        Writer writer = responseAdapter.getWriter();

        if (getName() != null || getData() != null) {
            Parameters parameters = new VariableParameters();
            ObjectToApon.putValue(parameters, getName(), getData());

            AponWriter aponWriter = new AponWriter(writer);
            if (isPrettyPrint()) {
                String indentString;
                if (indent > -1) {
                    indentString = StringUtils.repeat(' ', indent);
                } else {
                    indentString = activity.getSetting("indentString");
                }
                if (indentString != null) {
                    aponWriter.setIndentString(indentString);
                }
            } else {
                aponWriter.setIndentString(null);
            }
            aponWriter.write(parameters);
        }
    }

    private void toXML(Activity activity) throws IOException, TransformerException {
        ResponseAdapter responseAdapter = activity.getResponseAdapter();
        Writer writer = responseAdapter.getWriter();

        if (getData() != null) {
            if (getName() != null) {
                XmlTransformResponse.toXML(Collections.singletonMap(getName(), getData()),
                        writer, null, isPrettyPrint());
            } else {
                XmlTransformResponse.toXML(getData(), writer, null, isPrettyPrint());
            }
        }
    }

    private int parseIndent(MediaType contentType) {
        try {
            String indent = contentType.getParameter("indent");
            if (indent != null) {
                int depth = Integer.parseInt(indent);
                if (depth >= 0) {
                    if (depth > MAX_INDENT) {
                        depth = MAX_INDENT;
                    }
                    return depth;
                }
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        return -1;
    }

}
