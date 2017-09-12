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
package com.aspectran.core.context.rule;

import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.TransformResponseFactory;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.util.ToStringBuilder;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

/**
 * The Class ResponseRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class ResponseRule implements ResponseRuleApplicable, Replicable<ResponseRule> {

    public static final String CONTENT_ENCODING_SETTING_NAME = "contentEncoding";

    private String name;

    private String characterEncoding;

    private Response response;

    /**
     * Instantiates a new ResponseRule.
     */
    public ResponseRule() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the character encoding.
     *
     * @return the character encoding
     */
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    /**
     * Sets the character encoding.
     *
     * @param characterEncoding the new character encoding
     */
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
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

    @SuppressWarnings("unchecked")
    public <T> T getRespondent() {
        return (T)response;
    }

    @Override
    public Response applyResponseRule(DispatchResponseRule dispatchResponseRule) {
        Response response = new DispatchResponse(dispatchResponseRule);
        this.response = response;
        return response;
    }

    @Override
    public Response applyResponseRule(TransformRule transformRule) {
        Response response = TransformResponseFactory.createTransformResponse(transformRule);
        this.response = response;
        return response;
    }

    @Override
    public Response applyResponseRule(ForwardResponseRule forwardResponseRule) {
        Response response = new ForwardResponse(forwardResponseRule);
        this.response = response;
        return response;
    }

    @Override
    public Response applyResponseRule(RedirectResponseRule redirectResponseRule) {
        Response response = new RedirectResponse(redirectResponseRule);
        this.response = response;
        return response;
    }

    public ResponseRule replicate() {
        return replicate(this);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("characterEncoding", characterEncoding);
        tsb.append("response", response);
        return tsb.toString();
    }

    public static ResponseRule newInstance(String name, String characterEncoding) {
        if (characterEncoding != null && !Charset.isSupported(characterEncoding)) {
            throw new IllegalCharsetNameException("Given charset name is illegal. charsetName: " + characterEncoding);
        }

        ResponseRule responseRule = new ResponseRule();
        responseRule.setName(name);
        responseRule.setCharacterEncoding(characterEncoding);
        return responseRule;
    }

    public static ResponseRule newInstance(DispatchResponseRule drr) {
        ResponseRule responseRule = new ResponseRule();
        responseRule.applyResponseRule(drr);
        return responseRule;
    }

    public static ResponseRule newInstance(TransformRule tr) {
        ResponseRule responseRule = new ResponseRule();
        responseRule.applyResponseRule(tr);
        return responseRule;
    }

    public static ResponseRule newInstance(ForwardResponseRule frr) {
        ResponseRule responseRule = new ResponseRule();
        responseRule.applyResponseRule(frr);
        return responseRule;
    }

    public static ResponseRule newInstance(RedirectResponseRule rrr) {
        ResponseRule responseRule = new ResponseRule();
        responseRule.applyResponseRule(rrr);
        return responseRule;
    }

    public static ResponseRule replicate(ResponseRule responseRule) {
        ResponseRule newResponseRule = new ResponseRule();
        newResponseRule.setName(responseRule.getName());
        newResponseRule.setCharacterEncoding(responseRule.getCharacterEncoding());
        Response response = responseRule.getResponse();
        if (response != null) {
            Response newResponse = response.replicate();
            newResponseRule.setResponse(newResponse);
        }
        return newResponseRule;
    }

}
