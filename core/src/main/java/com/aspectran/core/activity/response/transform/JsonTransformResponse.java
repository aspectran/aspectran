/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.transform.json.ContentsJsonWriter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Writer;

/**
 * JSON Transform Response converts the response data to JSON and outputs it.
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
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
     * @param transformRule the transform rule
     */
    public JsonTransformResponse(TransformRule transformRule) {
        super(transformRule);

        this.contentType = transformRule.getContentType();
        this.encoding = transformRule.getEncoding();
        this.pretty = transformRule.getPretty();
    }

    @Override
    protected void transform(@NonNull Activity activity) throws Exception {
        ResponseAdapter responseAdapter = activity.getResponseAdapter();

        if (this.encoding != null) {
            responseAdapter.setEncoding(this.encoding);
        } else if (responseAdapter.getEncoding() == null) {
            String encoding = activity.getTranslet().getDefinitiveResponseEncoding();
            if (encoding != null) {
                responseAdapter.setEncoding(encoding);
            }
        }

        if (contentType != null) {
            responseAdapter.setContentType(contentType);
        }

        Writer writer = responseAdapter.getWriter();
        ProcessResult processResult = activity.getProcessResult();
        StringifyContext stringifyContext = activity.getStringifyContext();

        // support for jsonp
        String callback = activity.getTranslet().getParameter(CALLBACK_PARAM_NAME);
        if (callback != null) {
            writer.write(callback + ROUND_BRACKET_OPEN);
        }

        ContentsJsonWriter jsonWriter = new ContentsJsonWriter(writer);
        jsonWriter.setStringifyContext(stringifyContext);
        if (pretty != null) {
            jsonWriter.prettyPrint(pretty);
        }
        jsonWriter.writeValue(processResult);

        if (callback != null) {
            writer.write(ROUND_BRACKET_CLOSE);
        }
    }

    @Override
    public Response replicate() {
        return new JsonTransformResponse(getTransformRule().replicate());
    }

}
