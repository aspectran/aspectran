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

import com.aspectran.core.activity.response.Response;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.ContentType;
import com.aspectran.core.context.rule.type.FormatType;
import org.jspecify.annotations.NonNull;

/**
 * A factory for creating {@link TransformResponse} objects based on a {@link TransformRule}.
 *
 * <p>This factory inspects the {@code FormatType} in a {@code TransformRule} and
 * instantiates the appropriate concrete {@code TransformResponse} implementation
 * (e.g., {@link JsonTransformResponse}, {@link XmlTransformResponse}). It also sets
 * a default content type if one is not explicitly defined in the rule.</p>
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class TransformResponseFactory {

    /**
     * Creates a new Transform object with specified TransformRule.
     * @param transformRule the transform rule
     * @return the transform response
     */
    @NonNull
    public static Response create(@NonNull TransformRule transformRule) {
        FormatType formatType = transformRule.getFormatType();
        Response res;
        if (formatType == FormatType.APON) {
            if (transformRule.getContentType() == null) {
                transformRule.setContentType(ContentType.APPLICATION_APON.toString());
            }
            res = new AponTransformResponse(transformRule);
        } else if (formatType == FormatType.JSON) {
            if (transformRule.getContentType() == null) {
                transformRule.setContentType(ContentType.TEXT_PLAIN.toString());
            }
            res = new JsonTransformResponse(transformRule);
        } else if (formatType == FormatType.TEXT) {
            res = new TextTransformResponse(transformRule);
        } else if (formatType == FormatType.XML) {
            if (transformRule.getContentType() == null) {
                transformRule.setContentType(ContentType.APPLICATION_XML.toString());
            }
            res = new XmlTransformResponse(transformRule);
        } else if (formatType == FormatType.XSL) {
            res = new XslTransformResponse(transformRule);
        } else {
            res = new NoneTransformResponse(transformRule);
        }
        return res;
    }

}
