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
package com.aspectran.core.activity.response;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

/**
 * The Class RedirectResponse.
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class RedirectResponse implements Response {

    private static final Logger logger = LoggerFactory.getLogger(RedirectResponse.class);

    private final RedirectRule redirectRule;

    /**
     * Instantiates a new RedirectResponse.
     * @param redirectRule the redirect rule
     */
    public RedirectResponse(RedirectRule redirectRule) {
        this.redirectRule = redirectRule;
    }

    @Override
    public void commit(Activity activity) throws ResponseException {
        RedirectRule newRedirectRule = redirectRule.replicate();
        if (logger.isDebugEnabled()) {
            logger.debug("Response " + newRedirectRule);
        }

        try {
            ResponseAdapter responseAdapter = activity.getResponseAdapter();
            if (newRedirectRule.getEncoding() != null) {
                responseAdapter.setEncoding(newRedirectRule.getEncoding());
            } else {
                String encoding = activity.getTranslet().getIntendedResponseEncoding();
                if (encoding != null) {
                    responseAdapter.setEncoding(encoding);
                    newRedirectRule.setEncoding(encoding);
                } else {
                    newRedirectRule.setEncoding(responseAdapter.getEncoding());
                }
            }
            responseAdapter.redirect(newRedirectRule);
        } catch (Exception e) {
            throw new ResponseException("Failed to respond with redirect rule " + newRedirectRule, e);
        }
    }

    @Override
    public ResponseType getResponseType() {
        return RedirectRule.RESPONSE_TYPE;
    }

    @Override
    public String getContentType() {
        if (redirectRule != null) {
            return redirectRule.getContentType();
        } else {
            return null;
        }
    }

    @Override
    public Response replicate() {
        return new RedirectResponse(redirectRule.replicate());
    }

    /**
     * Returns the redirect rule.
     * @return the redirect rule
     */
    public RedirectRule getRedirectRule() {
        return redirectRule;
    }

    @Override
    public String toString() {
        return redirectRule.toString();
    }

}
