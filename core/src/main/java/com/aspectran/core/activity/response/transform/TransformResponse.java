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
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.FormatType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

/**
 * The Class TransformResponse.
 *
 * Created: 2008. 03. 22 PM 5:51:58
 */
public abstract class TransformResponse implements Response {

    private static final Logger logger = LoggerFactory.getLogger(TransformResponse.class);

    private final TransformRule transformRule;

    /**
     * Instantiates a new TransformResponse.
     * @param transformRule the transform rule
     */
    public TransformResponse(TransformRule transformRule) {
        this.transformRule = transformRule;
    }

    @Override
    public ResponseType getResponseType() {
        return TransformRule.RESPONSE_TYPE;
    }

    @Override
    public String getContentType() {
        return transformRule.getContentType();
    }

    /**
     * Gets the format type.
     * @return the format type
     */
    public FormatType getFormatType() {
        return transformRule.getFormatType();
    }

    /**
     * Gets the transform rule.
     * @return the transform rule
     */
    public TransformRule getTransformRule() {
        return transformRule;
    }

    @Override
    public void commit(@NonNull Activity activity) throws ResponseException {
        if (logger.isDebugEnabled()) {
            logger.debug("Response " + transformRule);
        }

        try {
            transform(activity);
        } catch (Exception e) {
            throw new TransformResponseException(transformRule, e);
        }
    }

    protected abstract void transform(Activity activity) throws Exception;

    @Override
    public String toString() {
        return transformRule.toString();
    }

}
