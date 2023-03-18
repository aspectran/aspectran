/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.core.activity.FormattingContext;
import com.aspectran.core.activity.response.transform.AponTransformResponse;
import com.aspectran.core.activity.response.transform.JsonTransformResponse;
import com.aspectran.core.activity.response.transform.XmlTransformResponse;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.util.Assert;
import com.aspectran.core.util.apon.ObjectToAponConverter;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.json.JsonWriter;
import com.aspectran.web.support.http.HttpMediaTypeNotAcceptableException;
import com.aspectran.web.support.http.HttpStatus;
import com.aspectran.web.support.http.MediaType;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.Writer;
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

        transformByContentType(activity, encoding, contentType);

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

    public String getContentType(Activity activity) {
        try {
            MediaType contentType = determineContentType(activity);
            return (contentType != null ? contentType.toString() : null);
        } catch (HttpMediaTypeNotAcceptableException e) {
            return null;
        }
    }

    protected void transformByContentType(Activity activity, String encoding, MediaType contentType) throws Exception {
        if (MediaType.APPLICATION_JSON.equalsTypeAndSubtype(contentType)) {
            toJSON(activity, parseIndent(contentType));
        } else if (MediaType.APPLICATION_APON.equalsTypeAndSubtype(contentType)) {
            toAPON(activity, parseIndent(contentType));
        } else if (MediaType.APPLICATION_XML.equalsTypeAndSubtype(contentType)) {
            toXML(activity, encoding, parseIndent(contentType));
        } else {
            toText(activity);
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
            FormattingContext formattingContext = FormattingContext.parse(activity);
            formattingContext.setPretty(isPrettyPrint());
            if (isPrettyPrint() && indent > -1) {
                formattingContext.setIndentSize(indent);
            }

            JsonWriter jsonWriter = new JsonWriter(writer);
            if (formattingContext.getDateFormat() != null) {
                jsonWriter.dateFormat(formattingContext.getDateFormat());
            }
            if (formattingContext.getDateTimeFormat() != null) {
                jsonWriter.dateTimeFormat(formattingContext.getDateTimeFormat());
            }
            if (formattingContext.getNullWritable() != null) {
                jsonWriter.nullWritable(formattingContext.getNullWritable());
            }
            if (formattingContext.isPretty()) {
                String indentString = formattingContext.makeIndentString();
                if (indentString != null) {
                    jsonWriter.indentString(indentString);
                } else {
                    jsonWriter.prettyPrint(true);
                }
            } else {
                jsonWriter.prettyPrint(false);
            }
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

            FormattingContext formattingContext = FormattingContext.parse(activity);
            formattingContext.setPretty(isPrettyPrint());
            if (isPrettyPrint() && indent > -1) {
                formattingContext.setIndentSize(indent);
            }

            ObjectToAponConverter aponConverter = new ObjectToAponConverter();
            if (formattingContext.getDateFormat() != null) {
                aponConverter.setDateFormat(formattingContext.getDateFormat());
            }
            if (formattingContext.getDateTimeFormat() != null) {
                aponConverter.setDateTimeFormat(formattingContext.getDateTimeFormat());
            }
            Parameters parameters = aponConverter.toParameters(getName(), getData());

            AponTransformResponse.transform(parameters, writer, formattingContext);
        }
    }

    private void toXML(Activity activity, String encoding, int indent) throws IOException, TransformerException {
        if (getName() != null || getData() != null) {
            ResponseAdapter responseAdapter = activity.getResponseAdapter();
            Writer writer = responseAdapter.getWriter();

            FormattingContext formattingContext = FormattingContext.parse(activity);
            formattingContext.setPretty(isPrettyPrint());
            if (isPrettyPrint() && indent > -1) {
                formattingContext.setIndentSize(indent);
            }

            if (getName() != null) {
                XmlTransformResponse.transform(Collections.singletonMap(getName(), getData()),
                        writer, encoding, formattingContext);
            } else {
                XmlTransformResponse.transform(getData(), writer, encoding, formattingContext);
            }
        }
    }

    private void toText(Activity activity) throws IOException {
        if (getName() != null || getData() != null) {
            ResponseAdapter responseAdapter = activity.getResponseAdapter();
            Writer writer = responseAdapter.getWriter();
            if (getName() != null) {
                writer.write(getName());
                writer.write(": ");
            }
            if (getData() != null) {
                writer.write(getData().toString());
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
