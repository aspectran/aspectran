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
package com.aspectran.core.activity.response.transform;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.FormattingContext;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.transform.apon.ContentsToAponConverter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.utils.apon.AponWriter;
import com.aspectran.utils.apon.Parameters;

import java.io.IOException;
import java.io.Writer;

/**
 * APON Transform Response converts the response data to APON and outputs it.
 *
 * Created: 2008. 03. 22 PM 5:51:58
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
    public void transform(Activity activity) throws Exception {
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

        ProcessResult processResult = activity.getProcessResult();
        if (processResult != null && !processResult.isEmpty()) {
            FormattingContext formattingContext = FormattingContext.parse(activity);
            if (pretty != null) {
                formattingContext.setPretty(pretty);
            }

            Writer writer = responseAdapter.getWriter();
            transform(processResult, writer, formattingContext);
        }
    }

    @Override
    public Response replicate() {
        TransformRule transformRule = getTransformRule().replicate();
        return new AponTransformResponse(transformRule);
    }

    private static void transform(ProcessResult processResult, Writer writer, FormattingContext formattingContext)
            throws IOException {
        ContentsToAponConverter aponConverter = new ContentsToAponConverter();
        if (formattingContext != null) {
            if (formattingContext.getDateFormat() != null) {
                aponConverter.setDateFormat(formattingContext.getDateFormat());
            }
            if (formattingContext.getDateTimeFormat() != null) {
                aponConverter.setDateTimeFormat(formattingContext.getDateTimeFormat());
            }
        }
        Parameters parameters = aponConverter.toParameters(processResult);
        transform(parameters, writer, formattingContext);
    }

    public static void transform(Parameters parameters, Writer writer, FormattingContext formattingContext)
            throws IOException {
        AponWriter aponWriter = new AponWriter(writer);
        if (formattingContext != null) {
            if (formattingContext.getNullWritable() != null) {
                aponWriter.nullWritable(formattingContext.getNullWritable());
            }
            if (formattingContext.isPretty()) {
                String indentString = formattingContext.makeIndentString();
                if (indentString != null) {
                    aponWriter.indentString(indentString);
                } else {
                    aponWriter.prettyPrint(true);
                }
            } else {
                aponWriter.prettyPrint(false);
            }
        }
        aponWriter.write(parameters);
    }

}
