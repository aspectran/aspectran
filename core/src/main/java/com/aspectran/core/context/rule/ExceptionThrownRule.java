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
package com.aspectran.core.context.rule;

import com.aspectran.core.activity.process.action.BeanAction;
import com.aspectran.core.activity.process.action.EchoAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.action.HeadingAction;
import com.aspectran.core.activity.process.action.MethodAction;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.TransformResponseFactory;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;
import com.aspectran.core.context.rule.type.ActionType;

/**
 * The Class ExceptionThrownRule.
 * 
 * <p>Created: 2008. 04. 01 PM 11:19:28</p>
 */
public class ExceptionThrownRule implements ResponseRuleApplicable, ActionRuleApplicable {

    private final AspectAdviceRule aspectAdviceRule;

    private String[] exceptionTypes;

    private ResponseMap responseMap = new ResponseMap();

    private Response defaultResponse;

    private Executable action;

    public ExceptionThrownRule() {
        this(null);
    }

    public ExceptionThrownRule(AspectAdviceRule aspectAdviceRule) {
        this.aspectAdviceRule = aspectAdviceRule;
    }

    public String[] getExceptionTypes() {
        return exceptionTypes;
    }

    public void setExceptionTypes(String... exceptionTypes) {
        this.exceptionTypes = exceptionTypes;
    }

    public Response getResponse(String contentType) {
        if (contentType != null) {
            Response response = responseMap.get(contentType);
            if (response != null) {
                return response;
            }
        }
        return getDefaultResponse();
    }

    /**
     * Gets the response map.
     *
     * @return the response map
     */
    public ResponseMap getResponseMap() {
        return responseMap;
    }

    /**
     * Sets the response map.
     *
     * @param responseMap the new response map
     */
    public void setResponseMap(ResponseMap responseMap) {
        this.responseMap = responseMap;
    }

    public Response getDefaultResponse() {
        if (defaultResponse == null && responseMap.size() == 1) {
            return responseMap.getFirst();
        } else {
            return defaultResponse;
        }
    }

    public void setDefaultResponse(Response response) {
        this.defaultResponse = response;
    }

    @Override
    public Response applyResponseRule(TransformRule transformRule) {
        Response response = TransformResponseFactory.createTransformResponse(transformRule);
        if (transformRule.getContentType() != null) {
            responseMap.put(transformRule.getContentType(), response);
        }
        if (transformRule.isDefaultResponse()) {
            defaultResponse = response;
        }
        if (defaultResponse == null && transformRule.getContentType() == null) {
            defaultResponse = response;
        }
        return response;
    }

    @Override
    public Response applyResponseRule(DispatchResponseRule dispatchResponseRule) {
        Response response = new DispatchResponse(dispatchResponseRule);
        if (dispatchResponseRule.getContentType() != null) {
            responseMap.put(dispatchResponseRule.getContentType(), response);
        }
        if (dispatchResponseRule.isDefaultResponse()) {
            defaultResponse = response;
        }
        if (defaultResponse == null && dispatchResponseRule.getContentType() == null) {
            defaultResponse = response;
        }
        return response;
    }

    @Override
    public Response applyResponseRule(RedirectResponseRule redirectResponseRule) {
        Response response = new RedirectResponse(redirectResponseRule);
        if (redirectResponseRule.getContentType() != null) {
            responseMap.put(redirectResponseRule.getContentType(), response);
        }
        if (redirectResponseRule.isDefaultResponse()) {
            defaultResponse = response;
        }
        if (defaultResponse == null && redirectResponseRule.getContentType() == null) {
            defaultResponse = response;
        }
        return response;
    }

    @Override
    public Response applyResponseRule(ForwardResponseRule forwardResponseRule) {
        Response response = new ForwardResponse(forwardResponseRule);
        if (forwardResponseRule.getContentType() != null) {
            responseMap.put(forwardResponseRule.getContentType(), response);
        }
        if (forwardResponseRule.isDefaultResponse()) {
            defaultResponse = response;
        }
        if (defaultResponse == null && forwardResponseRule.getContentType() == null) {
            defaultResponse = response;
        }
        return response;
    }

    @Override
    public void applyActionRule(BeanActionRule beanActionRule) {
        BeanAction action = new BeanAction(beanActionRule, null);
        if (aspectAdviceRule != null && beanActionRule.getBeanId() == null) {
            action.setAspectAdviceRule(aspectAdviceRule);
        }
        this.action = action;
    }

    @Override
    public void applyActionRule(MethodActionRule methodActionRule) {
        throw new UnsupportedOperationException(
                "Cannot apply the Method Action Rule to the Exception Thrown Rule");
    }

    @Override
    public void applyActionRule(IncludeActionRule includeActionRule) {
        throw new UnsupportedOperationException(
                "Cannot apply the Include Action Rule to the Exception Thrown Rule");
    }

    @Override
    public void applyActionRule(EchoActionRule echoActionRule) {
        action = new EchoAction(echoActionRule, null);
    }

    @Override
    public void applyActionRule(HeadingActionRule headingActionRule) {
        action = new HeadingAction(headingActionRule, null);
    }

    /**
     * Returns the executable action.
     *
     * @return the executable action
     */
    public Executable getExecutableAction() {
        return action;
    }

    public void setExecutableAction(MethodAction action) {
        this.action = action;
    }

    /**
     * Returns the action type of the executable action.
     *
     * @return the action type
     */
    public ActionType getActionType() {
        return (action != null ? action.getActionType() : null);
    }

    public static ExceptionThrownRule newInstance(Class<? extends Throwable>[] types, MethodAction action) {
        ExceptionThrownRule exceptionThrownRule = new ExceptionThrownRule();
        if (types != null && types.length > 0) {
            String[] exceptionTypes = new String[types.length];
            for (int i = 0; i < types.length; i++) {
                exceptionTypes[i] = types[0].getName();
            }
            exceptionThrownRule.setExceptionTypes(exceptionTypes);
        }
        exceptionThrownRule.setExecutableAction(action);
        return exceptionThrownRule;
    }

}
