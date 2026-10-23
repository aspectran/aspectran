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
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.context.rule.CustomTransformRule;
import com.aspectran.core.context.rule.type.ResponseType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Response} that delegates the transformation of activity results
 * to a user-implemented {@link CustomTransformer}.
 *
 * <p>This class provides a flexible way to integrate custom data transformation
 * logic. It can either use a pre-configured {@code CustomTransformer} or
 * dynamically discover one from the activity's process results by calling
 * {@link com.aspectran.core.activity.process.result.ProcessResult#findTransformer}.</p>
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

    /**
     * Retrieves the custom transformation rule associated with this response.
     * @return the {@link CustomTransformRule} instance
     */
    public CustomTransformRule getCustomTransformRule() {
        return customTransformRule;
    }

    @Override
    public void respond(@NonNull Activity activity) throws ResponseException {
        CustomTransformer transformer = customTransformRule.getTransformer();
        if (transformer == null && activity.getProcessResult() != null) {
            transformer = activity.getProcessResult().findTransformer();
        }
        if (transformer == null) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Response {}", toString(transformer));
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
        return toString(null);
    }

    /**
     * Returns a string representation of this response with the given {@code CustomTransformer}.
     * @param transformer the custom transformer; if null, the configured transformer is used
     * @return a string representation of this response
     */
    public String toString(@Nullable CustomTransformer transformer) {
        if (customTransformRule == null) {
            return super.toString();
        }
        return customTransformRule.toString(transformer);
    }

}
