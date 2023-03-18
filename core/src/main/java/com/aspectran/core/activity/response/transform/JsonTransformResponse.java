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
package com.aspectran.core.activity.response.transform;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.FormattingContext;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.transform.json.ContentsJsonWriter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.TransformRule;

import java.io.IOException;
import java.io.Writer;

/**
 * JSON Transform Response converts the response data to JSON and outputs it.
 *
 * Created: 2008. 03. 22 PM 5:51:58
 */
public class JsonTransformResponse extends TransformResponse {

    public static final String CALLBACK_PARAM_NAME = "callback";

    public static final String ROUND_BRACKET_OPEN = "(";

    public static final String ROUND_BRACKET_CLOSE = ")";

    private final String contentType;

    private final String encoding;

    private final Boolean pretty;

    /**
     * Instantiates a new JsonTransformResponse.
     *
     * @param transformRule the transform rule
     */
    public JsonTransformResponse(TransformRule transformRule) {
        super(transformRule);

        this.contentType = transformRule.getContentType();
        this.encoding = transformRule.getEncoding();
        this.pretty = transformRule.getPretty();
    }

    @Override
    protected void transform(Activity activity) throws Exception {
        ResponseAdapter responseAdapter = activity.getResponseAdapter();

        if (this.encoding != null) {
            responseAdapter.setEncoding(this.encoding);
        } else if (responseAdapter.getEncoding() == null) {
            String encoding = activity.getTranslet().getIntendedResponseEncoding();
            if (encoding != null) {
                responseAdapter.setEncoding(encoding);
            }
        }

        if (contentType != null) {
            responseAdapter.setContentType(contentType);
        }

        Writer writer = responseAdapter.getWriter();
        ProcessResult processResult = activity.getProcessResult();

        FormattingContext formattingContext = FormattingContext.parse(activity);
        if (pretty != null) {
            formattingContext.setPretty(pretty);
        }

        // support for jsonp
        String callback = activity.getTranslet().getParameter(CALLBACK_PARAM_NAME);

        transform(processResult, callback, writer, formattingContext);
    }

    @Override
    public Response replicate() {
        return new JsonTransformResponse(getTransformRule().replicate());
    }

    private void transform(ProcessResult processResult, String callback, Writer writer, FormattingContext formattingContext)
            throws IOException {
        if (callback != null) {
            writer.write(callback + ROUND_BRACKET_OPEN);
        }

        ContentsJsonWriter jsonWriter = new ContentsJsonWriter(writer);
        if (formattingContext != null) {
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
        }
        jsonWriter.write(processResult);

        if (callback != null) {
            writer.write(ROUND_BRACKET_CLOSE);
        }
    }

}
