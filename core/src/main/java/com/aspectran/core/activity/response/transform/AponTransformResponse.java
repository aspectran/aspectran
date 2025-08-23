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
package com.aspectran.core.activity.response.transform;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.transform.apon.ContentsToParameters;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.apon.AponWriter;
import com.aspectran.utils.apon.Parameters;

import java.io.IOException;
import java.io.Writer;

/**
 * {@code AponTransformResponse} converts the activity's processing results into
 * APON (Aspectran Plain Object Notation) format and outputs it.
 *
 * <p>This response type is responsible for taking the structured data from the
 * {@link com.aspectran.core.activity.process.result.ProcessResult} and serializing it
 * into a human-readable and machine-parsable APON string. It supports configuration
 * for content type, encoding, and pretty-printing.</p>
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class AponTransformResponse extends TransformResponse {

    private final String contentType;

    private final String encoding;

    private final Boolean pretty;

    /**
     * Instantiates a new AponTransformResponse.
     * @param transformRule the transform rule
     */
    public AponTransformResponse(TransformRule transformRule) {
        super(transformRule);

        this.contentType = transformRule.getContentType();
        this.encoding = transformRule.getEncoding();
        this.pretty = transformRule.getPretty();
    }

    @Override
    public void transform(@NonNull Activity activity) throws Exception {
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

        ProcessResult processResult = activity.getProcessResult();
        if (processResult != null && !processResult.isEmpty()) {
            Writer writer = responseAdapter.getWriter();
            StringifyContext stringifyContext = activity.getStringifyContext();
            Parameters parameters = ContentsToParameters.from(processResult, stringifyContext);
            transform(parameters, writer, stringifyContext, pretty);
        }
    }

    @Override
    public Response replicate() {
        TransformRule transformRule = getTransformRule().replicate();
        return new AponTransformResponse(transformRule);
    }

    public static void transform(
            Parameters parameters, Writer writer, StringifyContext stringifyContext)
            throws IOException {
        transform(parameters, writer, stringifyContext, null);
    }

    private static void transform(
            Parameters parameters, Writer writer, StringifyContext stringifyContext,
            Boolean prettyForce) throws IOException {
        AponWriter aponWriter = new AponWriter(writer);
        aponWriter.setStringifyContext(stringifyContext);
        if (prettyForce != null) {
            aponWriter.setPrettyPrint(prettyForce);
        }
        aponWriter.write(parameters);
    }

}
