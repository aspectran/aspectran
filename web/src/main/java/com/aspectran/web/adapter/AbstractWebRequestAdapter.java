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
package com.aspectran.web.adapter;

import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.web.activity.request.WebRequestBodyParser;
import com.aspectran.web.support.http.MediaType;

public abstract class AbstractWebRequestAdapter extends AbstractRequestAdapter implements WebRequestAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractWebRequestAdapter.class);

    private MediaType mediaType;

    private boolean bodyObtained;

    public AbstractWebRequestAdapter(MethodType requestMethod, Object adaptee) {
        super(requestMethod, adaptee);
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    protected void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String getBody() {
        if (!bodyObtained) {
            bodyObtained = true;
            try {
                String body = WebRequestBodyParser.parseBody(this);
                setBody(body);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to parse request body", e);
                }
                setBody(null);
            }
        }
        return super.getBody();
    }

    @Override
    public <T extends Parameters> T getBodyAsParameters(Class<T> requiredType) throws RequestParseException {
        if (getMediaType() != null) {
            return WebRequestBodyParser.parseBodyAsParameters(this, requiredType);
        } else {
            return null;
        }
    }

}
