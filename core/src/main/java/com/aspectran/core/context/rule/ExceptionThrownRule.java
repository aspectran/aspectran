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

import com.aspectran.core.activity.process.action.AdviceAction;
import com.aspectran.core.activity.process.action.AnnotatedAction;
import com.aspectran.core.activity.process.action.EchoAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.action.HeaderAction;
import com.aspectran.core.activity.process.action.InvokeAction;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.TransformResponseFactory;
import com.aspectran.core.context.rule.ability.HasActionRules;
import com.aspectran.core.context.rule.ability.HasResponseRules;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * Defines a rule for handling a specific type of exception.
 * It can specify an action to execute or a response to generate when one of the
 * specified exception types is caught.
 *
 * <p>Created: 2008. 04. 01 PM 11:19:28</p>
 */
public class ExceptionThrownRule implements HasActionRules, HasResponseRules {

    private final AdviceRule adviceRule;

    private String[] exceptionTypes;

    private Executable action;

    private ResponseMap responseMap;

    private Response defaultResponse;

    /**
     * Instantiates a new ExceptionThrownRule.
     */
    public ExceptionThrownRule() {
        this(null);
    }

    /**
     * Instantiates a new ExceptionThrownRule.
     * @param adviceRule the parent advice rule
     */
    public ExceptionThrownRule(AdviceRule adviceRule) {
        this.adviceRule = adviceRule;
    }

    /**
     * Gets the parent advice rule.
     * @return the parent advice rule
     */
    public AdviceRule getAdviceRule() {
        return adviceRule;
    }

    /**
     * Gets the exception types that this rule handles.
     * @return an array of exception type names
     */
    public String[] getExceptionTypes() {
        return exceptionTypes;
    }

    /**
     * Sets the exception types that this rule handles.
     * @param exceptionTypes an array of exception type names
     */
    public void setExceptionTypes(String... exceptionTypes) {
        this.exceptionTypes = exceptionTypes;
    }

    /**
     * Gets the action to be executed when the exception is thrown.
     * @return the executable action
     */
    public Executable getAction() {
        return action;
    }

    /**
     * Sets the action to be executed.
     * @param action the executable action
     */
    public void setAction(AnnotatedAction action) {
        this.action = action;
    }

    /**
     * Gets the type of the executable action.
     * @return the action type
     */
    public ActionType getActionType() {
        return (action != null ? action.getActionType() : null);
    }

    /**
     * Gets the response for a given content type.
     * @param contentType the content type
     * @return the response
     */
    public Response getResponse(String contentType) {
        if (responseMap != null && contentType != null) {
            Response response = responseMap.get(contentType);
            if (response != null) {
                return response;
            }
        }
        return getDefaultResponse();
    }

    private Response getDefaultResponse() {
        if (defaultResponse == null && responseMap != null && responseMap.size() == 1) {
            return responseMap.getFirst();
        } else {
            return defaultResponse;
        }
    }

    /**
     * Gets the map of responses, keyed by content type.
     * @return the response map
     */
    public ResponseMap getResponseMap() {
        return responseMap;
    }

    private ResponseMap touchResponseMap() {
        if (responseMap == null) {
            responseMap = new ResponseMap();
        }
        return responseMap;
    }

    @Override
    public Executable putActionRule(HeaderActionRule headerActionRule) {
        Executable action = new HeaderAction(headerActionRule);
        this.action = action;
        return action;
    }

    @Override
    public Executable putActionRule(EchoActionRule echoActionRule) {
        Executable action = new EchoAction(echoActionRule);
        this.action = action;
        return action;
    }

    @Override
    public Executable putActionRule(InvokeActionRule invokeActionRule) {
        InvokeAction action;
        if (adviceRule != null) {
            if (adviceRule.getAdviceBeanId() == null && adviceRule.getAdviceBeanClass() == null &&
                invokeActionRule.getBeanId() == null && invokeActionRule.getBeanClass() == null) {
                throw new IllegalStateException("Unknown advice bean for " + invokeActionRule + " in " + this);
            }
            action = new AdviceAction(adviceRule, invokeActionRule);
        } else {
            if (invokeActionRule.getBeanId() == null && invokeActionRule.getBeanClass() == null) {
                throw new IllegalStateException("Unknown action bean for " + invokeActionRule);
            }
            action = new InvokeAction(invokeActionRule);
        }
        this.action = action;
        return action;
    }

    @Override
    public Executable putActionRule(AnnotatedActionRule annotatedActionRule) {
        throw new UnsupportedOperationException("No support applying AnnotatedActionRule to AdviceRule");
    }

    @Override
    public Executable putActionRule(IncludeActionRule includeActionRule) {
        throw new UnsupportedOperationException("No support applying IncludeActionRule to AdviceRule");
    }

    @Override
    public Executable putActionRule(ChooseRule chooseRule) {
        throw new UnsupportedOperationException("No support applying ChooseRule to AdviceRule");
    }

    @Override
    public void putActionRule(Executable action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response putResponseRule(TransformRule transformRule) {
        Response response = TransformResponseFactory.create(transformRule);
        touchResponseMap().put(transformRule.getContentType(), response);
        if (transformRule.isDefaultResponse()) {
            defaultResponse = response;
        }
        if (defaultResponse == null && transformRule.getContentType() == null) {
            defaultResponse = response;
        }
        return response;
    }

    @Override
    public Response putResponseRule(DispatchRule dispatchRule) {
        Response response = new DispatchResponse(dispatchRule);
        touchResponseMap().put(dispatchRule.getContentType(), response);
        if (dispatchRule.isDefaultResponse()) {
            defaultResponse = response;
        }
        if (defaultResponse == null && dispatchRule.getContentType() == null) {
            defaultResponse = response;
        }
        return response;
    }

    @Override
    public Response putResponseRule(ForwardRule forwardRule) {
        throw new UnsupportedOperationException(
                "Cannot apply the forward response rule to the exception thrown rule");
    }

    @Override
    public Response putResponseRule(RedirectRule redirectRule) {
        Response response = new RedirectResponse(redirectRule);
        touchResponseMap().put(redirectRule.getContentType(), response);
        if (redirectRule.isDefaultResponse()) {
            defaultResponse = response;
        }
        if (defaultResponse == null && redirectRule.getContentType() == null) {
            defaultResponse = response;
        }
        return response;
    }

    /**
     * Creates a new instance of ExceptionThrownRule.
     * @param types the exception types to handle
     * @param action the action to execute
     * @return a new ExceptionThrownRule instance
     */
    @NonNull
    public static ExceptionThrownRule newInstance(Class<? extends Throwable>[] types, AnnotatedAction action) {
        ExceptionThrownRule exceptionThrownRule = new ExceptionThrownRule();
        if (types != null && types.length > 0) {
            String[] exceptionTypes = new String[types.length];
            for (int i = 0; i < types.length; i++) {
                exceptionTypes[i] = types[0].getName();
            }
            exceptionThrownRule.setExceptionTypes(exceptionTypes);
        }
        exceptionThrownRule.setAction(action);
        return exceptionThrownRule;
    }

}
