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

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.CustomTransformResponse;
import com.aspectran.core.activity.response.transform.TransformResponseFactory;
import com.aspectran.core.context.rule.ability.HasActionRules;
import com.aspectran.core.context.rule.ability.HasResponseRules;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.nio.charset.Charset;

/**
 * The Class ResponseRule.
 *
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class ResponseRule implements HasActionRules, HasResponseRules, Replicable<ResponseRule> {

    private final boolean explicit;

    private String name;

    /**
     * The response encoding is the character encoding of the textual response.
     */
    private String encoding;

    private ActionList actionList;

    private Response response;

    /**
     * Instantiates a new ResponseRule.
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
     * @return the response encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the response encoding.
     * @param encoding the new response encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public ActionList getActionList() {
        return actionList;
    }

    public void setActionList(ActionList actionList) {
        this.actionList = actionList;
    }

    @Override
    public Executable putActionRule(HeaderActionRule headerActionRule) {
        return touchActionList().putActionRule(headerActionRule);
    }

    @Override
    public Executable putActionRule(EchoActionRule echoActionRule) {
        return touchActionList().putActionRule(echoActionRule);
    }

    @Override
    public Executable putActionRule(InvokeActionRule invokeActionRule) {
        return touchActionList().putActionRule(invokeActionRule);
    }

    @Override
    public Executable putActionRule(AnnotatedActionRule annotatedActionRule) {
        return touchActionList().putActionRule(annotatedActionRule);
    }

    @Override
    public Executable putActionRule(IncludeActionRule includeActionRule) {
        return touchActionList().putActionRule(includeActionRule);
    }

    @Override
    public Executable putActionRule(ChooseRule chooseRule) {
        return touchActionList().putActionRule(chooseRule);
    }

    @Override
    public void putActionRule(Executable action) {
        touchActionList().putActionRule(action);
    }

    /**
     * Returns the action list.
     * If not yet instantiated then create a new one.
     * @return the action list
     */
    private ActionList touchActionList() {
        if (actionList == null) {
            actionList = new ActionList(false);
        }
        return actionList;
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
    public Response putResponseRule(TransformRule transformRule) {
        Response response = TransformResponseFactory.create(transformRule);
        this.response = response;
        return response;
    }

    @Override
    public Response putResponseRule(DispatchRule dispatchRule) {
        Response response = new DispatchResponse(dispatchRule);
        this.response = response;
        return response;
    }

    @Override
    public Response putResponseRule(ForwardRule forwardRule) {
        Response response = new ForwardResponse(forwardRule);
        this.response = response;
        return response;
    }

    @Override
    public Response putResponseRule(RedirectRule redirectRule) {
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

    @NonNull
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

    @NonNull
    public static ResponseRule newInstance(TransformRule transformRule) {
        ResponseRule responseRule = new ResponseRule(false);
        responseRule.putResponseRule(transformRule);
        return responseRule;
    }

    @NonNull
    public static ResponseRule newInstance(DispatchRule dispatchRule) {
        ResponseRule responseRule = new ResponseRule(false);
        responseRule.putResponseRule(dispatchRule);
        return responseRule;
    }

    @NonNull
    public static ResponseRule newInstance(ForwardRule forwardRule) {
        ResponseRule responseRule = new ResponseRule(false);
        responseRule.putResponseRule(forwardRule);
        return responseRule;
    }

    @NonNull
    public static ResponseRule newInstance(RedirectRule redirectRule) {
        ResponseRule responseRule = new ResponseRule(false);
        responseRule.putResponseRule(redirectRule);
        return responseRule;
    }

    @NonNull
    public static ResponseRule newInstance(CustomTransformResponse response) {
        ResponseRule responseRule = new ResponseRule(false);
        responseRule.setResponse(response);
        return responseRule;
    }

    @NonNull
    public static ResponseRule replicate(@NonNull ResponseRule responseRule) {
        ResponseRule rr = new ResponseRule(responseRule.isExplicit());
        rr.setName(responseRule.getName());
        rr.setEncoding(responseRule.getEncoding());
        rr.setActionList(responseRule.getActionList());
        Response response = responseRule.getResponse();
        if (response != null) {
            Response newResponse = response.replicate();
            rr.setResponse(newResponse);
        }
        return rr;
    }

}
