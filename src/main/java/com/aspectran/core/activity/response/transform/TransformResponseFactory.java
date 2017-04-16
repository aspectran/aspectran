/*
 * Copyright 2008-2017 Juho Jeong
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
import com.aspectran.core.context.rule.type.TransformType;

/**
 * A factory for creating Transform objects.
 * 
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class TransformResponseFactory {

    /**
     * Creates a new Transform object with specified TransformRule.
     *
     * @param transformRule the transform rule
     * @return the transform response
     */
    public static Response createTransformResponse(TransformRule transformRule) {
        TransformType tranformType = transformRule.getTransformType();

        Response transformResponse;

        if (tranformType == TransformType.XSL) {
            transformResponse = new XslTransformResponse(transformRule);
        } else if (tranformType == TransformType.XML) {
            if (transformRule.getContentType() == null) {
                transformRule.setContentType(ContentType.TEXT_XML.toString());
            }
            transformResponse = new XmlTransformResponse(transformRule);
        } else if (tranformType == TransformType.TEXT) {
            transformResponse = new TextTransformResponse(transformRule);
        } else if (tranformType == TransformType.JSON) {
            if (transformRule.getContentType() == null) {
                transformRule.setContentType(ContentType.TEXT_PLAIN.toString());
            }
            transformResponse = new JsonTransformResponse(transformRule);
        } else if (tranformType == TransformType.APON) {
            if (transformRule.getContentType() == null) {
                transformRule.setContentType(ContentType.TEXT_PLAIN.toString());
            }
            transformResponse = new AponTransformResponse(transformRule);
        } else {
            throw new TransformResponseException(transformRule, "Unknown transform type");
        }

        return transformResponse;
    }

}
