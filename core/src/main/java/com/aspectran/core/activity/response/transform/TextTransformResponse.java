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
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Writer;

/**
 * A {@link TransformResponse} that renders the activity's results as plain text.
 *
 * <p>This class handles two primary scenarios:
 * <ol>
 *   <li>If a {@link TemplateRule} is specified in the transform rule, it delegates
 *       rendering to the configured template engine.</li>
 *   <li>Otherwise, it iterates through the {@link ProcessResult} and writes the
 *       string representation of each action result directly to the response output,
 *       separated by newlines.</li>
 * </ol>
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class TextTransformResponse extends TransformResponse {

    private final String contentType;

    private final String encoding;

    private final String templateId;

    private final TemplateRule templateRule;

    /**
     * Instantiates a new TextTransformResponse.
     * @param transformRule the transform rule
     */
    public TextTransformResponse(TransformRule transformRule) {
        super(transformRule);

        this.contentType = transformRule.getContentType();
        this.encoding = transformRule.getEncoding();
        this.templateId = transformRule.getTemplateId();
        this.templateRule = transformRule.getTemplateRule();
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
        if (templateId != null) {
            activity.getTemplateRenderer().render(templateId, activity);
        } else if (templateRule != null) {
            activity.getTemplateRenderer().render(templateRule, activity);
        } else {
            ProcessResult processResult = activity.getProcessResult();
            if (processResult != null) {
                int chunks = 0;
                for (ContentResult contentResult : processResult) {
                    for (ActionResult actionResult : contentResult) {
                        Object resultValue = actionResult.getResultValue();
                        if (resultValue != null) {
                            if (chunks++ > 0) {
                                writer.write(System.lineSeparator());
                            }
                            writer.write(resultValue.toString());
                        }
                    }
                }
            }
        }
    }

    @Override
    public Response replicate() {
        return new TextTransformResponse(getTransformRule().replicate());
    }

}
