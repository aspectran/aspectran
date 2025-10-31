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
package com.aspectran.core.context.rule;

import com.aspectran.core.activity.response.transform.CustomTransformer;
import com.aspectran.core.context.rule.type.FormatType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * Defines a rule for a custom transformation that is implemented by a {@link CustomTransformer}.
 *
 * <p>Created: 2019. 06. 16</p>
 */
public class CustomTransformRule {

    public static final CustomTransformRule DEFAULT = CustomTransformRule.newInstance();

    private static final ResponseType RESPONSE_TYPE = ResponseType.TRANSFORM;

    private static final FormatType FORMAT_TYPE = FormatType.CUSTOM;

    private final CustomTransformer transformer;

    /**
     * Instantiates a new CustomTransformRule.
     */
    public CustomTransformRule() {
        this(null);
    }

    /**
     * Instantiates a new CustomTransformRule.
     * @param transformer the custom transformer
     */
    public CustomTransformRule(@Nullable CustomTransformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Gets the format type.
     * @return the format type
     */
    public FormatType getFormatType() {
        return FORMAT_TYPE;
    }

    /**
     * Gets the custom transformer.
     * @return the custom transformer
     */
    public CustomTransformer getTransformer() {
        return transformer;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.appendForce("type", RESPONSE_TYPE);
        tsb.appendForce("format", FORMAT_TYPE);
        tsb.append("transformer", transformer);
        return tsb.toString();
    }

    /**
     * Creates a new instance of CustomTransformRule.
     * @return a new CustomTransformRule instance
     */
    @NonNull
    public static CustomTransformRule newInstance() {
        return new CustomTransformRule();
    }

    /**
     * Creates a new instance of CustomTransformRule.
     * @param transformer the custom transformer
     * @return a new CustomTransformRule instance
     */
    @NonNull
    public static CustomTransformRule newInstance(@NonNull CustomTransformer transformer) {
        return new CustomTransformRule(transformer);
    }

}
