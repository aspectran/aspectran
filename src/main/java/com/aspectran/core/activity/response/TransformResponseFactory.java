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
package com.aspectran.core.activity.response;

import com.aspectran.core.activity.response.transform.JsonTransformResponse;
import com.aspectran.core.activity.response.transform.TextTransformResponse;
import com.aspectran.core.activity.response.transform.XmlTransformResponse;
import com.aspectran.core.activity.response.transform.XslTransformResponse;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.TransformType;

/**
 * The Class TransformResponseFactory.
 * 
 * @since 2011. 3. 12.
 */
public class TransformResponseFactory {

    /**
     * Creates a new TransformResponse object.
     *
     * @param transformRule the transform rule
     * @return the transform response
     */
    public static Response createTransformResponse(TransformRule transformRule) {
        if (transformRule == null) {
            throw new IllegalArgumentException("Argument 'transformRule' must not be null");
        }

        TransformType type = transformRule.getTransformType();
        Response res;

        if (type == TransformType.XML) {
            res = new XmlTransformResponse(transformRule);
        } else if (type == TransformType.XSL) {
            res = new XslTransformResponse(transformRule);
        } else if (type == TransformType.JSON) {
            res = new JsonTransformResponse(transformRule);
        } else if (type == TransformType.TEXT) {
            res = new TextTransformResponse(transformRule);
        } else {
            throw new IllegalArgumentException("Invalid transform-type for transformRule: " + transformRule);
        }

        return res;
    }

}
