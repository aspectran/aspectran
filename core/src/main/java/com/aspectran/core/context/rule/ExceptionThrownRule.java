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
import com.aspectran.utils.ToStringBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
     * Checks if the given exception matches any of the handled exception types.
     * If no exception types are defined, it matches all exceptions.
     * @param ex the exception to check
     * @return true if the exception matches, false otherwise
     */
    public boolean matches(Throwable ex) {
        if (ex == null) {
            return false;
        }
        if (exceptionTypes == null || exceptionTypes.length == 0) {
            return true;
        }
        Throwable currentEx = ex;
        while (currentEx != null) {
            Class<?> clazz = currentEx.getClass();
            while (clazz != null) {
                String className = clazz.getName();
                for (String exceptionType : exceptionTypes) {
                    if (className.equals(exceptionType)) {
                        return true;
                    }
                }
                clazz = clazz.getSuperclass();
            }
            currentEx = currentEx.getCause();
        }
        return false;
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
    public void setAction(Executable action) {
        this.action = action;
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
    public Executable putActionRule(@NonNull HeaderActionRule headerActionRule) {
        Executable action = new HeaderAction(headerActionRule);
        this.action = action;
        return action;
    }

    @Override
    public Executable putActionRule(@NonNull EchoActionRule echoActionRule) {
        Executable action = new EchoAction(echoActionRule);
        this.action = action;
        return action;
    }

    @Override
    public Executable putActionRule(@NonNull InvokeActionRule invokeActionRule) {
        InvokeAction action;
        if (adviceRule != null) {
            if (adviceRule.getAdviceBeanId() == null && adviceRule.getAdviceBeanClass() == null &&
                invokeActionRule.getBeanId() == null && invokeActionRule.getBeanClass() == null) {
                throw new IllegalStateException("No bean specified for an invoke action. An invoke action " +
                        "within an exception handling rule requires a target bean to be defined either on the " +
                        "invoke rule itself or on the parent advice rule. Rule: " + invokeActionRule);
            }
            action = new AdviceAction(adviceRule, invokeActionRule);
        } else {
            if (invokeActionRule.getBeanId() == null && invokeActionRule.getBeanClass() == null) {
                throw new IllegalStateException("No bean specified for an invoke action. The invoke action " +
                        "rule must specify a 'bean' or 'class' to invoke. Rule: " + invokeActionRule);
            }
            action = new InvokeAction(invokeActionRule);
        }
        this.action = action;
        return action;
    }

    @Override
    public Executable putActionRule(MethodActionRule methodActionRule) {
        throw new UnsupportedOperationException("An exception handling rule can only contain a single, direct action (e.g., echo, header, or invoke). " +
                "Rules like 'method' that imply complex logic are not supported");
    }

    @Override
    public Executable putActionRule(IncludeActionRule includeActionRule) {
        throw new UnsupportedOperationException("An exception handling rule can only contain a single, direct action (e.g., echo, header, or invoke). " +
                "Rules like 'include' that imply complex logic are not supported");
    }

    @Override
    public Executable putActionRule(ChooseRule chooseRule) {
        throw new UnsupportedOperationException("An exception handling rule can only contain a single, direct action (e.g., echo, header, or invoke). " +
                "Rules like 'choose' that imply conditional logic are not supported");
    }

    @Override
    public void putActionRule(Executable action) {
        throw new UnsupportedOperationException(
                "Adding a pre-constructed Executable action is not supported in ExceptionThrownRule");
    }

    @Override
    public Response putResponseRule(@NonNull TransformRule transformRule) {
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
    public Response putResponseRule(@NonNull DispatchRule dispatchRule) {
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
                "The forward response rule is not applicable to an exception handling rule " +
                        "because forwarding could trigger a new exception, potentially causing an infinite loop. " +
                        "Use 'dispatch' or 'redirect' instead");
    }

    @Override
    public Response putResponseRule(@NonNull RedirectRule redirectRule) {
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

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        if (exceptionTypes != null && exceptionTypes.length > 0) {
            tsb.append("types", exceptionTypes);
        }
        tsb.append("action", action);
        if (responseMap != null && !responseMap.isEmpty()) {
            tsb.append("responses", responseMap.keySet());
        }
        tsb.append("defaultResponse", defaultResponse);
        return tsb.toString();
    }

    /**
     * Generates a string representation of the current ExceptionThrownRule instance,
     * containing its type, aspect ID, description, and other relevant attributes.
     * @param adviceAction the executable action associated with the advice,
     *      used to generate additional string content if the description is not set
     * @return the string representation of the ExceptionThrownRule instance
     */
    public String toString(@Nullable Executable adviceAction) {
        ToStringBuilder tsb = new ToStringBuilder();
        if (adviceRule != null) {
            tsb.append("type", adviceRule.getAdviceType());
            tsb.append("aspect", adviceRule.getAspectId());
            if (adviceRule.getDescription() != null) {
                tsb.append("action", adviceRule.getDescription());
            } else if (adviceAction != null) {
                adviceAction.toString(tsb);
            }
        }
        if (exceptionTypes != null && exceptionTypes.length > 0) {
            tsb.append("exceptionTypes", exceptionTypes);
        }
        if (responseMap != null && !responseMap.isEmpty()) {
            tsb.append("responses", responseMap.keySet());
        }
        tsb.append("defaultResponse", defaultResponse);
        return tsb.toString();
    }

    /**
     * Creates a new instance of ExceptionThrownRule.
     * @param types the exception types to handle
     * @param action the action to execute
     * @return a new ExceptionThrownRule instance
     */
    @NonNull
    public static ExceptionThrownRule newInstance(Class<? extends Throwable>[] types, Executable action) {
        ExceptionThrownRule exceptionThrownRule = new ExceptionThrownRule();
        if (types != null && types.length > 0) {
            String[] exceptionTypes = new String[types.length];
            for (int i = 0; i < types.length; i++) {
                exceptionTypes[i] = types[i].getName();
            }
            exceptionThrownRule.setExceptionTypes(exceptionTypes);
        }
        exceptionThrownRule.setAction(action);
        return exceptionThrownRule;
    }

}
