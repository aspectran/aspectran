/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import com.aspectran.core.activity.response.transform.AponTransformResponse;
import com.aspectran.core.activity.response.transform.JsonTransformResponse;
import com.aspectran.core.activity.response.transform.XmlTransformResponse;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.apon.ObjectToAponConverter;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.json.JsonWriter;
import com.aspectran.web.support.http.HttpMediaTypeNotAcceptableException;
import com.aspectran.web.support.http.HttpStatus;
import com.aspectran.web.support.http.MediaType;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The default RestResponse supports APON, JSON, and XML data types.
 *
 * <p>Created: 2019-06-16</p>
 */
public class DefaultRestResponse extends AbstractRestResponse {

    private static final int MAX_INDENT = 8;

    private static final List<MediaType> supportedContentTypes;
    static {
        supportedContentTypes = List.of(
                MediaType.TEXT_PLAIN,
                MediaType.TEXT_HTML,
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_APON,
                MediaType.APPLICATION_XML);
    }

    private static final Map<String, MediaType> supportedPathExtensions;
    static {
        supportedPathExtensions = Map.of(
                "json", MediaType.APPLICATION_JSON,
                "apon", MediaType.APPLICATION_APON,
                "xml", MediaType.APPLICATION_XML,
                "txt", MediaType.TEXT_PLAIN,
                "html", MediaType.TEXT_HTML,
                "htm", MediaType.TEXT_HTML);
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
        Assert.notNull(activity, "activity must not be null");
        ResponseAdapter responseAdapter = activity.getResponseAdapter();

        MediaType acceptContentType;
        try {
            acceptContentType = determineAcceptContentType(activity);
        } catch (HttpMediaTypeNotAcceptableException e) {
            responseAdapter.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
            return;
        }

        MediaType responseContentType = determineResponseContentType(activity, acceptContentType);
        responseAdapter.setContentType(responseContentType.toString());

        transformByContentType(activity, acceptContentType);

        if (getHeaders() != null) {
            for (Map.Entry<String, List<String>> entry : getHeaders().entrySet()) {
                String name = entry.getKey();
                List<String> values = entry.getValue();
                for (String value : values) {
                    responseAdapter.addHeader(name, value);
                }
            }
        }

        if (getStatus() > 0) {
            responseAdapter.setStatus(getStatus());
        }
    }

    protected void transformByContentType(Activity activity, MediaType contentType) throws Exception {
        if (MediaType.APPLICATION_JSON.equalsTypeAndSubtype(contentType)) {
            toJSON(activity, parseIndent(contentType));
        } else if (MediaType.APPLICATION_APON.equalsTypeAndSubtype(contentType)) {
            toAPON(activity, parseIndent(contentType));
        } else if (MediaType.APPLICATION_XML.equalsTypeAndSubtype(contentType)) {
            Charset charset = contentType.getCharset();
            String encoding = (charset != null ? charset.name() : null);
            toXML(activity, encoding, parseIndent(contentType));
        } else {
            toText(activity);
        }
    }

    private void toJSON(@NonNull Activity activity, int indent) throws IOException {
        RequestAdapter requestAdapter = activity.getRequestAdapter();
        ResponseAdapter responseAdapter = activity.getResponseAdapter();
        Writer writer = responseAdapter.getWriter();

        // support for jsonp
        String callback = requestAdapter.getParameter(JsonTransformResponse.CALLBACK_PARAM_NAME);
        if (callback != null) {
            writer.write(callback + JsonTransformResponse.ROUND_BRACKET_OPEN);
        }
        if (getName() != null || getData() != null) {
            StringifyContext stringifyContext = resolveStringifyContext(activity, indent);
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.applyStringifyContext(stringifyContext);
            if (getName() != null) {
                jsonWriter.beginObject();
                jsonWriter.writeName(getName());
            }
            jsonWriter.write(getData());
            if (getName() != null) {
                jsonWriter.endObject();
            }
        }
        if (callback != null) {
            writer.write(JsonTransformResponse.ROUND_BRACKET_CLOSE);
        }
    }

    private void toAPON(Activity activity, int indent) throws IOException {
        if (getName() != null || getData() != null) {
            ResponseAdapter responseAdapter = activity.getResponseAdapter();
            Writer writer = responseAdapter.getWriter();
            StringifyContext stringifyContext = resolveStringifyContext(activity, indent);

            ObjectToAponConverter aponConverter = new ObjectToAponConverter();
            aponConverter.applyStringyContext(stringifyContext);

            Parameters parameters = aponConverter.toParameters(getName(), getData());

            AponTransformResponse.transform(parameters, writer, stringifyContext);
        }
    }

    private void toXML(Activity activity, String encoding, int indent) throws IOException, TransformerException {
        if (getName() != null || getData() != null) {
            ResponseAdapter responseAdapter = activity.getResponseAdapter();
            Writer writer = responseAdapter.getWriter();
            StringifyContext stringifyContext = resolveStringifyContext(activity, indent);

            Object data;
            if (getName() != null) {
                data = Collections.singletonMap(getName(), getData());
            } else {
                data = getData();
            }
            XmlTransformResponse.transform(data, writer, encoding, stringifyContext);
        }
    }

    private void toText(Activity activity) throws IOException {
        if (getName() != null || getData() != null) {
            ResponseAdapter responseAdapter = activity.getResponseAdapter();
            Writer writer = responseAdapter.getWriter();
            if (getData() != null) {
                writer.write(ToStringBuilder.toString(getName() + ":", getData()));
            }
        }
    }

    @NonNull
    private StringifyContext resolveStringifyContext(@NonNull Activity activity, int indent) {
        StringifyContext stringifyContext = activity.getStringifyContext().clone();
        if (hasPrettyPrint()) {
            stringifyContext.setPretty(isPrettyPrint());
        }
        if (isPrettyPrint() && indent > -1) {
            stringifyContext.setIndentSize(indent);
        }
        return stringifyContext;
    }

    private int parseIndent(@NonNull MediaType contentType) {
        try {
            String indent = contentType.getParameter("indent");
            if (indent != null) {
                int depth = Integer.parseInt(indent);
                return (Math.min(depth, MAX_INDENT));
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        return -1;
    }

}
