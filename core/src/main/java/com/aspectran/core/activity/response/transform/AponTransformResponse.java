/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.transform.apon.ContentsAponAssembler;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.io.Writer;

/**
 * The Class AponTransformResponse.
 * 
 * Created: 2008. 03. 22 PM 5:51:58
 */
public class AponTransformResponse extends TransformResponse {

    private static final Log log = LogFactory.getLog(AponTransformResponse.class);

    private final String encoding;

    private final String contentType;

    private final Boolean pretty;

    /**
     * Instantiates a new AponTransformResponse.
     *
     * @param transformRule the transform rule
     */
    public AponTransformResponse(TransformRule transformRule) {
        super(transformRule);

        this.encoding = transformRule.getEncoding();
        this.contentType = transformRule.getContentType();
        this.pretty = transformRule.getPretty();
    }

    @Override
    public void commit(Activity activity) throws TransformResponseException {
        ResponseAdapter responseAdapter = activity.getResponseAdapter();
        if (responseAdapter == null) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("response " + transformRule);
        }

        try {
            if (this.encoding != null) {
                responseAdapter.setEncoding(this.encoding);
            } else {
                String encoding = activity.getTranslet().getResponseEncoding();
                if (encoding != null) {
                    responseAdapter.setEncoding(encoding);
                }
            }
            if (contentType != null) {
                responseAdapter.setContentType(contentType);
            }

            Writer writer = responseAdapter.getWriter();
            ProcessResult processResult = activity.getProcessResult();
            Parameters parameters = ContentsAponAssembler.assemble(processResult);

            AponWriter aponWriter;
            if (pretty == Boolean.FALSE) {
                aponWriter = new AponWriter(writer, false);
            } else {
                String indentString = activity.getSetting("indentString");
                if (indentString != null) {
                    aponWriter = new AponWriter(writer, indentString);
                } else {
                    aponWriter = new AponWriter(writer, (pretty != null));
                }
            }
            aponWriter.write(parameters);
        } catch (Exception e) {
            throw new TransformResponseException(transformRule, e);
        }
    }

    @Override
    public ActionList getActionList() {
        return transformRule.getActionList();
    }

    @Override
    public Response replicate() {
        TransformRule transformRule = getTransformRule().replicate();
        return new AponTransformResponse(transformRule);
    }

}
