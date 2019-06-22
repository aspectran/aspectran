/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.CustomTransformResponse;
import com.aspectran.core.activity.response.transform.TransformResponseFactory;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.util.ToStringBuilder;

import java.nio.charset.Charset;

/**
 * The Class ResponseRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class ResponseRule implements ResponseRuleApplicable, Replicable<ResponseRule> {

    private final boolean explicit;

    private String name;

    /**
     * The response encoding is the character encoding of the textual response.
     */
    private String encoding;

    private Response response;

    /**
     * Instantiates a new ResponseRule.
     *
     * @param explicit whether this response rule is explicit
     */
    public ResponseRule(boolean explicit) {
        this.explicit = explicit;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the response encoding.
     *
     * @return the response encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the response encoding.
     *
     * @param encoding the new response encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public ResponseType getResponseType() {
        return (response != null ? response.getResponseType() : null);
    }

    @Override
    public Response applyResponseRule(TransformRule transformRule) {
        Response response = TransformResponseFactory.create(transformRule);
        this.response = response;
        return response;
    }

    @Override
    public Response applyResponseRule(DispatchRule dispatchRule) {
        Response response = new DispatchResponse(dispatchRule);
        this.response = response;
        return response;
    }

    @Override
    public Response applyResponseRule(ForwardRule forwardRule) {
        Response response = new ForwardResponse(forwardRule);
        this.response = response;
        return response;
    }

    @Override
    public Response applyResponseRule(RedirectRule redirectRule) {
        Response response = new RedirectResponse(redirectRule);
        this.response = response;
        return response;
    }

    @Override
    public ResponseRule replicate() {
        return replicate(this);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("encoding", encoding);
        tsb.append("response", response);
        return tsb.toString();
    }

    public static ResponseRule newInstance(String name, String encoding) throws IllegalRuleException {
        if (encoding != null) {
            try {
                Charset.forName(encoding);
            } catch (Exception e) {
                throw new IllegalRuleException("Unsupported character encoding name: " + encoding, e);
            }
        }

        ResponseRule responseRule = new ResponseRule(true);
        responseRule.setName(name);
        responseRule.setEncoding(encoding);
        return responseRule;
    }

    public static ResponseRule newInstance(TransformRule transformRule) {
        ResponseRule responseRule = new ResponseRule(false);
        responseRule.applyResponseRule(transformRule);
        return responseRule;
    }

    public static ResponseRule newInstance(DispatchRule dispatchRule) {
        ResponseRule responseRule = new ResponseRule(false);
        responseRule.applyResponseRule(dispatchRule);
        return responseRule;
    }

    public static ResponseRule newInstance(ForwardRule forwardRule) {
        ResponseRule responseRule = new ResponseRule(false);
        responseRule.applyResponseRule(forwardRule);
        return responseRule;
    }

    public static ResponseRule newInstance(RedirectRule redirectRule) {
        ResponseRule responseRule = new ResponseRule(false);
        responseRule.applyResponseRule(redirectRule);
        return responseRule;
    }

    public static ResponseRule newInstance(CustomTransformResponse response) {
        ResponseRule responseRule = new ResponseRule(false);
        responseRule.setResponse(response);
        return responseRule;
    }

    public static ResponseRule replicate(ResponseRule responseRule) {
        ResponseRule newResponseRule = new ResponseRule(responseRule.isExplicit());
        newResponseRule.setName(responseRule.getName());
        newResponseRule.setEncoding(responseRule.getEncoding());
        Response response = responseRule.getResponse();
        if (response != null) {
            Response newResponse = response.replicate();
            newResponseRule.setResponse(newResponse);
        }
        return newResponseRule;
    }

}
