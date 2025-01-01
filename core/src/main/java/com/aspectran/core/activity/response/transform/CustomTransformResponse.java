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
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.context.rule.CustomTransformRule;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.util.ListIterator;

/**
 * Responding with user-implemented {@link CustomTransformer}.
 *
 * <p>Created: 2019. 06. 15</p>
 */
public class CustomTransformResponse implements Response {

    private static final Logger logger = LoggerFactory.getLogger(CustomTransformResponse.class);

    private static final ResponseType RESPONSE_TYPE = ResponseType.TRANSFORM;

    private final CustomTransformRule customTransformRule;

    /**
     * Instantiates a new CustomTransformResponse.
     */
    public CustomTransformResponse() {
        this.customTransformRule = CustomTransformRule.DEFAULT;
    }

    /**
     * Instantiates a new CustomTransformResponse.
     * @param transformer the custom transformer
     */
    public CustomTransformResponse(CustomTransformer transformer) {
        this.customTransformRule = CustomTransformRule.newInstance(transformer);
    }

    @Override
    public void commit(@NonNull Activity activity) throws ResponseException {
        CustomTransformer transformer = customTransformRule.getTransformer();
        if (transformer == null) {
            transformer = findTransformer(activity.getProcessResult());
        }
        if (transformer == null) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Response " + transformer);
        }

        try {
            transformer.transform(activity);
        } catch (Exception e) {
            throw new CustomTransformResponseException(transformer, e);
        }
    }

    @Override
    public ResponseType getResponseType() {
        return RESPONSE_TYPE;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public Response replicate() {
        throw new UnsupportedOperationException("Non-Replicable");
    }

    @Override
    public String toString() {
        return customTransformRule.toString();
    }

    private CustomTransformer findTransformer(ProcessResult processResult) {
        if (processResult != null) {
            for (ListIterator<ContentResult> iter1 = processResult.listIterator(processResult.size());
                 iter1.hasPrevious(); ) {
                ContentResult contentResult = iter1.previous();
                for (ListIterator<ActionResult> iter2 = contentResult.listIterator(contentResult.size());
                     iter2.hasPrevious(); ) {
                    ActionResult actionResult = iter2.previous();
                    if (actionResult != null && actionResult.getResultValue() instanceof CustomTransformer) {
                        return (CustomTransformer)actionResult.getResultValue();
                    }
                }
            }
        }
        return null;
    }

}
