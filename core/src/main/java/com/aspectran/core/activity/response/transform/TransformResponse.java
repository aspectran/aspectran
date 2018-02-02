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

import com.aspectran.core.activity.response.Response;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.TransformType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * The Class TransformResponse.
 * 
 * Created: 2008. 03. 22 PM 5:51:58
 */
public abstract class TransformResponse implements Response {

    protected final TransformRule transformRule;

    /**
     * Instantiates a new TransformResponse.
     *
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
     * Gets the transform type.
     *
     * @return the transform type
     */
    public TransformType getTransformType() {
        return transformRule.getTransformType();
    }

    /**
     * Gets the transform rule.
     *
     * @return the transform rule
     */
    public TransformRule getTransformRule() {
        return transformRule;
    }

    /**
     * Gets the template as stream.
     *
     * @param url the url of the template
     * @return the input stream of the template
     * @throws IOException if an I/O error has occurred
     */
    protected InputStream getTemplateAsStream(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        return conn.getInputStream();
    }

    @Override
    public String toString() {
        return transformRule.toString();
    }

}
